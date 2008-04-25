package int_daisy_mixedContentNormalizer.dom;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.daisy.util.xml.pool.StAXInputFactoryPool;

/**
 * 
 * @author Markus Gylling
 */
public class DOMConfigLoader {

	public static DOMConfig load(URL url) throws XMLStreamException, IOException {
		final String pipelineNS = "http://www.daisy.org/pipeline/ns#";
		QName namespace = new QName(pipelineNS,"namespace");
		QName ignore = new QName(pipelineNS,"ignore");
		QName wrapper = new QName(pipelineNS,"wrapper");	
		QName syncScope = new QName(pipelineNS,"syncScope");
		QName syncForce = new QName(pipelineNS,"syncForce");
		QName syncAttr = new QName(pipelineNS,"syncMarkerAttribute");		
		QName whitespaceAttr = new QName("asWhitespace");
		QName scrubAttr = new QName("scrubWhitespace");
		DOMConfig dnc = new DOMConfig();
	
		XMLInputFactory xif = null;
		Map properties = null;
		try{
			properties = StAXInputFactoryPool.getInstance().getDefaultPropertyMap(false);
			xif = StAXInputFactoryPool.getInstance().acquire(properties);
			XMLEventReader reader = xif.createXMLEventReader(url.openStream());
			boolean inIgnore = false;
			boolean inWrapper = false;
			boolean inSyncAttr = false;
			boolean inSyncScope = false;
			boolean inSyncForce = false;
				
			String currentNS = null;
			while(reader.hasNext()) {
				XMLEvent xe = reader.nextEvent();
				if(xe.isStartElement()) {
					StartElement se = xe.asStartElement();
					if(se.getName().equals(namespace)) {
						for (Iterator iterator = se.getAttributes(); iterator.hasNext();) {
							Attribute a = (Attribute) iterator.next();
							if(a.getName().getLocalPart().equals("uri")) {								
								dnc.addSupportedNamespace(a.getValue());		
								currentNS = a.getValue();
							}							
						}						
					}else if(se.getName().equals(ignore)) {
						Attribute ws = se.getAttributeByName(whitespaceAttr);
						if (ws!=null) {
								dnc.addWhitespaceCharacters(currentNS, ws.getValue());
						}
						inIgnore = true; continue;
					}else if (se.getName().equals(wrapper)) {
						Attribute scrub = se.getAttributeByName(scrubAttr);
						if (scrub!=null) {
							if(scrub.getValue().equals("true")) {
								dnc.setIsScrubbingWrappers(currentNS);	
							}
						}
						inWrapper = true; continue;
					}else if (se.getName().equals(syncScope)) {
						inSyncScope = true; continue;
					}else if (se.getName().equals(syncAttr)) {
						inSyncAttr = true; continue;
					}else if (se.getName().equals(syncForce)) {
						inSyncForce = true; continue;						
					}else if (inIgnore) {
						dnc.addIgnorable(se);
					}else if (inWrapper) {
						dnc.addWrapper(se);
					}else if (inSyncScope) {
						dnc.addScope(se.getName().getNamespaceURI(), se.getName());
					}else if (inSyncForce) {
						dnc.addSyncForce(se);						
					}else if (inSyncAttr) {						
						dnc.addSyncPointAttribute(currentNS, se.getName(), "true");
					}
				}else if (xe.isEndElement()) {
					if(xe.asEndElement().getName().equals(ignore)) {
						inIgnore = false;
					}else if (xe.asEndElement().getName().equals(wrapper)) {
						inWrapper = false;
					}else if (xe.asEndElement().getName().equals(syncScope)) {
						inSyncScope = false;
					}else if (xe.asEndElement().getName().equals(syncForce)) {
						inSyncForce = false;						
					}else if (xe.asEndElement().getName().equals(syncAttr)) {
						inSyncAttr = false;
					}
				}
			}						
			reader.close();
			return dnc;
		}finally{
			StAXInputFactoryPool.getInstance().release(xif, properties);
		}
	}	
}
