package com.archermind.txtbl.taskfactory.strategys;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.archermind.txtbl.taskfactory.TaskFactoryEngineImp;
import com.archermind.txtbl.taskfactory.common.TFConfigManager;
import org.jboss.logging.Logger;

public class Pop3CollectionTimer
{
	private static Logger logger = Logger.getLogger(Pop3CollectionTimer.class);
	private Timer timer;
	private TaskFactoryEngineImp tf;
	private int pop3CollectionClearTime = 0;

	public Pop3CollectionTimer(TaskFactoryEngineImp tf)
    {
		timer = new Timer();
		pop3CollectionClearTime = TFConfigManager.getInstance().getPop3CollectionClearTime();
		this.tf = tf;
	}

	public void startTimer()
    {
		logger.info("start pop3 collection timer!!!");
		Calendar startTime = Calendar.getInstance();
		startTime.setTime(new Date());
		int correntHour = startTime.get(Calendar.HOUR_OF_DAY);
		startTime.set(Calendar.HOUR_OF_DAY, correntHour + 1);
		startTime.set(Calendar.MINUTE, 0);
		startTime.set(Calendar.SECOND, 0);
		startTime.set(Calendar.MILLISECOND, 0);
		logger.info(startTime.getTime());
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				logger.info("run");
				doAction();
			}
		}, startTime.getTime(), 60 * 60 * 1000);
	}

	private void doAction() {
		Calendar currentTime = Calendar.getInstance();
		currentTime.setTime(new Date());
		if (currentTime.get(Calendar.HOUR_OF_DAY) == pop3CollectionClearTime) {
			tf.pop3CollectionClear();
		}
	}

}
