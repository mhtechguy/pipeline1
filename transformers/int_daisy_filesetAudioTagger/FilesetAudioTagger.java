package int_daisy_filesetAudioTagger;

import int_daisy_filesetAudioTagger.id3.ID3Tagger;
import int_daisy_filesetAudioTagger.playlist.AbstractWriter;
import int_daisy_filesetAudioTagger.playlist.M3U8Writer;
import int_daisy_filesetAudioTagger.playlist.M3UWriter;
import int_daisy_filesetAudioTagger.playlist.PLSWriter;
import int_daisy_filesetAudioTagger.playlist.XSPFWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.daisy.dmfc.core.InputListener;
import org.daisy.dmfc.core.message.TransformerMessage;
import org.daisy.dmfc.core.message.property.Cause;
import org.daisy.dmfc.core.message.property.Type;
import org.daisy.dmfc.core.transformer.Transformer;
import org.daisy.dmfc.exception.TransformerRunException;
import org.daisy.util.file.EFile;
import org.daisy.util.file.EFolder;
import org.daisy.util.file.FileUtils;
import org.daisy.util.file.FilenameOrFileURI;
import org.daisy.util.fileset.exception.FilesetFileException;
import org.daisy.util.fileset.exception.FilesetFileFatalErrorException;
import org.daisy.util.fileset.impl.FilesetImpl;
import org.daisy.util.fileset.interfaces.Fileset;
import org.daisy.util.fileset.interfaces.FilesetErrorHandler;
import org.daisy.util.fileset.interfaces.FilesetFile;
import org.daisy.util.fileset.interfaces.audio.AudioFile;
import org.daisy.util.fileset.interfaces.audio.Mp3File;
import org.daisy.util.fileset.manipulation.FilesetFileManipulator;
import org.daisy.util.fileset.manipulation.FilesetManipulationException;
import org.daisy.util.fileset.manipulation.FilesetManipulator;
import org.daisy.util.fileset.manipulation.FilesetManipulatorListener;
import org.daisy.util.fileset.manipulation.manipulators.UnalteringCopier;
import org.daisy.util.fileset.util.FilesetLabelProvider;
import org.daisy.util.fileset.util.FilesetSpineProvider;

/**
 * Main transformer class. Generate ID3 Tags and playlists from Fileset instances.
 * @author Markus Gylling
 */
public class FilesetAudioTagger extends Transformer implements FilesetErrorHandler, FilesetManipulatorListener {
	private EFile mInputManifest = null;
	private Fileset mInputFileset = null;
	private EFolder mOutputDir = null;				
	private FilesetManipulator fm = null;
	private Collection mAudioSpine = null;
	private FilesetLabelProvider mLabelProvider = null;
	
	public FilesetAudioTagger(InputListener inListener, Set eventListeners, Boolean isInteractive) {
		super(inListener, eventListeners, isInteractive);		
	}
	
	@Override
	protected boolean execute(Map parameters) throws TransformerRunException {
		
		boolean doID3tags = false;
		boolean doPlaylists = false;
		
		String bool = (String)parameters.remove("doID3Tagging");		
		if(bool!=null && bool.equals("true")) doID3tags = true;
		
		bool = (String)parameters.remove("doPlaylistGeneration");		
		if(bool!=null && bool.equals("true")) doPlaylists = true;

		try{
			//set the input manifest
			mInputManifest = new EFile(FilenameOrFileURI.toFile((String)parameters.remove("input")));
			//set input fileset
			mInputFileset = new FilesetImpl(mInputManifest.toURI(),this,false,false);
			//set/create output dir
			mOutputDir = (EFolder)FileUtils.createDirectory(new EFolder(FilenameOrFileURI.toFile((String)parameters.remove("output"))));
			//find out if inparam in and out directories are the same 
			boolean inputDirEqualsOutputDir = mOutputDir.getCanonicalPath().equals(mInputFileset.getManifestMember().getFile().getParentFile().getCanonicalPath());
			//create the audioSpine ArrayList
			mAudioSpine = FilesetSpineProvider.getAudioSpine(mInputFileset);			
			//create the label provider
			mLabelProvider = new FilesetLabelProvider(mInputFileset);			
//			debugPrintAudioSpine();
			
			this.progress(0.05);
			this.checkAbort();
			
			if(doID3tags) {
				sendMessage(new TransformerMessage(this,i18n("GENERATING_ID3TAGS"),Type.INFO, Cause.SYSTEM));
				fm = new FilesetManipulator();
				fm.setInputFileset(mInputFileset);
				fm.setOutputFolder(mOutputDir);
				fm.setListener(this);
				fm.setFileTypeRestriction(Mp3File.class);
				fm.iterate();				
			}
			sendMessage(new TransformerMessage(this,i18n("GENERATED_ID3TAGS",mAudioSpine.size()),Type.INFO, Cause.SYSTEM));
						
			this.progress(0.95);
			this.checkAbort();
			
			if(doPlaylists) {
								
				String name = "playlist"; //TODO
				
				Map<String,Class> writers = new HashMap<String,Class>();				
				writers.put(name+".pls", PLSWriter.class);
				writers.put(name+".m3u", M3UWriter.class);
				writers.put(name+".m3u8", M3U8Writer.class);
				writers.put(name+".xspf", XSPFWriter.class);

				int generatedPlaylists = 0;				
				for (Iterator iter = writers.keySet().iterator(); iter.hasNext();) {
					String filename = (String)iter.next();
					Class writer = writers.get(filename);
					Constructor constr = writer.getDeclaredConstructor(new Class[] {FilesetLabelProvider.class, Collection.class});
					AbstractWriter plwr;
					try{
						plwr = (AbstractWriter)constr.newInstance(new Object[] {mLabelProvider,mAudioSpine});
						plwr.initialize();
						plwr.render(new File(mOutputDir,filename));
						generatedPlaylists++;
					}catch(Exception e) {
						sendMessage(new TransformerMessage(this,i18n("ERROR_GENERATING_PLAYLIST",filename),Type.ERROR, Cause.SYSTEM));
					}
				}
				sendMessage(new TransformerMessage(this,i18n("GENERATED_PLAYLISTS",generatedPlaylists),Type.INFO, Cause.SYSTEM));
			}
			
		} catch (Exception e) {
			sendMessage(new TransformerMessage(this,i18n("ERROR_ABORTING"),Type.ERROR, Cause.SYSTEM));
			throw new TransformerRunException(e.getMessage(),e);
		}
		
		return true;
	}


	/*
	 * (non-Javadoc)
	 * @see org.daisy.util.fileset.interfaces.FilesetErrorHandler#error(org.daisy.util.fileset.exception.FilesetFileException)
	 */
	public void error(FilesetFileException ffe) throws FilesetFileException {
		if (ffe instanceof FilesetFileFatalErrorException && !(ffe.getCause() instanceof FileNotFoundException)) {
			this.sendMessage(new TransformerMessage(this,ffe.getCause() + " in " + ffe.getOrigin(),Type.ERROR,Cause.INPUT));
		} else {
			this.sendMessage(new TransformerMessage(this,ffe.getCause() + " in " + ffe.getOrigin(),Type.WARNING,Cause.INPUT));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.daisy.util.fileset.manipulation.FilesetManipulatorListener#nextFile(org.daisy.util.fileset.interfaces.FilesetFile)
	 */
	private int nextFileCallCount = 0;
	private double spineSize = -1.0; 

	public FilesetFileManipulator nextFile(FilesetFile file) throws FilesetManipulationException {
		/*
		 * Restriction is set to only expose Mp3Files
		 */
		
		if(spineSize == -1.0) spineSize = Double.parseDouble(Integer.toString(mAudioSpine.size())+".0");
		nextFileCallCount++;				
		this.progress(0.05 + ((nextFileCallCount/spineSize)*0.9)); //assumes that progress 0.05 was called before first nextFile call
		
		if (mAudioSpine.contains(file)) {	
			//String titleFrame, String artistFrame, String albumFrame, String trackNumberFrame
			try {
				return new ID3Tagger(mLabelProvider.getFilesetFileTitle(file),mLabelProvider.getFilesetCreator(),mLabelProvider.getFilesetTitle(),getAudioSpinePosition(file));				
			} catch (FilesetFileException e) {
				throw new FilesetManipulationException(e.getMessage(),e);
			}
		}
		return new UnalteringCopier();
		
	}
	
	/**
	 * @return a string in format n(this)/n(tot), eg '4/23'
	 */
	private String getAudioSpinePosition(FilesetFile inFile) {
		long count = 0;
		for (Iterator iter = mAudioSpine.iterator(); iter.hasNext();) {
			count++;
			FilesetFile current = (FilesetFile) iter.next();
			if(current==inFile) {
				return Long.toString(count)+"/"+Long.toString(mAudioSpine.size());
			}			
		}
		return null;
	}

	private void debugPrintAudioSpine() {
		System.err.println("");
		for (Object o : mAudioSpine) {
			AudioFile file = (AudioFile)o;
			try {
				System.err.println(file.getName() + " " +  mLabelProvider.getFilesetFileTitle(file));
			} catch (FilesetFileException e) {
				e.printStackTrace();
			}
		}
		System.err.println("");
	}
}