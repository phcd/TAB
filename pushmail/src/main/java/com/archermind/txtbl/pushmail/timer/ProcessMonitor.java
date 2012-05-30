package com.archermind.txtbl.pushmail.timer;

import com.archermind.txtbl.utils.StringUtils;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;

import java.util.Timer;

public class ProcessMonitor
{
    private static Logger log = Logger.getLogger(ProcessMonitor.class);

    private static Timer timer = null;

    public void start()
    {
        // TODO Auto-generated method stub
        String intervalForTimer = SysConfigManager.instance().getValue("intervalForTimer");

        String intervalForRetry = SysConfigManager.instance().getValue("intervalForRetry");

        log.info(String.format("starting mapping and topic timers, timer(millis)=%s, retry(seconds)=%s", intervalForTimer, intervalForRetry));

        if (StringUtils.isEmpty(intervalForTimer) || !StringUtils.isNumeric(intervalForTimer))
        {
            log.error("intervalForTimer in milliseconds configuration property is missing or not valid: " + intervalForTimer + ", defaulting to 240000");
            intervalForTimer = "240000";
        }

        if (StringUtils.isEmpty(intervalForRetry) || !StringUtils.isNumeric(intervalForRetry))
        {
            log.error("intervalForRetry in seconds configuration property is missing or not valid: " + intervalForRetry + ", defaulting to 60");
            intervalForRetry = "60";
        }

        long interval = Long.parseLong(intervalForTimer);

        long retry = Long.parseLong(intervalForRetry);

        timer.schedule(new Timer4MappingImpl(), 0, interval);

        timer.schedule(new Timer4TopicImpl(), 0, retry * 1000);

    }

    public ProcessMonitor()
    {
        timer = new Timer(true);

    }


}
