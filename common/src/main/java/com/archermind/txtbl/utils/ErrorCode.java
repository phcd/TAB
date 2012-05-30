package com.archermind.txtbl.utils;


public interface ErrorCode {
	/**
	 * 
	100 = Send email 
	101 = Receive email 
	102 = Synch Contacts 
	103 = Make Payment from device 
	104 = Transfer Account to a new Device 
	105 = Activate email account 
	106 = Deactivate email account 
	107 = Activate QWERT account (i.e. receive payment) 
	108 = Deactivate QWERT account (non-payment) 

	 */
//   public final String CODE_SendEmail_="10000" ;

   public final String CODE_SendEmail_Xml="10001" ;
   
//   public final String CODE_SendEmail_FAIL="10002" ;
   
//   public final String CODE_ReceiveEmail_="10100" ;
   
   public final String CODE_ReceiveEmail_NoNew="10101" ;
   public final String CODE_ReceiveEmail_NoBody="10102" ;
   public final String CODE_ReceiveEmail_NoAtt="10103" ;
   public final String CODE_ReceiveEmail_BacklogError="10104";
   
   //Add on 2/20/2009 by Pengfei Justin
   public final String CODE_ReceiveEmail_Att_oFF="10150" ;



//   public final String CODE_SynchContacts_="10200" ;
   public final String CODE_SynchContacts_Handle="10201" ;
   public final String CODE_SynchContacts_Add="10202" ;
//   public final String CODE_SynchContacts_Modify="10203" ;
//   public final String CODE_SynchContacts_Delete="10204" ;
   public final String CODE_SynchContacts_Restore="10205" ;
   
   
//   public final String CODE_TransferAccount_="10400" ;  // register
   
//   public final String CODE_TransferAccount_NoUUID="10401" ;  // register
   
   
//   public final String CODE_EmailAccount_="10500" ;
//   public final String CODE_EmailAccount_Add="10501" ;
   public final String CODE_EmailAccount_Modify="10502" ;
   public final String CODE_EmailAccount_Delete="10503" ;
   public final String CODE_EmailAccount_Provider_Not_Support="10504" ;
   public final String CODE_EmailAccount_Validate="50001" ;
//   public final String CODE_EmailAccount_Notify="10505" ;
   
   
   public final String CODE_EmailAccount_Xml="10510" ;
   public final String CODE_EmailAccount_NoTgt="10511" ;
   public final String CODE_EmailAccount_NoCmd="10512" ;
   public final String CODE_EmailAccount_NoObj="10513" ;
   public final String CODE_EmailAccount_NoAct="10514" ;
//   public final String CODE_EmailAccount_NoSvr="10515" ;
//   public final String CODE_EmailAccount_NoSvrNoHost="10516" ;
   public final String CODE_EmailAccount_SaveUsr="10517" ;
//   public final String CODE_EmailAccount_UpdateUsr = "10518";
   
   public final String CODE_EmailAccount_Existed = "10519";
   public final String CODE_EmailAccounts_NotActive = "10520";
   public final String CODE_EmailAccount_Protocol_NotSupported = "10521";

//   public final String CODE_QWERTAaccount_="10700" ;

   public final String CODE_MakePayment_="10800" ;
   
//   public final String CODE_SYSTEM_="20000" ;
   
//   public final String CODE_BusinessProcess_="20100" ;
   
//   public final String CODE_BusinessProcess_Version="20101" ;
   
//   public final String CODE_BusinessProcess_Request="20102" ;
   
   
//   public final String CODE_BusinessProcess_UserValidate="20112" ;
   
//   public final String CODE_BusinessProcess_Innererr="20113" ;
   
   public final String CODE_BusinessProcess_Dispatch="20114" ;
   
//   public final String CODE_BusinessProcess_Handle="20115" ;
   
   public final String CODE_Parsing_="20200" ;
   
   public final String CODE_DAL_="30000" ;

}
