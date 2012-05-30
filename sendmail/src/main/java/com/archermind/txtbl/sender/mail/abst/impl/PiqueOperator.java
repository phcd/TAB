package com.archermind.txtbl.sender.mail.abst.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.impl.EmailRecievedService;
import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Email;
import com.archermind.txtbl.domain.EmailPojo;
import com.archermind.txtbl.sender.mail.abst.Operator;
import org.jboss.logging.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PiqueOperator extends Operator {

    private static final Logger log = Logger.getLogger(PiqueOperator.class);

    /**
     * @param list
     */
    public String sendMail(List<EmailPojo> list) {

        //get account of destination
        //save email to txtbl_email_received list of destination account
        String response=null;

        for(EmailPojo emailPojo:list){

            Email email = emailPojo.getEmail();
            log.debug(String.format("the email to be sent is %s ", email));

            String emailAddress = email.getTo();
            if(email.getTo().trim().endsWith(",")){
                   emailAddress = email.getTo().substring(0,email.getTo().indexOf(","));
            } else if(email.getTo().trim().endsWith(";")){
                    emailAddress = email.getTo().substring(0,email.getTo().indexOf(";"));
            }




            Account account = new UserService().getAccount(emailAddress);
            email.setOriginal_account(account.getName());
            email.setUserId(account.getUser_id());
            email.setMessage_type("PIQ");
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String sDate = fmt.format(new Date());
            email.setMaildate(sDate);
            email.setReply(emailPojo.getAccount().getName());



            log.debug(String.format("PiqueOperator looked up email %s and got account %s",emailAddress, account));
            try{
                new EmailRecievedService().newSaveEmail(account,email,emailPojo.getBody());
                log.info("[" + account.getName() + "] [sending piq success] [" + emailPojo.getEmail().getMailid() + "]");
                updateMailFlag(emailPojo.getEmail().getMailid());

                //send pushmail notifier so new user picks up new message
                PushMailNotifier.sendPushMailNotification(account,1,"[Pique]" + account.toString() + emailPojo.getAccount().getName());

                response = response == null ? String.valueOf(email.getMailid()) : response + "," + email.getMailid();
            }catch (DALException e){
                saveErrorMsg(e, account);
                log.fatal(String.format("unable to save sent piq message for %s %d ",account.getName(), email.getId()));
                for (EmailPojo emailPojo2 : list) {
                    Email email2 = emailPojo2.getEmail();
                    response = response == null ? String.valueOf(email2.getMailid()) : response + "," + email2.getMailid();
                }

            }
        }


        return response;
    }
}
