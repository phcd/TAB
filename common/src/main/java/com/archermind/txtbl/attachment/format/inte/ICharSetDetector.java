package com.archermind.txtbl.attachment.format.inte;

import java.io.File;

public interface ICharSetDetector {
	public String detect(byte[] bytes) throws Exception;
}
