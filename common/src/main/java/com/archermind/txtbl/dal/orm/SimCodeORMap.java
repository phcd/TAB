package com.archermind.txtbl.dal.orm;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import com.archermind.txtbl.domain.SimCode;

public class SimCodeORMap extends BaseORMap {

    public SimCodeORMap(boolean useMaster) {
        super(useMaster);
    }

    @SuppressWarnings("unchecked")
    public List<SimCode> loadAllSimCode(HashMap hmap) throws SQLException {
		return this.sqlMapClient.queryForList("SimCode.loadAllSimCode", hmap);
	}

}
