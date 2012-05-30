package com.archermind.txtbl.taskfactory.web;

import com.archermind.txtbl.taskfactory.TaskFactoryEngineImp;
import org.jboss.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class InitContext implements ServletContextListener
{
    private static Logger logger = Logger.getLogger(InitContext.class);

    private MyThread myThread = new MyThread();

    public void contextDestroyed(ServletContextEvent arg0)
    {

    }

    class MyThread extends Thread
    {
        public void run()
        {
            logger.info("web initialization hook - my thread is starting");

            try
            {
                TaskFactoryEngineImp.getInstance().start();
            }
            catch (Throwable t)
            {
                logger.fatal("web initialization hook - my thread is dying", t); 
            }
        }
    }

    public void contextInitialized(ServletContextEvent arg0)
    {
        logger.info("web initialization hook - starting taskfactory");
        myThread.start();
        logger.info("web initialization hook - started taskfactory");

    }

}
