/*
 * org.daisy.util (C) 2005-2008 Daisy Consortium
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
package org.daisy.util.xml;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.daisy.util.xml.peek.PeekResult;
import org.daisy.util.xml.peek.Peeker;
import org.daisy.util.xml.peek.PeekerPool;
import org.daisy.util.xml.validation.SchemaLanguageConstants;
import org.xml.sax.Attributes;

/**
 * @author Markus Gylling
 */
public class XMLUtils {
	
	/**
	 * Delegate for determining the Schema language type of a resource.
	 * <p>This method only detects schema types that are based on XML, ie you
	 * cannot use this to find out if a resource is a DTD.</p>
	 * <p>Note - this method will not report compound schemas, only the root schema type, 
	 * based on the default ns uri of the input document. For compound schema detection, 
	 * use {@link org.daisy.util.xml.NamespaceReporter}.</p>
	 * @return a SchemaLanguageConstant NS URI, or null if schema type was not detected, 
	 * meaning that the inparam resource was probably something else than an xml-based schema.
	 * @throws Exception if something really evil occurs
	 */
	public static String getSchemaType(URL url) throws Exception {
		try{
			Peeker peeker = PeekerPool.getInstance().acquire();
			PeekResult schemaPeekResult = peeker.peek(url);
			PeekerPool.getInstance().release(peeker);
			String rootName = schemaPeekResult.getRootElementLocalName();		
			String rootNsUri = schemaPeekResult.getRootElementNsUri();
			
			if(rootName == "schema") {
				if(rootNsUri == SchemaLanguageConstants.SCHEMATRON_NS_URI){
					return SchemaLanguageConstants.SCHEMATRON_NS_URI;
				}else if(rootNsUri == SchemaLanguageConstants.ISO_SCHEMATRON_NS_URI){
					return SchemaLanguageConstants.ISO_SCHEMATRON_NS_URI;
				}else if(rootNsUri == SchemaLanguageConstants.W3C_XML_SCHEMA_NS_URI){
					return SchemaLanguageConstants.W3C_XML_SCHEMA_NS_URI;
				}							
			}else if(rootName == "grammar" 
				&& rootNsUri == SchemaLanguageConstants.RELAXNG_NS_URI) {
				return SchemaLanguageConstants.RELAXNG_NS_URI;
			}else{				
				//... it may be a DTD or something completey other...
				return null;				
			}
		}catch (Exception e) {
			//peeker parse failure, or peeker getters returning null
			throw e;
		}
		return null;
	}
	
	/**
	 * Delegate for XSI retrieval from a SAX StartElement event.
	 * @return a Set of Strings (equalling the unaltered xsd uris), or an empty set
	 * if no XSI information was found in this StartElement. Parameters are a pure echo 
	 * of those fed to the SAX StartElement event. 
	 */
	@SuppressWarnings("unused")
	public static final Set<String> getXSISchemaLocationURIs(String uri, String localName,
			String qName, Attributes atts) {
		Set<String> set = new HashSet<String>();
		String xsi = null;
		   
		//we dont know how namespace handling is configured in SAX parser, 
		//so need to try all alternatives.
        xsi = atts.getValue("http://www.w3.org/2001/XMLSchema-instance","noNamespaceSchemaLocation");
        if(xsi==null) {	
        	xsi = atts.getValue("xsi:noNamespaceSchemaLocation");
        	if(xsi==null) {	
        		xsi = atts.getValue("noNamespaceSchemaLocation");
        	}
        }                
        if(xsi!=null) set.add(xsi);
        
        
        xsi=null;        
        xsi = atts.getValue("http://www.w3.org/2001/XMLSchema-instance","schemaLocation");
        if(xsi==null) {	
        	xsi = atts.getValue("xsi:schemaLocation");
        	if(xsi==null) {	
        		xsi = atts.getValue("schemaLocation");
        	}
        }       
        if(xsi!=null) {
        	String[] array = xsi.split(" ");
        	for (int i = 0; i < array.length; i++) {
        		//get only uneven				
        		if(i % 2 != 0) {
        			set.add(array[i]);
        		}        		
			}        	
        }
        
        return set;
	}
	
}
