package com.archermind.txtbl.domain;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.Date;

public class OriginalReceivedAttachment implements Serializable {

	private static final long serialVersionUID = -2504462440764661935L;

	private Integer id = 0;

	private Integer emailId = 0;

	private String name = "";

	private byte[] data;

    private String location = "";

    private Date savedOnDate; // Date-time of when the attachment was saved to 'location'

    private Date processedOnDate; // Date-time of when the attachement was processed by the attachment svc.

    private String contentType;
    
    private long size;

    public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getEmailId() {
		return emailId;
	}

	public void setEmailId(Integer emailId) {
		this.emailId = emailId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null) {
			this.name = name.trim();
		}
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getSavedOnDate() {
        return savedOnDate;
    }

    public void setSavedOnDate(Date savedOnDate) {
        this.savedOnDate = savedOnDate;
    }

    public Date getProcessedOnDate() {
        return processedOnDate;
    }

    public void setProcessedOnDate(Date processedOnDate) {
        this.processedOnDate = processedOnDate;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String toString() {
		return ReflectionToStringBuilder.toStringExclude(this, new String[] {"data"});
	}

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}