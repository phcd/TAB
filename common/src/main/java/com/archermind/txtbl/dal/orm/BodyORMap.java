package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.Body;

import java.sql.SQLException;
import java.util.HashMap;

public class BodyORMap extends BaseORMap {

    public BodyORMap(boolean useMaster) {
        super(useMaster);
    }

    public BodyORMap(BaseORMap another) {
        super(another);
    }

	public int addSentBody(Body body) throws SQLException {
		return this.sqlMapClient.update("SentEmailBody.addBody", body);
	}

	public Body getSentBody(HashMap param) throws SQLException {
		return (Body) this.sqlMapClient.queryForObject("SentEmailBody.getBody", param);
	}

	public Body getReceivedBody(HashMap param) throws SQLException {
		return (Body) this.sqlMapClient.queryForObject("ReceivedEmailBody.getBody", param);
	}

}