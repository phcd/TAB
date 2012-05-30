package com.archermind.txtbl.dal.business;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Server;

import java.util.List;

public interface IEmailServerService {

	public abstract List<Server> getServers(String status) throws DALException;

	public abstract List<Server> getSentServers(String status) throws DALException;

	public List<Server> getServersbyName(String name) throws DALException;

	public List<Server> getSentServersbyName(String name) throws DALException;

	public Server getServersbyId(String id) throws DALException;

	/**
	 * @param currentDay
	 *            String :'yyyy-mm-dd' for example :'2008-07-04'
	 * @return int 1=success 0=fail
	 */
	public int JobDeleteAllEmail(String currentDay) throws DALException;

	public String[] getAllReceiveProtocolType() throws DALException;

	public Server getSentServerConfig(String sName) throws DALException;

	public List<Server> getMailServerConfig(String name) throws DALException;

    public List<String> getDomainNames(String nameCriteria, int limit) throws DALException;
}