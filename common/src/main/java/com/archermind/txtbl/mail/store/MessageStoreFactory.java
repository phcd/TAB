package com.archermind.txtbl.mail.store;


public class MessageStoreFactory
{
    private static MessageStore store = new ApacheMessageStore();

    public static MessageStore getStore()
    {
        return store;
    }
}
