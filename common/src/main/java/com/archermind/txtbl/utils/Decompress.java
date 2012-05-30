package com.archermind.txtbl.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.zip.ZipInputStream;

import com.archermind.txtbl.exception.TxtblException;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class Decompress {

	public String handle(String message) throws TxtblException {
	    if(StringUtils.isEmpty(message))
            throw new TxtblException(null," No param");

		byte[] bytes = handle(Base64.decode(message) );
		if(bytes==null || bytes.length==0)
		    throw new TxtblException(null," Original message maybe  isnot compressed.");

		String messages = null ; 
		try {
			 messages = new String (bytes,"utf-8") ;
		} catch (UnsupportedEncodingException ignored) {
		}

		return messages;
		
	}

	public  byte[] handle(byte[] message) throws TxtblException {
		byte[] decompressData = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		ByteArrayInputStream bis = new ByteArrayInputStream(message);
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(bis));
		// create the output stream
		final int BUFFER = 1024;
		try {
			while (zis.getNextEntry() != null) {
				byte[] bytes = new byte[BUFFER];
				int count;
				while ((count = zis.read(bytes)) != -1) {
					bos.write(bytes,0,count);
				}

				bos.flush();
				decompressData = bos.toByteArray();
			}
		} catch (IOException e) {
			throw  new TxtblException(null,e);
		}
		finally {
            try {
                bos.close();
            } catch (IOException e) {
                // nothing to do here
            }

            try {
                zis.close();
            } catch (IOException e) {
                // nothing to do here
            }
		}

		return decompressData;

	}
	
	
	String dump(byte[] data) {
		StringBuffer strings = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			strings.append(String.format("%2X",data[i])).append(" ");
			if( (i+1) %30 ==0 )
                strings.append( "\n");
		}
		return strings.toString();
	}

    @SuppressWarnings("unchecked")
	byte[] atttachDivideSeqno(byte []data, String sseqno,int  max_data_length,HashMap cmdObjectMeta) {
		byte[] returnData;
		if (sseqno == null)
			return data;
		int seqno = Integer.parseInt(sseqno);

		if (data.length > (seqno + 1) * max_data_length) {
			returnData = new byte[max_data_length];
			System.arraycopy(data, seqno * max_data_length, returnData, 0, max_data_length);
			seqno++;
		} else if (data.length > (seqno) * max_data_length && data.length < (seqno + 1) * max_data_length) {
			returnData = new byte[ data.length -seqno * max_data_length];
			System.arraycopy(data, seqno * max_data_length, returnData, 0, data.length -seqno * max_data_length);
			seqno=-1;
		} else {
			returnData =data ;
			seqno = -1;
		}
		cmdObjectMeta.put("SEQNO", String.valueOf(seqno));
		return returnData;
	}
	
	public static void main(String[] args) {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><metric><qwertAccountActivations>0</qwertAccountActivations><emailAccountActivations>0</emailAccountActivations>" + "<qwertAccountDeactivations>0</qwertAccountDeactivations>" + "<emailAccountDeactivations>0</emailAccountDeactivations><dataSizeSent>0</dataSizeSent><dataSizeReceived>0</dataSizeReceived>" + "<paymentsMade>0</paymentsMade><contactsImported>0</contactsImported></metric>";

        Compress compress = new Compress();
		
		try {
			String compressxml = compress.handle(xml);
			System.out.println(compressxml);
		} catch (TxtblException ignored) {}
		
		Decompress decompress = new Decompress();
		System.out.println(decompress.dump(xml.getBytes()));
		HashMap cmdObjectMeta = new 		HashMap();
		
		String origin="<emailAccountDeactivations>0</emailAccountDeactivations><dataSizeSent>0</dataSizeSent><dataSizeReceived>0</dataSizeReceived>";
		System.out.println(decompress.dump(origin.getBytes()));
		String sseqno = "0";
		while (!"-1".equals(sseqno)) {
	     byte []divide=	decompress.atttachDivideSeqno(origin.getBytes(),  sseqno,  30, cmdObjectMeta);
	     sseqno= (String) cmdObjectMeta.get("SEQNO") ;
	     System.out.println(decompress.dump(divide));
		}
		
		
		
	}
}