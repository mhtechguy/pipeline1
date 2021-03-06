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
package org.daisy.util.xml.sax;

import org.xml.sax.Attributes;

/**
 * Clones the contents of a SAX Attributes proxy
 * and returns a nontransient immutable opaque orthogonal equivalent. 
 * @author Markus Gylling
 */
public class AttributesCloner {

	public static Attributes clone(Attributes proxy) {		
		return new AttributesCloner().new AttributesClone(proxy);
	}
		
	class AttributesClone implements Attributes {		
		Attribute[] mAttList = null;
		
		AttributesClone(Attributes atts) {
			mAttList = new Attribute[atts.getLength()];
			for (int i = 0; i < atts.getLength(); i++) {
				Attribute a = new Attribute();
				a.mQName = atts.getQName(i);
				a.mLocalName = atts.getLocalName(i);
				a.mType = atts.getType(i);
				a.mValue = atts.getValue(i);
				a.mURI = atts.getURI(i);
				mAttList[i]=a;
			}	
		}

		public int getIndex(String qName) {
			for (int i = 0; i < mAttList.length; i++) {
				if(mAttList[i].mQName.equals(qName)) {
					return i;
				}
			}
			return -1;
		}

		public int getIndex(String uri, String localName) {
			for (int i = 0; i < mAttList.length; i++) {
				if(mAttList[i].mURI.equals(uri) 
						&& mAttList[i].mLocalName.equals(localName)) {
					return i;
				}
			}
			return -1;			
		}

		public int getLength() {
			return mAttList.length;
		}

		public String getLocalName(int index) {
			return mAttList[index].mLocalName;
		}

		public String getQName(int index) {
			return mAttList[index].mQName;
		}

		public String getType(int index) {
			return mAttList[index].mType;
		}

		public String getType(String qName) {
			for (int i = 0; i < mAttList.length; i++) {
				if(mAttList[i].mQName.equals(qName)) {
					return mAttList[i].mType;
				}
			}
			return null;			
		}

		public String getType(String uri, String localName) {
			for (int i = 0; i < mAttList.length; i++) {
				if(mAttList[i].mURI.equals(uri)
						&& mAttList[i].mLocalName.equals(localName)) {
					return mAttList[i].mType;
				}
			}
			return null;
		}

		public String getURI(int index) {
			return mAttList[index].mURI;			
		}

		public String getValue(int index) {
			return mAttList[index].mValue;
		}

		public String getValue(String qName) {
			for (int i = 0; i < mAttList.length; i++) {
				if(mAttList[i].mQName.equals(qName)) {
					return mAttList[i].mValue;
				}
			}
			return null;	
		}

		public String getValue(String uri, String localName) {
			for (int i = 0; i < mAttList.length; i++) {
				if(mAttList[i].mURI.equals(uri)
						&& mAttList[i].mLocalName.equals(localName)) {
					return mAttList[i].mValue;
				}
			}
			return null;
		}
		
	}
	
	class Attribute {
		String mType;
		String mValue;
		String mURI;
		String mQName;
		String mLocalName;		
	}
	
}
