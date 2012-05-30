package com.archermind.txtbl.utils;


import org.jboss.logging.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueMessageCollection {
	private static final Logger logger = Logger.getLogger(QueueMessageCollection.class);
	private static Map<String, BlockingQueue<Serializable>> msgCollection = new HashMap<String, BlockingQueue<Serializable>>();

	public static void createNewQueue(String targetIdentifier) {
		if (msgCollection.get(targetIdentifier) == null) {
			BlockingQueue<Serializable> messageQueue = new LinkedBlockingQueue<Serializable>();
			msgCollection.put(targetIdentifier, messageQueue);
			logger.debug("+++New Queue With Identifier "+targetIdentifier+" Created+++");
			return;
		}
		logger.debug("+++Queue With Identifier "+targetIdentifier+" Already Exist+++");
	}

	@SuppressWarnings("unchecked")
	public static void addNewMessageToCollection(String targetIdentifier,Serializable msg) {
		logger.debug("+++Begin to add new message to the QueueMessageCollection with id:"+targetIdentifier+"+++");
		if(msgCollection.get(targetIdentifier)==null) {
			logger.debug("+++There is no message queue with id "+targetIdentifier+", create new one+++");
			createNewQueue(targetIdentifier);
		}
		msgCollection.get(targetIdentifier).offer(msg);
		logger.debug("+++Add New Message With Identifier "+targetIdentifier+" Success+++");
	}

	public static Serializable getOneMessageToSend(String targetIdentifier) throws Exception {
		logger.debug("+++Try to get one message from the queue fro sending+++");
		return msgCollection.get(targetIdentifier).take();
	}
}