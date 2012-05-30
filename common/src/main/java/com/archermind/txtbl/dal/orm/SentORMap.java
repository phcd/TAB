package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.Email;
import com.archermind.txtbl.utils.MySQLEncoder;
import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class SentORMap extends BaseORMap {
	private static final Logger logger = Logger.getLogger(SentORMap.class);

    public SentORMap() {}

	public int deleteSentBcc(String id) throws Exception {
		return this.sqlMapClient.delete("SentEmail.deleteSentBccEmailId", id);
	}

	public int deleteSentAttachment(String date) throws Exception {
		return this.sqlMapClient.delete("SentEmail.deleteSentAttachmentEmailId", date);
	}

	public int deleteSentHead(String id) throws SQLException {
		return this.sqlMapClient.delete("SentEmail.deleteSentHeadEmailId", id);
	}

	public int deleteSentBody(String id) throws SQLException {
		return this.sqlMapClient.delete("SentEmail.deleteSentBodyEmailId", id);
	}

	public int deleteSentEmail(String id) {
		int i;
		try {
			i = 1;
			this.sqlMapClient.delete("SentEmail.deleteSentAttachmentEmailId",
					id);
			this.sqlMapClient.delete("SentEmail.deleteSentBodyEmailId", id);
			this.sqlMapClient.delete("SentEmail.deleteSentHeadEmailId", id);
		} catch (SQLException e) {
			i = 0;
			logger.error("deleteSentEmail error!", e);
		}
		return i;
	}

	public Email getSentEmail(HashMap paras) {
		try {
			return MySQLEncoder.decode((Email) this.sqlMapClient.queryForObject("SentEmail.getSentEmail", paras));
		} catch (SQLException e) {
			logger.error("Get Sent Email error!", e);
		}
		return null;
	}

    @SuppressWarnings("unchecked")
	public List<Email> selectSentEmailDate(String date) throws SQLException {
		return MySQLEncoder.decode(this.sqlMapClient.queryForList("SentEmail.selectSentEmailDate", date));
	}

    @SuppressWarnings("unchecked")
	public List<Email> selectSentAttachmentDate(String date) throws SQLException {
		return MySQLEncoder.decode(this.sqlMapClient.queryForList("SentEmail.selectSentAttachmentDate",date));
	}

}