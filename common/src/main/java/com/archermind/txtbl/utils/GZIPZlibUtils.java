package com.archermind.txtbl.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.io.IOUtils;

import org.jboss.logging.Logger;

public class GZIPZlibUtils {
    private static final Logger logger = Logger.getLogger(SendQueueMessageClient.class);

	public static byte[] dicts = loadDict("/dict/dict.txt");

    private static byte[] loadDict(String name) {
        String fileName = System.getenv("TXTBL_HOME") + name;

        InputStream inputStream = null;

        try {
            logger.info(String.format("loading dictionary file from %s", fileName));

            File dictFile = new File(fileName);

            if (! dictFile.exists()) {
                throw new RuntimeException(String.format("dictionary file %s does not exist", fileName));
            }

            inputStream = new FileInputStream(dictFile);

            return IOUtils.toByteArray(inputStream);

        } catch (Throwable e) {
            throw new RuntimeException(String.format("dictionary file %s does not exist", fileName));
        } finally {
            FinalizationUtils.close(inputStream);
        }
    }


	public static byte[] compress(String originalText) {
		Deflater compressor = new Deflater();
		if (dicts != null)
			compressor.setDictionary(dicts);

		compressor.setInput(originalText.getBytes());
		compressor.finish();
		byte[] output = new byte[100];
		ByteArrayOutputStream bo = new ByteArrayOutputStream();

		while (true) {
			int compressedDataLength = compressor.deflate(output, 0, output.length);
			if (compressedDataLength == 0)
				break;
			bo.write(output, 0, compressedDataLength);
			if (compressedDataLength != output.length)
				break;

		}
		compressor.end();
		return bo.toByteArray();
	}

	public static byte[] decompress(byte[] originalByte) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        Inflater decompressor = new Inflater();
        decompressor.setInput(originalByte);
        try {
            byte[] output = new byte[100];
            int compressedDataLength = decompressor.inflate(output);

			if (compressedDataLength == 0 && decompressor.needsDictionary()) {
				logger.info("needsDictionary : ");
				if(dicts!=null)
				decompressor.setDictionary(dicts);

				while (true) {
					compressedDataLength = decompressor.inflate(output);
					if (compressedDataLength == 0)
						break;
					bo.write(output, 0, compressedDataLength);
					if (compressedDataLength != output.length)
						break;
				}
			} else {
				bo.write(output, 0, compressedDataLength);
				while (true) {

					compressedDataLength = decompressor.inflate(output);
					if (compressedDataLength == 0)
						break;
					bo.write(output, 0, compressedDataLength);
					if (compressedDataLength != output.length)
						break;
				}

			}

		} catch (Exception e) {
			logger.error(e);
			decompressor.end();
			throw e;
		}
		decompressor.end();
		return bo.toByteArray();

	}

    public static void main(String[] args) {

        byte[] input = "pageinput=<?xml version=\"1.0\" encoding=\"UTF-8\"?><QwertML><Header><Version>QwertML1.0.1</Version><Cred><UUID>49</UUID><DvcID>123456780020372</DvcID><SimCode>460001674532756</SimCode></Cred><Device><ClientSW>Ex:01.08.08</ClientSW><Location><LACID>20980</LACID><CellID>18501</CellID></Location></Device></Header><Body><Meta><Target>Mails</Target></Meta><Mails><Maillist><Meta><SeqNo>0</SeqNo></Meta></Maillist></Mails></Body></QwertML>".getBytes();

        String originalStr = new String(input);
        System.out.println("original string before zip : " + originalStr);
        byte[] aaa = GZIPZlibUtils.compress(new String(input));
        System.out.println("After zip : " + originalStr);
        System.out.println("compressedDataLength : " + aaa.length);
        System.out.println(StringUtils.dumpBytes(aaa));
        byte[] aaa11 = null;
        try {
            aaa11 = GZIPZlibUtils.decompress(aaa);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("after unzip,original string : " + new String(aaa11));

    }
}
