package com.archermind.txtbl.utils;

import java.io.*;
import java.util.List;
//
//import org.apache.poi.hssf.usermodel.HSSFRow;
//import org.apache.poi.hssf.usermodel.HSSFSheet;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.archermind.txtbl.domain.Attachment;


public class ExcelResponsesToWeb {

	 
	
	public static void main(String[] args) throws Exception {
		int size = 0;
		byte[] dataByte = null;
		InputStream is11 = new FileInputStream("c:/1.xls");
		size = is11.available();
		dataByte = new byte[size];
		is11.read(dataByte, 0, size);
		is11.close();

		List list =new ExcelResponsesToDateBase().ToDateBase(dataByte);
		Attachment att=(Attachment) list.get(0);
		
 		String listtext =new ExcelResponsesToWeb().ResponsesToWeb(2,2,att.getData());
		System.out.println(listtext);
		//System.out.println(new String (att.getData()));
	}
	
		
	private int characters=60;
	
//		public String ResponsesToWeb(int heng, int shu,byte dataByte[] , int  SheetNum) throws  Exception {
		
	public String ResponsesToWeb(int heng, int shu,byte dataByte[]) throws  Exception {
			String sb=new String (dataByte);
			StringBuffer stringBuffer= new StringBuffer(); 
//			String sb=new String (att.getData());
//			String XandY=att.getComment();
//			int  heng= new Integer (XandY.split(",")[0]);
//			int  shu= new Integer (XandY.split(",")[1]);
			
//			String sb= getALLExcelText(dataByte,SheetNum);
			
			int maxYlength= (sb.split("\n").length);
			int maxXlength=sb.split("\n")[0].split("%#%").length;
			
 
			int ytotal=0;
			for (int h=0;h<maxYlength;h++){
				if(h%10==0) { 
					 ytotal++;
				} 
			}

			int xtotal=0;
			for (int j=0;j<maxXlength;j++){
				if(j%2==0) { 
					xtotal++;
				} 
			}
			
			int startX=heng*characters-characters; int endX=heng*characters;
			
			System.out.println(maxYlength+"| ALL "+ytotal);
			System.out.println("-- ALL"+xtotal );
			
//			System.out.println(startX);
//			System.out.println(endX );
		
//			System.out.println("ALL wild"+maxXlength);
//			System.out.println("ALL SHU"+maxYlength ) ;
			
			int startY=0;
		    int endY=0;
			 
			
			if (shu==1){	 startY=0;   endY=10;	}
			
			else if (shu==ytotal) {  startY=maxYlength-10;   endY=maxYlength;	 }
			else{
		    startY=(shu-1)*10;     endY=startY+10;
			}
 	
			sb=sb.replaceAll("%#%", "");
			
			for (int i=startY;i<endY;i++){
				try{
					if (sb.split("\n")[i].length() <endX){
					endX=sb.split("\n")[i].length();
					}
					stringBuffer.append(sb.split("\n")[i].substring(startX, endX)).append("\n");
				}
				catch (Exception e) {
					return "Please return to the previous page, here have not  content." ;
				}
				

			}
			
		//	System.out.println(stringBuffer);
			//System.out.println(sb.replaceAll("%#%", " ").replaceAll(XUnitCountsTmp1, "")   ) ;
			return stringBuffer.toString();
			
		}
	
 	
	
}
