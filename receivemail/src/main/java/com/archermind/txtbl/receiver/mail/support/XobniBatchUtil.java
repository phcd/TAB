package com.archermind.txtbl.receiver.mail.support;

public class XobniBatchUtil {
    public static XobniBatchUtil INSTANCE = new XobniBatchUtil();

    private XobniBatchUtil() {
    }

    public int getBatchSize(int xobniBatchSize, int totalMessages, int messagesLength) {
        if (messagesLength - totalMessages < xobniBatchSize) {
            return (messagesLength - totalMessages);
        }

        return xobniBatchSize;
    }

    public int getStartIndex(int totalMessages, int messagesInInbox, boolean isReverseOrder) {
        if (isReverseOrder) {
            return totalMessages + 1;
        } else {
            return messagesInInbox - totalMessages;
        }
    }

    public int getEndIndex(int totalMessages, int batchSize, int messagesInInbox, boolean isReverseOrder) {
        int numDownload = batchSize;
        if ((messagesInInbox - totalMessages) < batchSize) {
            numDownload = messagesInInbox - totalMessages;
        }

        if (isReverseOrder) {
            return totalMessages + numDownload;
        } else {
            return (messagesInInbox - totalMessages - numDownload) + 1;
        }
    }

}
