package com.archermind.txtbl.utils;

import org.jboss.logging.Logger;

public class StopWatchUtils {
    public static void newTask(StopWatch watch, String task) {
        if (watch.isRunning()) {
            watch.stop();
        }
        
        watch.start(task);
    }

    public static void newTask(StopWatch watch, String task, String context, Logger log) {
        log.debug(String.format("newTask: %s, for %s", task, context));
        newTask(watch, task);
    }

}
