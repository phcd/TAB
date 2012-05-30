package com.archermind.txtbl.attachment.format.inte.impl;

import com.archermind.txtbl.attachment.format.inte.AttachmentFormat;
import com.archermind.txtbl.attachment.format.inte.ITextFilter;
import com.archermind.txtbl.attachment.format.support.ExcelUtil;
import com.archermind.txtbl.domain.Attachment;

import com.archermind.txtbl.utils.SysConfigManager;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jboss.logging.Logger;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractExcelFormater implements AttachmentFormat {
    private static final Logger log = Logger.getLogger(AbstractExcelFormater.class);

	private ITextFilter textFilter = null;
	public final static String CHARSET = "ISO-8859-1";
	public final static String NULL_ROW = "                              ";
	public final static String DEFAULT_MAX_COLS = "100";
	public final static String DEFAULT_MAX_ROWS = "100";

    public AbstractExcelFormater(TextFilter textFilter) {
        this.textFilter = textFilter;
    }

    protected abstract FormulaEvaluator getFormulaEvaluator(Workbook workBook);

	protected abstract Workbook getWorkBook(String filename, byte[] dataBytes) throws Exception;

	public List<Attachment> getALLExcelText(String filename, byte[] dataBytes) {
		List<Attachment> list = new ArrayList<Attachment>();
		try {
			Workbook wb = getWorkBook(filename, dataBytes);
			int noOfSheets = wb.getNumberOfSheets();
            if(noOfSheets >= 1) {
				try {
					Attachment att = new Attachment();
					StringBuffer sb = new StringBuffer();
					Sheet sheet = wb.getSheetAt(0);
					att.setName(filename + "_" + wb.getSheetName(0) + getExtension());
					att.setSize(dataBytes.length);

					int noOfRows = getNoOfRows(sheet);
					int maxNoOfColumns = 0;
					if (noOfRows == 0) {
						att.setData("".getBytes(CHARSET));
						list.add(att);
                    } else {
                        int actualNoOfRows = 0;
                        for (int r = 0; r < noOfRows + 1; r++) {
                            try {
                                Row row = sheet.getRow(r);
                                if(row == null) {
                                    continue;
                                }
                                int noOfColumns = getNoOfColumns(row);

                                
                                if (noOfColumns > maxNoOfColumns) {
                                    maxNoOfColumns = noOfColumns;
                                }
                                for (int i = 0; i < noOfColumns; i++) {
                                    Cell cell = row.getCell(i);
                                    CellValue value = null;
                                    try {
                                        if(cell != null && (Cell.CELL_TYPE_BLANK != cell.getCellType())) {
                                            value = getFormulaEvaluator(wb).evaluate(cell);
                                        }
                                    } catch (IllegalStateException e){
                                        log.error("getCellText ....", e);
                                    }
                                    String cellStrValue = cellValue2String(cell, value);
                                    cellStrValue = new ExcelUtil().subString(cellStrValue);
                                    String hang = cellStrValue + "%#%";
                                    sb.append(hang);
                                }
                                actualNoOfRows++;
                                sb.append("\n"); //
                            } catch (Exception e) {
                                log.error("getALLExcelText............", e);
                            }
                        }

                        String text = sb.toString();
                        text = textFilter.filter(text);
                        att.setData(text.getBytes(CHARSET));

                        att.setComment(maxNoOfColumns + ";" + actualNoOfRows);

                        if (maxNoOfColumns != 0) {
                            list.add(att);
                        }
                    }

				} catch (Exception e) {
					log.error("getALLExcelText............", e);
				}
			}
		} catch (Exception e) {
			log.error("getALLExcelText............", e);
		}

		return list;
	}

    private int getNoOfRows(Sheet sheet) {
        int maxNoOfRows = getMaxNoOfRows();
        int noOfRows = sheet.getPhysicalNumberOfRows() > sheet.getLastRowNum() ? sheet.getPhysicalNumberOfRows() : sheet.getLastRowNum();
        noOfRows = noOfRows <= maxNoOfRows ? noOfRows : maxNoOfRows;
        return noOfRows;
    }

    private int getNoOfColumns(Row row) {
        int maxNoOfCols = getMaxNoOfCols();
        int noOfColumns = row.getLastCellNum() > row.getPhysicalNumberOfCells() ? row.getLastCellNum() : row.getPhysicalNumberOfCells();
        noOfColumns = noOfColumns <= maxNoOfCols ? noOfColumns : maxNoOfCols;
        return noOfColumns;
    }

    private int getMaxNoOfRows() {
        return Integer.parseInt(SysConfigManager.instance().getValue("excel.max.rows", DEFAULT_MAX_ROWS));
    }

    private int getMaxNoOfCols() {
        return Integer.parseInt(SysConfigManager.instance().getValue("excel.max.columns", DEFAULT_MAX_COLS));
    }

    protected abstract String getExtension();

	private String cellValue2String(Cell cell, CellValue cValue) {
		if (cValue == null) {
			return NULL_ROW;
		}
		int cellType = cValue.getCellType();
		String cellStr;
		switch (cellType) {
		case Cell.CELL_TYPE_BLANK: {
			cellStr = "";
			break;
		}
		case Cell.CELL_TYPE_BOOLEAN: {
			cellStr = Boolean.toString(cValue.getBooleanValue());
			break;
		}
		case Cell.CELL_TYPE_ERROR: {
			cellStr = "";
			break;
		}
		case Cell.CELL_TYPE_NUMERIC: {
			cellStr = numericCell(cell);
			break;
		}
		case Cell.CELL_TYPE_STRING: {
			cellStr = cValue.getStringValue();
			break;
		}
		default: {
			cellStr = "";
		}
		}
		return cellStr.replace("%#%", " ").replace("\n", " ");
	}

	private String numericCell(Cell cell) {
		String cellStr;
		if (DateUtil.isCellDateFormatted(cell)) {// date type cell
			cellStr = new SimpleDateFormat("MM-dd-yyyy").format(cell.getDateCellValue());
		} else {// numeric type cell
			double d = cell.getNumericCellValue();
			cellStr = new DecimalFormat("##############.####").format(d);
		}
		return cellStr;
	}

	public List<Attachment> format(String mailbox, String msgID, String filename, byte[] dataByte, int targetHeight, int targetWidth) {
		log.info("XLSFormatImpl  format ................" + "  filename: " + filename + "  mailbox: " + mailbox + "  msgID: " + msgID);

		List<Attachment> list = getALLExcelText(filename, dataByte);
		if (list.size() == 0) {
			Attachment att = new Attachment();
			att.setName(filename);
			att.setData("Contents of the file format error ! Please check your file. ".getBytes());
			list.add(att);
		}
		return list;
	}
}
