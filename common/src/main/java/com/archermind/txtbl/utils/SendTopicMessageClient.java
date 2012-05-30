package com.archermind.txtbl.utils;


import org.jboss.logging.Logger;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SendTopicMessageClient {
    private static final Logger logger = Logger.getLogger(SendTopicMessageClient.class);

    // save destinations and sendMessageClient2s bound to the destinations
    private static Map<String, SendTopicMessageClient> destinationSendClientMapping = new HashMap<String, SendTopicMessageClient>();

    private final BlockingQueue<Serializable> messageQueue = new LinkedBlockingQueue<Serializable>();

    private Destination destination;
    private Connection connection;
    private Properties properties;
    private String jndi;
    private String providerURL;

    // main method of this class to send message

    public void send(Serializable obj) throws Exception {
        messageQueue.offer(obj);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("added %s to be sent to %s %s, %d messages pending", obj, jndi, providerURL, messageQueue.size()));
        }
    }

    // the unique constructor

    public SendTopicMessageClient(Properties props, String jndi) throws Exception {
        // init the connection
        Context ctx = new InitialContext(props);
        ConnectionFactory cf = (ConnectionFactory) ctx.lookup("ConnectionFactory");
        this.connection = cf.createConnection();

        this.jndi = jndi;
        this.destination = (Destination) ctx.lookup("topic/" + jndi);
        this.properties = props;

        // begin to detect the queue and send the content in the queue.
        new Thread(new InnerThread(this.messageQueue, this)).start();
    }

    // get instance of SendMessageClient2 by identifier , server url and queue

    public static SendTopicMessageClient getInstance(String destination, Object params, String jndi) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("getInstance(destination=%s, params=..., jndi=%s)", destination, jndi));

        String providerURL;

        if (params instanceof String) {
            providerURL = (String) params;
        } else {
            // in order to ensure the properties passed in is right, so replace it with my own.
            Properties tmpProps = (Properties) params;
            providerURL = tmpProps.getProperty(Context.PROVIDER_URL);
        }

        if (providerURL == null) {
            throw new RuntimeException("the value of Context.PROVIDER_URL is null!");
        }
        logger.info(String.format("creating new send topic message client for jndi=%s and to provider URL %s", jndi, providerURL));


        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        props.put(Context.PROVIDER_URL, providerURL);
        props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");

        // check if the instance of SendMessageClient2 already exist.
        SendTopicMessageClient sendClient = destinationSendClientMapping.get(destination);

        if (sendClient != null) {
            if (sendClient.properties != null && sendClient.properties.equals(props) && sendClient.jndi.equalsIgnoreCase(jndi)) {
                logger.info(String.format("using existing send topic message client configuration for jndi=%s", jndi));

                return sendClient;
            }
        }

        // otherwise create new one and save it to the instance map.
        logger.info(String.format("creating new send topic message client for jndi=%s", jndi));

        SendTopicMessageClient newSendClient = new SendTopicMessageClient(props, jndi);

        newSendClient.setProviderURL(providerURL);

        destinationSendClientMapping.put(destination, newSendClient);

        return newSendClient;
    }

    // re-connect after 5 minutes if exception occur.

    private static class TxtblExceptionListener implements ExceptionListener {
        private SendTopicMessageClient sendClient;
        private Serializable message;
        private BlockingQueue<Serializable> queue;

        public TxtblExceptionListener(SendTopicMessageClient sendClient, Serializable message, BlockingQueue<Serializable> queue) {
            this.sendClient = sendClient;
            this.message = message;
            this.queue = queue;
        }

        public void onException(JMSException jmsException) {
            try {

                logger.warn(String.format("connectivity issues in %s, %s, pausing for 5 minutes and then we will try again", sendClient.jndi, jmsException.toString()));

                Thread.sleep(1000 * 60 * 5);
                try {
                    // release the resource and unregisters the ExceptionListener
                    sendClient.connection.close();
                }
                catch (Exception ex) {
                    logger.error(ex);
                }

                Context ctx = new InitialContext(sendClient.properties);

                ConnectionFactory cf = (ConnectionFactory) ctx.lookup("ConnectionFactory");

                sendClient.connection = cf.createConnection();

                queue.offer(message);
            }
            catch (Throwable e) {
                logger.error(String.format("Connection exception listener unable to reconnect to %s on %s", sendClient.jndi, sendClient.properties.get(Context.PROVIDER_URL)), e);
            }
        }
    }

    private class InnerThread implements Runnable {

        private BlockingQueue<Serializable> blockingQueue = null;
        private SendTopicMessageClient sendClient;

        public InnerThread(BlockingQueue<Serializable> blockingQueue, SendTopicMessageClient sendClient) {
            this.blockingQueue = blockingQueue;
            this.sendClient = sendClient;
        }

        public void run() {
            while (true) {
                logger.info(String.format("send topic client is running for %s on %s", sendClient.jndi, sendClient.properties.get(Context.PROVIDER_URL)));

                Serializable objectToSend;

                try {
                    try {
                        objectToSend = blockingQueue.take();
                    }
                    catch (InterruptedException e) {
                        continue;
                    }

                    connection.setExceptionListener(new TxtblExceptionListener(sendClient, objectToSend, blockingQueue));

                    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                    MessageProducer messageProducer = session.createProducer(destination);

                    ObjectMessage objectMessage = session.createObjectMessage(objectToSend);

                    messageProducer.send(objectMessage);

                    logger.info(String.format("message sent to %s at %s", sendClient.jndi, sendClient.properties.get(Context.PROVIDER_URL)));

                    session.close();
                }
                catch (JMSException e) {
                    logger.error(String.format("Error while trying to send a message to %s at %s", destination, providerURL), e);
                }
            }
        }
    }

    /**
     *
     */
    private void reconnect() {
        while (true) {
            logger.info(String.format("attempting to reconnect to %s at %s", destination, providerURL));

            try {
                Context ctx = new InitialContext(properties);
                ConnectionFactory cf = (ConnectionFactory) ctx.lookup("ConnectionFactory");
                this.connection = cf.createConnection();

                return;
            }
            catch (Throwable t) {
                logger.error(String.format("error occurred while trying to reconnect to %s at %s", destination, providerURL), t);
            }

            try {
                logger.info(String.format("will sleep for 10 seconds and try connecting again to %s at %s", destination, providerURL));

                Thread.sleep(1000l * 10);
            }
            catch (InterruptedException e) {

            }
        }

    }


    public void setProviderURL(String providerURL) {
        this.providerURL = providerURL;
    }
}
