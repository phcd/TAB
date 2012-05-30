package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.authenticator.ImapAuthenticator;
import com.archermind.txtbl.authenticator.OauthAuthenticator;
import org.junit.Test;
import org.junit.Assert;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import com.archermind.txtbl.receiver.mail.support.NewProviderSupport;
import com.archermind.txtbl.receiver.mail.support.XobniProviderSupport;
import com.archermind.txtbl.domain.Protocol;

public class NewImapIdleProviderTest {
    @Test
    public void getProviders() {
        XmlBeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("com/archermind/txtbl/receiver/mail/xml/providersBeanFactory.xml"));
        NewImapIdleProvider bean1 = (NewImapIdleProvider) ((ObjectFactory) beanFactory.getBean(Protocol.IMAP_IDLE)).getObject();
        NewImapIdleProvider bean2 = (NewImapIdleProvider) ((ObjectFactory) beanFactory.getBean(Protocol.XOBNI_IMAP_IDLE)).getObject();
        NewImapIdleProvider bean3 = (NewImapIdleProvider) ((ObjectFactory) beanFactory.getBean(Protocol.XOBNI_OAUTH_IDLE)).getObject();
        Assert.assertNotSame(bean1, bean2);
        Assert.assertNotSame(bean1, bean3);
        Assert.assertNotSame(bean2, bean3);
        Assert.assertTrue(bean1.support instanceof NewProviderSupport);
        Assert.assertTrue(bean2.support instanceof XobniProviderSupport);
        Assert.assertTrue(bean3.support instanceof XobniProviderSupport);
        Assert.assertTrue(bean1.authenticator instanceof ImapAuthenticator);
        Assert.assertTrue(bean2.authenticator instanceof ImapAuthenticator);
        Assert.assertTrue(bean3.authenticator instanceof OauthAuthenticator);
    }
}
