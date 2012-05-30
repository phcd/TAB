package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.EmailPojo;
import com.archermind.txtbl.domain.OriginalReceivedEmail;
import com.archermind.txtbl.mail.store.MessageStoreFactory;
import com.archermind.txtbl.utils.IdUtil;
import com.archermind.txtbl.utils.StopWatch;
import com.archermind.txtbl.utils.StopWatchUtils;
import com.archermind.txtbl.utils.UtilsTools;
import com.archermind.txtbl.utils.xobni.XobniUserHandler;
import org.jboss.logging.Logger;

import javax.mail.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class XobniSaveProcess extends EmailSaveProcess {
    protected static final Logger log = Logger.getLogger(XobniSaveProcess.class);

    HashMap<String, List<EmailPojo>> emailPojosMap = new HashMap<String, List<EmailPojo>>();
    List<String> messageIds = new ArrayList<String>();

    private XobniUserHandler xobniUserHandler = null;
    private String messageStoreBucket;
    private int xobniBatchSize = 1000;       //TODO - hack by Dan for Xobni ActiveSync - only used in XobniActiveSync

    public XobniSaveProcess(Account account, String messageStoreBucket, StopWatch watch, String context, boolean saveAttachments) {
        super(account, watch, context);
        this.saveAttachments = saveAttachments;
        this.saveBodies = false;
        this.xobniUserHandler = new XobniUserHandler(account);
        this.messageStoreBucket = messageStoreBucket;
    }

    protected void saveMessage(Message message, String messageId, OriginalReceivedEmail original, EmailPojo pojo) throws DALException {
        String folderName = message.getFolder().getFullName();
        List<EmailPojo> emailPojos = emailPojosMap.get(folderName);
        if(emailPojos == null) {
            emailPojos = new ArrayList<EmailPojo>();
            emailPojosMap.put(folderName, emailPojos);
        }
        messageIds.add(messageId);
        emailPojos.add(pojo);
    }

    public boolean complete(Boolean finalBatch, Integer emailsInInbox, Integer totalEmailsUploadedSoFar) throws Exception {
        try {
            StopWatchUtils.newTask(watch, "push & update", context, log);
            for (String folderName : emailPojosMap.keySet()) {
                List<EmailPojo> emailPojos = emailPojosMap.get(folderName);
                if(!UtilsTools.isEmpty(emailPojos)) {
                    boolean success = xobniUserHandler.syncXobniUser(folderName, emailPojos, finalBatch, emailsInInbox, totalEmailsUploadedSoFar);

                    if(!success) {
                        log.error(String.format("Failure to sync messages %s", context));
                        return false;
                    }
                }
            }

            if(!UtilsTools.isEmpty(messageIds)) {
                StopWatchUtils.newTask(watch, "save message ids in bulk", context, log);
                MessageStoreFactory.getStore().addMessageInBulk(account.getId(), messageStoreBucket, IdUtil.encodeMessageIds(messageIds), account.getCountry());
                log.info(String.format("Finished updating bulk IDs %s",context));
            }
            return true;
        } finally {
            reset();
        }
    }

    //TODO - hack by Dan for XobniActiveSync - only used in XobniActiveSync
    public boolean shouldComplete(){

        return (emailPojosMap.size() >= xobniBatchSize);

    }

    @Override
    public boolean complete() throws Exception {
        return complete(null, null, null);
    }

    private void reset() {
        emailPojosMap.clear();
        messageIds.clear();
    }

    @Override
    public boolean shouldProcess() {
        return xobniUserHandler.shouldProcess();
    }

    @Override
    public boolean shouldStopProcess() {
        return xobniUserHandler.accountReset();
    }
}
