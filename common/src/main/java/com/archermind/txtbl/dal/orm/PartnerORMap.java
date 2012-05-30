package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.Partner;
import redis.clients.jedis.Client;

import java.sql.SQLException;

public class PartnerORMap extends BaseORMap {


    public PartnerORMap(boolean useMaster) {
        super(useMaster);
    }

    public PartnerORMap() {
    }

    public int addPartner(Partner partner) throws SQLException {
        return this.sqlMapClient.update("Partner.addPartner", partner);
    }

    public Partner getPartnerById(String id) throws SQLException {
        return (Partner) this.sqlMapClient.queryForObject("Partner.getPartnerById", id);
    }


}
