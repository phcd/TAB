package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.PeekdomUser;

import java.sql.SQLException;
import java.util.HashMap;

public class PeekdomORMap extends BaseORMap {
    public PeekdomORMap(boolean useMasterForUser) {
        super(useMasterForUser);
    }

    public PeekdomUser getUser(HashMap<String, String> params) throws SQLException {
        return (PeekdomUser) this.sqlMapClient.queryForObject("PeekdomUser.getUser", params);
    }
}
