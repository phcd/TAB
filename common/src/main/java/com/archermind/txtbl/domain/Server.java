package com.archermind.txtbl.domain;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

public class Server implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String SSL = "ssl";
    private static final String HTTPS_PORT = "443";
    private static final String HTTP_PORT = "80";

    private int id = 0; //receive id

    private int sent_id = 0; // sent id

    private String name = "";

    private String status = "";

    private String receiveHost = "";

    private String receivePort = "";

    private String receiveProtocolType = "";

    private String receiveTs = "";

    private String sendHost = "";

    private String sendPort = "";

    private String sendProtocolType;

    private String sendTs = "";

    private String needAuth = "";

    private String comment = "";

    private String command = "";

    private String orderFlag = "";

    private String level = ""; //receive level

    private String sentLevel = ""; //sent level

    private String save_time = "";

    private String receiveHostFbaPath = "";

    private String receiveHostPrefix = "";

    private String sendHostFbaPath = "";

    private String sendHostPrefix = "";

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    // Constructors

    /**
     * default constructor
     */

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReceiveHost() {
        return this.receiveHost;
    }

    public void setReceiveHost(String receiveHost) {
        this.receiveHost = receiveHost;
    }

    public String getReceivePort() {
        return this.receivePort;
    }

    public void setReceivePort(String receivePort) {
        this.receivePort = receivePort;
    }

    public String getReceiveProtocolType() {
        return this.receiveProtocolType;
    }

    public void setReceiveProtocolType(String receiveProtocolType) {
        this.receiveProtocolType = receiveProtocolType;
    }

    public String getReceiveTs() {
        return this.receiveTs;
    }

    public void setReceiveTs(String receiveTs) {
        this.receiveTs = receiveTs;
    }

    public String getSendHost() {
        return this.sendHost;
    }

    public void setSendHost(String sendHost) {
        this.sendHost = sendHost;
    }

    public String getSendPort() {
        return this.sendPort;
    }

    public void setSendPort(String sendPort) {
        this.sendPort = sendPort;
    }

    public String getSendProtocolType() {
        return this.sendProtocolType;
    }

    public void setSendProtocolType(String sendProtocolType) {
        this.sendProtocolType = sendProtocolType;
    }

    public String getSendTs() {
        return this.sendTs;
    }

    public void setSendTs(String sendTs) {
        this.sendTs = sendTs;
    }

    public String getNeedAuth() {
        return this.needAuth;
    }

    public void setNeedAuth(String needAuth) {
        this.needAuth = needAuth;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getOrderFlag() {
        return orderFlag;
    }

    public void setOrderFlag(String orderFlag) {
        this.orderFlag = orderFlag;
    }

    public String getSave_time() {
        return save_time;
    }

    public void setSave_time(String save_time) {
        this.save_time = save_time;
    }

    public String getSentLevel() {
        return sentLevel;
    }

    public void setSentLevel(String sentLevel) {
        this.sentLevel = sentLevel;
    }

    public int getSent_id() {
        return sent_id;
    }

    public void setSent_id(int sent_id) {
        this.sent_id = sent_id;
    }

    public String getReceiveHostFbaPath() {
        return receiveHostFbaPath;
    }

    public void setReceiveHostFbaPath(String receiveHostFbaPath) {
        this.receiveHostFbaPath = receiveHostFbaPath;
    }

    public String getReceiveHostPrefix() {
        return receiveHostPrefix;
    }

    public void setReceiveHostPrefix(String receiveHostPrefix) {
        this.receiveHostPrefix = receiveHostPrefix;
    }

    public String getSendHostFbaPath() {
        return sendHostFbaPath;
    }

    public void setSendHostFbaPath(String sendHostFbaPath) {
        this.sendHostFbaPath = sendHostFbaPath;
    }

    public String getSendHostPrefix() {
        return sendHostPrefix;
    }

    public void setSendHostPrefix(String sendHostPrefix) {
        this.sendHostPrefix = sendHostPrefix;
    }

    public void setSendReceiveDetails(String domainName, String host, String ts, String fbaPath, String prefix) {
        String ssl = null;
        String port = HTTP_PORT;
        if (SSL.equals(StringUtils.lowerCase(StringUtils.trim(ts)))) {
            ssl = SSL;
            port = HTTPS_PORT;
        }

        String status = "1";
        String level = "6";


        setName(domainName);
        setStatus(status);

        setReceiveHost(host);
        setOrderFlag("1");
        setLevel(level);
        setReceiveTs(ssl);
        setReceivePort(port);
        setReceiveHostFbaPath(fbaPath);
        setReceiveHostPrefix(prefix);
        setReceiveProtocolType(Protocol.EXCHANGE);

        setSendHost(host);
        setSentLevel(level);
        setNeedAuth("1");
        setSendTs(ssl);
        setSendPort(port);
        setSendHostFbaPath(fbaPath);
        setSendHostPrefix(prefix);
        setSendProtocolType(Protocol.EXCHANGE);
    }

    public boolean isXobni() {
        return Protocol.isXobni(receiveProtocolType);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}