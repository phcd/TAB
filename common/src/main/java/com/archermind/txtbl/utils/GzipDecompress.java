package com.archermind.txtbl.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;

import com.archermind.txtbl.exception.TxtblException;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class GzipDecompress extends Decompress {

	public byte[] handle(byte[] message) throws TxtblException {

		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		GZIPInputStream gzin = null;
		ByteArrayInputStream is = null;
		
		try {
			byte[] buf = new byte[100];

			is = new ByteArrayInputStream(message);
	
			int size;
			gzin= new GZIPInputStream(is);
			
			while ((size = gzin.read(buf)) > 0) {
				bao.write(buf, 0, size);
			}
		} catch (Exception ex) {
			throw new TxtblException("GzipDecompress",ex);			
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// nothing to do here
				}
			}
			
			if (gzin != null) {
				try {
					gzin.close();
				} catch (IOException e) {
					// nothing to do here
				}
			}
			
            try {
                bao.close();
            } catch (IOException e) {
                // nothing to do here
            }
		}

		return bao.toByteArray();
	}
	
	
	public String handle(String message) throws TxtblException {
		 
		if(message==null ||"".equals(message)) throw new TxtblException(null," No param");
		
		
		byte[] bytes = handle(Base64.decode(message) );
		if(bytes==null || bytes.length==0)
		    throw new TxtblException(null," Original message maybe  isnot compressed.");
		//return Base64.encode(bytes);
		
		String messages = null ; 
		try {
			 messages = new String (bytes,"iso8859-1") ;
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		
		return messages;
		
	}
	
}
