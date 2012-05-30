package com.archermind.txtbl.receiver.mail.mdb;

import EDU.oswego.cs.dl.util.concurrent.FutureResult;
import EDU.oswego.cs.dl.util.concurrent.TimeoutException;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.receiver.mail.abst.impl.NewImapIdleProvider;
import com.archermind.txtbl.receiver.mail.config.ReceiverConfig;
import com.archermind.txtbl.receiver.mail.support.XobniFirstTimeHandler;
import com.archermind.txtbl.receiver.mail.threadpool.TaskCallable;
import com.archermind.txtbl.utils.SysConfigManager;
import com.archermind.txtbl.utils.UtilsTools;
import org.apache.commons.lang.StringUtils;
import org.jboss.annotation.ejb.PoolClass;
import org.jboss.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "150"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/receivemail")})
@TransactionManagement(TransactionManagementType.CONTAINER)
@PoolClass(value = org.jboss.ejb3.StrictMaxPool.class, maxSize = 150, timeout = 60000)
public class ReceiveMailMDB implements MessageListener
{

    private static final Logger log = Logger.getLogger(ReceiveMailMDB.class);
    private static final Logger sysLog = Logger.getLogger("SystemMonitor");

    private static long timeout = 1L;

    private static final long activeSyncTimeout = Long.parseLong(SysConfigManager.instance().getValue("activeSyncMDBTimeout", "540000"));

    private static long idleMessagePause = 0l;

    static
    {
        new ReceiverConfig(false);

        String shortTimeout = ReceiverConfig.getProp("shortTimeOut");

        timeout = Long.parseLong(shortTimeout) * 1000l;



        String idlePause = SysConfigManager.instance().getValue("idleMessagePause");

        if (StringUtils.isNotEmpty(idlePause) && StringUtils.isNumeric(idlePause)) {
            idleMessagePause = Long.parseLong(idlePause);
        }
    }

    /**
     * @param msg
     * @see Message Drive Bean
     */
    public void onMessage(Message msg)
    {
        if(log.isDebugEnabled())
            log.debug(String.format("onMessage(msg=%s)", String.valueOf(msg)));

        Account account = null;

        boolean isIdling = false;

        Thread thread = null;

        try
        {
            account = (Account) ((ObjectMessage) msg).getObject();

            isIdling = account.isIdleAccount();

            if (account.isDeleted())
            {
                if(isIdling) {
                    log.info("Deleting imap idle account " + account.getName());
                    NewImapIdleProvider.removeImapIdleProcess(account);
                } else {
                    log.info("Deleting from xobni initial fetch account " + account.getName());
                    XobniFirstTimeHandler.INSTANCE.removeFromXobniInitialFetchMap(account);
                }
                return;
            }

            log.info("[start] [" + account.getName() + "] [uid=" + account.getUser_id() + "] [registered=" + account.getRegister_time() + "] [key=" + account.getKey_id() + "] [status=" + account.getRegister_status() + "] [thread=" + Thread.currentThread().getName() + "] [idling=" + isIdling + "]");

            TaskCallable taskCallable = new TaskCallable(account);

            FutureResult futureResult = new FutureResult();

            thread = new Thread(futureResult.setter(taskCallable));

            thread.start();

            if (isIdling)
            {
                if (idleMessagePause > 0)
                {
                    //log.info(String.format("pausing for %s ms., account=%s, uid=%s", idleMessagePause, account.getName(), account.getUser_id()));

                    //DM turning off idle pause
                    //Thread.currentThread().sleep(idleMessagePause);
                }
            }
            else if ("activesync".equals(account.getReceiveProtocolType()))
            {
                if (log.isDebugEnabled())
                {
                    log.debug(String.format("we have an active sync protoocol for %s %s will setup mdb timeout at %s", account.getId(), account.getName(), activeSyncTimeout));
                }

                futureResult.timedGet(activeSyncTimeout);
            }
            else if (!"1".equals(account.getRegister_status()))
            {
                // no idea what this is. old stuff from archermind.
                futureResult.timedGet(timeout);
            }
        }
        catch (TimeoutException e)
        {
            log.info("[receiver timed out after " + timeout + " millis] [close] [" + account.getName() + "] [uid=" + account.getUser_id() + "] [key=" + account.getKey_id() + "] [status=" + account.getRegister_status() + "] [thread=" + Thread.currentThread().getName() + "]");

            thread.interrupt();
        }
        catch (Throwable e)
        {
            log.error("Unexpected error while receiving messages: [" + (account != null ? account.getName() : "null account") + "]", e);
        }
        finally
        {
            if (!isIdling)
            {
                log.info("[close] [" + account.getName() + "] [uid=" + account.getUser_id() + "] [key=" + account.getKey_id() + "] [status=" + account.getRegister_status() + "] [protocol=" + account.getReceiveProtocolType() + "] [thread=" + Thread.currentThread().getName() + "]");
            }

            UtilsTools.logSystemStats(sysLog);
        }
    }
}
