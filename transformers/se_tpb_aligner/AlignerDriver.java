package se_tpb_aligner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.daisy.pipeline.core.InputListener;
import org.daisy.pipeline.core.event.MessageEvent;
import org.daisy.pipeline.core.transformer.Transformer;
import org.daisy.pipeline.exception.TransformerRunException;
import org.daisy.util.file.EFile;
import org.daisy.util.file.EFolder;
import org.daisy.util.file.FileUtils;
import org.daisy.util.file.FilenameOrFileURI;
import org.daisy.util.xml.LanguageReporter;
import org.daisy.util.xml.pool.StAXInputFactoryPool;

import se_tpb_aligner.align.Aligner;
import se_tpb_aligner.align.AlignerException;
import se_tpb_aligner.align.AlignerFactory;
import se_tpb_aligner.align.AlignerFactoryException;
import se_tpb_aligner.subtree.DivisionStrategy;
import se_tpb_aligner.subtree.SubTree;
import se_tpb_aligner.subtree.SubTreeHandler;
import se_tpb_aligner.subtree.SubTreeHandlerFactory;
import se_tpb_aligner.textpre.PreProcessor;
import se_tpb_aligner.textpre.PreProcessorException;
import se_tpb_aligner.textpre.PreProcessorFactory;
import se_tpb_aligner.textpre.PreProcessorFactoryException;
import se_tpb_aligner.util.AudioSource;
import se_tpb_aligner.util.Triple;
import se_tpb_aligner.util.XMLResult;
import se_tpb_aligner.util.XMLSource;

/**
 * Main class for this Transformer. See /doc/transformers/se_tpb_aligner.
 * @author Markus Gylling
 */
public class AlignerDriver extends Transformer {
	private XMLSource mInputDoc = null;
	private LinkedList<File> mInputAudioFiles = null;
	private XMLResult mFinalOutputDoc = null;
	private EFolder mFinalOutputDir = null;
	private EFolder mTempDir = null;
	private String mLanguage = null;
	private boolean mUseFallbacks = true;
	
	public AlignerDriver(InputListener inListener, Boolean isInteractive) {
		super(inListener, isInteractive);		
	}
	
	/*
	 * TODO
	 * 
	 * 
	 */
	
	@Override
	protected boolean execute(Map parameters) throws TransformerRunException {
				
		try{			
			
			mFinalOutputDoc = new XMLResult(FilenameOrFileURI.toFile((String)parameters.remove("outputXML")));
			mFinalOutputDir = new EFolder(FileUtils.createDirectory(mFinalOutputDoc.getParentFile()));
			mTempDir = new EFolder(FileUtils.createDirectory(new File(mFinalOutputDir, "aligner__temp")));
			
			/*
			 * Get input data: an XML document, and a list of audio files. 
			 */
			mInputDoc = new XMLSource(FilenameOrFileURI.toFile((String)parameters.remove("inputXML")));
			EFolder audioFileDir = new EFolder(FilenameOrFileURI.toFile((String)parameters.remove("inputAudioDir")));
			mInputAudioFiles = new LinkedList<File>(audioFileDir.getFiles(false, ".+\\.[Ww][Aa][Vv]$"));
			Collections.sort(mInputAudioFiles);
			this.sendMessage(i18n("FOUND_AUDIO_FILES", mInputAudioFiles.size()), MessageEvent.Type.INFO);
			
			/*
			 * Identify language of the input XML
			 */
			LanguageReporter langReporter = new LanguageReporter(mInputDoc.toURI().toURL());
			mLanguage = langReporter.getRootLanguage();
			if(null==mLanguage) throw new TransformerRunException(i18n("NO_ROOT_LANGUAGE"));
			
			/*
			 * Instantiate a PreProcessor
			 */			
			PreProcessorFactory ppfac = PreProcessorFactory.newInstance();
			PreProcessor tpp = null;
			try{
				tpp = ppfac.getPreProcessor(mLanguage);
			}catch (PreProcessorFactoryException e) {
				this.sendMessage(i18n("PREPROCESSOR_FACTORY_ERROR", e.getMessage()), MessageEvent.Type.ERROR);
				tpp = ppfac.getFallbackInstance();
			}	
			
			this.sendMessage(0.05);
			
			/*
			 * Run the PreProcessor on the input doc
			 */
			XMLSource tppSource = new XMLSource(mInputDoc);
			XMLResult tppResult = new XMLResult(mTempDir, "preprocessed.xml");
			try{
				this.sendMessage("Running text preprocessor on " + tppSource.getName(), MessageEvent.Type.DEBUG);
				tpp.process(tppSource, mLanguage, tppResult);
			}catch (PreProcessorException e) {
				String message = i18n("PREPROCESSOR_ERROR", e.getMessage());										
				if(mUseFallbacks) {
					this.sendMessage(message, MessageEvent.Type.ERROR);
					PreProcessor fbpp = ppfac.getFallbackInstance();
					fbpp.process(tppSource, mLanguage, tppResult);
				}
			}				
			this.sendMessage(0.2);
			
			/*
			 * Prepare for alignment.
			 * Find out how to subsegment the input doc, and do it.
			 */
			DivisionStrategy ds = setDivisionStrategy(parameters);		
			XMLSource xs = new XMLSource(tppResult);
			SubTreeHandlerFactory sthf = SubTreeHandlerFactory.newInstance();
			SubTreeHandler subTreeHandler = sthf.getHandler(xs, ds);				
			subTreeHandler.initialize();						
			this.sendMessage(i18n("CREATED_SUBDOCUMENTS", subTreeHandler.size()), MessageEvent.Type.INFO);		
			
			/*
			 * Make sure the number of audiofiles match the number of segments
			 */
			if(subTreeHandler.size()!= mInputAudioFiles.size()) {
				throw new TransformerRunException(i18n("SIZE_MISMATCH"));
			}
			
			
			/*
			 * Prepare for alignment.
			 * Associate input XML segment path, output XML segment path, and audio file.
			 * Create a temporary directory, and store the input segments there.
			 */															
			List<Triple> triples = new LinkedList<Triple>();			
			int i = 0;
			for(SubTree subtree : subTreeHandler) {
				this.checkAbort();
				i++;
				XMLSource alignerXMLSource = new XMLSource(mTempDir, "segment_source"+i+".xml");
				AudioSource alignerAudioSource = new AudioSource(mInputAudioFiles.get(i-1));
				XMLResult alignerXMLResult = new XMLResult(mTempDir, "segment_result"+i+".xml");				
				Triple triple = new Triple(alignerXMLSource, alignerAudioSource, alignerXMLResult);
				triples.add(triple);				
				subtree.render(new XMLResult(triple.getXMLSource()));				
			}
			
						
			/*
			 * Instantiate an Aligner that supports the current language.			
			 */			
			AlignerFactory alignerFactory = AlignerFactory.newInstance(); 
			Aligner aligner = null;
			try{
				aligner = alignerFactory.getAligner(mLanguage);
			}catch (AlignerFactoryException e) {
				String message = i18n("ALIGNER_FACTORY_ERROR", e.getMessage());
				throw new TransformerRunException(message);
			}	
			this.sendMessage(0.3);
			
			/*
			 * Loop over the triples list.
			 * The Aligner will render its output to the destination given 
			 * in the result field of the Triple object.
			 * Replace subtrees in the subtreehandler with the aligner output.
			 */			
			int k = 0;
			for (Triple t : triples) {	
				this.checkAbort();	
				k++;
				try {
					this.sendMessage("Aligning " + t.getXMLSource().getName(), MessageEvent.Type.DEBUG);
					aligner.process(t.getXMLSource(), t.getAudioSource(), mLanguage, t.getXMLResult());
					double rel = (double)k/(double)triples.size();	
					this.sendMessage(0.6*rel+0.3);					
				}catch (AlignerException e) {
					String message = i18n("ALIGNER_ERROR", e.getMessage());					
					if(mUseFallbacks) {
						this.sendMessage(message, MessageEvent.Type.ERROR);
						Aligner fallbackAligner = alignerFactory.getFallbackInstance();
						fallbackAligner.process(t.getXMLSource(), t.getAudioSource(), mLanguage, t.getXMLResult());
					}else{
						throw new TransformerRunException(message);
					}
				}	
				subTreeHandler.set(k-1, new SubTree(new XMLSource(t.getXMLResult())));
			}
			this.sendMessage(0.9);
			
			/*
			 * Render the one XML doc to final destination.
			 * This is the input doc with SMIL namespace attribute decorations added. 
			 */
			subTreeHandler.render(mFinalOutputDoc);
			
			/*
			 * Copy the audio files to the same dir as mFinalOutputDoc
			 */
			mFinalOutputDir.addFiles(mInputAudioFiles, true);
			
			/*
			 * delete the contents of the internal temp dir
			 */
			System.gc();
			mTempDir.deleteContents(true);
			mTempDir.delete();
												
		} catch (Exception e) {
			String message = i18n("ERROR_ABORTING", e.getMessage());
			throw new TransformerRunException(message, e);
		}

		return true;
		
	}

	private int countElements(File doc) throws FileNotFoundException, XMLStreamException {
		EFile efile = new EFile(doc);
		Map properties = null;
		int k = 0;
		XMLInputFactory xif = StAXInputFactoryPool.getInstance().acquire(properties);
		XMLEventReader xer = xif.createXMLEventReader(efile.asInputStream());
		while(xer.hasNext()) {
			XMLEvent xe = xer.nextEvent();
			if (xe.isStartElement()) ++k;			
		}		
		StAXInputFactoryPool.getInstance().release(xif, properties);
		return k;
	}

	private DivisionStrategy setDivisionStrategy(Map parameters) throws TransformerRunException {
		String strategyParam = (String)parameters.remove("divider");		
		if(strategyParam.equals("levels")) {
			return DivisionStrategy.LEVELS;
		} else if(strategyParam.equals("pages")) {
			return DivisionStrategy.PAGES;
		} else {
			throw new TransformerRunException("divider parameter not set to recognized value");
		}
	}

}
