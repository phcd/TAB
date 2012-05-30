package com.archermind.txtbl.domain;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class GeoLocationServiceProvider implements Serializable {
	private static final long serialVersionUID = -7726160407262716031L;
	
	private int id;
	private String key;
	private String name;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int hashCode() {
		return key != null ? key.hashCode() : super.hashCode();
	}
	
	public boolean equals(Object other) {
		if (other instanceof GeoLocationServiceProvider) {
			GeoLocationServiceProvider geoLocationServiceProvider = (GeoLocationServiceProvider) other;
			return geoLocationServiceProvider.getKey() != null && geoLocationServiceProvider.getKey().equals(getKey());
		}
		return false;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
	
}
