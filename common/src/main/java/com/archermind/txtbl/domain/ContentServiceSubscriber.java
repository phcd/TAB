package com.archermind.txtbl.domain;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class ContentServiceSubscriber implements Serializable {
	private static final long serialVersionUID = -4066152413266791815L;

	private int id;
	
	private int contentServiceProviderId;
	private ContentServiceProvider contentServiceProvider;
	
	private String uuid;
	private String email;
	private Date createDate;
	private Date optInDate;
	private Date optOutDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getOptInDate() {
		return optInDate;
	}

	public void setOptInDate(Date optInDate) {
		this.optInDate = optInDate;
	}

	public Date getOptOutDate() {
		return optOutDate;
	}

	public void setOptOutDate(Date optOutDate) {
		this.optOutDate = optOutDate;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}
	
	public int getContentServiceProviderId() {
		return contentServiceProviderId;
	}

	public void setContentServiceProviderId(int contentServiceProviderId) {
		this.contentServiceProviderId = contentServiceProviderId;
	}

	public ContentServiceProvider getContentServiceProvider() {
		return contentServiceProvider;
	}

	public void setContentServiceProvider(
			ContentServiceProvider contentServiceProvider) {
		this.contentServiceProvider = contentServiceProvider;
	}

	public int hashCode() {
		return uuid != null ? uuid.hashCode() : super.hashCode();
	}
	
	public boolean equals(Object other) {
		if (other instanceof ContentServiceSubscriber) {
			ContentServiceSubscriber contentServiceSubscriber = (ContentServiceSubscriber) other;
			return contentServiceSubscriber.getUuid() != null && contentServiceSubscriber.getUuid().equals(getUuid());
		}
		return false;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
