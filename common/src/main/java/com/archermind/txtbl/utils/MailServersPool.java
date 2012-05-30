package com.archermind.txtbl.utils;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.impl.EmailServerService;
import com.archermind.txtbl.dal.business.impl.TxtblWebService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Country;
import com.archermind.txtbl.domain.Server;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MailServersPool {

    private static final Logger logger = Logger.getLogger(MailServersPool.class);

    public static final MailServersPool mailServersPool = new MailServersPool();

    final HashMap<String, List<Server>> serverSendMap = new HashMap<String, List<Server>>();

    final HashMap<String, List<Server>> serverReceiveMap = new HashMap<String, List<Server>>();

    private MailServersPool() {
        loadPool();
    }

    public void loadPool() {

        EmailServerService emailServerService = new EmailServerService();

        synchronized (serverSendMap) {
            serverSendMap.clear();
            try {
                List<Server> list = emailServerService.getSentServers("1");
                for (Server server : list) {

                    String suffixName = server.getName();

                    List<Server> sameServerName = serverSendMap.get(suffixName);

                    if (sameServerName == null) sameServerName = new ArrayList<Server>();

                    sameServerName.add(server);
                    serverSendMap.put(suffixName, sameServerName);
                }
            } catch (DALException e) {
                logger.error(e);
            }
        }

        synchronized (serverReceiveMap) {
            serverReceiveMap.clear();
            try {
                List<Server> list = emailServerService.getServers("1");

                for (Server server : list) {

                    String suffixName = server.getName();

                    List<Server> sameServerName = serverReceiveMap.get(suffixName);

                    if (sameServerName == null) sameServerName = new ArrayList<Server>();

                    sameServerName.add(server);
                    serverReceiveMap.put(suffixName, sameServerName);
                }
            } catch (DALException e) {
                logger.error(e);
            }
        }
    }

    public List<Server> getSendServerListByServerName(String emailladdress) {
        String suffixName = UtilsTools.parseSuffixServerName(emailladdress);
        List<Server> serverList = serverSendMap.get(suffixName);
        if (serverList == null) {
            logger.error("Send server not found for " + suffixName);
            return new ArrayList<Server>();
        }
        return serverList;
    }

    //TODO - Paul - need to use the method that filters xobni servers
    public List<Server> getReceiveServerListByServerName(String emailladdress) {
        if(logger.isTraceEnabled())
            logger.trace(String.format("getReceiveServerListByServerName(emailladdress=%s)", String.valueOf(emailladdress)));

        String suffixName = UtilsTools.parseSuffixServerName(emailladdress);
        if(logger.isTraceEnabled())
            logger.trace("suffixName:"+suffixName);
        
        List<Server> serverList = serverReceiveMap.get(suffixName);
        if (serverList == null) {
            logger.error("Servers not found for " + suffixName);
            return new ArrayList<Server>();
        }
        return serverList;
    }

    int saveMailSrverServer(Server emailAServer) {

        int i = -1;
        try {
            i = new TxtblWebService().addPop3Server(emailAServer);
        } catch (DALException e) {

            logger.error(e);

        }
        if (i == -1)
            return -1;
        else
            return emailAServer.getId();
    }

    public int addSend(Server emailAServer) {

        int serverID = -1;
        String suffixName = UtilsTools.parseSuffixServerName(emailAServer.getName());

        List<Server> sameServerName = serverSendMap.get(suffixName);

        if (sameServerName == null) {
            sameServerName = new ArrayList<Server>();

            serverID = saveMailSrverServer(emailAServer);
            sameServerName.add(emailAServer);
            serverSendMap.put(suffixName, sameServerName);

        } else {
            synchronized (mailServersPool) {
                if (!isExistSameSendServer(emailAServer)) {
                    sameServerName.add(emailAServer);
                    String ptl = emailAServer.getReceiveProtocolType();
                    emailAServer.setReceiveProtocolType(null);

                    serverID = saveMailSrverServer(emailAServer);
                    emailAServer.setReceiveProtocolType(ptl);
                    // save to database

                }
            }

        }

        return serverID;
    }

    public int addReceive(Server emailAServer) {

        int serverID = -1;

        String suffixName = UtilsTools.parseSuffixServerName(emailAServer.getName());

        List<Server> sameServerName = serverReceiveMap.get(suffixName);

        if (sameServerName == null) {
            sameServerName = new ArrayList<Server>();
            serverID = saveMailSrverServer(emailAServer);
            sameServerName.add(emailAServer);
            serverReceiveMap.put(suffixName, sameServerName);

        } else {
            synchronized (mailServersPool) {
                if (!isExistSameReceiveServer(emailAServer)) {
                    sameServerName.add(emailAServer);
                    String ptl = emailAServer.getSendProtocolType();
                    emailAServer.setSendProtocolType(null);

                    serverID = saveMailSrverServer(emailAServer);
                    emailAServer.setSendProtocolType(ptl);
                    // save to database

                }
            }
        }

        return serverID;
    }

    public int add(Account account) {
        addSend(account);
        return addReceive(account);
    }

    public int addSend(Account account) {
        Server emailAServer = new Server();
        UtilsTools.mapSendServerDetails(emailAServer, account);

        emailAServer.setLevel("6");
        emailAServer.setStatus("1");
        addSend(emailAServer);
        account.setSent_id(emailAServer.getSent_id());

        return emailAServer.getSent_id();
    }

    public int addReceive(Account account) {
        Server emailAServer = new Server();
        UtilsTools.mapReceiveServerDetails(emailAServer, account);

        emailAServer.setLevel("6");
        emailAServer.setStatus("1");
        emailAServer.setOrderFlag("2");

        addReceive(emailAServer);
        account.setServer_id(emailAServer.getId());

        return emailAServer.getId();

    }

    private boolean isExistSameSendServer(Server emailAServer) {
        boolean isSame = false;
        String suffixName = UtilsTools.parseSuffixServerName(emailAServer.getName());
        List<Server> sameServerName = serverSendMap.get(suffixName);

        if (sameServerName != null) {
            for (Server aSameServerName : sameServerName) {
                isSame = compareSendServer(emailAServer, aSameServerName);
                if (isSame) break;
            }
        }

        return isSame;
    }

    private boolean isExistSameReceiveServer(Server emailAServer) {
        boolean isSame = false;
        String suffixName = UtilsTools.parseSuffixServerName(emailAServer.getName());
        List<Server> sameServerName = serverReceiveMap.get(suffixName);

        if (sameServerName != null) {

            for (Server aSameServerName : sameServerName) {

                isSame = compareReceiveServer(emailAServer, aSameServerName);
                if (isSame) break;
            }
        }

        return isSame;
    }

    public boolean compareSendServer(Server newServer, Server oldServer) {

        boolean isSame = newServer.getSendHost() != null && newServer.getSendHost().equals(oldServer.getSendHost());
        isSame = isSame && newServer.getSendPort() != null && newServer.getSendPort().equals(oldServer.getSendPort());
        isSame = isSame && newServer.getSendProtocolType() != null && newServer.getSendProtocolType().equals(oldServer.getSendProtocolType());
        isSame = isSame && newServer.getSendTs() != null && newServer.getSendTs().equals(oldServer.getSendTs());

        return isSame;

    }

    public boolean compareReceiveServer(Server newServer, Server oldServer) {
        boolean isSame = newServer.getReceiveHost() != null && newServer.getReceiveHost().equals(oldServer.getReceiveHost());
        isSame = isSame && newServer.getReceivePort() != null && newServer.getReceiveHost().equals(oldServer.getReceivePort());
        isSame = isSame && newServer.getReceiveProtocolType() != null && newServer.getReceiveProtocolType().equals(oldServer.getReceiveProtocolType());
        isSame = isSame && newServer.getReceiveTs() != null && newServer.getReceiveTs().equals(oldServer.getReceiveTs());
        return isSame;

    }

    public static MailServersPool getInstance() {
        mailServersPool.loadPool();
        return mailServersPool;
    }

    public List<Server> getReceiveServerList(String domain, Country country) {
        if (logger.isTraceEnabled())
            logger.trace(String.format("getReceiveServerList(domain=%s, country=%s)", domain, String.valueOf(country)));

        List<Server> servers = getReceiveServerListByServerName(domain);
        if (logger.isTraceEnabled())
            logger.trace("servers:"+String.valueOf(servers));

        List<Server> serversFiltered = UtilsTools.filterXobniServers(servers, country);
        if (logger.isTraceEnabled())
            logger.trace("serversFiltered:"+String.valueOf(serversFiltered));

        return serversFiltered;
    }

    public List<Server> getReceiveServerListByProtocolType(String domain, String protocolType, Country country) {
        if (logger.isTraceEnabled())
            logger.trace(String.format("getReceiveServerList(domain=%s, protocolType=%s, country=%s)", domain, protocolType, country));

        List<Server> serverList = getReceiveServerList(domain, country);
        if (logger.isTraceEnabled())
               logger.trace("serverList=" + String.valueOf(serverList));

        List<Server> protocolTypeServerList = new ArrayList<Server>();
        for (Server server : serverList) {
            if (protocolType.equals(server.getReceiveProtocolType())) {
                protocolTypeServerList.add(server);
            }
        }
        return protocolTypeServerList;
    }
}
