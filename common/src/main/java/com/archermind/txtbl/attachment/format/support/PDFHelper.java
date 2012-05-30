package com.archermind.txtbl.attachment.format.support;


import com.archermind.txtbl.utils.FinalizationUtils;
import com.archermind.txtbl.utils.SysConfigManager;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

public class PDFHelper {
    private static final Logger log = Logger.getLogger(PDFHelper.class);

    private static DecimalFormat dcFormat = new DecimalFormat("000");

    public static int getPageCount(byte[] bytes) {
        PDDocument document = null;

        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(bytes);

            PDFParser parser = new PDFParser(inputStream);

            String OS = System.getProperty("os.name");

            if (OS.indexOf("Windows") != -1) {
                parser.setTempDirectory(new File("c:/"));
            } else {
                parser.setTempDirectory(new File("/tmp"));
            }

            parser.parse();

            document = parser.getPDDocument();

            return document.getDocumentCatalog().getAllPages().size();
        } catch (IOException t) {
            log.warn("Could not create images from this pdf file due to inability to establish page count, but the text will still be processed.", t);
        } finally {
            FinalizationUtils.close(document);
            FinalizationUtils.close(inputStream);
        }

        return 0;
    }

    public static int getMaxPdfPagesCount() {
        return Integer.valueOf(SysConfigManager.instance().getValue("maxPdfPagesCount", "20"));
    }

    public static String getImageName(String filename, int attachIndex, int pageNumber) {
        String tempFile = filename;

        //Dan - arbitrarily chose 10 as length
        if(filename.length() > 10) {
           tempFile=filename.substring(0, 9);
        }
        return "pdf"+ tempFile + attachIndex + "_" + dcFormat.format(pageNumber) + ".jpeg";
    }
}
