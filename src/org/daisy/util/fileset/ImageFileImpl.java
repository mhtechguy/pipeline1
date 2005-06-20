package org.daisy.util.fileset;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

/**
 * @author Markus Gylling
 */
class ImageFileImpl extends FilesetFileImpl implements ImageFile {

    ImageFileImpl(URI uri) throws FileNotFoundException, IOException {
        super(uri);
    }

	public void parse(){}

}
