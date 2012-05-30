package com.archermind.txtbl.attachment.format.support;

import com.archermind.txtbl.attachment.format.inte.impl.AbstractExcelFormater;

public class ExcelResponsesToWeb {
    private static final int CHARACTERS = 60;
    private static final int PAGE_ROW_NUM = 10;

    public String ResponsesToWeb(int heng, int shu, byte[] dataByte)
        throws Exception {
        String sb = new String(dataByte);
        StringBuffer stringBuffer = new StringBuffer();
        String[] str = sb.split("\n");

        int maxXlength = 0;
        int maxYlength = str.length;

        for (int i = 0; i < str.length; i++) {
            String[] cells = str[i].split("%#%");
            StringBuffer row = new StringBuffer();
            for (String cell : cells) {
            	row.append(new ExcelUtil().subString(cell, ExcelUtil.MAX_NO_OF_CHARACTERS_PER_COLUMN_FOR_OLD_EXCEL));
			}
            str[i] = row.toString();
            if(cells.length > maxXlength){
            	maxXlength = cells.length;
            }
        }

		int ytotal=0;
		for (int h=0;h<maxYlength;h++){
			if ((h % PAGE_ROW_NUM) == 0) {
				 ytotal++;
			} 
		}

		int xtotal=0;
		for (int j=0;j<maxXlength;j++){
			if(j%2==0) { 
				xtotal++;
			}
        }

        final int startX = (heng * CHARACTERS) - CHARACTERS;
        final int endX = heng * CHARACTERS;

		System.out.println(maxYlength+"| ALL "+ytotal);
		System.out.println(maxXlength+"-- ALL"+xtotal );

		int startY;
		int endY;
		StringBuffer letterstemp = new StringBuffer();

		for (int i = 0; i < 500; i++) {
			letterstemp.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		}

        String letters = letterstemp.toString();
        int heng2 = heng * 2;
        int heng1 = heng2 - 2;
        letters = letters.substring(heng1, heng2);

	    String letters1=letters.substring(0,1);
	    String letters2 = letters.substring(1, 2);
	    stringBuffer.append("            ").append(letters1)
                    .append("                   ").append("            ")
                    .append(letters2).append("                           \n");

        if (maxYlength < PAGE_ROW_NUM) {
            startY = 0;
            endY = maxYlength;
        } else if ((heng == 1) && (shu == 1)) {
            startY = 0;
            endY = PAGE_ROW_NUM;
        } else if (shu == ytotal) {
            startY = PAGE_ROW_NUM * (shu-1);
            endY = maxYlength;
        } else {
            startY = (shu - 1) * PAGE_ROW_NUM;
            endY = startY + PAGE_ROW_NUM;
        }

        for (int i = startY; i < endY; i++) {
            try {
            	String row = str[i];
            	int localEndX = row.length();
                if(startX>=localEndX){
                	stringBuffer.append(i + 1).append(" ").append(AbstractExcelFormater.NULL_ROW).append("\n");
                }
                else{
                    stringBuffer.append(i + 1).append(" ").append(row.substring(startX, endX<localEndX?endX:localEndX)).append("\n");
                }
            } catch (Exception e) {
                return "Please return to the previous page, here have not  content.";
            }
        }

        return stringBuffer.toString();
    }

    public static int findMax(int[] a) {
        int i = a[0];

        for (int anA : a) {
            if (anA > i) {
                i = anA;
            }
        }

        return i;
    }

	public String getNoOfRows(String totalRows) {
		int ytotal = 0;

		for (int h = 0; h < parseOrZero(totalRows); h++) {
			if ((h % PAGE_ROW_NUM) == 0) {
				ytotal++;
			}
		}
		return Integer.toString(ytotal);
	}

	public String getNoOfColumns(String totalColumns) {
		int xtotal = 0;

		for (int j = 0; j < parseOrZero(totalColumns); j++) {
			if ((j % 2) == 0) {
				xtotal++;
			}
		}
		return Integer.toString(xtotal);
	}

	private int parseOrZero(String intString) {
		try {
			return Integer.parseInt(intString);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
