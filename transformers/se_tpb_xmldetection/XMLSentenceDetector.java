/*
 * Daisy Pipeline (C) 2005-2008 Daisy Consortium
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package se_tpb_xmldetection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.daisy.util.xml.catalog.CatalogExceptionNotRecoverable;
import org.daisy.util.xml.stax.BookmarkedXMLEventReader;
import org.daisy.util.xml.stax.ContextStack;

/**
 * Performs sentence detection.
 * @author Linus Ericson
 */
@SuppressWarnings("unchecked")
public class XMLSentenceDetector extends XMLBreakDetector {

//    private static Logger logger = Logger.getLogger(XMLSentenceDetector.class.getName());
//    static {        
//        logger.setLevel(Level.ALL);
//    }
    
    protected final static String LAST_EVENT = "last event";
    protected final static String LATEST_BREAKING = "latest breaking";    
    
    protected BookmarkedXMLEventReader reader = null;
    
    protected ContextStack contextStack = null;
        
    private Locale lastLocale = null;
    private boolean doctypeSeen = false;    
    private boolean sentenceOpen = false;
    //mg200805: whether to add single sents (e.g. <h1><sent>Text</sent></h1>). True is the original behavior.
    private boolean doSingleSentAdd = true;
    
    /**
     * Constructor.    
     * @param inFile the input document
     * @param outFile the output document
     * @throws CatalogExceptionNotRecoverable
     * @throws XMLStreamException
     * @throws IOException
     */
    public XMLSentenceDetector (File inFile, File outFile) throws CatalogExceptionNotRecoverable, XMLStreamException, IOException {
        this(inFile, outFile, null, false, false);
    }
    
    /**
     * Constructor.
     * @param inFile the input document
     * @param outFile the output document
     * @param customLang URL to a custom language file (or null)
     * @param override override the language defined in the document with the custom language file
     * @param singleSentAdd add sentence markup even for single sentences
     * @throws CatalogExceptionNotRecoverable
     * @throws XMLStreamException
     * @throws IOException
     */
    public XMLSentenceDetector (File inFile, File outFile, URL customLang, boolean override, boolean singleSentAdd) throws CatalogExceptionNotRecoverable, XMLStreamException, IOException {
        super(outFile);
        ContextAwareBreakSettings cabi = new ContextAwareBreakSettings(true); 
        setBreakSettings(cabi);
        setContextStackFilter(cabi);
        Set xmllang = LangDetector.getXMLLangSet(inFile);
        setBreakFinder(new DefaultSentenceBreakFinder(xmllang, customLang, override));
        doSingleSentAdd = singleSentAdd;
        reader = new BookmarkedXMLEventReader(inputFactory.createXMLEventReader(new FileInputStream(inFile)));
    }
    
    /**
     * Perform the sentence detection.    
     */
	protected void detect() throws UnsupportedDocumentTypeException, FileNotFoundException, XMLStreamException {
        // The buffer for the text to detect sentence breaks in
        StringBuffer buffer = new StringBuffer();
        
        QName firstTagName = null;
        boolean firstTagIsStart = false;
        
        boolean skipContent = false;
        boolean writeWhileSkipping = false;
        int skipContextStackLength = 0;
        
        ArrayList abbrAcronymList = new ArrayList();
        Abbr abbrAcronym = null;
        
        boolean rootElementSeen = false;
        
        // Main event loop
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            contextStack.addEvent(event);
            //printEvent(event);
            
            if (skipContent) {
            	// We are in a context that shouldn't have sentence breaks, e.g.
            	// within an address element in DTBook. Skip until we reach the
            	// end of this context
                boolean isEnd = event.isEndElement();
                if (writeWhileSkipping) {
                    writeEvent(event);
                }
                if (isEnd) {
                    int newlen = contextStack.getContext().size();
                    if (newlen == skipContextStackLength) {
                        firstTagName = event.asEndElement().getName();
                        firstTagIsStart = false;
                        skipContent = false;
                        writeElements(false, new Vector());
                        reader.setBookmark(LATEST_BREAKING);
                    }
                }
            } else if (event.isStartElement() || event.isEndElement()) {
                if (!rootElementSeen) {
                    rootElementSeen = true;                    
                    if (!doctypeSeen) {
                    	// If we haven't seen a doctype declaration, we can use the namespace
                    	// declaration to load a configuration file
                        StartElement se = event.asStartElement();
                        if (!parseNamespace(se.getName().getNamespaceURI())) {
                            throw new UnsupportedDocumentTypeException("Unsupported document type.");
                        }
                    }
                }                
                
                // Event is a start element or an end element
                boolean isStart = false;
                
                QName elementName = null;
                if (event.isStartElement()) {
                    elementName = event.asStartElement().getName();
                    isStart = true;
                } else {
                    elementName = event.asEndElement().getName();
                }
                
                // Is this a sentence breaking tag or a non-processable context?
                if (breakSettings.isSentenceBreaking(elementName) ||
                        (isStart && breakSettings.skipContent(elementName)) ) {
                                        
                    if (reader.bookmarkExists(LATEST_BREAKING)) {
                        // There was an earlier breaking tag
                        
                        // Find the sentence breaks between these tags and write everything
                        //System.err.println("In the buffer: " + buffer.toString());
                        //System.err.println("Locale: " + lastLocale);
                        //if (lastLocale != null) {
                            breakFinder.setLocale(lastLocale);
                        //}
                        if (shouldBeProcessed(firstTagName, firstTagIsStart, elementName, event.isStartElement())) {
                            //System.err.println("Should be processed.");
                            if (!buffer.toString().matches("\\s*")) {
                                Vector breaks = breakFinder.findBreaks(buffer.toString(), abbrAcronymList);
                                /*
                                System.err.println(buffer);
                                System.err.println(breaks);
                                */
                                if(breaks.size()!=0 || doSingleSentAdd) {
                                	writeElements(true, breaks);
                                }else{
                                	//mg20080513 if we dont find any breaks and doSingleSentAdd is false,
                                	//dont write any sents. This avoids things like <h1><sent>Text</sent></h1>
                                	writeElements(false, new Vector());
                                }
                            } else {
                                // Just whitespace in the buffer. 
                                // Write the elements, but no sentence tags.
                                writeElements(false, new Vector());
                            }
                        } else /* shouldBeProcessed(...) */ {
                            //System.err.println("Should NOT be processed.");
                            writeElements(false, new Vector());
                        }                                               
                        
                        if (breakSettings.skipContent(elementName)) {
                            // There was a non-processable start element inside a processable one
                            
                            // Fast forward to the end of that context
                            //skipUntilMatchingClose(elementName, false);
                            skipContextStackLength = contextStack.getParentContext().size();
                            writeWhileSkipping = false;
                            skipContent = true;
                        }
                        abbrAcronymList = new ArrayList();
                        buffer = new StringBuffer();
                        firstTagName = elementName;
                        firstTagIsStart = event.isStartElement();
                        // Set a new latest breaking tag bookmark
                        reader.setBookmark(LATEST_BREAKING);
                    } else /* reader.bookmarkExists(...) */ {                        
                        if (breakSettings.skipContent(elementName)) {
                            // There was a non-processable start element
                            
                            // Write to the end of that context
                            skipContextStackLength = contextStack.getParentContext().size();
                            writeWhileSkipping = true;
                            skipContent = true;
                        } else {                            
                            reader.setBookmark(LATEST_BREAKING);
                            writeEvent(event);
                        }
                    }
                    
                } else if (!reader.bookmarkExists(LATEST_BREAKING)) {
                    // There is no latest breaking tag, so just write the event
                    writeEvent(event);
                } else {
                    // This is a non-breaking tag, and we have seen a breaking tag before
                    if (event.isStartElement()) {
                        int abbrType = 0;
                        String expandAttributeName = null;                        
                        boolean skip = false;
                        if (elementName.equals(breakSettings.getAbbrElement()) && attributesMatch(event.asStartElement(), breakSettings.getAbbrAttributes())) {
                            expandAttributeName = breakSettings.getAbbrExpandAttribute();
                            abbrType = Abbr.ABBREVIATION;
                        } else if (elementName.equals(breakSettings.getAcronymElement()) && attributesMatch(event.asStartElement(), breakSettings.getAcronymAttributes())) {
                            expandAttributeName = breakSettings.getAcronymExpandAttribute();
                            abbrType = Abbr.ACRONYM;
                        } else if (elementName.equals(breakSettings.getInitialismElement()) && attributesMatch(event.asStartElement(), breakSettings.getInitialismAttributes())) {
                            expandAttributeName = breakSettings.getInitialismExpandAttribute();
                            abbrType = Abbr.INITIALISM;
                        } else {
                            skip = true;
                            abbrAcronym = null;
                        }
                        if (!skip) {
	                        Attribute expand = event.asStartElement().getAttributeByName(new QName(expandAttributeName));
	                        QName expAttrName = breakSettings.getExpAttr();
	                        Attribute expAttr = null;
	                        if (expAttrName != null) {
	                            expAttr = event.asStartElement().getAttributeByName(expAttrName);
	                        }
	                        abbrAcronym = new Abbr(null, expand!=null?expand.getValue():null, expAttr!=null?expAttr.getValue():null, abbrType, buffer.length(), 0);
                        }
                    } else /* isEndElement */ {
                        // FIXME make sure end matches with start
                        if (abbrAcronym !=null && (elementName.equals(breakSettings.getAbbrElement()) ||
                                elementName.equals(breakSettings.getAcronymElement()) ||
                                elementName.equals(breakSettings.getInitialismElement()))) {
                            abbrAcronym = new Abbr(buffer.substring(abbrAcronym.getStart(), buffer.length()), abbrAcronym.getExpansion(), abbrAcronym.getExpAttr(), abbrAcronym.getType(), abbrAcronym.getStart(), buffer.length());
                            abbrAcronymList.add(abbrAcronym);
                            //logger.finer(abbrAcronym.toString());
                        }                        
                    }
                }
            } else if (event.isCharacters()) {
                Characters ch = event.asCharacters();
                if (reader.bookmarkExists(LATEST_BREAKING)) {
                    // Buffer the text for future use...
                    buffer.append(ch.getData());
                } else {
                    // There is no latest breaking tag, why don't we just write this event...
                    writeEvent(ch);
                }
            } else if (event.getEventType() == XMLStreamConstants.DTD) {
                DTD dtd = (DTD)event;                
                parseDoctype(dtd.getDocumentTypeDeclaration());
                writeEvent(event);
                doctypeSeen = true;
            } else if (event.isStartDocument()) { 
                StartDocument sd = (StartDocument)event;
                if (sd.encodingSet()) {
                	this.setXMLEventWriter(outputFactory.createXMLEventWriter(new FileOutputStream(outputFile), sd.getCharacterEncodingScheme()));
                    writeEvent(event);
                } else {
                	this.setXMLEventWriter(outputFactory.createXMLEventWriter(new FileOutputStream(outputFile), "utf-8"));
                    writeEvent(eventFactory.createStartDocument("utf-8", "1.0"));                    
                }
            } else if (!reader.bookmarkExists(LATEST_BREAKING)) {
                writeEvent(event);
            }
            
            reader.setBookmark(LAST_EVENT);
            lastLocale = contextStack.getCurrentLocale();
        }
        reader.close();
    }
    
    /**
     * Check if attributes match.
     * For every key-value pair in attributes, check if the given start
     * element has the same attribute key-value pair.
     * @param se
     * @param attributes
     * @return
     */
    private boolean attributesMatch(StartElement se, Map<String,String> attributes) {
        boolean result = true;
        for (Iterator<String> it = attributes.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            String value = attributes.get(key);
            Attribute att = se.getAttributeByName(new QName(key));
            if (att != null) {
                if (!att.getValue().equals(value)) {
                    //logger.info("Attribute " + key + " has wrong value " + att.getValue() + "(" + value + ")");
                    return false;
                }
            } else {
                //logger.info("Missing attribute " + key);
                return false;
            }
        }
        return result;
    }
    
    /**
     * Checks whether the current position (xpath path) in the document is allowed. 
     * @return true if the current path is allowed, false otherwise
     */
    private boolean isPathAllowed() {
        if (allowedPaths == null) {
            return true;
        }
        String currentPath = contextStack.getContextPath();
        for (Iterator<String> it = allowedPaths.iterator(); it.hasNext(); ) {
            String allowed = it.next();
            if (currentPath.startsWith(allowed)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Write the events from the latest breaking tag to the current position.
     * Add sentence elements where needed.
     * @param writeStartAndEndTag add sentence tags?
     * @param breaks positions in the text where sentence breaks should be added 
     * @throws XMLStreamException
     */
    private void writeElements(boolean writeStartAndEndTag, Vector breaks) throws XMLStreamException {
        //reader.setBookmark("mark");
        if (writeStartAndEndTag) {
            openSentence();
        }
        
        XMLEvent event = null;
        
        // Go through all events between LATEST_BREAKING and LAST_EVENT
        // For each start or end element, remember if a matching end or start element is found
        MatchingTags matchingTags = new MatchingTags();
        reader.gotoBookmark(LATEST_BREAKING);
        while (!reader.atBookmark(LAST_EVENT)) {
        	event = reader.nextEvent();
        	if (event.isStartElement()) {
        		matchingTags.add(event.asStartElement());
        	} else if (event.isEndElement()) {
        		matchingTags.add(event.asEndElement());
        	}
        }
        reader.gotoAndRemoveBookmark(LATEST_BREAKING);
        
        int offset = 0;
        int tagNumber = -1;
        while (!reader.atBookmark(LAST_EVENT)) {
            event = reader.nextEvent();
            if (event.isCharacters()) {
            	// Write characters, possibly with some sentence breaks
                offset = writeCharacters(event.asCharacters().getData(), breaks, offset);
            } else {
            	if (event.isStartElement() || event.isEndElement()) {
            		tagNumber++;
            		if (!matchingTags.hasMatchingTag(tagNumber) && sentenceOpen && writeStartAndEndTag) {
            			// We need to create a sentence break around this tag in order to
            			// keep the document well-formed.
            			closeSentence();
            			writeEvent(event);
            			openSentence();
            		} else {
            			writeEvent(event);
            		}
            	} else {
            		writeEvent(event);
            	}                
            }
        }
        
        // Add the final closing tag
        if (writeStartAndEndTag) {
            closeSentence();
        }
        //System.err.println("before last event");
        event = reader.nextEvent();
        writeEvent(event);
    }
    
    /**
     * Write characters to the output, and break a sentence if there are
     * sentence breaks within the text.
     * @param text the text to write
     * @param breaks the indexes of the sentence breaks
     * @param offset the offset of the current text
     * @return
     * @throws XMLStreamException
     */
    private int writeCharacters(String text, Vector breaks, int offset) throws XMLStreamException {
        if (breaks.size() == 0) {
            writeString(text);
            return offset + text.length();
        } 
        
        for (int i = 0; i < breaks.size(); ++i) {
            
            int currentBreak = ((Integer)breaks.elementAt(i)).intValue();
            if (text.length() + offset < currentBreak) {
                writeString(text);
                return offset + text.length();
            }
            if (offset < currentBreak) {
                writeString(text.substring(0, currentBreak - offset));
                text = text.substring(currentBreak - offset, text.length());
                offset = currentBreak;
                ContextStack.ContextInfo tmp = 
                	writeStack.getContext().pop();
                QName lastWritten = tmp.getName();
                /*
                 *  FIXME we only need to check that the last element really was
                 * a sentence break element, right? Hmm...
                 */
                if (breakSettings.isSentenceBreaking(lastWritten)) {                
                    breakSentence();                
                }
            }
        }
        if (text.length() > 0) {
            writeString(text);
            offset = offset + text.length();
        }
        return offset;
    }
    
    /**
     * Open a sentence
     * @throws XMLStreamException
     */
    private void openSentence() throws XMLStreamException {
        writeEvent(eventFactory.createStartElement(getBreakElement(), getBreakAttributes(), null), true);
        sentenceOpen = true;
    }
    
    /**
     * Close a sentence
     * @throws XMLStreamException
     */
    private void closeSentence() throws XMLStreamException {
        writeEvent(eventFactory.createEndElement(getBreakElement(), null), true);
        sentenceOpen = false;
    }
    
    /**
     * Break (i.e. close and open) a sentence.
     * @throws XMLStreamException
     */
    private void breakSentence() throws XMLStreamException {
        EndElement ee = eventFactory.createEndElement(getBreakElement(), null);
        StartElement se = eventFactory.createStartElement(getBreakElement(), getBreakAttributes(), null); 
        try {
	        writeEvent(ee);
	        writeEvent(se);
        } catch (IllegalArgumentException e) {
            //logger.warning("XML is unsuitable for sentence tagging...");
            throw e;
        }
    }
    
    /**
     * Should we look for sentece breaks between these tags?
     * @param firstName the first tag
     * @param firstIsStart is the first tag a start tag?
     * @param lastName the last tag
     * @param lastIsStart is the first tag a start tag?
     * @return true if the context should be processed, false otherwise
     */
    private boolean shouldBeProcessed(QName firstName, boolean firstIsStart, QName lastName, boolean lastIsStart) {
        //System.err.println("first: " + firstName + " " + firstIsStart);
        //System.err.println("last: " + lastName + " " + lastIsStart);
        if (!isPathAllowed()) {
            return false;
        }
        if (firstIsStart) {
            // The first tag is a start tag
            return breakSettings.mayContainText(firstName);
        } 
        // The first tag is an end tag        
        if (lastIsStart) {
        	// The last tag is a start tag
        	ContextStack.ContextInfo ci = 
        		contextStack.getParentContext().peek();
        	
        	return breakSettings.mayContainText(ci.getName());
        } 
        // The last tag is an end tag
        return breakSettings.mayContainText(lastName);
    }
            
    public void setContextStackFilter(ContextStack csf) {
        contextStack = csf;
    }
    
    /* *** TEST *** */
    
    public static void main(String[] args) throws UnsupportedDocumentTypeException, CatalogExceptionNotRecoverable, XMLStreamException, IOException {        
        XMLSentenceDetector detector = new XMLSentenceDetector(new File(args[0]), new File(args[1]));
        detector.detect(null);
    }
    
}
