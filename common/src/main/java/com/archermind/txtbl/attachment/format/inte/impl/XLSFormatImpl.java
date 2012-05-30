package com.archermind.txtbl.attachment.format.inte.impl;

import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class XLSFormatImpl extends AbstractExcelFormater {
    public XLSFormatImpl(TextFilter textFilter) {
        super(textFilter);
    }

    protected FormulaEvaluator getFormulaEvaluator(Workbook workBook) {
		return new HSSFFormulaEvaluator((HSSFWorkbook) workBook);
	}

	protected Workbook getWorkBook(String filename, byte[] dataBytes) throws Exception {
		InputStream bis = new ByteArrayInputStream(dataBytes);
		POIFSFileSystem fs = new POIFSFileSystem(bis);
        return new HSSFWorkbook(fs);
	}

	protected String getExtension() {
		return ".xls";
	}

}