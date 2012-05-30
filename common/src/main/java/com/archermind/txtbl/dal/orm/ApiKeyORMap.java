package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.ApiKey;


import java.sql.SQLException;

public class ApiKeyORMap extends BaseORMap {

    public ApiKeyORMap (boolean useMaster) {
        super(useMaster);
    }

    public ApiKeyORMap() {
        super();
    }

    public int addApiKey(ApiKey apikey) throws SQLException {
        return this.sqlMapClient.update("ApiKey.addApiKey", apikey);
    }

    public ApiKey getApiKeyById(String id) throws SQLException {
        return (ApiKey) this.sqlMapClient.queryForObject("ApiKey.getApiKeyById", id);
    }

    public ApiKey getApiKeyByAccessKey(String access_key) throws SQLException {
        return (ApiKey) this.sqlMapClient.queryForObject("ApiKey.getApiKeyByAccessKey", access_key);
    }

    public int deleteApiKeyByUserId(String user_id) throws SQLException {
        return this.sqlMapClient.delete("ApiKey.deleteApiKeyByUserId", user_id);
    }

}
