package org.daisy.pipeline.test.impl;

import java.util.List;

import org.daisy.pipeline.test.PipelineTest;
import org.daisy.util.file.EFolder;

public class NarratorSADX_Simple_NoCreatorMeta extends PipelineTest {

	public NarratorSADX_Simple_NoCreatorMeta(EFolder dataInputDir, EFolder dataOutputDir) {
		super(dataInputDir, dataOutputDir);
	}

	@Override
	public void confirm() {
		// TODO Auto-generated method stub
	}

	@Override
	public List<String> getParameters() {
		mParameters.add("--input=" + mDataInputDir + "/sadx/Simple_NoCreatorMeta.xml");
		mParameters.add("--outputPath=" + mDataOutputDir
				+ "/NarratorSADX-Simple_NoCreatorMeta/");
		return mParameters;
	}

	@Override
	public String getResultDescription() {
		return "";
	}

	@Override
	public boolean supportsScript(String scriptName) {
		if ("DTBookFix_NarratorValidation.taskScript".equals(scriptName)) {
			return true;
		}
		return false;
	}

}
