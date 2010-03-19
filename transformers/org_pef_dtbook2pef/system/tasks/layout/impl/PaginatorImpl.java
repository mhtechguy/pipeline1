package org_pef_dtbook2pef.system.tasks.layout.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org_pef_dtbook2pef.system.tasks.layout.flow.LayoutException;
import org_pef_dtbook2pef.system.tasks.layout.flow.Marker;
import org_pef_dtbook2pef.system.tasks.layout.flow.Row;
import org_pef_dtbook2pef.system.tasks.layout.impl.field.CompoundField;
import org_pef_dtbook2pef.system.tasks.layout.impl.field.CurrentPageField;
import org_pef_dtbook2pef.system.tasks.layout.impl.field.MarkerReferenceField;
import org_pef_dtbook2pef.system.tasks.layout.page.CurrentPageInfo;
import org_pef_dtbook2pef.system.tasks.layout.page.LayoutMaster;
import org_pef_dtbook2pef.system.tasks.layout.page.Paginator;
import org_pef_dtbook2pef.system.tasks.layout.page.PagedMediaWriter;
import org_pef_dtbook2pef.system.tasks.layout.page.Template;
import org_pef_dtbook2pef.system.tasks.layout.text.StringFilter;
import org_pef_dtbook2pef.system.tasks.layout.utils.LayoutTools;
import org_pef_dtbook2pef.system.tasks.layout.utils.LayoutToolsException;
import org_pef_dtbook2pef.system.utils.StateObject;

public class PaginatorImpl implements Paginator, CurrentPageInfo {
	private static final Character SPACE_CHAR = ' '; //'\u2800'
	private final StringFilter filters;
	private final Stack<PageSequence> sequence;
	private PagedMediaWriter writer;
	private StateObject state;
	//private HashMap<String, LayoutMaster> templates;

	public PaginatorImpl(StringFilter filters) { //HashMap<String, LayoutMaster> templates
		this.filters = filters;
		this.sequence = new Stack<PageSequence>();
		this.state = new StateObject();
		//this.templates = templates;
	}
	
	public void open(PagedMediaWriter writer) {
		state.assertUnopened();
		state.open();
		this.writer = writer;
	}

	public void newSequence(LayoutMaster master, int pagesOffset) {
		state.assertOpen();
		//sequence.push(new PageSequence(templates.get(masterName)));
		sequence.push(new PageSequence(master, pagesOffset));
	}
	
	public void newSequence(LayoutMaster master) {
		if (sequence.size()==0) {
			newSequence(master, 0);
		} else {
			int next = sequence.peek().currentPage().getPageIndex()+1;
			if (sequence.peek().getLayoutMaster().duplex() && (next % 2)==1) {
				next++;
			}
			newSequence(master, next);
		}
	}
	
	private PageSequence currentSequence() {
		return sequence.peek();
	}

	private Page currentPage() {
		return currentSequence().currentPage();
	}

	public void newPage() {
		state.assertOpen();
		currentSequence().newPage();
	}
	
	public void newRow(Row row) {
		state.assertOpen();
		currentSequence().newRow(row);
	}

	public void insertMarkers(ArrayList<Marker> m) {
		state.assertOpen();
		currentSequence().currentPage().addMarkers(m);
	}
/*
	public LayoutMaster getCurrentLayoutMaster() {
		return currentSequence().getLayoutMaster();
	}*/
	
	public CurrentPageInfo getPageInfo() {
		return this;
	}
	
	// CurrentPageInfo
	public int countRows() {
		state.assertOpen();
		//return currentSequence().rowsOnCurrentPage();
		return currentPage().rowsOnPage();
	}
	
	public int getFlowHeight() {
		state.assertOpen();
		return currentPage().getFlowHeight();
	}
	// End CurrentPageInfo

	
	public void close() throws IOException {
		if (state.isClosed()) {
			return;
		}
		state.assertOpen();
		try {
		int rowNo = 1;
		for (PageSequence s : sequence) {
			LayoutMaster lm = s.getLayoutMaster();
			writer.newSection(lm);
			for (Page p : s.getPages()) {
				writer.newPage();
				int pagenum = p.getPageIndex()+1;
				Template t = lm.getTemplate(pagenum);
				//p.setHeader(renderFields(lm, p, t.getHeader()));
				//p.setFooter(renderFields(lm, p, t.getFooter()));
				ArrayList<Row> rows = new ArrayList<Row>();
				rows.addAll(renderFields(lm, p, t.getHeader()));
				rows.addAll(p.getRows());
				if (t.getFooterHeight()>0) {
					while (rows.size()<lm.getPageHeight()-t.getFooterHeight()) {
						rows.add(new Row());
					}
					rows.addAll(renderFields(lm, p, t.getFooter()));
				}
				for (Row row : rows) {
					if (row.getChars().length()>0) {
						int margin = ((pagenum % 2 == 0) ? lm.getOuterMargin() : lm.getInnerMargin()) + row.getLeftMargin();
						// remove trailing whitespace
						String chars = row.getChars().replaceAll("\\s*\\z", "");
						// add left margin
						int rowWidth = LayoutTools.length(chars)+row.getLeftMargin();
						String r = 	LayoutTools.fill(SPACE_CHAR, margin) + chars;
						if (rowWidth>lm.getFlowWidth()) {
							throw new LayoutException("Row no " + rowNo + " is too long (" + rowWidth + "/" + lm.getFlowWidth() + ") '" + chars + "'");
						}
						writer.newRow(r);
					} else {
						writer.newRow();
					}
					rowNo++;
				}
			}
		}
		} catch (LayoutException e) {
			IOException ex = new IOException("Layout exception");
			ex.initCause(e);
			throw ex;
		} finally {
			state.close();
		}
	}
	
	private ArrayList<Row> renderFields(LayoutMaster lm, Page p, ArrayList<ArrayList<Object>> fields) throws LayoutException {
		ArrayList<Row> ret = new ArrayList<Row>();
		for (ArrayList<Object> row : fields) {
			try {
				ret.add(new Row(distribute(row, lm.getFlowWidth(), " ", p)));
			} catch (LayoutToolsException e) {
				throw new LayoutException("Error while rendering header", e);
			}
		}
		return ret;
	}
	
	private String distribute(ArrayList<Object> chunks, int width, String padding, Page p) throws LayoutToolsException {
		ArrayList<String> chunkF = new ArrayList<String>();
		for (Object f : chunks) {
			chunkF.add(filters.filter(resolveField(f, p).replaceAll("\u00ad", "")));
		}
		return LayoutTools.distribute(chunkF, width, padding, LayoutTools.DistributeMode.EQUAL_SPACING);
	}

	private String resolveField(Object field, Page p) {
		if (field instanceof CompoundField) {
			return resolveCompoundField((CompoundField)field, p);
		} else if (field instanceof MarkerReferenceField) {
			MarkerReferenceField f2 = (MarkerReferenceField)field;
			return findMarker(p, f2);
		} else if (field instanceof CurrentPageField) {
			return resolveCurrentPageField((CurrentPageField)field, p);
		} else {
			return field.toString();
		}
	}

	private String resolveCompoundField(CompoundField f, Page p) {
		StringBuffer sb = new StringBuffer();
		for (Object f2 : f) {
			sb.append(resolveField(f2, p));
		}
		return sb.toString();
	}

	public String findMarker(Page page, MarkerReferenceField markerRef) {
		int dir = 1;
		int index = 0;
		int count = 0;
		List<Marker> m;
		if (markerRef.getSearchScope() == MarkerReferenceField.MarkerSearchScope.PAGE_CONTENT) {
			m = page.getContentMarkers();
		} else {
			m = page.getMarkers();
		}
		if (markerRef.getSearchDirection() == MarkerReferenceField.MarkerSearchDirection.BACKWARD) {
			dir = -1;
			index = m.size()-1;
		}
		while (count < m.size()) {
			Marker m2 = m.get(index);
			if (m2.getName().equals(markerRef.getName())) {
				return m2.getValue();
			}
			index += dir; 
			count++;
		}
		if (markerRef.getSearchScope() == MarkerReferenceField.MarkerSearchScope.SEQUENCE) {
			int nextPage = page.getPageIndex() - page.getParent().getOffset() + dir;
			//System.out.println("Next page: "+page.getPageIndex() + " | " + nextPage);
			if (nextPage < page.getParent().getPages().size() && nextPage >= 0) {
				Page next = page.getParent().getPages().get(nextPage);
				return findMarker(next, markerRef);
			}
		}
		return "";
	}

	private String resolveCurrentPageField(CurrentPageField f, Page p) {
		int pagenum = p.getPageIndex() + 1;
		return f.style(pagenum);
	}

}