package com.archermind.txtbl.mail.store;

import org.apache.commons.httpclient.methods.GetMethod;

public class PeekMethod extends GetMethod
{
    public PeekMethod(String uri)
    {
        super(uri);
    }

    @Override
    public String getName()
    {
        return "PEEK";
    }
}