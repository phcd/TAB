package com.archermind.txtbl.receiver.mail.support;

import javax.mail.Message;

public abstract class EmailProcess {
    public abstract boolean process(Message message, int messageNumber, String messageId) throws Exception;

    public boolean complete() throws Exception {
        return true;
    }

    public boolean shouldProcess() {
        return true;
    }

    public boolean shouldStopProcess() {
        return false;
    }
}
