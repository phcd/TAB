package com.archermind.txtbl.attachment.format.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jboss.logging.Logger;

public class DefaultJPG {

	public static byte[] jpgData = null;
	
	private static final String txtbl_home = "TXTBL_HOME";
	
	private static final String PIC_FILE_NAME = "txtbl.jpg";

    private static final Logger log = Logger.getLogger(DefaultJPG.class);
	public static byte[] getJPG() {
		if (jpgData == null || jpgData.length < 2) {
			new DefaultJPG().initJPG();
		}
		return jpgData;
	}

	private void initJPG() {
		if (jpgData == null || jpgData.length < 2) {
			InputStream is = null;
			try {
				File file = new File(new StringBuffer().append(System.getenv(txtbl_home) == null ? System.getProperty(txtbl_home) : System.getenv(txtbl_home)).append("/").append(PIC_FILE_NAME).toString());
				is = new FileInputStream(file);
				int size = is.available();
				jpgData = new byte[size];
				is.read(jpgData, 0, size);
			} catch (Throwable t) {
				jpgData = new byte[0];
				log.error("initJPG/DefaultJPG/Exception: ", t);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException ignored) {}
				}
			}
		}
	}
}
