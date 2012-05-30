package com.archermind.txtbl.receiver.mail.threadpool;

import EDU.oswego.cs.dl.util.concurrent.Callable;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.receiver.mail.abst.Provider;
import com.archermind.txtbl.receiver.mail.abst.impl.PushMailNotifier;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

public class TaskCallable implements Callable {

    private Account account = null;

    private static BeanFactory beanFactory = null;

    private String context;

    private static final Logger log = Logger.getLogger(TaskCallable.class);

    /** initialize spring bean factory */
    static {
        try {
            beanFactory = new XmlBeanFactory(new ClassPathResource("com/archermind/txtbl/receiver/mail/xml/providersBeanFactory.xml"));

            log.info("[initialization receiving beanFactory success]");
        }
        catch (Exception e) {
            log.error("static/TaskCallable/Exception: ", e);
        }
    }

    public TaskCallable(Account account) {
        if(log.isDebugEnabled())
            log.debug(String.format("TaskCallable(account=%s)", String.valueOf(account)));
        this.account = account;
        this.context = String.format("account=%s, uid=%s, email=%s", account.getId(), account.getUser_id(), account.getName());
    }

    /**
     * @return
     * @throws Exception
     */
    public Integer call() throws Exception {
        if(log.isDebugEnabled())
            log.debug("call... ");
        
        long startTask = System.nanoTime();

        int total = 0;

        try
        {
            Provider provider = null;

            log.info("[begin] [small] [" + context + "]");

            if(log.isDebugEnabled())
                log.debug("loading provider for account="+String.valueOf(account));
            
            if (beanFactory.containsBean(account.getReceiveProtocolType())) {
                Object bean = beanFactory.getBean(account.getReceiveProtocolType());

                if (bean instanceof Provider) {
                    provider = (Provider) bean;
                } else if (bean instanceof ObjectFactory) {
                    Object object = ((ObjectFactory) bean).getObject();
                    provider = (Provider) object;
                }
            }

            long getProviderTime = (System.nanoTime() - startTask) / 1000000;

            if (provider == null) {
                throw new Exception(String.format("Unable to find provider for protocol %s, %s", account.getReceiveProtocolType(), context));
            }

            long startReceive = System.nanoTime();

            log.info(String.format("delegating small mail receipt for %s, to provider %s", context, provider));
            total = provider.receiveMail(account);

            log.debug(String.format("attempting sending push mail notificaiton for account=%s, uid=%s", account.getName(), account.getUser_id()));

            long receiveTime = (System.nanoTime() - startReceive) / 1000000;

            long startNotify = System.nanoTime();


            if (total > 0) {
                String notifierResult = PushMailNotifier.sendPushMailNotification(account, total, context);

                // VV: no idea what this is about :(

                if (total < 0) {
                    String temp = String.valueOf(total);
                    if (temp.length() > 4) {
                        total = Integer.parseInt(temp.substring(0, 4));
                    }
                }

                log.info(String.format("%s [%d] [end] [small] [%s] [task=%d, notify=%d, lookup=%d, receive=%d]", notifierResult, total, context, (System.nanoTime() - startTask) / 1000000, (System.nanoTime() - startNotify) / 1000000, getProviderTime, receiveTime));
            }
        }
        catch (Exception e) {
            log.error("task callable failure: [" + context + "] " + "[" + total + "]", e);
        }
        return total;
    }

}
