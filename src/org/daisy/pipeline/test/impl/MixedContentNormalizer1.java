package org.daisy.pipeline.test.impl;

import java.util.List;

import org.daisy.pipeline.test.PipelineTest;
import org.daisy.util.file.EFolder;

public class MixedContentNormalizer1 extends PipelineTest {

	public MixedContentNormalizer1(EFolder dataInputDir, EFolder dataOutputDir) {
		super(dataInputDir, dataOutputDir);
	}
	
	@Override
	public List<String> getParameters() {		
		//mParameters.add("--inputXML=" + mDataInputDir + "/xhtml/mixed-content.html");		
		mParameters.add("--inputXML=" + mDataInputDir + "/dtbook/hauy-2005-1.xml");
		mParameters.add("--outputXML=" + mDataOutputDir + "/MixedContentNormalizer1/normalized.xml"); 

//		mParameters.add("--inputXML=D:\\aligner-test-dtbs\\christ-pages\\christ-pages.xml");
//		mParameters.add("--inputXML=" + "D:\\very-large-invalid-dtbook.xml");				
//		mParameters.add("--outputXML=" + "D:\\very-large-invalid-dtbook-out.xml");
		
		return mParameters;
	}

	@Override
	public String getResultDescription() {		
		return "";
	}

	@Override
	public boolean supportsScript(String scriptName) {
		if("MixedContentNormalizer.taskScript".equals(scriptName)) {
			return true;
		}		
		return false;
	}

	@Override
	public void confirm() {
		// TODO Auto-generated method stub		
	}

}
