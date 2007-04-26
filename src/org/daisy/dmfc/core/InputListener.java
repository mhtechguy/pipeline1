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
package org.daisy.dmfc.core;

import org.daisy.dmfc.core.event.RequestEvent;
import org.daisy.dmfc.core.event.UserReplyEvent;

/**
 * A user interface implementing this interface will get requests 
 * of user input from Transformers (provided the Transformers are
 * in interactive mode).
 * 
 * @author Linus Ericson
 */
public interface InputListener {
		
	/**
	 * Receive a request for user input from the Pipeline core, 
	 * most probably from a Transformer
	 * @param event The system request
	 * @return a user reply
	 */
	public UserReplyEvent getUserReply(RequestEvent event);
	
}
