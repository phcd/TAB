package com.archermind.txtbl.dal.orm;

import java.sql.SQLException;
import java.util.HashMap;

import com.archermind.txtbl.domain.GeoLocationServiceProvider;

public class GeoLocationServiceProviderORMap extends BaseORMap {

    public GeoLocationServiceProviderORMap(boolean useMaster) {
        super(useMaster);
    }

    public GeoLocationServiceProviderORMap() {}

    /**
	 * Creates a new geo-location service provider.
	 * 
	 * @param geoLocationServiceProvider The geo-location service provider to create.
	 * @return 1-successful, 0-failed
	 * @throws SQLException
	 */
	public int createGeoLocationServiceProvider(GeoLocationServiceProvider geoLocationServiceProvider) throws SQLException {
		int rc = this.sqlMapClient.update("GeoLocationServiceProvider.addGeoLocationServiceProvider", geoLocationServiceProvider);
		int lastId = (int) (Long.parseLong(((getSqlMapClient().queryForObject("Global.getLastInsertId")).toString())));
		geoLocationServiceProvider.setId(lastId);
		return rc;
	}

	public GeoLocationServiceProvider getGeoLocationServiceProviderById(int id) throws SQLException {
		HashMap<String, Integer> param = new HashMap<String, Integer>();
		param.put("id", id);		
		return (GeoLocationServiceProvider) this.sqlMapClient.queryForObject("GeoLocationServiceProvider.getGeoLocationServiceProviderById", param);
	}

	public GeoLocationServiceProvider getGeoLocationServiceProviderByKey(String key) throws SQLException {
		HashMap<String, String> param = new HashMap<String, String>();
		param.put("key", key);		
		return (GeoLocationServiceProvider) this.sqlMapClient.queryForObject("GeoLocationServiceProvider.getGeoLocationServiceProviderByKey", param);
	}

	public int updateGeoLocationServiceProvider(GeoLocationServiceProvider geoLocationServiceProvider) throws SQLException {
		return this.sqlMapClient.update("GeoLocationServiceProvider.updateGeoLocationServiceProvider", geoLocationServiceProvider);
	}

}