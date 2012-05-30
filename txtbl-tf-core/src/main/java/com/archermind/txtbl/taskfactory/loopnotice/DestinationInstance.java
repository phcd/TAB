package com.archermind.txtbl.taskfactory.loopnotice;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Server;
import com.archermind.txtbl.domain.SysConfig;
import com.archermind.txtbl.taskfactory.ReceiveNoticer;
import com.archermind.txtbl.taskfactory.common.FactoryTools;
import com.archermind.txtbl.taskfactory.common.MacroDefine;
import com.archermind.txtbl.taskfactory.common.TFConfigManager;
import com.archermind.txtbl.utils.SendQueueMessageClient;
import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DestinationInstance extends ReceiveNoticer {
	private static Logger logger = Logger.getLogger(DestinationInstance.class);

	private Map<String, Queue<Account>> aur = new ConcurrentHashMap<String, Queue<Account>>();

	private int loopTime = 5;
	protected String[] destinationUrls;
	private long startTime;
	protected String destinationName = "default";

	private Queue<Account> queue = null;

	private boolean needReload = false;

	private String[] protocols = null;

    private List<Account> source = null;


    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("DestinationInstance {");
        builder.append("name=").append(destinationName);
        builder.append(",destination=").append(StringUtils.join(destinationUrls, " and "));
        builder.append(",queue=").append(queue != null ? queue.size() : "empty");
        builder.append("}");
        return builder.toString();
    }

    /**
     * @param config Has the form of loopTime;ipaddress:port;reloadInterval
     * @param protocols
     * @param destination
     */
	public DestinationInstance(String config, String[] protocols, String destination)
    {
        if(logger.isTraceEnabled())
            logger.trace(String.format("DestinationInstance(config=%s, protocols=%s, destination=%s)", config, org.apache.commons.lang.StringUtils.join(protocols, ";"), destination));

		String[] tmp = config.split(";");
		loopTime = Integer.parseInt(tmp[0].trim());
		destinationUrls = getDestinationUrls(tmp[1]);
		startTime = System.currentTimeMillis();
		destinationName = destination;
		this.protocols = protocols;
	}

	@Override
	public void start()
    {
		new StartThread().start();
	}

	private void loadAccount(String[] protocols)
    {
        if(logger.isTraceEnabled())
            logger.trace(String.format("loadAccount(protocols=%s)", StringUtils.join(protocols, ",")));

		long time = System.currentTimeMillis();

		logger.info("[" + destinationName + "] loading accounts, protocols: " + StringUtils.join(protocols, ","));

		List<Account> accounts;

		if ((accounts = FactoryTools.loadAccounts(protocols)) != null)
        {
			aur.clear();
            source = accounts;
        }

        logger.info(String.format("[%s] loaded %s accounts in %s millis", destinationName, source == null ? "none" : String.valueOf(source.size()), System.currentTimeMillis() - time));
	}

	public void reloadAccount()
    {
		needReload = true;
	}

	public DestinationInstance updateAccount(Account account)
    {
		logger.info(String.format("%s updating account: account=%s", destinationName, account));

            Queue<Account> queue = aur.get(account.getName());

            if (queue == null)
                queue = new ConcurrentLinkedQueue<Account>();

            boolean added = queue.offer(account);

            if (! added)
            {
                logger.error(String.format("DestinationInstance unable to add to queue, uid=%s", account.getUser_id()));
            }

        if (isAddAccount(account))
        {
            account.setNewAccountStatus();
        }

        if (isDeleteAccount(account))
        {
            account.setDeletedAccountStatus();
            logger.info(String.format("Deleting %s, this is account is idle=%s", account.getName(), account.isIMapIdle()));
        }

        aur.put(account.getName(), queue);

        return this;

	}

	private void updateAccount(Account account, Account recentCopy) {
		account.setAlias_name(recentCopy.getAlias_name());
		account.setPassword(recentCopy.getPassword());
		account.setUser_id(recentCopy.getUser_id());
		account.setRegister_status(recentCopy.getRegister_status());
		account.setFeatures(recentCopy.getFeatures());
        account.setStatus(recentCopy.getStatus());
        account.setCountry(recentCopy.getCountry());
        account.setPartnerCode(recentCopy.getPartnerCode());
	}

	class StartThread extends Thread {
		public void run() {
            if(logger.isTraceEnabled())
                logger.trace("run()");

			logger.info(String.format("starting destination instance: %s", destinationName));

			loadAccount(protocols);

			while (true) {
				//if ((System.currentTimeMillis() - startTime) > lReloadInterval || needReload)
                if ((System.currentTimeMillis() - startTime) > 1000l*60*5 || needReload) {
                    logger.info(String.format("Destination %s, time has come to reload accounts", destinationName));
					startTime = System.currentTimeMillis();
					needReload = false;
					loadAccount(protocols);
				} else {
                    logger.debug(String.format("Destination %s, no need to reload accounts just yet", destinationName));
                }

                logger.debug(String.format("Destination %s, processing accounts", destinationName));

                for (Account account : source) {
                    if (aur.containsKey(account.getName())) {
                        queue = aur.get(account.getName());

                        logger.info(String.format("%s, working on account uid=%s, we have %d queued requests", destinationName, account.getUser_id(), queue.size()));

						while (! queue.isEmpty()) {
							Account queuedAccount = queue.peek();

                            if (MacroDefine.OTHER.CONST_UPDATETASK.equals(queuedAccount.getCommand())) {
								updateAccount(account, queuedAccount);
                            }

                            if (isDeleteAccount(queuedAccount)) {
                                account.setDeletedAccountStatus();
                            }

                            if (isAddAccount(queuedAccount)) {
                                logger.info("handling new account " + queuedAccount.getName() + " " + queuedAccount.getStatus());
                                handleAddAccount(account, queuedAccount);
                            }

                            queue.poll();
						}

						aur.remove(account.getName());
					} else {
                       logger.debug(String.format("Destination %s, skipping update account for uid=%s, we have no queued requests", destinationName, account.getUser_id()));
                    }

                    if (account.isNewAccount()) {
                        logger.info("resetting new account status: " + account.getName() + " " + account.getStatus());
                        account.setStatus(null);
                    } else if (!account.isDeleted()) {
                        sendAccount(account);
                    }
                }

                logger.debug(String.format("Destination %s, now let's check to see if we have any new accounts", destinationName));

				if (aur.keySet().size() > 0) {
					for (String name : aur.keySet()) {
						logger.info("an new account:" + name);

						Account account = null;
						queue = aur.get(name);
						if (queue == null)
							continue;
                        Account queuedAccount = queue.peek();

						if (queuedAccount == null)
							continue;

						while (! queue.isEmpty()) {
							queuedAccount = queue.peek();
                            if (isAddAccount(queuedAccount)) {
                                account = queuedAccount;
                            }

                            if (isDeleteAccount(queuedAccount)) {
								account = null;
                            }

							if (MacroDefine.OTHER.CONST_UPDATETASK.equals(queuedAccount.getCommand()) && account != null) {
								updateAccount(account, queuedAccount);
                            }

							queue.poll();
						}

						if (account != null) {
                            source.add(account);

                            if (account.isNewAccount()) {
                                account.setStatus(null);
                            } else {
                                sendAccount(account);
                            }
                        }

						aur.remove(name);
					}
				}
                logger.info(String.format("Destination %s, finishing processing accounts, will sleep for %d seconds now", destinationName, loopTime));

				try {
                    int sleepingValue = loopTime * 1000;
                    if(logger.isTraceEnabled())
                        logger.trace("Sleeping for " + sleepingValue + " ms");
					Thread.sleep(sleepingValue);
				} catch (InterruptedException e) {
					logger.error("this thread had been interruped by some one, but it will continue :)");
				}
			}
		}
	}

	public void sendAccount(Account account) {
        if(logger.isDebugEnabled())
            logger.debug(String.format("sendAccount(account=%s)", String.valueOf(account)));

        long start = System.nanoTime();

        String destinationUrl = getDestinationUrl(account);

        String destinationName = getDestinationName(account);

        logger.debug(String.format("%s (%s), send accounting %s", destinationName, destinationUrl, account));

        String receiverJndi = TFConfigManager.getInstance().getJNDI_RECEIVER();

        Serializable object = FactoryTools.copyObject(account);

        try {
            logger.debug(String.format("%s sending copy of account uid=%s, name=%s to receiver url=%s, jndi=%s", destinationName,  account.getUser_id(), account.getName(), destinationUrl, receiverJndi));

            SendQueueMessageClient.getInstance(destinationName, destinationUrl, receiverJndi).send(object);

            logger.info(String.format("%s, sent copy of account uid=%s, name=%s to receiver url=%s, jndi=%s in %d millis", destinationName,  account.getUser_id(), account.getName(), destinationUrl, receiverJndi, (System.nanoTime() - start) / 1000000));
        } catch (Throwable e) {
            logger.error(String.format("%s, sending copy of account uid=%s, name=%s to receiver url=%s, jndi=%s failed", destinationName,  account.getUser_id(), account.getName(), destinationUrl, receiverJndi), e);
        }
	}


    @Override
	public void pop3CollectionClear() {
	}

	// it is never used
	@Override
	public void updateMSConfig(Server server) {}

	// it is never used
	@Override
	public void updateConfig(SysConfig sysConfig) {}

    private boolean isDeleteAccount(Account queuedAccount) {
        return MacroDefine.OTHER.CONST_DELETETASK.equals(queuedAccount.getCommand());
    }

    private void handleAddAccount(Account existingAccount, Account newAccount) {
        updateAccount(existingAccount, newAccount);
        existingAccount.setNewAccountStatus();
    }

    private boolean isAddAccount(Account queuedAccount) {
        return MacroDefine.OTHER.CONST_ADDTASK.equals(queuedAccount.getCommand());
    }

    protected String[] getDestinationUrls(String url) {
        return new String[]{url.trim()};
    }

    protected String getDestinationUrl(Account account) {
        return destinationUrls[0];
    }

    protected String getDestinationName(Account account) {
        return this.destinationName;
    }

}
