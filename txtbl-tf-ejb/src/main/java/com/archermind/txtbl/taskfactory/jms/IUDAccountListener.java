package com.archermind.txtbl.taskfactory.jms;

import com.archermind.txtbl.dal.business.impl.EmailServerService;
import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Server;
import com.archermind.txtbl.taskfactory.TaskFactoryEngineImp;
import com.archermind.txtbl.taskfactory.common.MacroDefine;
import com.archermind.txtbl.utils.UtilsTools;
import org.jboss.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

@MessageDriven(activationConfig = {@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"), @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/" + MacroDefine.JNDI.CONST_IDUACCOUNT_JNDI + "")})
public class IUDAccountListener implements MessageListener
{

    private static final Logger logger = Logger.getLogger(IUDAccountListener.class);

    public void onMessage(Message msg)
    {
        try {
            Account account;
            
            if(msg instanceof TextMessage) {
                String accountId = ((TextMessage) msg).getText();
                try {
                    logger.info("adding account with id: " + accountId);
                    account = new UserService().getAccount(Integer.valueOf(accountId));
                    if(account == null) {
                        logger.error("Unable to find account with id: " + accountId);
                        return;
                    }
                    account.setCommand(MacroDefine.OTHER.CONST_ADDTASK);
                } catch (Exception e) {
                    logger.error("Error while adding account " + accountId, e);
                    return;
                }
            } else {
                account = (Account) ((ObjectMessage) msg).getObject();
            }

            String command = account.getCommand();

            logger.info(String.format("task factory received a message from web module: command=%s, account=%s", command, account));

            Server server = null;

            Server proposedServer = new EmailServerService().getServersbyId(account.getServer_id() + "");

            if (proposedServer == null)
            {
                logger.fatal("unable to find a server for new registration with id=" + account.getServer_id() + ", account=" + account);
                return;
            }

            if (server == null)
            {
                server = proposedServer;
            }

            UtilsTools.mapReceiveServerDetails(account, server);

            logger.info(String.format("sending account=%s, email=%s to receiving server %s[%s,%s]", account.getUser_id(), account.getName(), server.getId(), server.getReceiveProtocolType(), server.getReceiveHost()));

            TaskFactoryEngineImp.getInstance().updateAccountAndSend(account);

        } catch (Throwable e) {
            logger.error("Receive IDU message failed ", e);
        }
    }

}
