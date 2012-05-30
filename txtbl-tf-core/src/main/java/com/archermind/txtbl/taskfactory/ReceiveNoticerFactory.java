package com.archermind.txtbl.taskfactory;

import com.archermind.txtbl.taskfactory.common.FactoryTools;
import com.archermind.txtbl.taskfactory.common.TFConfigManager;
import com.archermind.txtbl.taskfactory.loopnotice.LoopNoticer;
import com.archermind.txtbl.taskfactory.subscribe.SubscribeNoticer;
import org.jboss.logging.Logger;

public class ReceiveNoticerFactory
{
	private static Logger logger = Logger.getLogger(ReceiveNoticerFactory.class);

	public static ReceiveNoticer create(String type)
    {
        if(logger.isTraceEnabled())
            logger.trace(String.format("create(type=%s)", type ));
        
		if (type.equals("loop"))
        {
			LoopNoticer loopNoticer = new LoopNoticer();
			loopNoticer.setDestinationGroup(TFConfigManager.getInstance().getDestinationGroup());
			loopNoticer.setGroupOfProtocols(TFConfigManager.getInstance().getGroupOfProtocols());
			loopNoticer.setAllProtocols(TFConfigManager.getInstance().getAllProtocols());
			loopNoticer.setSubscribeArray(FactoryTools.getSubscribeArray());
			return loopNoticer;
		}
        else if (type.equals("subscribe"))
        {
			SubscribeNoticer subscribeNoticer = new SubscribeNoticer();
			String[] SubscribeArray = FactoryTools.getSubscribeArray();
			subscribeNoticer.setSubscribeArray(SubscribeArray);
			return subscribeNoticer;
		}
        else
        {
			logger.error("there is no this type [" + type + "]!");

            return null;
		}

	}


}
