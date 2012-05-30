package com.archermind.txtbl.utils;

import java.io.UnsupportedEncodingException;

import com.archermind.txtbl.exception.TxtblException;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class GzipDictDecompress extends GzipDecompress {

	public byte[] handle(byte[] message) throws TxtblException {

	    try {
			return GZIPZlibUtils.decompress(message);
		} catch (Exception e) {
			throw new TxtblException("GzipDictDecompress",e);
		}
	}
	
	
	public String handle(String message) throws TxtblException {
		if(message==null ||"".equals(message)) throw new TxtblException(null," No param");

		byte[] bytes = handle(Base64.decode(message) );
		if(bytes==null || bytes.length==0)
		    throw new TxtblException(null," Original message maybe  isnot compressed.");

		try {
			 return new String (bytes,"iso8859-1") ;
		} catch (UnsupportedEncodingException e1) {
			throw new TxtblException("GzipDictDecompress",e1);
		} 
	}
	
}
