package com.archermind.txtbl.domain;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.Date;

public class Attachment implements Serializable {

	private static final long serialVersionUID = 1L;

	// Fields

	private int id = 0;

	private int emailId = 0;

	private int size = 0;

	private String name = "";

	private byte[] data = null;

	private String comment = "";

	private String browseAttSeqNo = "";

    private String location = "";

    private Date savedOnDate; // date on which the attachment was saved

	private String contentType = "";
	// Constructors

	/** default constructor */

	// Property accessors
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getEmailId() {
		return this.emailId;
	}

	public void setEmailId(int emailId) {
		this.emailId = emailId;
	}

	public int getSize() {
		return this.size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		try {
			if (name != null) {

				name = name.replaceAll("\\\\", "_");

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.name = name;
	}

	public byte[] getData() {
		return this.data;
	}

	public void setData(byte[] data) {
		this.data = data;
		if (data != null) {
			setSize(data.length);
		}
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getBrowseAttSeqNo() {
		return browseAttSeqNo;
	}

	public void setBrowseAttSeqNo(String browseAttSeqNo) {
		this.browseAttSeqNo = browseAttSeqNo;
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String toString() {
		return ReflectionToStringBuilder.toStringExclude(this, new String[] {"data"});
	}

}