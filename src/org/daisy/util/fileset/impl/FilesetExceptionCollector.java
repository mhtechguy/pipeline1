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
package org.daisy.util.fileset.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.daisy.util.fileset.FilesetErrorHandler;
import org.daisy.util.fileset.exception.FilesetFileException;

/*package*/ class FilesetExceptionCollector {
	private Set<Exception> exceptions = new HashSet<Exception>();
	private FilesetErrorHandler errH;
	
	FilesetExceptionCollector(FilesetErrorHandler fseh) {
		this.errH = fseh;
	}
	
	/*package*/ void add(FilesetFileException ffe) {
		//store it
		this.exceptions.add(ffe);
		try {
			//send it to listener
			this.errH.error(ffe);			
		} catch (FilesetFileException e) {
			e.printStackTrace();
		}		
	}
	
	/*package*/ Collection<Exception> getExceptions(){
		return this.exceptions;		
	}
	
	/*package*/ boolean hasExceptions(){
		return (!this.exceptions.isEmpty());
	}
}
