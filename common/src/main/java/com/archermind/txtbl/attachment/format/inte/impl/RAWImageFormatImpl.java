package com.archermind.txtbl.attachment.format.inte.impl;

import com.archermind.txtbl.attachment.format.inte.AttachmentFormat;
import com.archermind.txtbl.domain.Attachment;
import org.jboss.logging.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 11-09-16
 * Time: 2:44 PM
 */
public class RAWImageFormatImpl implements AttachmentFormat {
    private static final Logger log = Logger.getLogger(TXTFormatImpl.class);

    public List<Attachment> format(String mailbox, String msgID, String filename, byte[] dataByte, int targetHeight, int targetWidth) {
        try {
            log.info("formatting image for RAW format/RAWIMGFormatImpl/: [" + mailbox + "] [" + msgID + "] [" + filename + "]");

            List<Attachment> attachList = new LinkedList<Attachment>();
            Attachment attachment = new Attachment();
            attachment.setName(filename);
            attachment.setData(dataByte);
            if (attachment.getData() != null) {
                attachment.setSize(attachment.getData().length);
            }
            attachList.add(attachment);
            return attachList;
        } catch (Exception e) {
            log.error("format/RAWImageFormatImpl/Exception: [" + mailbox + "] [" + msgID + "] [" + filename + "]", e);
        }
        return null;
    }
}
