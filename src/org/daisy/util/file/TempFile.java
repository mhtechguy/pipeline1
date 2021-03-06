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
package org.daisy.util.file;

import java.io.File;
import java.io.IOException;

/**
 * Creates a temporary file. The file will be automatically deleted when
 * the virtual machine exists.
 * @author Linus Ericson
 */
public class TempFile {
	
	private static File tempDir = null;
	
	private File file;
	
	/**
	 * Sets the directory where the temporary files will be stored.
	 * If the directory is not set, the temporary directory of the system
	 * will be used.
	 * @param directory a directory
	 */
	public static void setTempDir(File directory) {
		if (directory.isDirectory()) {
			tempDir = directory;
		}
	}
	
	/**
	 * Creates a new TempFile.
	 * @throws IOException
	 */
	public TempFile() throws IOException {
		file = File.createTempFile("temp", ".tmp", tempDir);
		file.deleteOnExit();
	}
	
	/**
	 * Creates a new TempFile from an existing file.
	 * This constructor creates a new temporary file and copies
	 * the contents of <code>sourceFile</code> to it.
	 * @param sourceFile the File to copy
	 * @throws IOException
	 */
	public TempFile(File sourceFile) throws IOException {
		file = File.createTempFile("temp", ".tmp", tempDir);
		FileUtils.copyFile(sourceFile, file);
		file.deleteOnExit();
	}
	
	/**
	 * Creates a new <code>TempFile</code> and returns the <code>File</code> reference to it.
	 * @return the <code>File</code> reference to to the <code>TempFile</code>
	 * @throws IOException
	 */
	public static File create() throws IOException {
		TempFile temp = new TempFile();
		return temp.getFile();
	}
	
	/**
	 * Creates a temporary directory. The directory is <em>not</em>
	 * automatically deleted when the virtual machine exists.
	 * @return a temporary directory
	 * @throws IOException
	 */
	public static File createDir() throws IOException {
	    synchronized (TempFile.class) {
	        File temp = File.createTempFile("temp", ".tmp", tempDir);
	        temp.delete();
	        temp.mkdirs();
	        return temp;
	    }
	}
	
	/**
	 * @return the <code>File</code> reference to to the <code>TempFile</code> 
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * Deletes the temporary file. The file will automatically deleted on exit, but
	 * this function can be used to delete the file manually earlier.
	 * @return <code>true</code> if the deletion was successful, <code>false</code> otherwise
	 */
	public boolean delete() {
		return file.delete();
	}
}
