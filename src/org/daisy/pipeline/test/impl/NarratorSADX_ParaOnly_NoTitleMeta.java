package org.daisy.pipeline.test.impl;

import java.util.List;

import org.daisy.pipeline.test.PipelineTest;
import org.daisy.util.file.EFolder;

public class NarratorSADX_ParaOnly_NoTitleMeta extends PipelineTest {

	public NarratorSADX_ParaOnly_NoTitleMeta(EFolder dataInputDir, EFolder dataOutputDir) {
		super(dataInputDir, dataOutputDir);
	}

	@Override
	public void confirm() {
		// TODO Auto-generated method stub
	}

	@Override
	public List<String> getParameters() {
		mParameters.add("--input=" + mDataInputDir + "/sadx/ParaOnly_NoTitleMeta.xml");
		mParameters.add("--outputPath=" + mDataOutputDir
				+ "/NarratorSADX-ParaOnly_NoTitleMeta/");
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
