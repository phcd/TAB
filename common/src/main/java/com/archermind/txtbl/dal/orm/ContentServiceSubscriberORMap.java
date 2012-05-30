package com.archermind.txtbl.dal.orm;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import com.archermind.txtbl.domain.ContentServiceProvider;
import com.archermind.txtbl.domain.ContentServiceSubscriber;

public class ContentServiceSubscriberORMap extends BaseORMap {

    public ContentServiceSubscriberORMap(boolean useMaster) {
        super(useMaster);
    }

    public ContentServiceSubscriberORMap() {}

    /**
	 * Creates a new content service subscriber.
	 * 
	 * @param contentServiceSubscriber The content service subscriber to create.
	 * @return 1-successful, 0-failed
	 * @throws SQLException
	 */
	public int createContentServiceSubscriber(ContentServiceSubscriber contentServiceSubscriber) throws SQLException {
		int rc = this.sqlMapClient.update("ContentServiceSubscriber.addContentServiceSubscriber", contentServiceSubscriber);
		int lastId = (int) (Long.parseLong(((getSqlMapClient().queryForObject("Global.getLastInsertId")).toString())));
		contentServiceSubscriber.setId(lastId);
		return rc;
	}

	/**
	 * Returns the content service subscriber identified by 'email'.
	 * @return email The service subscriber email
	 * @throws SQLException
	 */
    @SuppressWarnings("unchecked")
	public List<ContentServiceSubscriber> getContentServiceSubscriberByEmail(String email) throws SQLException {
		HashMap<String, String> param = new HashMap<String, String>();
		param.put("email", email);		
		List<ContentServiceSubscriber> contentServiceSubscribers = this.sqlMapClient.queryForList("ContentServiceSubscriber.getContentServiceSubscriberByEmail", param);
		
		for (ContentServiceSubscriber contentServiceSubscriber : contentServiceSubscribers) {			
			ContentServiceProviderORMap contentServiceProviderORMap = new ContentServiceProviderORMap(this);
			ContentServiceProvider contentServiceProvider = contentServiceProviderORMap.getContentServiceProviderById(contentServiceSubscriber.getContentServiceProviderId());
			contentServiceSubscriber.setContentServiceProvider(contentServiceProvider);
		}
		
		return contentServiceSubscribers;
	}

	/**
	 * Returns the content service subscriber identified by 'uuid'.
	 * @return uuid The service subscriber uuid
	 * @throws SQLException
	 */
    @SuppressWarnings("unchecked")
	public ContentServiceSubscriber getContentServiceSubscriberByUuid(String uuid) throws SQLException {
		HashMap<String, String> param = new HashMap<String, String>();
		param.put("uuid", uuid);	
		
		ContentServiceSubscriber contentServiceSubscriber = (ContentServiceSubscriber) this.sqlMapClient.queryForObject("ContentServiceSubscriber.getContentServiceSubscriberByUuid", param);
		populateContentServiceProvider(contentServiceSubscriber);		
		return contentServiceSubscriber;
	}

    @SuppressWarnings("unchecked")
	public List<ContentServiceSubscriber> getAllActiveSubscribers() throws SQLException {
		List<ContentServiceSubscriber> contentServiceSubscribers = this.sqlMapClient.queryForList("ContentServiceSubscriber.getAllActiveSubscribers");		
		populateContentServiceProvider(contentServiceSubscribers);
		
		return contentServiceSubscribers;
	}
	
	public int updateContentServiceSubscriber(ContentServiceSubscriber contentServiceSubscriber) throws SQLException {
		return this.sqlMapClient.update("ContentServiceSubscriber.updateContentServiceSubscriber", contentServiceSubscriber);
	}
	
	private void populateContentServiceProvider(List<ContentServiceSubscriber> contentServiceSubscribers) throws SQLException {
		for (ContentServiceSubscriber contentServiceSubscriber : contentServiceSubscribers) {			
			populateContentServiceProvider(contentServiceSubscriber);
		}		
	}

	private void populateContentServiceProvider(ContentServiceSubscriber contentServiceSubscriber) throws SQLException {
		ContentServiceProviderORMap contentServiceProviderORMap = new ContentServiceProviderORMap();
		ContentServiceProvider contentServiceProvider = contentServiceProviderORMap.getContentServiceProviderById(contentServiceSubscriber.getContentServiceProviderId());
		contentServiceSubscriber.setContentServiceProvider(contentServiceProvider);
	}
	
}