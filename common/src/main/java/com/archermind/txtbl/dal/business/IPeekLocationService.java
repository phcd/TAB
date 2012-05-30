package com.archermind.txtbl.dal.business;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.*;

import java.util.Date;
import java.util.List;

/**
 * Peek Location Service 
 */
public interface IPeekLocationService {
	void updateLocation(Device device)  throws DALException;
	
	/**
	 * Registers a user for a service. This creates a pending subscription which is not
	 * active until the user has confirmed the subscription. 
	 * 
	 * @param key The Geo Location Service provider Peek Location Service access key.
	 * @param contentProviderId The UUID of the content service provider.
	 * @param contentProviderName The name of the content service provider.
	 * @param optInUrl The URL used to register for the service.
	 * @param optOutUrl The URL used to unregister from the service.
	 * @param peekEmail The peek email address used to register for the service.
	 * 
	 * @return LocationServiceResponse containing operation status.
	 */
    LocationServiceResponse optIn(String key, String contentProviderId, String contentProviderName, String optInUrl, String optOutUrl, String peekEmail);
    
    /**
     * Unregisters a user from a service.
	 * @param key The Geo Location Service provider Peek Location Service access key.
     * @param uuid The user's service-specific UUID, which was created during registration.
     * @param peekEmail The peek email address used to register for the service.
	 * 
	 * @return LocationServiceResponse containing operation status.
     */
    LocationServiceResponse optOut(String key, String uuid, String peekEmail);

    /**
     * Returns the last reported location of the device having the given IMEI.
     *
	 * @param key The Geo Location Service provider Peek Location Service access key.
     * @param email The peekster's email address.
     *
     * @return A location record or null if one does not exist.
     */
    LocationServiceResponse locate(String key, String email);

    /**
     * Returns the last reported location of the device having the given IMEI.
     *
     * @param email The peekster's email address.
     *
     * @return A location record or null if one does not exist.
     */
    PeekLocation locate(String email);

    /**
     * Returns all peek users who have subscribed to a content service and whose
     * subscriptions are active (have not opted out).
     * 
     * @return A list of the subscribers.
     */
	List<ContentServiceSubscriber> getAllActiveSubscribers();
	
	/**
	 * Confirms a user's subscription to a content provider service. 
	 * 
	 * @param contentProviderId The content provider UUID.
	 * @param peekEmail The peek email used to register for the service.
	 */
	void confirmSubscription(String contentProviderId, String peekEmail);

    /**
     * Sends an email to a Peekster.
     *
     * @param key The caller's PeekAPI key.
     * @param type The message type (SMS || EMAIL).
     * @param mailDate The mail sent date.*
     * @param fromEmailAddress The "from" email address.
     * @param fromAlias  The from alias name.
     * @param peekEmailAddress The Peekster's email address.
     * @param subject The message subject line.
     * @param body The message body.
     * @param push True if the Peekster should be notified immediately.
     */
    LocationServiceResponse poke(String key, String type, Date mailDate, String fromEmailAddress, String fromAlias, String peekEmailAddress, String subject, String body, boolean push);

    int createExchangeLogin(String accountName, String loginName);

    Date getLastUpdatedTimestamp(String user_id) throws DALException;
}
