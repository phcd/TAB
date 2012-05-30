package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.Email;
import com.archermind.txtbl.utils.MySQLEncoder;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.HashMap;
import java.util.Date;
import java.util.Map;

public class EmailORMap extends BaseORMap {

    public EmailORMap(boolean useMaster) {
        super(useMaster);
    }

    public EmailORMap() {}

	public int addSentEmail(Email emailHeader) throws SQLException {
        return this.sqlMapClient.update("SentEmail.addEmail", emailHeader);
    }

	public int addSentEmailBcc(Email emailHeader) throws SQLException {
	    return this.sqlMapClient.update("SentEmail.addEmailBcc", MySQLEncoder.encode(emailHeader));
	}

    @SuppressWarnings("unchecked")
	public List<Email> getReceivedEmail(HashMap param) throws SQLException {
		return this.sqlMapClient.queryForList("ReceivedEmail.getNewEmail", param);
	}

	public int getEmailCount(HashMap param) throws SQLException {
		return (Integer)sqlMapClient.queryForObject("ReceivedEmail.getEmailCount", param);
	}

    public int getEmailCountByUserId(HashMap param) throws SQLException {
        return (Integer)sqlMapClient.queryForObject("ReceivedEmail.getEmailCountByUserId", param);
    }


    @SuppressWarnings("unchecked")
    public List<Email> getIMAPDirtyEmail(HashMap param) throws SQLException {
        return this.sqlMapClient.queryForList("ReceivedEmail.getIMAPDirtyEmails", param);
    }

    public int updateOldestMailsAfterCutoffHavingStatus(HashMap param) throws SQLException {
        return this.sqlMapClient.update("ReceivedEmail.updateOldestMailsAfterCutoffHavingStatus", param);
    }

    public void clearImapDirtyStatusFlags(HashMap param) throws SQLException {
        this.sqlMapClient.update("ReceivedEmail.updateImapStatus", param);
    }

    public int clearImapDirtyStatusFlagsBulk(HashMap param) throws SQLException {
        return this.sqlMapClient.update("ReceivedEmail.updateImapStatusBulk", param);
    }

	public Email getSentEmail(HashMap param) throws SQLException {
    	return (Email) this.sqlMapClient.queryForObject("SentEmail.getEmail", param);
    }
	
	public Email getSentEmailBcc(HashMap param) throws SQLException {
    	return (Email) this.sqlMapClient.queryForObject("SentEmail.getEmailBcc", param);
    }

	public int updateSendStatus(HashMap param) throws SQLException {
		return this.sqlMapClient.update("SentEmail.modifyStatus", param);
	}

	public int updateStatusBulk(HashMap param) throws SQLException {
		return this.sqlMapClient.update("ReceivedEmail.updateStatusBulk", param);
	}

    public int updateReceivedUuidStatusByEmailID(HashMap param) throws SQLException {
        return this.sqlMapClient.update("ReceivedEmail.modifyStatusByUUIDandEmailID", param);
    }
	
	public int updateReceivedUuidStatus(HashMap param) throws SQLException {
		return this.sqlMapClient.update("ReceivedEmail.modifyUuidStatus", param);
	}

    public Date getReceivedEmailReceivedDate(int mailid) throws SQLException {
        Map<String, Integer> params = new HashMap<String, Integer>();
        params.put("mailid", mailid);
        return new Date(((Timestamp) this.sqlMapClient.queryForObject("ReceivedEmail.getReceivedEmailReceivedDate", params)).getTime());
    }

}
