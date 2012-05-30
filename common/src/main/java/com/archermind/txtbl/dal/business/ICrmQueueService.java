package com.archermind.txtbl.dal.business;

import java.util.List;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.CrmQueue;

public interface ICrmQueueService {
	/**
	 * 
	 * @param user_id
	 * @param sName
	 * @return int 0 fail 1 sucess
	 * @throws DALException
	 */

	public int delDeviceSimcode(String user_id, String sName) throws DALException;

	/**
	 * 
	 * @param crmQueue
	 * @return int 0 fail 1 sucess
	 * @throws DALException
	 */
	public int addCrmQueue(CrmQueue crmQueue) throws DALException;

	/**
	 * 
	 * @param user_id
	 * @param sName
	 * @return list
	 * @throws DALException
	 */
	public List<CrmQueue> getQueueMessage(String user_id, String sName) throws DALException;
}
