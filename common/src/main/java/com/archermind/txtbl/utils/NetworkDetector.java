package com.archermind.txtbl.utils;


import org.jboss.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingException;

public class NetworkDetector {
    private static final Logger logger = Logger.getLogger(NetworkDetector.class);

	public static final int networkDetectTimeInterval = 1000 * 60;
	private static boolean isNetworkOK = true;
	private static boolean isDetectingNow = false;
	private static boolean preventChecking = false;

	private NetworkDetector() {
		logger.info("+++No One Could Create Instance Of Mine Except My G-O-D ZhangJianPing!+++");
	}

	public static boolean isDetectingNow() {
		return isDetectingNow;
	}

	public static boolean isNetworkOK() {
		return isNetworkOK;
	}

	public static void setNetworkOK(boolean networkOK) {
		isNetworkOK = networkOK;
	}

	public static void startDetect(Context ctx) {
		if (isDetectingNow) {
			logger.info("+++Take It Easy, Already Do Network Checking Now, But I Will ReCheck It For You!+++");
			stopDetect();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ignored) {
				logger.info("+++Who interrupt me?+++");
			}
		}
		logger.info("+++Begin To Check Network Now!+++");
		isDetectingNow = true;
		preventChecking = false;
		isNetworkOK = false;
		StartDetect startDetect = new StartDetect(ctx);
		startDetect.start();
	}

	public static void stopDetect() {
		logger.info("+++Now Prevent Network Detection From Executing+++");
		StopDetect stopDetect = new StopDetect();
		stopDetect.start();
	}

	private static class StartDetect extends Thread {
		private Context ctx = null;

		public StartDetect(Context ctx) {
			logger.info("+++New Instance Of StartDetect Created+++");
			this.ctx = ctx;
		}

		@Override
		public void run() {
			while (!preventChecking) {
				try {
                    if(logger.isTraceEnabled())
                        logger.trace("lookup "+ SendQueueMessageClient.jbossConnectionFactoryJndi);
					ctx.lookup(SendQueueMessageClient.jbossConnectionFactoryJndi);
					logger.info("+++Congratulations! Network Is OK Now!+++");
					isDetectingNow = false;
					isNetworkOK = true;
					break;
				} catch (NamingException e) {
					logger.info("+++Network Not OK Now, Will Do Detect Again "+NetworkDetector.networkDetectTimeInterval+" MM Later!+++");
					try {
						Thread.sleep(networkDetectTimeInterval);//
					} catch (InterruptedException e1) {
						logger.info("+++Why You Interrupt Me When I Sleeping?+++");
					}
				}
			}
			logger.info("+++Nework Detecting Finished!+++");
		}
	}

	private static class StopDetect extends Thread 	{
		@Override
		public void run() {
			preventChecking = true;
			logger.info("+++Network Checking Will Stop Soon!+++");
		}
	}
}