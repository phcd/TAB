package com.archermind.txtbl.receiver.mail.mdb;

import com.archermind.txtbl.authenticator.Authenticator;
import com.archermind.txtbl.authenticator.ImapAuthenticator;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.receiver.mail.abst.impl.NewImapIdleProvider;
import com.archermind.txtbl.receiver.mail.support.NewProviderSupport;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "topic/idleevents")})
public class IdleMDB implements MessageListener
{

    private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(IdleMDB.class);

    private NewProviderSupport newProviderSupport = new NewProviderSupport();
    private Authenticator authenticator = new ImapAuthenticator();

    public void onMessage(Message message)
    {
        String context = null;

        try
        {
            final Account account = (Account) ((ObjectMessage) message).getObject();

            context = String.format("account=%s, uid=%s, email=%s", account.getId(), account.getUser_id(), account.getName());

            log.info(String.format("Processing event for %s", context));

            final String finalContext = context;

            if (NewImapIdleProvider.isIdlingForAccount(account))
            {
                log.info(String.format("Current server is idling for %s, event will be processed", context));

                final NewImapIdleProvider provider = new NewImapIdleProvider(newProviderSupport, authenticator);

                final ExecutorService executor = Executors.newSingleThreadExecutor();

                executor.submit(new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            provider.receiveMail(account);
                        }
                        catch (Throwable t)
                        {
                            log.fatal("Unexpected error during idle event processing for " + finalContext, t);
                        }
                        finally
                        {
                            executor.shutdown();
                        }
                    }
                });
            }
            else
            {
                log.info(String.format("Current server is not idling for %s, event will be discarded", context));
            }
        }
        catch (Throwable t)
        {
            log.fatal(String.format("Unable to process idle event for %s", context), t);
        }
    }
}
