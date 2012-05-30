package com.archermind.txtbl.dal.business.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.ISysConfigService;
import com.archermind.txtbl.dal.orm.SysConfigORMap;
import com.archermind.txtbl.domain.SysConfig;
import com.archermind.txtbl.utils.ErrorCode;
import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class SysConfigService implements ISysConfigService {
    private static final Logger logger = Logger.getLogger(SysConfigService.class);

    public HashMap loadConfig() throws DALException {
        try {
            logger.info("SysConfigService is loading configuration ....");
            HashMap<String, SysConfig> resultMap = new HashMap<String, SysConfig>();
            SysConfigORMap configOR = new SysConfigORMap();
            List<SysConfig> list = configOR.getConfig();
            for (SysConfig aList : list) {
                String name = aList.getName();
                resultMap.put(name, aList);

                // dump the config for debug pu
                String value = aList.getValue();
                if (name.toLowerCase().contains("key") || name.toLowerCase().contains("password")) {
                    value = "************";
                }

                if (value.length() > 160) {
                    value = value.replaceAll(System.getProperty("line.separator"), "").substring(0, 160) + "...";
                }

                logger.info(String.format("%40s......%s", name, value));
            }
            return resultMap;
        } catch (SQLException e) {
            logger.error("Load Config  error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public int updateSystemParameters(SysConfig sysconfig) throws DALException {
        SysConfigORMap sc = new SysConfigORMap();
        return sc.updateSystemParameters(sysconfig);
    }

    public HashMap<String, String> loadPeekConfig() throws DALException {
        if (logger.isTraceEnabled())
            logger.trace("loadPeekConfig()");
        try {
            HashMap<String, String> resultMap = new HashMap<String, String>();
            SysConfigORMap configOR = new SysConfigORMap();
            List<SysConfig> list = configOR.getPeekConfig();

            for (SysConfig aList : list) {
                String name = aList.getName();
                name = name.replaceAll("peek.", "");
                name = name.toUpperCase().substring(0, 1) + name.substring(1, name.length());
                String value = aList.getValue();
                resultMap.put(name, value);
            }
            return resultMap;
        } catch (SQLException e) {
            logger.error("Load peek Config error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public List<SysConfig> getMailboxConfig(String sLikeKey) throws DALException {
        if (logger.isTraceEnabled())
            logger.trace(String.format("getMailboxConfig(sLikeKey=%s)", sLikeKey));
        SysConfigORMap sc = new SysConfigORMap();
        try {
            sLikeKey = sLikeKey + "%";
            return sc.getMailboxConfig(sLikeKey);
        } catch (SQLException e) {
            logger.error("get Mailbox Config error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }

    public SysConfig getTaskFactoryParameter(String sName) throws DALException {
        if (logger.isTraceEnabled())
            logger.trace(String.format("getTaskFactoryParameter(sName=%s)", sName));
        SysConfigORMap sc = new SysConfigORMap();
        try {
            return sc.getTaskFactoryParameter(sName);
        } catch (SQLException e) {
            logger.error("get Task Factory Parameter error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
    }
}
