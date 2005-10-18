/*
 * DMFC - The DAISY Multi Format Converter
 * Copyright (C) 2005  Daisy Consortium
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
package se_tpb_xmldetection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

/**
 * 
 * @author Linus Ericson
 */
/*package*/ class LangDetector {

    public static Set getXMLLangSet(File file) throws FileNotFoundException, XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
        factory.setProperty("javax.xml.stream.isNamespaceAware", Boolean.TRUE);
        
        XMLEventReader er = factory.createXMLEventReader(new FileInputStream(file));
        
        Set result = new LinkedHashSet();
        
        while (er.hasNext()) {
            XMLEvent event = er.nextEvent();
            if (event.isStartElement()) {
                Attribute xmllang = event.asStartElement().getAttributeByName(new QName(XMLConstants.XML_NS_URI, "lang", XMLConstants.XML_NS_PREFIX));
                if (xmllang != null) {
                    result.add(xmllang.getValue());
                }
            }
        }
        
        er.close();
        
        return result;
    }
    
}
