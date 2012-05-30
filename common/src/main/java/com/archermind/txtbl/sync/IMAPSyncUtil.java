package com.archermind.txtbl.sync;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.impl.EmailRecievedService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Email;
import com.archermind.txtbl.utils.StopWatch;
import com.archermind.txtbl.utils.UtilsTools;
import org.jboss.logging.Logger;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 11-08-11
 * Time: 12:05 PM
 */
public class IMAPSyncUtil {

    private static final Logger log = Logger.getLogger(IMAPSyncUtil.class);


    //TODO: #ME Move this variable into txtbl_txtconfig eventually
    private static int numFlags = 5;

    /*
    * Syncs read/unread, delete & other message flags back to the inbox
    */

    public boolean syncEmails(Account account, Folder folder, String context) throws DALException {
        return syncEmails(account, folder, context, null);
    }

    public boolean syncEmails(Account account, Folder folder, String context, StopWatch watch) throws DALException {

        boolean syncFlag = false;

        if (watch != null) watch.newTask("getting saved flags");

        EmailRecievedService one = new EmailRecievedService();
        List<Email> emailList = one.getIMAPDirtyEmail(account.getUser_id());

        if (watch != null) watch.newTask("checking flags");

        ArrayList<Integer> imapSeenMessIdArray = new ArrayList<Integer>();
        Flags fSeen = new Flags(Flags.Flag.SEEN);

        ArrayList<Integer> imapSeenDeletedMessIdArray = new ArrayList<Integer>();
        Flags fSeenDel = new Flags(Flags.Flag.SEEN);
        fSeenDel.add(Flags.Flag.DELETED);

        ArrayList<Integer> imapDeletedMessIdArray = new ArrayList<Integer>();
        Flags fDel = new Flags(Flags.Flag.DELETED);

        int testStatus;

        for (Email email : emailList) {
            testStatus = getFlippedTrueBitSet(email.getImap_status());
            //The message has only been flagged seen = true;
            if (testStatus == 0x01) {
                imapSeenMessIdArray.add(Integer.parseInt(email.getMessageId()));
                email.setImap_status(email.getImap_status() & 0x1f);  //Uses a bit mask to clear dirty flag bits.
            } else if (testStatus == 0x09) {
                //The message has been flagged seen = true, deleted = true
                imapSeenDeletedMessIdArray.add(Integer.parseInt(email.getMessageId()));
                email.setImap_status(email.getImap_status() & 0x1f);
            } else if (testStatus == 0x08) {
                //The message has been flagged deleted = true
                imapDeletedMessIdArray.add(Integer.parseInt(email.getMessageId()));
                email.setImap_status(email.getImap_status() & 0x1f);
            } else {
                //If it does not fit into one of the common used cases above, the message is processed by itself
                setFlagOnMessage(folder, Integer.parseInt(email.getMessageId()), email.getImap_status());
                email.setImap_status(email.getImap_status() & 0x1f);
                //NOTE: should i make sure the setImap_status was successful before clearing the dirty flag bit.
                one.clearImapDirtyStatusFlags(account.getUser_id(), email, email.getImap_status());  //Make sure that email.getImap_status is what is needed here.
            }
        }

        try {
            if (watch != null) watch.newTask("updating flags");
            //Set flags on the 3 batch group
            if (imapSeenMessIdArray.size() > 0) {
                folder.setFlags(UtilsTools.convertIntegersArrayToIntArray(imapSeenMessIdArray), fSeen, true);
                one.clearImapDirtyStatusFlagsBulk(account.getUser_id(), UtilsTools.convertIntegersArrayToIntArray(imapSeenMessIdArray), getStatusFromImapFlags(fSeen));
            }

            if (imapSeenDeletedMessIdArray.size() > 0) {
                folder.setFlags(UtilsTools.convertIntegersArrayToIntArray(imapSeenDeletedMessIdArray), fSeenDel, true);
                one.clearImapDirtyStatusFlagsBulk(account.getUser_id(), UtilsTools.convertIntegersArrayToIntArray(imapSeenDeletedMessIdArray), getStatusFromImapFlags(fSeenDel));
            }

            if (imapDeletedMessIdArray.size() > 0) {
                folder.setFlags(UtilsTools.convertIntegersArrayToIntArray(imapDeletedMessIdArray), fDel, true);
                one.clearImapDirtyStatusFlagsBulk(account.getUser_id(), UtilsTools.convertIntegersArrayToIntArray(imapDeletedMessIdArray), getStatusFromImapFlags(fDel));
            }

            syncFlag = true;
        } catch (MessagingException e) {
            log.debug(String.format("failed to sync FLAG for %s", context), e);
        }

        return syncFlag;
    }

    public void setFlagOnMessage(Folder folder, int messageNum, int imapStatus) {
        Message syncMessage;
        Flags flagVal;

        try {
            syncMessage = folder.getMessage(messageNum);

            //Update the messages.
            //If there are flags that have been set to true.
            if ((flagVal = getFlippedToTrueFlags(imapStatus)).getSystemFlags().length > 0) {
                syncMessage.setFlags(flagVal, true);
            }

            //If there are flags that have been set to true.
            if ((flagVal = getFlippedToFalseFlags(imapStatus)).getSystemFlags().length > 0) {
                syncMessage.setFlags(flagVal, false);
            }

        } catch (MessagingException e) {
            log.debug(String.format("failed to set flags on message for msgID %s from folder %s", messageNum, folder.getFullName()), e);
        }
    }


    //TODO: #ME need to wrap these methods into their own class somewher
    public static int getFlippedTrueBitSet(int imapStatus) {
        // 0x1f masks the bottom 5 bits, this will have to eventually come from numFlags for dynamic use   (as hex value)
        // & the flag flipped dirty bit with the flag value for flags set to true
        return (imapStatus >>> (32 - numFlags)) & (imapStatus & 0x1f);
    }

    public static int getFlippedFalseBitSet(int imapStatus) {
        // 0x1f masks the bottom 5 bits, this will have to eventually come from numFlags for dynamic use
        // exclusive or the flag flipped dirty bit with the flag values, then and that with the flag flipped dirty bit to find flags set to false.
        return ((imapStatus >>> (32 - numFlags)) ^ (imapStatus & 0x1f)) & (imapStatus >>> (32 - numFlags));
    }

    public static Flags getFlippedToTrueFlags(int imapStatus) {
        Flags flagVal = new Flags();

        int changedFlags = getFlippedTrueBitSet(imapStatus);
        //Looks for true flags that have changed
        if (changedFlags != 0) {
            flagVal = getImapFlagsFromStatus(changedFlags);
        }

        return flagVal;
    }

    public static Flags getFlippedToFalseFlags(int imapStatus) {
        Flags flagVal = new Flags();

        int changedFlags = getFlippedFalseBitSet(imapStatus);

        if (changedFlags != 0) {
            flagVal = getImapFlagsFromStatus(changedFlags);
        }

        return flagVal;
    }

    public static Flags getImapFlagsFromStatus(int imapStatus) {
        Flags flagVal = new Flags();

        if ((imapStatus & 0x1f) != 0) {
            //Set Seen flag
            if ((imapStatus & 0x01) != 0) {
                flagVal.add(Flags.Flag.SEEN);
            }
            //Set Answered flag
            if ((imapStatus & 0x02) != 0) {
                flagVal.add(Flags.Flag.ANSWERED);
            }
            //Set Flagged flag
            if ((imapStatus & 0x04) != 0) {
                flagVal.add(Flags.Flag.FLAGGED);
            }
            //Set Deleted flag
            if ((imapStatus & 0x08) != 0) {
                flagVal.add(Flags.Flag.DELETED);
            }
            //Set Draft flag
            if ((imapStatus & 0x10) != 0) {
                flagVal.add(Flags.Flag.DRAFT);
            }
        }

        return flagVal;
    }

    public static int getStatusFromImapFlags(Flags imapFlags) {
        int flags = 0;

        if (imapFlags != null && imapFlags.getSystemFlags() != null && imapFlags.getSystemFlags().length > 0) {
            if (imapFlags.contains(Flags.Flag.SEEN)) {
                flags = flags | 0x01;
            }
            if (imapFlags.contains(Flags.Flag.ANSWERED)) {
                flags = flags | 0x02;
            }
            if (imapFlags.contains(Flags.Flag.FLAGGED)) {
                flags = flags | 0x04;
            }
            if (imapFlags.contains(Flags.Flag.DELETED)) {
                flags = flags | 0x08;
            }
            if (imapFlags.contains(Flags.Flag.DRAFT)) {
                flags = flags | 0x10;
            }
        }

        return flags;
    }
}
