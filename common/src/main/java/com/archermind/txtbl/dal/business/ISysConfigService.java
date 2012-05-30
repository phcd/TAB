package com.archermind.txtbl.dal.business;

import java.util.HashMap;
import java.util.List;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.SysConfig;

public interface ISysConfigService {

	public abstract HashMap loadConfig() throws DALException;

	public int updateSystemParameters(SysConfig sysconfig) throws DALException;

	public HashMap loadPeekConfig() throws DALException;

	public List getMailboxConfig(String sLikeKey) throws DALException;

	public SysConfig getTaskFactoryParameter(String sName) throws DALException;
}