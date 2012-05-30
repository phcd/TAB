package com.archermind.txtbl.mail.store;

import org.apache.commons.httpclient.methods.PostMethod;

public class DeleteMethod extends PostMethod {

    public DeleteMethod(String uri) {
        super(uri);
    }

    @Override
    public String getName()
    {
        return "DELETE";
    }
    
}
