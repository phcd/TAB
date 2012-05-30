package com.archermind.txtbl.pushmail.utility;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

import java.util.Date;


public class TopicInfo implements Serializable {

    private String uuid;

	private String flag;

	private Date date;

	private byte[] UdpPacket;

	private String ip;

    public TopicInfo()
    {

    }

    public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public byte[] getUdpPacket() {
		return UdpPacket;
	}

	public void setUdpPacket(byte[] udpPacket) {
		UdpPacket = udpPacket;
	}
    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
