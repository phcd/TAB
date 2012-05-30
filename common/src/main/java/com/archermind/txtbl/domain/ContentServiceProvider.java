package com.archermind.txtbl.domain;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class ContentServiceProvider implements Serializable {
	private static final long serialVersionUID = 4080774682550693396L;

	private int id;
	private int geoLocationServiceProviderId;
	private GeoLocationServiceProvider geoLocationServiceProvider;
	private String cpid;
	private String name;
	private String optInUrl;
	private String optOutUrl;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getGeoLocationServiceProviderId() {
		return geoLocationServiceProviderId;
	}

	public void setGeoLocationServiceProviderId(int geoLocationServiceProviderId) {
		this.geoLocationServiceProviderId = geoLocationServiceProviderId;
	}

	public GeoLocationServiceProvider getGeoLocationServiceProvider() {
		return geoLocationServiceProvider;
	}

	public void setGeoLocationServiceProvider(
			GeoLocationServiceProvider geoLocationServiceProvider) {
		this.geoLocationServiceProvider = geoLocationServiceProvider;
	}
	
	public String getCpid() {
		return cpid;
	}

	public void setCpid(String cpid) {
		this.cpid = cpid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOptInUrl() {
		return optInUrl;
	}

	public void setOptInUrl(String optInUrl) {
		this.optInUrl = optInUrl;
	}

	public String getOptOutUrl() {
		return optOutUrl;
	}

	public void setOptOutUrl(String optOutUrl) {
		this.optOutUrl = optOutUrl;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public int hashCode() {
		return cpid != null ? cpid.hashCode() : super.hashCode();
	}
	
	public boolean equals(Object other) {
		if (other instanceof ContentServiceProvider) {
			ContentServiceProvider contentServiceProvider = (ContentServiceProvider) other;
			return contentServiceProvider.getCpid() != null && contentServiceProvider.getCpid().equals(getCpid());
		}
		return false;
	}	
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
