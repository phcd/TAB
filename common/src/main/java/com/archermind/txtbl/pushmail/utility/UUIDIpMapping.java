package com.archermind.txtbl.pushmail.utility;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;


public class UUIDIpMapping implements Serializable {

    private String uuid;
    private String ip;
    private boolean avaible;
    private Date lastHeartbeat;
    private Date lastNotification;
    private ScheduledFuture<?> nextNotification;
    private int times;

    public ScheduledFuture<?> getNextNotification() {
        return nextNotification;
    }

    public void setNextNotification(ScheduledFuture<?> nextNotification) {
        this.nextNotification = nextNotification;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public Date getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Date lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public Date getLastNotification() {
        return lastNotification;
    }

    public void setLastNotification(Date lastNotification) {
        this.lastNotification = lastNotification;
    }

    public boolean isAvaible() {
        return avaible;
    }

    public void setAvaible(boolean avaible) {
        this.avaible = avaible;
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

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
