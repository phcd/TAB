package com.archermind.txtbl.attachmentsvc.msg;

import com.archermind.txtbl.domain.Account;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

public class AttachmentSvcMessage implements Serializable {
    private Account account;
    private Integer emailId;
    private Integer originalAttachmentId;
    private Integer receivedAttachmentId;
    private Date mailDate;
    private int size;
    private String originalAttachmentName;
    private Collection<Integer> extraReceivedAttachmentIds;

    public AttachmentSvcMessage(Account account, Date mailDate, Integer emailId, String originalAttachmentName, Integer originalAttachmentId, Integer receivedAttachmentId, Collection<Integer> extraReceivedAttachmentIds, int size) {
        this.account = account;
        this.mailDate = mailDate;
        this.emailId = emailId;
        this.originalAttachmentId = originalAttachmentId;
        this.receivedAttachmentId = receivedAttachmentId;
        this.size = size;
        this.extraReceivedAttachmentIds = extraReceivedAttachmentIds;
        this.originalAttachmentName = originalAttachmentName;
    }

    public Account getAccount() {
        return this.account;
    }

    public Integer getEmailId() {
        return emailId;
    }

    public Integer getOriginalAttachmentId() {
        return originalAttachmentId;
    }

    public Integer getReceivedAttachmentId() {
        return receivedAttachmentId;
    }

    public Date getMailDate() {
        return mailDate;
    }

    public int getSize() {
        return size;
    }

    public String getOriginalAttachmentName() {
        return originalAttachmentName;
    }

    public Collection<Integer> getExtraReceivedAttachmentIds() {
        return extraReceivedAttachmentIds;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
