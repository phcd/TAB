package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.ContactTrack;

import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.util.HashMap;

public class ContactTrackORMap extends BaseORMap {

    private static final Logger logger = Logger.getLogger(ContactTrackORMap.class);

    public ContactTrackORMap(boolean useMaster) {
        super(useMaster);
    }

    public ContactTrackORMap() {}

	public int addContactTrack(ContactTrack contactTrack) {
		try {
			this.sqlMapClient.insert("ContactTrack.addContactTrack", contactTrack);
            return 1;
		} catch (SQLException e) {
			logger.error("addContactTrack error!", e);
            return 0;
		}
	}

	public ContactTrack getContactTrack(HashMap hs) throws SQLException {
        if(logger.isTraceEnabled())
            logger.trace(String.format("getContactTrack(hs=%s)",String.valueOf(hs)));
		return (ContactTrack) this.sqlMapClient.queryForObject("ContactTrack.getContactTrack", hs);
	}

	public int modifyContactTrack(ContactTrack contactTrack) throws SQLException {
		return this.sqlMapClient.update("ContactTrack.modifyContactTrack", contactTrack);
	}

	public int removeContactTrack(HashMap hs) throws SQLException {
		return this.sqlMapClient.delete("ContactTrack.removeContactTrack", hs);
	}

}