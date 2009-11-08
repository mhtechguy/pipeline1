package org_pef_dtbook2pef.setups.sv_SE.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.daisy.pipeline.exception.TransformerRunException;
import org.daisy.util.xml.catalog.CatalogEntityResolver;
import org.daisy.util.xml.catalog.CatalogExceptionNotRecoverable;
import org.daisy.util.xml.stax.StaxEntityResolver;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org_pef_dtbook2pef.system.InternalTask;
import org_pef_dtbook2pef.system.tasks.layout.text.StringFilter;
import org_pef_dtbook2pef.system.tasks.layout.utils.TextBorder;

public class VolumeCoverPageTask extends InternalTask {
	private StringFilter filters;
	private TextBorder tb;
	private File dtbook;
	private int height;

	public VolumeCoverPageTask(String name, StringFilter filters, TextBorder tb, File dtbook, int height) {
		super(name);
		this.filters = filters;
		this.tb = tb;
		this.dtbook = dtbook;
		this.height = height;
	}

	@Override
	public void execute(File input, File output)
			throws TransformerRunException {

        XMLInputFactory inFactory = XMLInputFactory.newInstance();
		inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);        
        inFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        inFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);
        inFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.TRUE);
        
    	try {
			inFactory.setXMLResolver(new StaxEntityResolver(CatalogEntityResolver.getInstance()));
		} catch (CatalogExceptionNotRecoverable e1) {
			e1.printStackTrace();
		}
		
		try {

			DocumentBuilder docBuilder = initDocumentBuilder();
			Document d = docBuilder.parse(dtbook);
			Document d2 = docBuilder.parse(input);
			XPath xp = XPathFactory.newInstance().newXPath();
			int vol = ((Double)xp.evaluate("count(//volume)", d2, XPathConstants.NUMBER)).intValue();
			String doctitle = xp.evaluate("/dtbook/book/frontmatter/doctitle", d);
			org.w3c.dom.NodeList ns = (org.w3c.dom.NodeList)xp.evaluate("/dtbook/book/frontmatter/docauthor", d, XPathConstants.NODESET);
			ArrayList<String> al = new ArrayList<String>();
			for (int i=0; i<ns.getLength(); i++) {
				al.add(ns.item(i).getTextContent());
			}
			VolumeCoverPageFilter pf = new VolumeCoverPageFilter(
					inFactory.createXMLEventReader(new FileInputStream(input)), 
					new FileOutputStream(output),
					filters,
					doctitle, al, tb, height, vol);
			pf.filter();
			pf.close();
		} catch (FileNotFoundException e) {
			throw new TransformerRunException("FileNotFoundException:", e);
		} catch (XMLStreamException e) {
			throw new TransformerRunException("XMLStreamException:", e);
		} catch (IOException e) {
			throw new TransformerRunException("IOException:", e);
		} catch (ParserConfigurationException e) {
			throw new TransformerRunException("ParserConfigurationException:", e);
		} catch (SAXException e) {
			throw new TransformerRunException("SAXException:", e);
		} catch (XPathExpressionException e) {
			throw new TransformerRunException("XPathExpressionException:", e);
		}
	}
	
	protected DocumentBuilder initDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		try {
			db.setEntityResolver(CatalogEntityResolver.getInstance());
		} catch (CatalogExceptionNotRecoverable e) {
			ParserConfigurationException pce = new ParserConfigurationException("Unable to set CatalogEntityResolver");
			pce.initCause(e);
			throw pce;
		}
		return db;
	}

}