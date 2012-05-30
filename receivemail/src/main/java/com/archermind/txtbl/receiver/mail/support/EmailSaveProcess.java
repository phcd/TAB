package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.EmailPojo;
import com.archermind.txtbl.domain.OriginalReceivedEmail;
import com.archermind.txtbl.receiver.mail.dal.DALDominator;
import com.archermind.txtbl.sync.IMAPSyncUtil;
import com.archermind.txtbl.utils.StopWatch;
import org.jboss.logging.Logger;

import javax.mail.Message;

public class EmailSaveProcess extends EmailProcess {
    private static final Logger log = Logger.getLogger(EmailSaveProcess.class);

    protected Account account;
    protected StopWatch watch;
    protected String context;
    protected boolean saveBodies = true;
    protected boolean saveAttachments = true;

    public EmailSaveProcess(Account account, StopWatch watch, String context) {
        this.account = account;
        this.watch = watch;
        this.context = context;
    }

    public boolean process(Message message, int messageNumber, String messageId) throws Exception {
        String context = String.format("[%s] account=%s, uid=%s, email=%s", this.hashCode(), account.getId(), account.getUser_id(), account.getName());

        OriginalReceivedEmail original = OriginalReceivedEmailMapper.getInstance().getFullyLoaded(account, message, messageId, saveBodies, saveAttachments, context);

        EmailPojo pojo = EmailPojoMapper.getInstance().getEmailPojo(original, account.getName(), original.getEmailFromAlias());

        saveMessage(message, messageId, original, pojo);
        return true;
    }

    protected void saveMessage(Message message, String messageId, OriginalReceivedEmail original, EmailPojo pojo) throws Exception {
        //If imap flags exist in the message, put these in the pojo so it can be written to the database.
        if (message.getFlags() != null) {
            pojo.setImapStatus(IMAPSyncUtil.getStatusFromImapFlags(message.getFlags()));
        }


        int saveStatus = DALDominator.newSaveMail(pojo, original, account);
        if (log.isDebugEnabled())
            log.debug(String.format("[%s] messageId=%s, saving completed with status %s", account.getName(), messageId, saveStatus));
    }

}
