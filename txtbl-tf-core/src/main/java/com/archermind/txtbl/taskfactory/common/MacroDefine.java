package com.archermind.txtbl.taskfactory.common;

public interface MacroDefine {

	public interface JNDI {

		public final static String CONST_IDUACCOUNT_JNDI = "iduaccount";

		public final static String CONST_MAILSERVERCONFIG_JNDI = "mailserver";

		public final static String CONST_RELOADTASKFACTORYCONFIG_JNDI = "reloadtaskfactoryconfig";
	}

	public interface OTHER {
        //TODO: Paul - consolidate constants in JmsNotifyUtils
		public final static String CONST_ADDTASK = "0";

		public final static String CONST_UPDATETASK = "1";

		public final static String CONST_DELETETASK = "2";

	}
	
	public interface SYSCONFIG{
		public final static String CONST_GROUP_ID_Prefix = "destination.target.group";
		public final static String CONST_GROUP_ID = "destination.protocol.group";
		public final static String CONST_TASKFACTORY_SUBSCRIBE_GROUP="taskfactory.subscribe.protocol";
		public final static String CONST_TASKFACTORY_PROVIDER_URL_NAME = "notify.provider.url";
		public final static String CONST_DEFAULT_DESTINATION_NAME = "default";
		public final static String CONST_WEB_RELOAD_URL_KEY = "reload.web.config.host";
		public final static String CONST_WEB_RELOAD_JNDI_KEY = "reload.web.config.jndi";
		public final static String CONST_JNDI_RECEIVER_KEY = "recivemail-jndi";
		public final static String CONST_TASKFACTORY_ACTIVE_KEY = "keytaskfactory";
		public final static String CONST_SUBSCRIBE_TIMEOUT_KEY = "taskfactory.subscribe.timeout";
		public final static String CONST_Pop3_Collection_Clear_Time = "taskfactory.pop3CollectionClearTime";
	}
}
