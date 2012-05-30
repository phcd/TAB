package com.archermind.txtbl.dal.business;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.*;

import java.util.Date;
import java.util.List;

public interface IEmailReceivedService {


	public abstract int saveEmail(EmailPojo emailPojo) throws DALException;

	public abstract Body getBody(String uuid, String emailId, boolean useCannedMessage) throws DALException;

    public abstract List<Attachment> getAttachmentListNoData(int emailId, boolean preferOriginals) throws DALException;

    OriginalReceivedAttachment getOriginalAttachmentFromReceivedAttachment(int emailId, String name) throws DALException;

	public Attachment getAttachment(String uuid, int emailId, String name, Integer id, Date mailDate) throws DALException;

	public abstract int updateStatusBulk(String status, int[] mailId) throws DALException;

	public int saveEmail(EmailPojo ep, OriginalReceivedEmail ore) throws DALException;

	public int deleteReceivedAttachment(String date) throws DALException;

	public int deletedOriginalAttachment(String date) throws DALException;

	public int deleteReceivedEmail(String id) throws DALException;

	public Email getReceivedEmail(String mailid, String uuid) throws DALException;

    public Email getReceivedEmailByMessageId(String messageId, String uuid) throws DALException;

    public List<Email> getNewEmail(String userId, String status, Integer limit, boolean useMaster) throws DALException;

    public List<Email> getNewEmail(String accountName, String status, boolean useMaster) throws DALException;

    public List<Email> getIMAPDirtyEmail(String userID) throws DALException;

	public int updateStatus(String uuid, String newStatus, String oldStatus) throws DALException;

	/**
	 * @param date
	 *            String 'yyyy-mm-dd' for example :'2008-07-04'
	 * 
	 * @return int 1=success 0=fail
	 */

	public int delReceiveEmail(String date) throws DALException;

	public List<Attachment> getAttachmentDataList(String emailId, Date mailDateAsDate) throws DALException;

	public int getNewEmailCount(String userId) throws DALException;

    public int getNewEmailCountForAccount(String accountName) throws DALException;

    public int getDeliveredEmailCount(String userId) throws DALException;

    public int updateStatusSingle(String uuid, int emailid, String newStatus) throws DALException;

    List<Attachment> getAttachmentHeaders(String uuid, String emailStatus, int startEmailId, int endEmailId) throws DALException;

    public int truncateUnreadList(String uuid, int count) throws DALException;

}