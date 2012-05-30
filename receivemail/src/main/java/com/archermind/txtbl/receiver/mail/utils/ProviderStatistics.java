package com.archermind.txtbl.receiver.mail.utils;

import java.util.Map;
import java.util.HashMap;
import java.text.DecimalFormat;

public class ProviderStatistics
{
    private Map<String, Stats> map = new HashMap<String, Stats>();

    public synchronized String enterStats(String provider, int depth, int newMessages, long time)
    {
        Stats stats = map.get(provider);

        if (stats == null)
        {
            stats = new Stats();

            map.put(provider, stats);
        }

        stats.update(depth, newMessages, time);

        StringBuilder summary = new StringBuilder();

        summary.append(String.format("average-mail-check=%s", stats.getAverageMailcheckTime()));
        summary.append(",");
        summary.append(String.format("average-depth=%s", stats.getAverageDepth()));
        summary.append(",");
        summary.append(String.format("average-new-mails=%s", new DecimalFormat("#.#####").format(stats.getAverageNewMails())));

        return summary.toString();
    }

    private class Stats
    {
        private int totalDepth = 0;
        private int totalNew = 0;
        private long totalTimeToProcess = 0;
        private int totalMailchecks = 0;

        public double getAverageNewMails()
        {
            return totalNew * 1d / totalMailchecks;
        }

        public long getAverageMailcheckTime()
        {
            return totalTimeToProcess / totalMailchecks; 
        }

        public int getAverageDepth()
        {
            return totalDepth / totalMailchecks;
        }

        public int getTotalDepth()
        {
            return totalDepth;
        }

        public int getTotalNew()
        {
            return totalNew;
        }

        public long getTotalTimeToProcess()
        {
            return totalTimeToProcess;
        }

        public int getTotalMailchecks()
        {
            return totalMailchecks;
        }

        public void update(int depth, int newMessages, long time)
        {
            this.totalMailchecks++;
            this.totalNew += newMessages;
            this.totalDepth += depth;
            this.totalTimeToProcess += time;            
        }



    }
}
