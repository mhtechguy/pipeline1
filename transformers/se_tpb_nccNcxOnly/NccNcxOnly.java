/*
 * DMFC - The DAISY Multi Format Converter
 * Copyright (C) 2006  Daisy Consortium
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package se_tpb_nccNcxOnly;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.daisy.dmfc.core.InputListener;
import org.daisy.dmfc.core.transformer.Transformer;
import org.daisy.dmfc.exception.TransformerAbortException;
import org.daisy.dmfc.exception.TransformerRunException;
import org.daisy.util.file.FileUtils;
import org.daisy.util.file.FilenameOrFileURI;
import org.daisy.util.fileset.exception.FilesetFatalException;
import org.daisy.util.fileset.exception.FilesetFileErrorException;
import org.daisy.util.fileset.exception.FilesetFileException;
import org.daisy.util.fileset.exception.FilesetFileFatalErrorException;
import org.daisy.util.fileset.exception.FilesetFileWarningException;
import org.daisy.util.fileset.impl.FilesetImpl;
import org.daisy.util.fileset.interfaces.Fileset;
import org.daisy.util.fileset.interfaces.FilesetErrorHandler;
import org.daisy.util.fileset.interfaces.FilesetFile;
import org.daisy.util.fileset.interfaces.ManifestFile;
import org.daisy.util.fileset.interfaces.xml.d202.D202MasterSmilFile;
import org.daisy.util.fileset.interfaces.xml.d202.D202NccFile;
import org.daisy.util.fileset.interfaces.xml.d202.D202SmilFile;
import org.daisy.util.fileset.interfaces.xml.d202.D202TextualContentFile;
import org.daisy.util.xml.catalog.CatalogEntityResolver;
import org.daisy.util.xml.catalog.CatalogExceptionNotRecoverable;
import org.daisy.util.xml.stax.BookmarkedXMLEventReader;
import org.daisy.util.xml.stax.StaxEntityResolver;
import org.daisy.util.xml.xslt.Stylesheet;
import org.daisy.util.xml.xslt.XSLTException;

/**
 * Create a NCC/NCX only version of a fileset. The textual content document
 * is removed from the fileset.
 * 
 * FIXME add support for z3986 filesets
 *  
 * @author Linus Ericson 
 */
public class NccNcxOnly extends Transformer implements FilesetErrorHandler {

	private static final String XSLT_FACTORY = "net.sf.saxon.TransformerFactoryImpl";
	
	// Main progress values
    private static final double FILESET_DONE = 0.15;
    private static final double NCCURI_DONE = 0.17;
    private static final double SMIL_DONE = 0.35;
    private static final double NCC_DONE = 0.37;
    private static final double COPY_DONE = 0.99;
    
    private XMLInputFactory mFactory;
	
    /**
     * Constructor.
     * @param inListener
     * @param eventListeners
     * @param isInteractive
     */
	public NccNcxOnly(InputListener inListener, Set eventListeners, Boolean isInteractive) {
		super(inListener, eventListeners, isInteractive);		
        try {
        	mFactory = XMLInputFactory.newInstance();
            mFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
            mFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
            //mFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);
            mFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.TRUE);
			mFactory.setXMLResolver(new StaxEntityResolver(CatalogEntityResolver.getInstance()));
			mFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
		} catch (CatalogExceptionNotRecoverable e) {
			e.printStackTrace();
		}
        
	}

	/*
	 * (non-Javadoc)
	 * @see org.daisy.dmfc.core.transformer.Transformer#execute(java.util.Map)
	 */
	protected boolean execute(Map parameters) throws TransformerRunException {
		String manifest = (String)parameters.remove("manifest");
        String outDir = (String)parameters.remove("outDir");

        File outputDir = new File(outDir);
        
        try {
	        // Build a fileset
	        this.sendMessage(Level.INFO, i18n("BUILDING_FILESET"));
	        Fileset fileset = this.buildFileSet(manifest);   
	        if (fileset.hadErrors()) {
	        	throw new TransformerRunException(i18n("FILESET_HAD_ERRORS"));
	        }
	        D202NccFile nccFile = (D202NccFile)fileset.getManifestMember();
	        this.progress(FILESET_DONE);
	        this.checkAbort();
	        
	        // Create output directory
            outputDir = FileUtils.createDirectory(new File(outDir)); 
	        
	        // Collect smil URIs from the NCC            
            NccIdUriList idUri = NccIdUriList.parseNcc(nccFile.getFile());
            this.progress(NCCURI_DONE);
	        this.checkAbort();
	        
	        // Loop through smil files and change links
	        Collection spineItems = nccFile.getSpineItems();
	        int i = 0;
	        for (Iterator it = spineItems.iterator(); it.hasNext(); ) {
	        	D202SmilFile smilFile = (D202SmilFile)it.next();
	        	i++;
	        	this.updateSmil(smilFile, outputDir, idUri);
	        	this.progress(NCCURI_DONE + (SMIL_DONE-NCCURI_DONE)*((double)i/spineItems.size()));	        	
	            this.checkAbort();
	        }
	        this.progress(SMIL_DONE);
	        this.checkAbort();
	        
	        // Update the NCC meta data (ncc:files, ncc:kByteSize, ncc:multimediaType)
	        File inputFile = fileset.getManifestMember().getFile();
	        File outputFile = new File(outputDir, "ncc.html");
	        File sheet = new File(this.getTransformerDirectory(), "ncc-meta.xsl");	        
	        Stylesheet.apply(inputFile.getAbsolutePath(), sheet.getAbsolutePath(), outputFile.getAbsolutePath(), XSLT_FACTORY, null, CatalogEntityResolver.getInstance());
	        this.progress(NCC_DONE);
	        this.checkAbort();
	        
	        // Copy other fileset members (mp3s)	        
	        this.copyFiles(nccFile, fileset, outputDir);
	        this.progress(COPY_DONE);
	        this.checkAbort();
        } catch (FilesetFatalException e) {
        	throw new TransformerRunException(e.getMessage(), e);
        } catch (IOException e) {
        	throw new TransformerRunException(e.getMessage(), e);
		} catch (CatalogExceptionNotRecoverable e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (XMLStreamException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (URISyntaxException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (XSLTException e) {
			throw new TransformerRunException(e.getMessage(), e);
		}
        
		return true;
	}
	
	/**
	 * Update a single Daisy 2.02 SMIL file.
	 * @param smilFile the smil file to update
	 * @param outDir the output directory
	 * @param idUriList
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 */
	private void updateSmil(D202SmilFile smilFile, File outDir, NccIdUriList idUriList) throws FileNotFoundException, XMLStreamException {
		XMLEventReader reader = mFactory.createXMLEventReader(new FileInputStream(smilFile.getFile()));
		BookmarkedXMLEventReader bookmarked = new BookmarkedXMLEventReader(reader);
		File outFile = new File(outDir, smilFile.getName());
		OutputStream os = new FileOutputStream(outFile);
		SmilUpdater updater = new SmilUpdater(bookmarked, os, idUriList, smilFile.getName());
		updater.filter();
	}
	
	/**
	 * Copy the remaining fileset files.
	 * @param manifest
	 * @param fileset
	 * @param outputDir
	 * @throws IOException
	 * @throws TransformerAbortException
	 */
	private void copyFiles(ManifestFile manifest, Fileset fileset, File outputDir) throws IOException, TransformerAbortException {
		Collection toCopy = new ArrayList();
        long totalSize = 0;
        long currentSize = 0;
        for (Iterator it = fileset.getLocalMembers().iterator(); it.hasNext(); ) {
        	FilesetFile fsf = (FilesetFile)it.next();
        	if (fsf instanceof D202TextualContentFile ||
        		fsf instanceof D202NccFile ||
        		fsf instanceof D202SmilFile ||
        		fsf instanceof D202MasterSmilFile) {
        		// ignore
        	} else {
        		toCopy.add(fsf);
        		totalSize += fsf.getFile().length();	        		
        	}
        }
        for (Iterator it = toCopy.iterator(); it.hasNext(); ) {
        	FilesetFile fsf = (FilesetFile)it.next();
        	currentSize += fsf.getFile().length();
        	URI relative = manifest.getRelativeURI(fsf);
    		File out = new File(outputDir.toURI().resolve(relative));
    		FileUtils.copy(fsf.getFile(), out);
    		this.progress(NCC_DONE + (COPY_DONE-NCC_DONE)*((double)currentSize/totalSize));
            this.checkAbort();
        }
	}
	
	/**
	 * Builds a fileset.
	 * @param manifest
	 * @return
	 * @throws FilesetFatalException
	 */
	private Fileset buildFileSet(String manifest) throws FilesetFatalException {
        return new FilesetImpl(FilenameOrFileURI.toFile(manifest).toURI(), this, false, true);
    }

	/*
	 * (non-Javadoc)
	 * @see org.daisy.util.fileset.interfaces.FilesetErrorHandler#error(org.daisy.util.fileset.exception.FilesetFileException)
	 */
	public void error(FilesetFileException ffe) throws FilesetFileException {		
		if(ffe instanceof FilesetFileFatalErrorException) {
			this.sendMessage(Level.WARNING, "Serious error in "	+ ffe.getOrigin().getName() + ": " 
					+ ffe.getCause().getMessage() + " [" + ffe.getCause().getClass().getSimpleName() + "]");
		}else if (ffe instanceof FilesetFileErrorException) {
			this.sendMessage(Level.WARNING, "Error in " + ffe.getOrigin().getName() + ": " 
					+ ffe.getCause().getMessage() + " [" + ffe.getCause().getClass().getSimpleName() + "]");
		}else if (ffe instanceof FilesetFileWarningException) {
			this.sendMessage(Level.WARNING, "Warning in " + ffe.getOrigin().getName() + ": " 
					+ ffe.getCause().getMessage() + " [" + ffe.getCause().getClass().getSimpleName() + "]");
		}else{
			this.sendMessage(Level.WARNING, "Exception with unknown severity in " + ffe.getOrigin().getName() + ": "
					+ ffe.getCause().getMessage() + " [" + ffe.getCause().getClass().getSimpleName() + "]");
		}		
	}

}