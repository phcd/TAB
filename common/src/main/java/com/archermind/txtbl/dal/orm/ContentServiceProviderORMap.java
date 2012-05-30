package com.archermind.txtbl.dal.orm;

import java.sql.SQLException;
import java.util.HashMap;

import com.archermind.txtbl.domain.ContentServiceProvider;
import com.archermind.txtbl.domain.GeoLocationServiceProvider;

public class ContentServiceProviderORMap extends BaseORMap {

    public ContentServiceProviderORMap(BaseORMap another) {
        super(another);
    }

    public ContentServiceProviderORMap() {}

	public int createContentServiceProvider(ContentServiceProvider contentServiceProvider) throws SQLException {
		int rc = this.sqlMapClient.update("ContentServiceProvider.addContentServiceProvider", contentServiceProvider);
		int lastId = (int) (Long.parseLong(((getSqlMapClient().queryForObject("Global.getLastInsertId")).toString())));
		contentServiceProvider.setId(lastId);
		
		return rc;
	}

	public ContentServiceProvider getContentServiceProviderById(int id) throws SQLException {
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("id", id);		
		ContentServiceProvider contentServiceProvider = (ContentServiceProvider) this.sqlMapClient.queryForObject("ContentServiceProvider.getContentServiceProviderById", param);
		
		GeoLocationServiceProviderORMap geoLocationServiceProviderORMap = new GeoLocationServiceProviderORMap();
		GeoLocationServiceProvider geoLocationServiceProvider = geoLocationServiceProviderORMap.getGeoLocationServiceProviderById(contentServiceProvider.getGeoLocationServiceProviderId());
		contentServiceProvider.setGeoLocationServiceProvider(geoLocationServiceProvider);
		
		return contentServiceProvider;
	}

	public ContentServiceProvider getContentServiceProviderByCpid(int geoLocServiceProviderId, String cpid) throws SQLException {
		HashMap<String, String> param = new HashMap<String, String>();
        param.put("geo_loc_svc_provider_id", String.valueOf(geoLocServiceProviderId));
        param.put("cpid", cpid);
		ContentServiceProvider contentServiceProvider = (ContentServiceProvider) this.sqlMapClient.queryForObject("ContentServiceProvider.getContentServiceProviderByCpid", param);

        if (contentServiceProvider != null) {
            GeoLocationServiceProviderORMap geoLocationServiceProviderORMap = new GeoLocationServiceProviderORMap();
            GeoLocationServiceProvider geoLocationServiceProvider = geoLocationServiceProviderORMap.getGeoLocationServiceProviderById(contentServiceProvider.getGeoLocationServiceProviderId());
            contentServiceProvider.setGeoLocationServiceProvider(geoLocationServiceProvider);
        }
		
		return contentServiceProvider;
	}

}