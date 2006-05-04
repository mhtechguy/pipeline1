package org.daisy.util.fileset.impl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import org.apache.batik.css.parser.Parser;
import org.daisy.util.fileset.exception.FilesetFileErrorException;
import org.daisy.util.fileset.exception.FilesetFileFatalErrorException;
import org.daisy.util.fileset.exception.FilesetFileWarningException;
import org.daisy.util.fileset.interfaces.ManifestFile;
import org.daisy.util.fileset.interfaces.text.CssFile;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorList;

/**
 * @author Markus Gylling
 */

final class CssFileImpl extends FilesetFileImpl implements DocumentHandler, ErrorHandler, CssFile, ManifestFile {
	//private ErrorHandler listeningErrorHandler = null;
	
	private Parser parser;
	
	CssFileImpl(URI uri) throws CSSException, FileNotFoundException, IOException {		
		super(uri, CssFile.mimeStringConstant);
		initialize();
	}
		
	private void initialize() {
		parser = new Parser();
		parser.setLocale(Locale.getDefault()); 
		parser.setDocumentHandler(this);        		
		parser.setErrorHandler(this);				  
	}	
	
	public void parse() throws CSSException, IOException {
		parser.parseStyleSheet(this.asSacInputSource());		
	}
	
	public void property(String name, LexicalUnit value, boolean important) throws CSSException {
		try {
			//collect all properties that contain url() statements  
			if (regex.matches(regex.CSS_PROPERTIES_WITH_URLS,name)) {
				try {
					String str = value.getStringValue();
					if (regex.matches(regex.FILE_IMAGE,str)) {
						  putUriValue(value.getStringValue());
					}
				}catch (Exception e) {
					//System.err.println("value.getStringValue failed in css");					
				}				 
		 	}
		} catch (Exception e) {			
			myExceptions.add(new FilesetFileWarningException(this,new CSSParseException("css property event",null,e)));
		}
	}
	
	public void importStyle(String inuri, SACMediaList media, String defaultNamespaceURI) { 
		this.putUriValue(inuri);		
	}
	
	public void error(CSSParseException cpe) throws CSSException {
        myExceptions.add(new FilesetFileErrorException(this,cpe)); 
	}
	
	public void fatalError(CSSParseException cpe) throws CSSException {
        myExceptions.add(new FilesetFileFatalErrorException(this,cpe));	
    }
	
	public void warning(CSSParseException cpe) throws CSSException {
		myExceptions.add(new FilesetFileWarningException(this,cpe));	
	}
		
	public org.w3c.css.sac.InputSource asSacInputSource() throws FileNotFoundException {
		org.w3c.css.sac.InputSource sacis = new org.w3c.css.sac.InputSource(new FileReader(this));
		sacis.setURI(this.toURI().toASCIIString());
		return sacis;
	}
	
	public void startDocument(InputSource source) throws CSSException {}	
	public void endDocument(InputSource source) throws CSSException {}
	public void startSelector(SelectorList selectors) throws CSSException {}
	public void endSelector(SelectorList selectors) throws CSSException {}
	public void comment(String text) throws CSSException {}
	public void startPage(String name, String pseudo_page) throws CSSException {}
	public void endPage(String name, String pseudo_page) throws CSSException {}
	public void ignorableAtRule(String atRule) throws CSSException {}    
	public void namespaceDeclaration(String prefix, String uri) throws CSSException {}
	public void startFontFace() throws CSSException {}
	public void endFontFace() throws CSSException {}
	public void startMedia(SACMediaList media) throws CSSException {}
	public void endMedia(SACMediaList media) throws CSSException {}	
	
	private static final long serialVersionUID = -9074302258050588711L;
}


//CssFileImpl(URI uri, org.w3c.css.sac.ErrorHandler errh) throws CSSException, FileNotFoundException, IOException, MIMETypeException {
//super(uri,CssFile.mimeStringConstant);		
//this.listeningErrorHandler = errh;
//initialize();
//}