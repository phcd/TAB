package com.archermind.txtbl.attachment.format.inte.impl;

import com.archermind.txtbl.attachment.format.inte.ICharSetDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.UnicodeDetector;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

public class CharSetDetector implements ICharSetDetector {
	public String detect(byte[] bytes) throws Exception {
		InputStream inStream = new ByteArrayInputStream(bytes);
		CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
		detector.add(UnicodeDetector.getInstance());
		detector.add(JChardetFacade.getInstance());
		Charset set = detector.detectCodepage(inStream, bytes.length);
		return set.displayName();
	}

}
