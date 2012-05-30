package com.archermind.txtbl.attachment.format.inte.impl;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;

public class XLSXFormatImpl extends AbstractExcelFormater {
    public XLSXFormatImpl(TextFilter textFilter) {
        super(textFilter);
    }

    protected FormulaEvaluator getFormulaEvaluator(Workbook wb) {
		return new XSSFFormulaEvaluator((XSSFWorkbook) wb);
	}

	protected Workbook getWorkBook(String filename, byte[] dataBytes) throws Exception {
		String OS = System.getProperty("os.name");
		String filename1 = (OS.indexOf("Windows") != -1) ? "c:/" + filename : "/tmp/" + filename;

		FileOutputStream fos = new FileOutputStream(filename1, true);
		fos.write(dataBytes);
		fos.flush();

        return new XSSFWorkbook(filename1);
	}

	protected String getExtension() {
		return ".xlsx";
	}
}