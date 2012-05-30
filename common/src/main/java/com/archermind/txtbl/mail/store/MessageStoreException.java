package com.archermind.txtbl.mail.store;

public class MessageStoreException extends Exception
{
    public MessageStoreException()
    {
    }

    public MessageStoreException(String message)
    {
        super(message);
    }

    public MessageStoreException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public MessageStoreException(Throwable cause)
    {
        super(cause);
    }
}
