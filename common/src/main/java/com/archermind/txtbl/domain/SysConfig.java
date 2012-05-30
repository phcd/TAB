package com.archermind.txtbl.domain;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

public class  SysConfig  implements Serializable {

	private static final long serialVersionUID = 1L;

	private String actionFlag = "";

	private String operateMsg = "";
	
	private int id;

	private String name="";

	private String value="";

	private String description="";
	
	private String configtype="";

	private String comment="";

	private String need_notify = "0";

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getConfigtype() {
    	return configtype;
    }

	public void setConfigtype(String configtype) {
    	this.configtype = configtype;
    }

	public String getActionFlag() {
    	return actionFlag;
    }

	public void setActionFlag(String actionFlag) {
    	this.actionFlag = actionFlag;
    }

	public String getOperateMsg() {
    	return operateMsg;
    }

	public void setOperateMsg(String operateMsg) {
    	this.operateMsg = operateMsg;
    }

	public int getId() {
    	return id;
    }

	public void setId(int id) {
    	this.id = id;
    }

	public String getNeed_notify() {
		return need_notify;
	}

	public void setNeed_notify(String need_notify) {
		this.need_notify = need_notify;
	}


     @Override
    public String toString()
    {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}