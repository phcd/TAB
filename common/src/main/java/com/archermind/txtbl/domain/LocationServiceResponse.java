package com.archermind.txtbl.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class LocationServiceResponse implements Serializable {
	private static final long serialVersionUID = 1766394838454752645L;
	
	public static final int SUCCESS = 1001;
	
	public static final int VALIDATION_ERROR = 2010;
	public static final int INTERNAL_ERROR = 2020;
    public static final int NOT_A_PEEKSTER_ERROR = 2030;
    public static final int DATE_FORMAT_ERROR = 2040;
    public static final int INVALID_EMAIL_ERROR = 2050;
    public static final int INVALID_REQUEST_ERROR = 2060;
    public static final int NO_SERVER_FOR_DOMAIN_ERROR = 2070;
    public static final int LOGIN_FAILED_ERROR = 2080;

    public static final int INVALID_KEY = 9000;
	
	private static final HashMap<Integer, String> messages = new HashMap<Integer, String>();
	static {
		messages.put(SUCCESS, "Request processed successfully.");
		messages.put(INTERNAL_ERROR, "We ran into a problem processing your request. Please contact Peek if the problem persists.");
		messages.put(VALIDATION_ERROR, "Invalid key used to access service. Please contact Peek to verify your service key.");
        messages.put(NOT_A_PEEKSTER_ERROR, "Sorry, the email address specified is not that of a Peekster!");
        messages.put(DATE_FORMAT_ERROR, "Invalid date format, expected UTC format: yyyy-MM-dd'T'HH:mm:ss.SSSZ (2009-04-07T14:53:36.093Z)");
        messages.put(INVALID_EMAIL_ERROR, "Invalid email address");
        messages.put(INVALID_REQUEST_ERROR, "Unable to process request- the request is missing one or more required parameters");
        messages.put(NO_SERVER_FOR_DOMAIN_ERROR, "No email server is configured for the domain specified");
        messages.put(LOGIN_FAILED_ERROR, "Could not successfully login using the parameters specified");
        messages.put(INVALID_KEY, "Invalid API Key");
	}
	
	private int code = SUCCESS;
	private PeekLocation peekLocation;
	private String uuid;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public PeekLocation getPeekLocation() {
		return peekLocation;
	}

	public void setPeekLocation(PeekLocation peekLocation) {
		this.peekLocation = peekLocation;
	}

	public String getMessage() {
		String message = messages.get(code);
		if (message == null) {
			message = "Internal system error - no message specified for message code " + code;
		}
		return message;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

	public String toXML() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<PeekLocationResponse>");
		sb.append("<code>").append(getCode()).append("</code>");
		sb.append("<message>").append(getMessage()).append("</message>");
		sb.append("<uuid>").append(getUuid()).append("</uuid>");
		sb.append("<timestamp>").append(new Timestamp(System.currentTimeMillis())).append("</timestamp>");
		sb.append(peekLocation == null ? "<location/>" : peekLocation.toXML());
		sb.append("</PeekLocationResponse>");
		
		return sb.toString();
	}

    public String toJSON() {
        StringBuffer sb = new StringBuffer();

        sb.append("[{");
        sb.append("\"status\"").append(" : \"").append(getCode() == 1001 ? "ok" : "error").append("\", ");
        sb.append("\"code\"").append(" : \"").append(getCode()).append("\", ");
        sb.append("\"message\"").append(" : \"").append(getMessage()).append("\", ");
        sb.append("\"uuid\"").append(" : \"").append(getUuid()).append("\", ");
        sb.append("\"timestamp\"").append(" : \"").append(new Timestamp(System.currentTimeMillis())).append("\", ");
        sb.append(peekLocation == null ? "{\"location\":\"\"}" : peekLocation.toJSON());
        sb.append("}]");

        return sb.toString();
    }
}
