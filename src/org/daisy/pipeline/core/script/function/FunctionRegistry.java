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
package org.daisy.pipeline.core.script.function;

import java.util.HashMap;
import java.util.Map;

/**
 * Script function registry.
 * @author Linus Ericson
 */
public class FunctionRegistry {

	private static Map<String,Function> sFunctions = new HashMap<String, Function>();
	
	static {
		sFunctions.put("filename", new FilenameFunction());
		sFunctions.put("parent", new ParentFunction());
		sFunctions.put("safeFilenameNoExt", new SafeFilenameNoExtFunction());
	}
	
	/**
	 * Lookup a function in the registry.
	 * @param name the name of the function
	 * @return a Function, or null a function with the specified name is not found
	 */
	public static Function lookup(String name) {
		return sFunctions.get(name);
	}
}
