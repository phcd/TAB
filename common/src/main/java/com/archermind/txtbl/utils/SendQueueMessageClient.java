package com.archermind.txtbl.utils;


import org.jboss.logging.Logger;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.Serializable;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SendQueueMessageClient {
    private static final Logger logger = Logger.getLogger(SendQueueMessageClient.class);

    public static final String jbossConnectionFactoryJndi = "ConnectionFactory";
    private static Map<String, SendQueueMessageClient> sendQueueMessageClientCollection = new HashMap<String, SendQueueMessageClient>();
    private static final String jbossQueuePrefix = "queue/";

    private Properties contextProperties = null;
    public String jndiName = null;
    public String targetIdentifier = null;
    private Destination destination;
    private Connection connection;
    private QueueMessageSender queueMessageSender = null;

    public QueueMessageSender getQueueMessageSender() {
        return queueMessageSender;
    }

    public Properties getContextProperties() {
        return contextProperties;
    }

    public String getJndiName() {
        return jndiName;
    }

    public String getTargetIdentifier() {
        return targetIdentifier;
    }

    public Destination getDestination() {
        return destination;
    }

    public Connection getConnection() {
        return connection;
    }

    public void disconnect() throws JMSException {
        Connection connection  = this.connection;
        this.connection = null;
        if(connection != null) {
            connection.setExceptionListener(null);
            connection.close();
        }
    }

    private SendQueueMessageClient(String targetIdentifier, Properties props, String jndiName) throws Exception {
        if(logger.isTraceEnabled())
            logger.trace(String.format("SendQueueMessageClient(targetIdentifier=%s, props=%s, jndiName=%s)",
                    targetIdentifier,String.valueOf(props), jndiName));

        logger.debug(String.format("+++creating new send message client for %s, jndi=%s", targetIdentifier, jndiName));

        Context ctx = new InitialContext(props);
        ConnectionFactory connectionFactory = getConnectionFactory(ctx);
        if (connectionFactory != null) {
            this.jndiName = jndiName;
            this.targetIdentifier = targetIdentifier;
            this.connection = createConnection(connectionFactory);
            this.destination = (Destination) ctx.lookup(jbossQueuePrefix + jndiName);
            this.contextProperties = props;
            QueueMessageCollection.createNewQueue(targetIdentifier);
            queueMessageSender = new QueueMessageSender(this);
            queueMessageSender.start();
        }
    }

    private Connection createConnection(ConnectionFactory connectionFactory) throws JMSException {
        Connection connection = connectionFactory.createConnection();
        connection.setExceptionListener(new ExceptionListener() {
            public void onException(JMSException e) {
                if (e.getCause() instanceof SocketException) {
                    logger.fatal(String.format("connection has received a socket exception, will reconnect target=%s, jndi=%s", targetIdentifier, jndiName), e);
                } else {
                    logger.fatal(String.format("connection has received an exception, will reconnect target=%s, jndi=%s", targetIdentifier, jndiName), e);
                }
                try {
                    disconnect();
                } catch (JMSException e1) {
                    logger.fatal(String.format("unable to disconnect connection on connection target=%s, jndi=%s", targetIdentifier, jndiName), e);
                }
                queueMessageSender.setConnected(false);
            }

        });
        return connection;
    }

    public static ConnectionFactory getConnectionFactory(Context ctx) {
        ConnectionFactory connectionFactory;
        try {
            connectionFactory = (ConnectionFactory) ctx.lookup(jbossConnectionFactoryJndi);
            return connectionFactory;
        }
        catch (Exception e) {
            NetworkDetector.setNetworkOK(false);
            NetworkDetector.startDetect(ctx);
            logger.error("+++Network Is Not OK, Try It Later Please.+++");
            return null;
        }
    }

    public void send(Serializable obj) {
        if(logger.isTraceEnabled())
            logger.trace(String.format("send(obj=%s)",String.valueOf(obj)));

        QueueMessageCollection.addNewMessageToCollection(targetIdentifier, obj);
    }

    public static SendQueueMessageClient getInstance(String targetIdentifier, Object contextProperties, String jndiName) {
        if (logger.isTraceEnabled())
            logger.trace(String.format("getInstance(targetIdentifier=%s, jndiName=%s ...)", targetIdentifier, jndiName));

        Properties props = buildPropertiesFromObject(contextProperties);
        if(logger.isTraceEnabled())
            logger.trace("props="+String.valueOf(props));

        if (props == null) {
            throw new RuntimeException(String.format("+++Context Properties Error, Please Check It And Then Try Again! Target=%s, JNDI=%s +++", targetIdentifier, jndiName));
        }
        SendQueueMessageClient sendQueueMessageClient = getSendQueueMessageClient(targetIdentifier);
        if (sendQueueMessageClient != null) {
            if (sendQueueMessageClient.getContextProperties() != null && sendQueueMessageClient.getContextProperties().equals(props)) {
                logger.debug("+++An Instance Of SendQueueMessageClient Alreay Exist, Get It+++");
                return sendQueueMessageClient;
            } else {
                // TODO - Look into why the this.connection is not released here!!!

                logger.debug("+++Remove an instance Of SendQueueMessageClient from the pool+++");
                removeSendQueueMessageClientCollection(targetIdentifier);
                logger.debug("+++Mark QueueMessageSender instance deprecated+++");
                sendQueueMessageClient.queueMessageSender.setDeprecated(true);
            }
        }

        try {
            sendQueueMessageClient = new SendQueueMessageClient(targetIdentifier, props, jndiName);
        }
        catch (Exception e) {
            throw new RuntimeException(String.format("Unexpected error target=%s, jndi=%s, error=%s", targetIdentifier, jndiName, e.getMessage()), e);
        }
        if (sendQueueMessageClient.connection == null) {


            throw new RuntimeException(String.format("Queue message client has lost its connection, target=%s, jndi=%s, props=%s", targetIdentifier, jndiName, contextProperties));
        } else {
            setSendQueueMessageClient(targetIdentifier, sendQueueMessageClient);
            logger.debug("+++ Add one SendQueueMessageClient instance to the pool[" + sendQueueMessageClientCollection.size() + "]+++");
            return sendQueueMessageClient;
        }
    }

    public void reconnect() {

        Context ctx = null;

        try {

            ctx = new InitialContext(contextProperties);

            ConnectionFactory connectionFactory = getConnectionFactory(ctx);

            if (connectionFactory != null) {
                this.connection = createConnection(connectionFactory);
                this.destination = (Destination) ctx.lookup(jbossQueuePrefix + jndiName);
            }


        } catch (Exception e) {

            NetworkDetector.setNetworkOK(false);
            NetworkDetector.startDetect(ctx);
            logger.error("+++Network Is Not OK, Try It Later Please.+++");

        }


    }

    /**

     */
    private static Properties buildPropertiesFromObject(Object contextProperties) {
        logger.debug("+++Begin to build Properties from object+++");
        Properties props = new Properties();


        String tmpUrl = null;

        if (contextProperties instanceof String) {
            tmpUrl = (String) contextProperties;
        } else if (contextProperties instanceof Properties) {
            Properties tmpProps = (Properties) contextProperties;
            tmpUrl = tmpProps.getProperty(Context.PROVIDER_URL);
        }
        if (tmpUrl == null) {
            logger.debug("+++Build Context Properties Fail!+++");
            return null;
        }
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        props.put(Context.PROVIDER_URL, tmpUrl);
        props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
        logger.debug("+++After build the properties:" + props + "+++");
        return props;
    }

    public void destroy() {
        removeSendQueueMessageClientCollection(targetIdentifier);

        queueMessageSender.setDeprecated(true);

        logger.info("Instance of SendQueueMessageClient with ID " + targetIdentifier + " Destroyed!");
    }

    public static boolean hasSendQueueMessageClientCollection(String targetIdentifier) {
        return sendQueueMessageClientCollection.containsKey(targetIdentifier);
    }

    public static void removeSendQueueMessageClientCollection(String targetIdentifier) {
        SendQueueMessageClient sendQueueMessageClient = sendQueueMessageClientCollection.remove(targetIdentifier);
        if(sendQueueMessageClient != null) {
            try {
                sendQueueMessageClient.disconnect();
            } catch (JMSException e) {
                logger.error("Trying to close connection for " + targetIdentifier, e);
            }
        }

    }

    public static SendQueueMessageClient getSendQueueMessageClient(String targetIdentifier) {
        return sendQueueMessageClientCollection.get(targetIdentifier);
    }

    public static void setSendQueueMessageClient(String targetIdentifier, SendQueueMessageClient sqmc) {
        sendQueueMessageClientCollection.put(targetIdentifier, sqmc);
    }
}