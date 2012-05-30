package com.archermind.txtbl.utils;


import org.jboss.logging.Logger;

import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.Context;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QueueMessageSender extends Thread {
    private static final Logger logger = Logger.getLogger(QueueMessageSender.class);

    private boolean isDeprecated = false;

    private SendQueueMessageClient sendQueueMessageClient = null;

    private boolean isConnected = true;

    private static List<String> sendingList = new ArrayList<String>();

    public QueueMessageSender(SendQueueMessageClient sendQueueMessageClient) {
        this.sendQueueMessageClient = sendQueueMessageClient;
    }

    private void reconnect(){
        sendQueueMessageClient.reconnect();
    }

    @Override
    public void run() {
        logger.debug(String.format("+++message sender is starting, target identifier %s", sendQueueMessageClient.getTargetIdentifier()));

        if (sendingList.contains(sendQueueMessageClient.getTargetIdentifier())) {
            logger.debug(String.format("+++sending list already contains %s, sender will not be started", sendQueueMessageClient.getTargetIdentifier()));
            return;
        }

        sendingList.add(sendQueueMessageClient.getTargetIdentifier());

        while (!isDeprecated) {

            if(!isConnected) {
                reconnect();
                isConnected=true;
            }

            Serializable msg;

            try
            {
                logger.debug("+++pulling a message to send from target identifier " + sendQueueMessageClient.getTargetIdentifier());

                msg = QueueMessageCollection.getOneMessageToSend(sendQueueMessageClient.getTargetIdentifier());

            }
            catch (Exception e)
            {
                logger.fatal("+++unable to get message to send for target identifier " + sendQueueMessageClient.getTargetIdentifier(), e);
                continue;
            }

            try
            {
                logger.debug("+++sending jms message");

                Session session = sendQueueMessageClient.getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);

                MessageProducer messageProducer = session.createProducer(sendQueueMessageClient.getDestination());

                ObjectMessage objectMessage = session.createObjectMessage(msg);

                if (isDeprecated)
                {
                    logger.debug("+++SendQueueMessageClient instance was deprecated, drop sending and put message back on the queue");

                    QueueMessageCollection.addNewMessageToCollection(sendQueueMessageClient.getTargetIdentifier(), msg);

                    break;
                }

                messageProducer.send(objectMessage);

                session.close();

                logger.debug(String.format("+++sent jms message to %s succesfully", sendQueueMessageClient.getContextProperties().getProperty(Context.PROVIDER_URL)));

                if (NetworkDetector.isDetectingNow())
                {
                    NetworkDetector.stopDetect();
                }
            }
            catch (Exception e)
            {
                logger.debug("+++send message failed due to [" + e.getMessage() + "]");

                while (!isDeprecated)
                {
                    if (SendQueueMessageClient.hasSendQueueMessageClientCollection(sendQueueMessageClient.getTargetIdentifier()))
                    {
                        logger.debug("+++destroying SendQueueMessageClient instance and marking its QueueMessageSender instance deprecated, target identifier " + sendQueueMessageClient.getTargetIdentifier());

                        SendQueueMessageClient.removeSendQueueMessageClientCollection(sendQueueMessageClient.getTargetIdentifier());

                        sendQueueMessageClient.getQueueMessageSender().setDeprecated(true);
                    }

                    if (sendingList.contains(sendQueueMessageClient.getTargetIdentifier()))
                    {
                        sendingList.remove(sendQueueMessageClient.getTargetIdentifier());
                    }

                    if (NetworkDetector.isNetworkOK())
                    {
                        logger.debug("+++Network seems OK, Rebuild SendQueueMessageClient Instance Now!");

                        try
                        {
                            SendQueueMessageClient sqmc = SendQueueMessageClient.getInstance(
                                    sendQueueMessageClient.getTargetIdentifier(),
                                    SendQueueMessageClient.getSendQueueMessageClient(sendQueueMessageClient.getTargetIdentifier()).getContextProperties(),
                                    sendQueueMessageClient.getJndiName());

                            sqmc.send(msg);

                            SendQueueMessageClient.setSendQueueMessageClient(sendQueueMessageClient.getTargetIdentifier(), sqmc);

                            logger.debug("+++Add new instance of SendQueueMessagClient to the pool!+++");

                            logger.debug("+++Network Maybe OK Now, The Failed Message Pulled Back!+++");

                            break;
                        }
                        catch (RuntimeException ignored)
                        {
                        }
                    }
                    try
                    {
                        Thread.sleep(NetworkDetector.networkDetectTimeInterval);
                        logger.debug("+++Network Not OK Now, Will Try To Send Again " + NetworkDetector.networkDetectTimeInterval + " MM Later!");
                    }
                    catch (InterruptedException e2)
                    {
                        //
                    }
                }
            }
        }

        sendingList.remove(sendQueueMessageClient.getTargetIdentifier());

        logger.debug(String.format("+++message sender %s has died", sendQueueMessageClient.getTargetIdentifier()));
    }

    public void setDeprecated(boolean deprecated)
    {
        isDeprecated = deprecated;
        sendingList.remove(sendQueueMessageClient.getTargetIdentifier());
    }

    public void setConnected(boolean connected){
        isConnected=connected;
    }
}