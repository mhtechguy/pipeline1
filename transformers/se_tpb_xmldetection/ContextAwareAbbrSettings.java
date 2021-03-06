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

import java.util.Map;

import javax.xml.namespace.QName;

/**
 * BreakSettings implementation used for abbreviation/acronym/initialism detection.
 * @author Linus Ericson
 */

/*package*/ class ContextAwareAbbrSettings extends ContextAwareBreakSettings {
    
    /* *** Constructor *** */
    
    public ContextAwareAbbrSettings() {
        super(false);
    }
     
    /* *** Methods from ContextAwareBreakSettings *** */
    
    public QName getBreakElement() {        
        return abbrElement;
    }
    
    
    @SuppressWarnings("unchecked")
	public Map getBreakAttributes() {
        return abbrAttributes;           
    }
    
}
