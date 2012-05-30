package com.archermind.txtbl.attachment.format.support;

public class GridExcelResponsesToWeb extends ExcelResponsesToWeb {
    private static final int PAGE_ROW_NUM = 9;
    private static final int PAGE_COL_NUM = 3;

    public String ResponsesToWeb(int heng, int shu, byte[] dataByte)
        throws Exception {
        String sb = new String(dataByte);
        StringBuffer stringBuffer = new StringBuffer();
        String[] str = sb.split("\n");
        int maxYlength = str.length;
        int[] a = new int[maxYlength];

        for (int i = 0; i < maxYlength; i++) {
            a[i] = str[i].split("%#%").length;
        }

        int maxXlength = findMax(a);

        StringBuffer letterstemp = new StringBuffer();

        for (int i = 0; i < 500; i++) {
            letterstemp.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        }

        int startY = shu - 1;
        int endY = startY + PAGE_ROW_NUM;
        
        if(endY > maxYlength) {
        	endY = maxYlength;
        }

        if(heng > maxXlength)
        	return "";
        
        int noOfColumns = maxXlength - (heng - 1) > PAGE_COL_NUM ? PAGE_COL_NUM : maxXlength - (heng - 1); 
        str = sb.split("\n");
        for (int i = startY; i < endY; i++) {
            try {
            	String row = str[i];
            	String[] cells = row.split("%#%");
            	for (int j = heng; j < (heng + noOfColumns); j++) {
            		if(cells.length >= j){
            			String cell = cells[j - 1].trim().replace(",", " ");
						stringBuffer.append(cell);
            		}
					stringBuffer.append(",");
				}
            	stringBuffer.deleteCharAt(stringBuffer.length() - 1);
            	stringBuffer.append("\n");
            } catch (Exception e) {
                return "Please return to the previous page, here have not  content.";
            }
        }

        return stringBuffer.toString();
    }

	public String getNoOfColumns(String totalColumns) {
		return totalColumns;
	}

	public String getNoOfRows(String totalRows) {
		return totalRows;
	}
    
    
}
