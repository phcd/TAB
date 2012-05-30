package com.archermind.txtbl.utils;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.impl.RandomKeyService;
import com.archermind.txtbl.domain.*;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jboss.logging.Logger;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class UtilsTools {

    private static final Logger logger = Logger.getLogger(UtilsTools.class);

    public static String ws_address = "txtbl.ws.address";

    public static String mmaxAndroidClient = "MMAX_Android:01.01.00"; // TODO: remove this!

    public static int[] convertIntegersArrayToIntArray(List<Integer> integers) {
        int[] ret = new int[integers.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = integers.get(i);
        }
        return ret;
    }


    public static String extraStringToArray(byte[] array, int start, int length) {
        if (array.length >= length) {
            StringBuilder text = new StringBuilder(length);

            for (int i = start; i < start + length; i++) {
                text.append(String.valueOf((char) array[i]));
            }

            return text.toString();
        } else {
            return new String(array);
        }
    }

    public static int size(Serializable serializable) {
        ByteArrayOutputStream baos = null;

        try {
            baos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(serializable);

            return baos.toByteArray().length;
        } catch (IOException e) {
            return -1;
        } finally {
            FinalizationUtils.close(baos);
        }
    }

    public static void logSystemStats(Logger logger) {
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();

        String threadSummary = String.format("[ThreadMonitor] thread-count=%s, total-started-thread-count=%s, daemon-thread-count=%s, peak-thread-count=%s", threads.getThreadCount(), threads.getTotalStartedThreadCount(), threads.getDaemonThreadCount(), threads.getPeakThreadCount());

        if (logger == null) {
            System.out.println(threadSummary);
        } else {
            logger.debug(threadSummary);
        }

        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

        String memorySummary = String.format("[MemoryMonitor] heap-memory-usage=%s, non-heap-memory-usage=%s, objects-pending-finalization=%s", memory.getHeapMemoryUsage(), memory.getNonHeapMemoryUsage(), memory.getObjectPendingFinalizationCount());

        if (logger == null) {
            System.out.println(memorySummary);
        } else {
            logger.debug(memorySummary);
        }
    }

    public static String normalDateToStr(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return date == null ? format.format(new Date()) : format.format(date);
    }

    public static RandomKey getRandomKey(int key_id) {
        try {
            if (key_id == 0) {
                return new RandomKeyService().getMaxRandomKey();
            }
            return new RandomKeyService().getRandomKey(key_id);
        } catch (DALException e) {
            logger.error("UtilsTools getRandomKey error!", e);
        }
        return null;
    }

    public static String wsAddress() {
        SysConfigManager sysConfigManager = SysConfigManager.instance();
        return sysConfigManager.getValue(ws_address);
    }

    public static String getKeyString(int key_id, String name) {
        RandomKey randomKey = getRandomKey(key_id);
        String sKey;
        int iKeyId;

        if (randomKey == null) {
            sKey = name;
            iKeyId = 0;
        } else {
            sKey = randomKey.getRandom_key() + name;
            iKeyId = randomKey.getId();
        }
        return sKey + ";,;,;" + iKeyId;
    }

    public static String dateToUTC(String dateStr) {
        String dateTime = "";
        try {
            if (dateStr != null && !"".equals(dateStr.trim())) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = dateFormat.parse(dateStr);
                TimeZone timeZone = TimeZone.getTimeZone("UTC");
                dateFormat.setTimeZone(timeZone);
                if (date != null) {
                    dateTime = dateFormat.format(date);
                }
            }
        } catch (Exception e) {
            logger.warn("dateToUTC/UtilsTools/Exception: [" + dateStr + "] ", e);
        }
        return dateTime;
    }

    public static boolean isEmpty(List list) {
        return list == null || list.size() == 0;
    }


    public static boolean isEmpty(Set set) {
        return set == null || set.size() == 0;
    }

    public static Address[] tranAddr(String addr) {
        if (com.archermind.txtbl.utils.StringUtils.isEmpty(addr)) {
            return null;
        }
        String[] tempAddr;
        if (addr.indexOf(',') > -1) {
            tempAddr = addr.trim().split(",");
        } else if (addr.indexOf(';') > -1) {
            tempAddr = addr.trim().split(";");
        } else {
            tempAddr = new String[]{addr};
        }
        InternetAddress[] address = null;
        if (tempAddr != null) {
            address = new InternetAddress[tempAddr.length];
            for (int i = 0; i < tempAddr.length; i++) {
                address[i] = new InternetAddress();
                address[i].setAddress(tempAddr[i].trim());
            }
        }

        return address == null ? new InternetAddress[0] : address;
    }

    public static void mapSendServerDetails(Server server, Account account) {
        server.setName(parseSuffixServerName(account.getName()));
        server.setNeedAuth(account.getNeedAuth());
        server.setSendHost(account.getSendHost());
        server.setSendPort(account.getSendPort());
        server.setSendProtocolType(account.getSendProtocolType());
        server.setSendTs(account.getSendTs());
        server.setSendHostFbaPath(account.getSendHostFbaPath());
        server.setSendHostPrefix(account.getSendHostPrefix());
    }

    public static void mapReceiveServerDetails(Server server, Account account) {
        server.setName(parseSuffixServerName(account.getName()));
        server.setReceiveHost(account.getReceiveHost());
        server.setReceivePort(account.getReceivePort());
        server.setReceiveProtocolType(account.getReceiveProtocolType());
        server.setReceiveTs(account.getReceiveTs());
        server.setReceiveHostFbaPath(account.getReceiveHostFbaPath());
        server.setReceiveHostPrefix(account.getReceiveHostPrefix());
    }

    public static void mapReceiveServerDetails(Server server, Server receiveServer) {
        server.setReceiveHost(receiveServer.getReceiveHost());
        server.setReceivePort(receiveServer.getReceivePort());
        server.setReceiveProtocolType(receiveServer.getReceiveProtocolType());
        server.setReceiveTs(receiveServer.getReceiveTs());
        server.setReceiveHostFbaPath(receiveServer.getReceiveHostFbaPath());
        server.setReceiveHostPrefix(receiveServer.getReceiveHostPrefix());
    }

    public static void mapSendServerDetails(Server server, Server sendServer) {
        server.setNeedAuth(sendServer.getNeedAuth());
        server.setSendHost(sendServer.getSendHost());
        server.setSendPort(sendServer.getSendPort());
        server.setSendProtocolType(sendServer.getSendProtocolType());
        server.setSendTs(sendServer.getSendTs());
        server.setSendHostFbaPath(sendServer.getSendHostFbaPath());
        server.setSendHostPrefix(sendServer.getSendHostPrefix());
    }

    public static void mapReceiveDetails(Account account, Account acco) {
        account.setReceiveHost(acco.getReceiveHost());
        account.setReceivePort(acco.getReceivePort());
        account.setReceiveProtocolType(acco.getReceiveProtocolType());
        account.setReceiveTs(acco.getReceiveTs());
        account.setReceiveHostFbaPath(acco.getReceiveHostFbaPath());
        account.setReceiveHostPrefix(acco.getReceiveHostPrefix());
        account.setLoginName(acco.getLoginName());
    }

    public static void mapSendDetails(Account account, Account acco) {
        account.setSendHost(acco.getSendHost());
        account.setSendPort(acco.getSendPort());
        account.setSendProtocolType(acco.getSendProtocolType());
        account.setSendTs(acco.getSendTs());
        account.setNeedAuth(acco.getNeedAuth());
        account.setSendHostFbaPath(acco.getSendHostFbaPath());
        account.setSendHostPrefix(acco.getSendHostPrefix());
        account.setLoginName(acco.getLoginName());
    }

    public static void mapReceiveServerDetails(Account account, Server server) {
        if (logger.isTraceEnabled())
            logger.trace(String.format("mapReceiveServerDetails(account=%s, server=%s", String.valueOf(account), String.valueOf(server)));

        account.setReceiveHost(server.getReceiveHost());
        account.setReceivePort(server.getReceivePort());
        account.setReceiveProtocolType(server.getReceiveProtocolType());
        account.setReceiveTs(server.getReceiveTs());
        account.setServer_id(server.getId());
        account.setOrderFlag(server.getOrderFlag());
        account.setReceiveHostFbaPath(server.getReceiveHostFbaPath());
        account.setReceiveHostPrefix(server.getReceiveHostPrefix());
    }

    public static void mapSendServerDetails(Account account, Server server) {
        account.setSendHost(server.getSendHost());
        account.setSendPort(server.getSendPort());
        account.setSendProtocolType(server.getSendProtocolType());
        account.setSendTs(server.getSendTs());
        account.setSent_id(server.getSent_id());
        account.setNeedAuth(server.getNeedAuth());
        account.setOrderFlag(server.getOrderFlag());
        account.setSendHostFbaPath(server.getSendHostFbaPath());
        account.setSendHostPrefix(server.getSendHostPrefix());
    }

    public static void mapServerDetails(Account account, Server server) {
        mapReceiveServerDetails(account, server);
        mapSendServerDetails(account, server);
    }

    public static void mapServerDetails(Server server, Account account) {
        mapReceiveServerDetails(server, account);
        mapSendServerDetails(server, account);
    }

    public static String parseSuffixServerName(String emailAddress) {
        String[] names = StringUtils.parseString(emailAddress, "@");
        if (names != null && names.length > 0) {
            String suffixname = names.length > 1 ? names[1] : names[0];
            return suffixname.trim();
        }
        return null;
    }

    public static String stripDomainName(String emailAddress) {
        return emailAddress.substring(0, emailAddress.indexOf("@"));
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static Server createServer(String domainName, String host, String port, String security, String protocol, String fbaPath, String prefix) {
        Server server = new Server();
        server.setName(domainName);
        server.setReceiveHost(host);
        server.setSendHost(host);
        server.setReceivePort(port);
        server.setSendPort(port);
        server.setReceiveHostFbaPath(fbaPath);
        server.setSendHostFbaPath(fbaPath);
        server.setReceiveHostPrefix(prefix);
        server.setSendHostPrefix(prefix);
        server.setReceiveTs(security);
        server.setSendTs(security);
        server.setReceiveProtocolType(protocol);
        server.setSendProtocolType(protocol);
        server.setLevel("6");
        server.setStatus("1");
        return server;
    }

    public static void mapReceieveDtailsToSend(Account account) {
        account.setSendHost(account.getReceiveHost());
        account.setSendPort(account.getReceivePort());
        account.setSendProtocolType(account.getReceiveProtocolType());
        account.setSendTs(account.getReceiveTs());
        account.setSendHostFbaPath(account.getReceiveHostFbaPath());
        account.setSendHostPrefix(account.getReceiveHostPrefix());
    }

    static List<Server> filterXobniServers(List<Server> serverList, Country country) {
        ArrayList<Server> serverListCopy = new ArrayList<Server>(serverList);
        ArrayList<Server> xobniServerList = new ArrayList<Server>();
        for (Server server : serverList) {
            if (server.isXobni()) {
                xobniServerList.add(server);
            }
        }
        serverListCopy.removeAll(xobniServerList);
        if (country == Country.Xobni) {
            //xobni servers first and then other servers
            xobniServerList.addAll(serverListCopy);
            return xobniServerList;
        } else {
            return serverListCopy;
        }
    }

    public static boolean containsIdleProtocol(String[] protocols) {
        if (!isEmpty(protocols)) {
            for (String protocol : protocols) {
                if (Protocol.isIdle(protocol)) {
                    return true;
                }
            }
        }
        return false;
    }

    // TODO: factor these out into a "ClientUtils" or other utility class
    public static boolean isDeviceUsingRestApi(User user, Device device) {
        return isDeviceUsingRestApi(user.getPartnerCode(), device.getClientsw());
    }

    public static boolean isDeviceUsingRestApi(PartnerCode code, String clientSw) {
        // if device has no client string and is partnerCode 'qcom', 'peekint', 'sprd', 'mmax', it is using rest
        // if mmax and has client string MMAX_Android:01.01.00 it's rest

        if (code == null)
            return false;
        
        logger.debug(String.format("Checking API usage for partnerCode=%s, clientSw=%s", code.toString(), clientSw));
        if (clientSw == null || "".equals(clientSw)) {
            if (code.equals(PartnerCode.qcom)
                    || code.equals(PartnerCode.peekint)
                    || code.equals(PartnerCode.sprd)
                    || code.equals(PartnerCode.mmax))
                return true;
        } else if (code.equals(PartnerCode.sprd)) {
            return true;
        } else if (code.equals(PartnerCode.mmax) && !clientSw.startsWith("MTK")) {
            return true;
        }

        return false;
    }


    /**
     * Provides a String representation of the provided Throwable's stack trace
     * that is extracted via PrintWriter.
     *
     * @param exception Throwable/Exception from which stack trace is to be
     *                  extracted.
     * @return String with provided Throwable's stack trace.
     */
    public static String getExceptionStackTrace(final Exception exception) {
        final Writer writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);
        exception.printStackTrace(printWriter);
        return writer.toString();
    }

    public static String beanToString(Object object) {
        return ReflectionToStringBuilder.toString(object, ToStringStyle.MULTI_LINE_STYLE);
    }
}
