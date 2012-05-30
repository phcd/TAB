package com.archermind.txtbl.domain;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class PeekLocation implements Serializable {
	private static final long serialVersionUID = 7758485714439473520L;

	private int id;
	
	private String imei;
	private int cellId;
	private int lacId;
	private int mnc;
	private int mcc;
	private Timestamp lastUpdateTimestamp;

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public int getCellId() {
		return cellId;
	}

	public void setCellId(int cellId) {
		this.cellId = cellId;
	}

	public int getLacId() {
		return lacId;
	}

	public void setLacId(int lacId) {
		this.lacId = lacId;
	}

	public int getMnc() {
		return mnc;
	}

	public void setMnc(int mnc) {
		this.mnc = mnc;
	}

	public int getMcc() {
		return mcc;
	}

	public void setMcc(int mcc) {
		this.mcc = mcc;
	}

	public Timestamp getLastUpdateTimestamp() {
		return lastUpdateTimestamp;
	}

	public void setLastUpdateTimestamp(Timestamp lastUpdateTimestamp) {
		this.lastUpdateTimestamp = lastUpdateTimestamp;
	}
	
	public int hashCode() {
		return imei != null ? imei.hashCode() : super.hashCode();
	}
	
	public boolean equals(Object other) {
		if (other instanceof PeekLocation) {
			PeekLocation peekLocation = (PeekLocation) other;
			return peekLocation.getImei() != null && peekLocation.getImei().equals(getImei());
		}
		return false;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
	
	public String toXML() {
		StringBuffer sb = new StringBuffer();

		sb.append("<location>");
		sb.append("<imei>").append(getImei()).append("</imei>");
		sb.append("<cellId>").append(getCellId()).append("</cellId>");
		sb.append("<lacId>").append(getLacId()).append("</lacId>");
		sb.append("<mnc>").append(getMnc()).append("</mnc>");
		sb.append("<mcc>").append(getMcc()).append("</mcc>");
		sb.append("<timestamp>").append(getLastUpdateTimestamp()).append("</timestamp>");
		sb.append("</location>");
		
		return sb.toString();
	}

    public String toJSON() {
        StringBuffer sb = new StringBuffer();

        sb.append("{\"location\" : ");
        sb.append("\"imei\"").append(" : \"").append(getImei()).append("\", ");
        sb.append("\"cellId\"").append(" : \"").append(getCellId()).append("\", ");
        sb.append("\"lacId\"").append(" : \"").append(getLacId()).append("\", ");
        sb.append("\"mnc\"").append(" : \"").append(getMnc()).append("\", ");
        sb.append("\"mcc\"").append(" : \"").append(getMcc()).append("\", ");
        sb.append("\"timestamp\"").append(" : \"").append(getLastUpdateTimestamp()).append("\"");
        sb.append("}");

        return sb.toString();
    }
    
}
