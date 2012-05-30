package com.archermind.txtbl.taskfactory.subscribe;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Server;
import com.archermind.txtbl.domain.SysConfig;
import com.archermind.txtbl.taskfactory.ReceiveNoticer;
import com.archermind.txtbl.taskfactory.common.AccountSenderIMP;
import com.archermind.txtbl.taskfactory.common.FactoryTools;
import com.archermind.txtbl.taskfactory.common.MacroDefine;
import static com.archermind.txtbl.taskfactory.common.MacroDefine.OTHER.CONST_ADDTASK;
import static com.archermind.txtbl.taskfactory.common.MacroDefine.OTHER.CONST_DELETETASK;
import static com.archermind.txtbl.taskfactory.common.MacroDefine.OTHER.CONST_UPDATETASK;
import com.archermind.txtbl.taskfactory.common.TFConfigManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jboss.logging.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SubscriberImp extends ReceiveNoticer implements Subscribe
{
    private final static Logger logger = Logger.getLogger(SubscriberImp.class);
    private final Map<SubscriberKey, SubscribeAccount> accounts = new HashMap<SubscriberKey, SubscribeAccount>();
    private final static long ONE_MINUTE = 60 * 1000l;

    private long accountReloadInterval;
    private long threadSleepTime;

    private String[] subscribeProtocols;

    private long timeSinceLastReload;

    //private int updateIndex = 0;
    //private int updateBufferLength = 2;

    //private List<Account>[] updateAcocuntsList = new List[updateBufferLength];

    private long subscribeTimeOut = 1000l * 60; // 1 minute

    private StartThread startThread;

    class SubscribeAccount
    {
        long timer = System.currentTimeMillis();
        Account account;
        boolean isActive = true;

        SubscribeAccount(Account account)
        {
            this.account = account;
        }

        boolean isTimeOut()
        {
            return System.currentTimeMillis() - timer > subscribeTimeOut;
        }

        void resetTimer()
        {
            timer = System.currentTimeMillis();
        }

        public Account getAccount()
        {
            return account;
        }

        void setAccount(Account account)
        {
            this.account = account;
        }
    }

    public SubscriberImp(String[] subscribeProtocols, long lReloadInterval, long lThreadSleepMinutes)
    {
        if(logger.isDebugEnabled())
            logger.debug(String.format("SubscriberImp(subscribeProtocols=%s, lReloadInterval=%s, lThreadSleepMinutes=%s)", StringUtils.join(subscribeProtocols,","),
                    String.valueOf(lReloadInterval), String.valueOf(lThreadSleepMinutes)));

        this.accountReloadInterval = lReloadInterval * 60l * 60l * 1000l;
        this.subscribeProtocols = subscribeProtocols;
        this.subscribeTimeOut = TFConfigManager.getInstance().getSubscribeTimeOut();
        this.subscribeTimeOut = this.subscribeTimeOut * 60l * 1000l;
        this.threadSleepTime = lThreadSleepMinutes * 60l * 1000l;

        logger.info(String.format("constructed subscriber reloadInterval=%s, protocols=%s, subscriberTimeOut=%s", lReloadInterval, StringUtils.join(subscribeProtocols, ","), subscribeTimeOut));

        loadAccounts();

    }

    private void subscribeAccounts(Map<SubscriberKey, SubscribeAccount> accounts)
    {
        if(logger.isDebugEnabled())
            logger.debug(String.format("subecribeAccounts(accounts=%s)", accounts));

        if (accounts == null || accounts.size() == 0)
        {
            logger.warn("there is account to send message in the list!");
        }

        for (Iterator<SubscriberKey> it = accounts.keySet().iterator(); it.hasNext();)
        {
            Account account = accounts.get(it.next()).getAccount();
            subscribeAccount(account);
        }
    }

    private void subscribeAccount(Account account)
    {
        if(logger.isDebugEnabled())
            logger.debug(String.format("subscribeAccount(account=%s)", String.valueOf(account)));

        logger.info(String.format("establishing msp subscription for account=%s uid=%s", account.getName(), account.getUser_id()));

        sendAccount(account);
    }

    class StartThread extends Thread
    {
        public void run()
        {
            if(logger.isDebugEnabled())
                logger.debug("StartThread.run...");

            subscribeAccounts(accounts);

            while (true)
            {
                logger.info("subscriber cruising... ");

                try
                {
                    if ((System.currentTimeMillis() - timeSinceLastReload) > accountReloadInterval)
                    {
                        logger.info("time to reload subscriber accounts");

                        loadAccounts();

                        subscribeAccounts(accounts);
                    }
                    else
                    {
                        updateAccounts();

                        for (SubscribeAccount account : accounts.values())
                        {
                            if (account.isTimeOut())
                            {
                                logger.info(String.format("account=%s, uid=%s timed out, sending to msp provider", account.getAccount().getName(), account.getAccount().getUser_id()));

                                account.getAccount().setComment("timeout");

                                Serializable object = FactoryTools.copyObject(account.getAccount());

                                subscribeAccount((Account) object);

                                account.resetTimer();
                                account.getAccount().setComment("");
                            }
                            else
                            {
                                logger.info(String.format("account=%s, uid=%s is still on time, will not send to msp provider", account.getAccount().getName(), account.getAccount().getUser_id()));
                            }
                        }
                    }


                }
                catch (Throwable t)
                {
                    logger.warn(String.format("unexpected error occured in subscriber %s", t.toString(), t));

                    logger.fatal(String.format("unexpected error occured in subscriber %s", t.toString(), t));
                }
                finally
                {
                    try
                    {
                        Thread.sleep(threadSleepTime);
                    }
                    catch (InterruptedException e)
                    {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }

        private void updateAccounts()
        {
            if(logger.isDebugEnabled())
                logger.debug("updateAccounts()");
            
            logger.info("updating accounts is starting...");

            for (SubscribeAccount subscribeAccount : accounts.values())
            {
                Account account = subscribeAccount.getAccount();

                SubscriberKey subscriberKey = new SubscriberKey(account);

                if (MacroDefine.OTHER.CONST_DELETETASK.equals(account.getCommand()))
                {
                    if (accounts.containsKey(subscriberKey))
                    {
                        logger.info(String.format("deleting acocunt account=%s, uid=%s", account.getName(), account.getUser_id()));

                        accounts.remove(subscriberKey);
                    }
                    else
                    {
                        logger.warn(String.format("unable to delete acocunt account=%s, uid=%s, it does not exist", account.getName(), account.getUser_id()));
                    }
                }
                else if (MacroDefine.OTHER.CONST_UPDATETASK.equals(account.getCommand()))
                {
                    if (accounts.containsKey(subscriberKey))
                    {
                        logger.info(String.format("updating acocunt account=%s, uid=%s", account.getName(), account.getUser_id()));
                        accounts.get(subscriberKey).setAccount(account);
                    }
                    else
                    {
                        logger.warn(String.format("unable to update acocunt account=%s, uid=%s, it does not exist, adding", account.getName(), account.getUser_id()));

                        accounts.put(subscriberKey, new SubscribeAccount(account));
                    }
                }
                else if (MacroDefine.OTHER.CONST_ADDTASK.equals(account.getCommand()))
                {
                    if (accounts.containsKey(subscriberKey))
                    {
                        logger.info(String.format("adding acocunt account=%s, uid=%s, it already exists", account.getName(), account.getUser_id()));
                        accounts.get(subscriberKey).setAccount(account);
                    }
                    else
                    {
                        logger.info(String.format("adding acocunt account=%s, uid=%s", account.getName(), account.getUser_id()));
                        accounts.put(subscriberKey, new SubscribeAccount(account));
                    }
                }
            }
        }
    }

    /**
     *
     */
    private void loadAccounts()
    {
        if(logger.isDebugEnabled())
            logger.debug("loadAccounts()");

        if (subscribeProtocols == null)
        {
            logger.error("there are no protocols to load accounts");
            return;
        }

        List<Account> loadedAccountList = FactoryTools.loadAccounts(subscribeProtocols);

        if (loadedAccountList == null)
        {
            logger.warn("Unable to load any accounts");
            return;
        }

        logger.info(String.format("loaded %d subscribe accounts", loadedAccountList.size()));

        accounts.clear();

        for (Account account : loadedAccountList)
        {
            accounts.put(new SubscriberKey(account), new SubscribeAccount(account));
        }

        timeSinceLastReload = System.currentTimeMillis();
    }

    @Override
    public void start()
    {
        startThread = new StartThread();

        startThread.start();
    }

    @Override
    public ReceiveNoticer updateAccount(Account account)
    {
        
        if (account.getCommand().equals(CONST_DELETETASK))
        {
            logger.warn(String.format("delete account not implemented account=%s, uid=%s", account.getName(), account.getUser_id()));
        }

        if (account.getCommand().equals(CONST_ADDTASK))
        {
            logger.warn(String.format("add account not implemented account=%s, uid=%s", account.getName(), account.getUser_id()));
        }

        if (account.getCommand().equals(CONST_UPDATETASK))
        {
            logger.warn(String.format("modify account not implemented account=%s, uid=%s", account.getName(), account.getUser_id()));
        }

        if (! accounts.containsKey(account.getName()))
        {
            accounts.put(new SubscriberKey(account), new SubscribeAccount(account));
        }

        subscribeAccount(account);

        return this;
    }

    @Override
    public void updateConfig(SysConfig SysConfig)
    {

    }

    @Override
    public void updateMSConfig(Server server)
    {

    }

    public void receivedMail(Account account)
    {
        logger.info(String.format("get account account=%s, uid=%s received mail message", account.getName(), account.getUser_id()));
        
        if (accounts.containsKey(account.getName()))
        {
            accounts.get(account.getName()).resetTimer();
        }

    }

    public void sendAccount(Account account)
    {
        account.setJustKey_id(0);

        //logger.debug("key_id[" + account.getKey_id() + "]");
        AccountSenderIMP.getInstance().SendAccount(account);
    }

    @Override
    public void pop3CollectionClear()
    {
        logger.info("pop3 collection clear!!!");
        SubscribeAccount account;
        Iterator<Entry<SubscriberKey, SubscribeAccount>> it = accounts.entrySet().iterator();
        Serializable object = null;
        while (it.hasNext())
        {
            account = it.next().getValue();
            account.getAccount().setJustKey_id(1);
            logger.info("key_id[" + account.getAccount().getKey_id() + "]");
            object = FactoryTools.copyObject(account.getAccount());
            AccountSenderIMP.getInstance().SendAccount((Account) object);
            account.resetTimer();
            account.getAccount().setJustKey_id(0);
        }
    }

}


class SubscriberKey
{
    public String account;
    public String userId;

    SubscriberKey(Account account)
    {
        this(account.getName(), account.getUser_id());
    }

    SubscriberKey(String account, String userId)
    {
        this.account = account;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object obj)
    {
        SubscriberKey key = (SubscriberKey) obj;

        return new EqualsBuilder().append(account, key.account).append(userId, key.userId).isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(account).append(userId).toHashCode();
    }
}