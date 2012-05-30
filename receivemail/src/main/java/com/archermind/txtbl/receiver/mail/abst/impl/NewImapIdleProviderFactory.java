package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.authenticator.Authenticator;
import com.archermind.txtbl.authenticator.ImapAuthenticator;
import com.archermind.txtbl.receiver.mail.support.NewProviderSupport;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;


public class NewImapIdleProviderFactory implements ObjectFactory
{
    private NewProviderSupport support;
    private Authenticator authenticator;

    @SuppressWarnings("unused")
    public NewImapIdleProviderFactory(NewProviderSupport support, Authenticator authenticator) {
        this.support = support;
        this.authenticator = authenticator;
    }


    @SuppressWarnings("unused")
    public NewImapIdleProviderFactory() {
        this.support = new NewProviderSupport();
        this.authenticator = new ImapAuthenticator();
    }

    public Object getObject() throws BeansException {
        return new NewImapIdleProvider(support, authenticator);
    }


}
