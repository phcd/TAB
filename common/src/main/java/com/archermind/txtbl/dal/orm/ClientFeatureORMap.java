package com.archermind.txtbl.dal.orm;


import com.archermind.txtbl.domain.ClientFeature;

import java.sql.SQLException;

public class ClientFeatureORMap extends BaseORMap {

    public ClientFeatureORMap(boolean useMaster) {
        super(useMaster);
    }

    public ClientFeatureORMap() {
    }

    public int addClientFeature(ClientFeature clientfeature) throws SQLException {
        return this.sqlMapClient.update("ClientFeature.addClientFeature", clientfeature);
    }

    public ClientFeature getClientFeatureById(String id) throws SQLException {
        return (ClientFeature) this.sqlMapClient.queryForObject("ClientFeature.getClientFeatureById", id);
    }

}
