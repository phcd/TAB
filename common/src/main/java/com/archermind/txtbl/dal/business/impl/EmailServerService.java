package com.archermind.txtbl.dal.business.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.IEmailServerService;
import com.archermind.txtbl.dal.orm.ServerORMap;
import com.archermind.txtbl.dal.orm.UserORMap;
import com.archermind.txtbl.domain.Email;
import com.archermind.txtbl.domain.Server;
import com.archermind.txtbl.domain.User;
import com.archermind.txtbl.utils.ErrorCode;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmailServerService implements IEmailServerService {
    private static final Logger logger = Logger.getLogger(EmailServerService.class);

    private static boolean useMasterForEmailServerInfo;

    static {
        useMasterForEmailServerInfo = Boolean.parseBoolean(SysConfigManager.instance().getValue("useMasterForEmailServerInfo", "false"));
    }

    public List<Server> getServers(String status) throws DALException {
        try {
            HashMap<String, Object> param = new HashMap<String, Object>();
            if (status != null) {
                param.put("status", status);
            }

            return new ServerORMap(useMasterForEmailServerInfo).getConfigs(param);
        } catch (SQLException e) {
            logger.error("Get servers error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public List<Server> getServersbyName(String name) throws DALException {
        if (logger.isTraceEnabled())
            logger.trace(String.format("getServersbyName(name=%s)", name));

        try {
            HashMap<String, Object> param = new HashMap<String, Object>();
            if (name != null) {
                param.put("name", name);
            }

            return new ServerORMap(useMasterForEmailServerInfo).getConfigs(param);
        } catch (SQLException e) {
            logger.error("Get servers error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public Server getServersbyId(String id) throws DALException {
        try {
            HashMap<String, Object> param = new HashMap<String, Object>();
            if (id != null) {
                param.put("id", id);
            }
            List<Server> servers = new ServerORMap(useMasterForEmailServerInfo).getConfigs(param);
            if (servers.size() > 0) {
                return servers.get(0);
            }
            return null;
        } catch (SQLException e) {
            logger.error("Get servers error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        } 
    }

    /**
     * @param currentDay String :'yyyy-mm-dd' for example :'2008-07-04'
     * @return int 1=success 0=fail
     */
    public int JobDeleteAllEmail(String currentDay) throws DALException {
        try {
            String sBegCurrentDay = currentDay + " 00:00:00";
            String sEndCurrentDay = currentDay + " 23:59:59";

            HashMap<String, Object> param = new HashMap<String, Object>();
            param.put("beginCurrentDay", sBegCurrentDay);
            param.put("endCurrentDay", sEndCurrentDay);

            logger.info("JobDeleteAllEmail input day " + currentDay + " beginCurrentDay " + sBegCurrentDay + " endCurrentDay " + sEndCurrentDay);
            // query current date user histroy
            List listCurDateHistroyUserid = new UserORMap().getCurDateHistoyUserid(param);
            // delete sent email by userid
            logger.info("JobDeleteAllEmail  DeleteSentEmail begin");
            JobDeleteSentEmail(listCurDateHistroyUserid);
            logger.info("JobDeleteAllEmail  DeleteSentEmail end");
            // delete recieve email by userid
            logger.info("JobDeleteAllEmail  JobDeleteRecieveEmail begin");
            JobDeleteRecieveEmail(listCurDateHistroyUserid);
            logger.info("JobDeleteAllEmail  JobDeleteRecieveEmail end");
            logger.info("JobDeleteAllEmail end");
            return 1;
        } catch (SQLException e) {
            logger.error("Job Delete All Email error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public int JobDeleteRecieveEmail(List list) {
        try {
            UserORMap uORMap = new UserORMap();
            // query current date user histroy
            int li_CurDateHistroyUseridCount = list.size();

            if (li_CurDateHistroyUseridCount > 0) {

                for (int iUserid = 0; iUserid < li_CurDateHistroyUseridCount; iUserid++) {
                    User user = (User) list.get(iUserid);
                    String sUserId = user.getId();
                    System.out.println("userid :" + sUserId);

                    // query recieve mail to userid
                    List listRecieveEmail = uORMap.selectReceivedEmail(sUserId);
                    int li_RecieveEmailCount = listRecieveEmail.size();
                    if (li_RecieveEmailCount > 0) {

                        // uORMap.startBatch();

                        for (int iRecieveEmailid = 0; iRecieveEmailid < li_RecieveEmailCount; iRecieveEmailid++) {
                            Email recieveEmail = (Email) listRecieveEmail.get(iRecieveEmailid);
                            String sRecieveEmailid = String.valueOf(recieveEmail.getMailid());
                            System.out.println("recieve emailid :" + sRecieveEmailid);

                            // delete txtbl_original_attachment to
                            // sRecieveEmailid
                            uORMap.removePeek11(sRecieveEmailid);

                            // delete txtbl_received_attachment to
                            // sRecieveEmailid
                            uORMap.removePeek06(sRecieveEmailid);
                            // delete txtbl_rcvmail_body to sRecieveEmailid
                            uORMap.removePeek07(sRecieveEmailid);

                            // delete txtbl_email_received to sRecieveEmailid
                            uORMap.removePeek08(sRecieveEmailid);
                        }
                    }
                }

            }
            return 1;
        } catch (SQLException e) {
            logger.error("Job Delete Recieve Email error!", e);
        }

        return 0;
    }

    public int JobDeleteSentEmail(List list) {
        UserORMap uORMap = new UserORMap();

        try {
            int li_CurDateHistroyUseridCount = list.size();
            if (li_CurDateHistroyUseridCount > 0) {
                for (int iUserid = 0; iUserid < li_CurDateHistroyUseridCount; iUserid++) {

                    User user = (User) list.get(iUserid);
                    String sUserId = user.getId();

                    System.out.println("userid :" + sUserId);
                    // query sent mail to userid
                    List listSentEmail = uORMap.selectSentEmail(sUserId);
                    int li_SentEmailCount = listSentEmail.size();

                    if (li_SentEmailCount > 0) {
                        // uORMap.startBatch();
                        for (int iSentEmailid = 0; iSentEmailid < li_SentEmailCount; iSentEmailid++) {
                            Email sentEmail = (Email) listSentEmail.get(iSentEmailid);
                            String sSentEmailid = String.valueOf(sentEmail.getMailid());
                            System.out.println("sent emailid :" + sSentEmailid);
                            // delete txtbl_email_sent_bcc to sSentEmailid
                            uORMap.removePeekSentBcc(sSentEmailid);
                            // delete txtbl_sent_attachment to sSentEmailid
                            uORMap.removePeek03(sSentEmailid);
                            // delete txtbl_sentmail_body to sSentEmailid
                            uORMap.removePeek04(sSentEmailid);
                            // delete txtbl_email_sent to sSentEmailid
                            uORMap.removePeek05(sSentEmailid);
                        }
                    }
                }
            }
            return 1;
        } catch (SQLException e) {
            logger.error("Job Delete Sent Email error!", e);
        }
        return 0;
    }

    public String[] getAllReceiveProtocolType() throws DALException {
        if (logger.isTraceEnabled())
            logger.trace("getAllReceiveProtocolType()");
        try {
            String sRet[] = new String[0];
            ServerORMap orm = new ServerORMap(useMasterForEmailServerInfo);
            List<Server> list = orm.getAllReceiveProtocolType();
            if(logger.isTraceEnabled())
                logger.trace("servers="+org.apache.commons.lang.StringUtils.join(list,";"));
            if (list.size() > 0)
                sRet = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Server server = list.get(i);
                sRet[i] = server.getReceiveProtocolType();

            }
            return sRet;
        } catch (SQLException e) {
            logger.error("get All Receive Protocol Type error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public Server getSentServerConfig(String sName) throws DALException {
        if (logger.isTraceEnabled())
            logger.trace(String.format("getSentServerConfig(sName=%s", sName));
        try {
            ServerORMap orm = new ServerORMap(useMasterForEmailServerInfo);
            List<Server> list = orm.getSentServerConfig(sName);
            if (list.size() > 0) {
                return list.get(0);
            }
        } catch (SQLException e) {
            logger.error("getSentServerConfig error!", e);
        }
        return null;
    }

    public List<Server> getSentServers(String status) throws DALException {
        try {
            HashMap<String, Object> param = new HashMap<String, Object>();
            if (status != null) {
                param.put("status", status);
            }
            return new ServerORMap(useMasterForEmailServerInfo).getSentConfigs(param);
        } catch (SQLException e) {
            logger.error("getSentServers error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public List<Server> getSentServersbyName(String name) throws DALException {
        try {
            HashMap<String, Object> param = new HashMap<String, Object>();
            if (name != null) {
                param.put("name", name);
            }
            return new ServerORMap(useMasterForEmailServerInfo).getSentConfigs(param);
        } catch (SQLException e) {
            logger.error("getSentServersbyName error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public List<Server> getMailServerConfig(String name) throws DALException {
        try {
            return new ServerORMap(useMasterForEmailServerInfo).getMailServerConfig(prepareForLikeQuery(name));
        } catch (SQLException e) {
            logger.error("getMailServerConfig error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public List<String> getDomainNames(String nameCriteria, int limit) throws DALException {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("name", prepareForLikeQuery(nameCriteria));
            if (limit != 0) {
                params.put("limit", limit);
            }
            return new ServerORMap().getDomainNames(params);
        } catch (SQLException e) {
            logger.error("Get domain names error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    private String prepareForLikeQuery(String param) {
        return param != null ? "%" + param + "%" : "%";
    }

}
