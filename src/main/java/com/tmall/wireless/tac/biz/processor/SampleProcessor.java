package com.tmall.wireless.tac.biz.processor;

import com.tmall.wireless.tac.client.ProcessContext;
import com.tmall.wireless.tac.client.ProcessHandler;
import com.tmall.wireless.tac.client.ProcessResult;

public class SampleProcessor implements ProcessHandler {

	public ProcessResult execute(ProcessContext context) throws Exception {
		ProcessResult processResult = new ProcessResult();
		System.out.print("Hello Word TAC!!!");
		// TODO Auto-generated method stub
		return processResult;
	}

}
