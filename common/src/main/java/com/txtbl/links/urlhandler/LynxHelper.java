package com.txtbl.links.urlhandler;


import com.archermind.txtbl.utils.StopWatch;
import com.archermind.txtbl.utils.StopWatchUtils;
import com.archermind.txtbl.utils.SysConfigManager;
import com.archermind.txtbl.utils.UtilsTools;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;

import java.util.concurrent.*;
import java.util.regex.Pattern;


public class LynxHelper
{
    private static final Logger log = Logger.getLogger(JPGUrlHandler.class);

    private static final String LYNX_EXE = "LYNX_EXE";
    public static final String LYNK = "\\[\\d+\\]";

    // TODO - see getAuthString
    private static String nyTimesUsername = "peek";
    private static String nyTimesPassword = "lex10017";

    private static final int LYNX_WIDTH = Integer.valueOf(SysConfigManager.instance().getValue("lynxWidth", "2000"));

    private static final Pattern END_LINES = Pattern.compile(".*[.?!,:;'\"]$");
    private static final Pattern LYNX_LINK = Pattern.compile("((\\+)|(\\*)|(\\d+\\.)|(IFRAME:)|(o)) (\\[.*\\]).*");
    private static final Pattern LYNX_SIMPLE_LINK = Pattern.compile("(\\[.*\\]).*");
    private static final Pattern LYNX_REFERENCE = Pattern.compile("(\\d+\\.) ((javascript:)|(http://)|(ftp://)|(https://)|(file://)).*");

    private static long timeout;

    static
    {
        timeout = Long.parseLong(SysConfigManager.instance().getValue("lynxTimeout", "10000"));
    }

    /**
     * Runs "lynx -dump" against the target URL.
     *
     * @param targetUrl
     * @return
     */
    public static String dumpUrl(final String targetUrl)
    {

        final StopWatch watch = new StopWatch("running lynx command - timeout set to: " + timeout);

        StopWatchUtils.newTask(watch, "init executor", targetUrl, log);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<String> future = executor.submit(new Callable<String>()
        {
            public String call() throws Exception
            {
                try
                {
                    return doDump(targetUrl, watch);
                }
                catch (Throwable e)
                {
                    log.warn("lynx command failed: ", e);
                }

                return null;
            }
        });

        String result = null;

        try
        {
            StopWatchUtils.newTask(watch, "future.get", targetUrl, log);

            result = future.get(timeout, TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException e)
        {
            log.warn(String.format("timed out while running lynx command for url %s", targetUrl));
        }
        catch (Throwable e)
        {
            log.warn(String.format("unexpected error while running lynx command"));
        }
        finally
        {
            StopWatchUtils.newTask(watch, "executor.shutdown", targetUrl, log);

            executor.shutdown();

            watch.stop();

            log.info(watch.prettyPrint());

            UtilsTools.logSystemStats(log);
        }

        return result;
    }

    /**
     * Checks if the line is readable for the twitter device
     *
     * @param line - lynx line
     * @return if the line is readable, return is set into true, else false
     */
    public static boolean isReadable(String line)
    {
        line = line.trim();
        boolean isEndline = END_LINES.matcher(line).matches();
        if (line.length() < 15 && !isEndline)
        { //empty line
            return false;
        }
        else if ((line.startsWith("[") && line.endsWith("]")) || line.contains("]["))
        {
            return false;
        }
        else if (LYNX_LINK.matcher(line).matches())
        {
            return false;
        }
        else if (LYNX_SIMPLE_LINK.matcher(line).matches() && !isEndline)
        { //TODO revise
            return false;
        }
        else if (LYNX_REFERENCE.matcher(line).matches())
        {
            return false;
        }
        else if (line.startsWith("#"))
        {
            return false;
        }
        else if (line.contains("____________________"))
        {
            return false;
        }
        else if (line.contains("lynx") || line.toLowerCase().contains("jboss"))
        {
            return false;
        }
        return true;
    }

    private static String doDump(String targetUrl, final StopWatch watch)
    {
        return runLynx(getDumpArgs(targetUrl), watch);
    }

    private static String runLynx(String[] args, final StopWatch watch)
    {

        String text = null;

        // only used for debug/info messages
        String lynxCommand = buildPrintFriendlyCommand(args);

        Process process = null;

        try
        {
            StopWatchUtils.newTask(watch, "createProcessBuilder", lynxCommand, log);

            ProcessBuilder processBuilder = new ProcessBuilder(args);
            processBuilder.redirectErrorStream(true);

            log.info(String.format("Executing Lynx command: %s", lynxCommand));

            StopWatchUtils.newTask(watch, "processBuilder.start", lynxCommand, log);

            process = processBuilder.start();

            // need to start consuming the stream immediately
            StopWatchUtils.newTask(watch, "process.getInputStream", lynxCommand, log);

            text = IOUtils.toString(process.getInputStream());

            if (StringUtils.isEmpty(text))
            {
                text = null;
                log.warn(String.format("Lynx command %s failed", lynxCommand));
            }

            StopWatchUtils.newTask(watch, "process.waitFor", lynxCommand, log);

            int status = process.waitFor();

            log.info(String.format("Lynx command %s exited with code %s", lynxCommand, status));

        }
        catch (Throwable t)
        { // expecting IOException

            log.warn(String.format("Lynx command %s failed", lynxCommand), t);

        }
        finally
        {

            if (process != null)
            {

                StopWatchUtils.newTask(watch, "process.destroy", lynxCommand, log);

                process.destroy();
            }
        }

        return text;
    }

    private static String[] getDumpArgs(String targetUrl)
    {
        String lynxExe = System.getenv(LYNX_EXE);
        if (lynxExe == null)
        {
            log.info("Environment variable LYNX_EXE is not defined. Defaulting to 'lynx'");
            lynxExe = "lynx";
        }

        String[] args;

        if (System.getProperty("os.name").indexOf("Windows") != -1)
        {
            args = new String[]{
                    "cmd",
                    "/c",
                    lynxExe,
                    targetUrl,
                    "" + LYNX_WIDTH
            };
        }
        else
        {
            args = new String[]{
                    lynxExe,
                    "-dump",
                    "-hiddenlinks=ignore",
                    String.format("-width=%s", LYNX_WIDTH),
                    getAuthString(targetUrl),
                    targetUrl
            };
        }

        return args;
    }

    /**
     * Returns the -auth string for the given target url
     *
     * @param targetUrl
     * @return
     */
    private static String getAuthString(String targetUrl)
    {
        // TODO - implement me. This probably calls for using sys config to retrieve the credentials based on domain
        return "-auth " + nyTimesUsername + ":" + nyTimesPassword;
    }

    /**
     * Returns print-friendly version of the program args
     *
     * @param args The args.
     * @return The print-friendly string.
     */
    private static String buildPrintFriendlyCommand(String[] args)
    {
        StringBuilder sb = new StringBuilder();

        for (String s : args)
        {
            sb.append(s).append(" ");
        }

        return sb.toString().trim();
    }
}
