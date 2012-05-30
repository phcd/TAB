package com.archermind.txtbl.domain;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.HashMap;

public class  Contact   implements Serializable {

	private static final long serialVersionUID = 1L;
	// Fields

	private int id=0;
	
	private String userid="";
	
	private String email="";

	private String alias_name="";

	private String phone="";

	private String comment="";

    private String type; // email_contacts, twitter_followers, twitter_following, whitelist, facebook_friend

	private transient HashMap<String, String> meta;
	
	/** default constructor */
	 
	// Property accessors

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}


	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		if(email!=null)
			this.email = email.toLowerCase();
		else
		this.email = email;
	}

	public String getAlias_name() {
		return this.alias_name;
	}

	public void setAlias_name(String alias_name) {
		this.alias_name = alias_name;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

	public HashMap<String, String> getMeta() {
		return meta;
	}

	public void setMeta(HashMap<String, String> meta) {
		this.meta = meta;
	}

    public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}