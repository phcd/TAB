package com.archermind.txtbl.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import com.archermind.txtbl.domain.Attachment;

public class ExcelResponsesToDateBase {

	 
	
	public static void main(String[] args) throws Exception {
		int size = 0;
		byte[] dataByte = null;
		InputStream is11 = new FileInputStream("c:/1.xls");
		size = is11.available();
		dataByte = new byte[size];
		is11.read(dataByte, 0, size);
		is11.close();

			List list =new ExcelResponsesToDateBase().ToDateBase(dataByte);
			Attachment att=(Attachment) list.get(1);
 			System.out.println( att.getName() );
 			System.out.println( new String (att.getData()) );
 			System.out.println(  att.getComment() );
	}
	
		
	private int charactersSub=30;
	
		public List  ToDateBase( byte dataByte[]) throws  Exception {
			List<Attachment> listatt = new ArrayList<Attachment> ();
			List list= getALLExcelText(dataByte );
			
			for (int i=0;i<list.size();i++){
//				Attachment att= new Attachment();
//				String sb=list.get(i).toString();
				Attachment att= (Attachment) list.get(i);
				String sb=new String (att.getData());
				int maxYlength= (sb.split("\n").length)-1;
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
					att.setData(   sb.getBytes("ISO-8859-1")  );
					att.setComment(xtotal+","+ytotal);
					listatt.add( att );
//			System.out.println(xtotal );
//			System.out.println(ytotal );
			
			}
			return listatt;
			
		}
	
	
		private List getALLExcelText( byte[] file  ) throws Exception{
			List<Attachment> list = new ArrayList<Attachment> ();
			InputStream bis = new ByteArrayInputStream(file);
			POIFSFileSystem fs = new POIFSFileSystem(bis);
			HSSFWorkbook wb  = new HSSFWorkbook(fs);   
			int tableCount=wb.getNumberOfSheets();
			
//			System.out.println( tableCount);
			for (int h = 0; h < tableCount; h++) {
				Attachment att= new Attachment();
				StringBuffer sb= new StringBuffer();
				HSSFSheet sheet= wb.getSheetAt(h);
				att.setName(  wb.getSheetName(h)  );
//				System.out.println( );
				
				
			for (int r = 0; r < sheet.getLastRowNum()+1; r++) {
//				maxYlength=sheet.getLastRowNum();
				HSSFRow row = sheet.getRow(r   ) ;
//			System.out.print( (r+1)   );
			//sb.append((r+1+"  ") );
			for (int i = 0; i < row.getLastCellNum(); i++) {
//				maxXlength=row.getLastCellNum();
//				LastCellNum=row.getLastCellNum();
				Object rowc=row.getCell(i);
				if (rowc!=null){
					rowc=SubString(rowc.toString());
				}
				else{
					rowc="                              ";
					}
//				String hang= rowc.toString();
 				String hang= rowc+"%#%" ;
// 				System.out.print( " "+ rowc+" | " ); //  
 				sb.append(hang );
				}
			
				sb.append( "\n");        // 
//				System.out.print("\n");
			
		}
//			sb.append(XUnitCounts).append(LastCellNum);
			att.setData(  sb.toString().getBytes("ISO-8859-1")   );
			list.add(att);
			}
			return list;
	}
	
		private String SubString (String para){
			if (para.length()>charactersSub){	
				para=para.substring(0, charactersSub);	
				return para;
				}	
			else{
				return para+fillblank( charactersSub-para.length());
				}
		}

		
		private String fillblank(int blank ){
			StringBuffer sb= new  StringBuffer();
			for (int i=0;i<blank;i++){
				sb.append(" ");
			}
			return sb.toString();
		}
	
 
	
}
