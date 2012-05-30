package com.archermind.txtbl.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPOutputStream;

import com.archermind.txtbl.exception.TxtblException;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class Compress {

    public String handle(String message)throws TxtblException {
 
		if(message==null) throw new TxtblException(null," No param");
		
		try {
			byte[] messages = message.getBytes("utf-8");
			message = Base64.encode(handle(messages));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return message;
		
    }
    
    public  byte[] handle(byte[] message)
    {
    	ByteArrayOutputStream   baos   =   new   ByteArrayOutputStream();  
        GZIPOutputStream zos = null;
		try {
			zos = new   GZIPOutputStream(baos);
			zos.write(message,   0,   message.length);  
		} catch (IOException ignored) {
		} finally {
			try {
				if (zos!= null) {
					zos.close();
				}
			} catch (IOException ignored) {
			}
		}

        return baos.toByteArray();
    }
}