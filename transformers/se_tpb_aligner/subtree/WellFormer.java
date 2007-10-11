package se_tpb_aligner.subtree;

import java.util.List;

import javax.xml.stream.events.XMLEvent;

/**
 * Make a (DocumentFragment-like) list of XMLEvents generated by a SubTreeHandler wellformed. 
 * @author Markus Gylling
 */
public interface WellFormer {

	public List<XMLEvent> makeWellFormed(List<XMLEvent> list);
	
}
