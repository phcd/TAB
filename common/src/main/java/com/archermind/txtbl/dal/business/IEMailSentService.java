package com.archermind.txtbl.dal.business;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Email;
import com.archermind.txtbl.domain.EmailPojo;

public interface IEMailSentService {

	/**
	 * @param emailPojo
	 * @return int 1=success 0=fail save emailPojo
	 */
	public abstract int saveEmail(EmailPojo emailPojo) throws DALException;

	/**
	 * @param status,mailId
	 * @return int 1=success 0=fail update the status of the sending emails.
	 */
	public abstract int updateStatus(String status, int[] mailId) throws DALException;

	public abstract EmailPojo getEmailPojo(String email_id) throws DALException;

	public int deleteSentAttachment(String date) throws DALException;

	public int deleteSentEmail(String id) throws DALException;

	public Email getSentEmail(String mailid, String uuid) throws DALException;

	/**
	 * @param date
	 *            String 'yyyy-mm-dd' for example :'2008-07-04'
	 * 
	 * @return int 1=success 0=fail
	 */
	public int delSentEmail(String date) throws DALException;

}