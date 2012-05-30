package com.archermind.txtbl.dal.business.impl;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.IEMailSentService;
import com.archermind.txtbl.dal.orm.AccountORMap;
import com.archermind.txtbl.dal.orm.AttachmentORMap;
import com.archermind.txtbl.dal.orm.BodyORMap;
import com.archermind.txtbl.dal.orm.EmailORMap;
import com.archermind.txtbl.dal.orm.SentORMap;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Attachment;
import com.archermind.txtbl.domain.Body;
import com.archermind.txtbl.domain.Email;
import com.archermind.txtbl.domain.EmailPojo;
import com.archermind.txtbl.utils.ErrorCode;
import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class EmailSentService implements IEMailSentService {
	private static final Logger logger = Logger.getLogger(EmailSentService.class);

	public int saveEmail(EmailPojo emailPojo) throws DALException {
		Email emailHead = emailPojo.getEmail();
		Body body = emailPojo.getBody();
		List<Attachment> attachments = emailPojo.getAttachement();
		int result = 0;
		EmailORMap emailORMap = new EmailORMap();
		try {
			emailORMap.startTransaction();
			if (emailHead != null) {
				if ("".equals(emailHead.getBcc()) || emailHead.getBcc() == null) {
					emailHead.setBcc_flag("0");
				} else {
					emailHead.setBcc_flag("1");
				}
				result = emailORMap.addSentEmail(emailHead);
			}
			int lastId = (int) (Long.parseLong(((emailORMap.getSqlMapClient()
					.queryForObject("Global.getLastInsertId")).toString())));

			if ("".equals(emailHead.getBcc()) || emailHead.getBcc() == null) {
				emailHead.setBcc_flag("0");
			} else {
				emailHead.setMailid(lastId);
				emailORMap.addSentEmailBcc(emailHead);
			}

			BodyORMap bodyORMap = new BodyORMap(emailORMap);

			if ((body != null) && (result == 1)) {
				body.setEmailid(lastId);

				result = bodyORMap.addSentBody(body);
			}
			AttachmentORMap attachORMap = new AttachmentORMap(emailORMap);
			if ((attachments != null) && (result == 1)) {
				Iterator<Attachment> it = attachments.iterator();
				while ((it.hasNext()) && (result == 1)) {
					Attachment attachment = it.next();
					attachment.setEmailId(lastId);
					result = attachORMap.addSentAttachment(attachment);
				}
			}
			if (result != 0) {
				emailORMap.commitTransaction();
				result = 1;
			}
		} catch (SQLException e) {
			result = 0;
			logger.error("Save email error!", e);
			throw new DALException(ErrorCode.CODE_DAL_, e);
		} finally {
			try {
				emailORMap.endTransaction();
			} catch (SQLException e) {
				result = 0;
				logger.error("Save email error!", e);
			}
		}

		return result;
	}

	public int updateStatus(String status, int[] mailId) throws DALException {
		int result = 1;
		HashMap<String, Object> param = new HashMap<String, Object>();
		EmailORMap emailORMap = new EmailORMap();
		try {
			emailORMap.startTransaction();
            for (int aMailId : mailId) {
                param.put("status", status);
                param.put("mailid", aMailId);
                result = emailORMap.updateSendStatus(param);
                if (result == 0) {
                    break;
                }
            }
			if (result != 0) {
				emailORMap.commitTransaction();
				result = 1;
			}
		} catch (SQLException e) {
			result = 0;
			logger.error("Update status error!", e);
			throw new DALException(ErrorCode.CODE_DAL_, e);
		} finally {
			try {
				emailORMap.endTransaction();
			} catch (SQLException e) {
				result = 0;
				logger.error("Update status error!", e);
			}
		}
		return result;
	}

	public EmailPojo getEmailPojo(String email_id) throws DALException {
        HashMap<String, Object> tmpMap = new HashMap<String, Object>();
        tmpMap.put("emailid", email_id);
        try {
            EmailORMap emailORMap = new EmailORMap();
            Email emailHead = emailORMap.getSentEmail(tmpMap);
			if (emailHead != null) {
				if (emailHead.getBcc_flag().equals("1")) {

                    HashMap<String, Object> tmpMap1 = new HashMap<String, Object>();
                    tmpMap1.put("mailid", email_id);
                    if (emailORMap.getSentEmailBcc(tmpMap1) != null) {
						emailHead.setBcc(emailORMap.getSentEmailBcc(tmpMap1).getBcc());
					}

				}
			}
			BodyORMap bodyORMap = new BodyORMap(emailORMap);
			Body body = bodyORMap.getSentBody(tmpMap);
			AttachmentORMap attachORMap = new AttachmentORMap(emailORMap);
			List<Attachment> attachments = attachORMap.getSentAttachment(tmpMap);

            Account account = new Account();
			if (emailHead != null) {
				String user_id = emailHead.getUserId();
				String account_name = emailHead.getFrom();
				tmpMap.put("user_id", user_id);
				tmpMap.put("name", account_name);
				account = new AccountORMap(emailORMap).getAccount(tmpMap);
			}

            EmailPojo emailPojo = new EmailPojo();
            emailPojo.setEmail(emailHead);
            emailPojo.setBody(body);
            emailPojo.setAttachement(attachments);
            emailPojo.setAccount(account);
            return emailPojo;
        } catch (SQLException e) {
            logger.error("Get email pojo error!", e);
            throw new DALException(ErrorCode.CODE_DAL_, e);
        }
	}

	public int deleteSentAttachment(String date) throws DALException {
		SentORMap ro = new SentORMap();
		try {
			ro.startTransaction();
			List list = ro.selectSentAttachmentDate(date);

			if (list.size() > 0) {
				Email email = (Email) list.get(0);
				ro.deleteSentAttachment(String.valueOf(email.getMailid()));
			}
			ro.commitTransaction();
            return 1;
		} catch (Exception e) {
			logger.error("delete sent attachment error!", e);
			throw new DALException(ErrorCode.CODE_DAL_, e);
		} finally {
			try {
				ro.endTransaction();
			} catch (SQLException e) {
				logger.error("delete sent attachment error!", e);
			}
		}
	}

	public int deleteSentEmail(String id) throws DALException {
		try {
			return new SentORMap().deleteSentEmail(id);
		} catch (Exception e) {
			logger.error("delete sent email error!", e);
			throw new DALException(ErrorCode.CODE_DAL_, e);
		}
	}

	public Email getSentEmail(String mailid, String uuid) throws DALException {
        HashMap<String, String> paras = new HashMap<String, String>();
		paras.put("mailid", mailid);
		paras.put("uuid", uuid);

		Email email = new SentORMap().getSentEmail(paras);
		if (email.getBcc_flag().equals("1")) {
			EmailORMap em = new EmailORMap();
			try {
				if (em.getSentEmailBcc(paras) != null) {
					email.setBcc(em.getSentEmailBcc(paras).getBcc());
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("get sent email error!", e);
				throw new DALException(ErrorCode.CODE_DAL_, e);
			}
		}
		return email;
	}

	public int delSentEmail(String date) throws DALException {
		try {
            SentORMap ro = new SentORMap();
            logger.info("Sent Email input date " + date);

			List list = ro.selectSentEmailDate(date);

			if (list.size() > 0) {
                for (Object aList : list) {
                    Email email = (Email) aList;

                    ro.deleteSentBcc(String.valueOf(email.getMailid()));
                    ro.deleteSentBody(String.valueOf(email.getMailid()));
                    ro.deleteSentAttachment(String.valueOf(email.getMailid()));
                    ro.deleteSentHead(String.valueOf(email.getMailid()));
                }
			}
			logger.info("Sent Email end");
            return 1;
		} catch (Exception e) {
			logger.error("del Sent Email error!", e);
			throw new DALException(ErrorCode.CODE_DAL_, e);
		} 
	}
}
