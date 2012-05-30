package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.Device;
import com.archermind.txtbl.domain.PeekLocation;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PeekLocationORMap extends BaseORMap {
    private static final Logger logger = Logger.getLogger(PeekLocationORMap.class);

	private static final int DEFAULT_MOBILE_COUNTRY_CODE = 310; // USA
	private static final int DEFAULT_MOBILE_NETWORK_CODE = 260; // T-MOBILE

    private static boolean userMasterForPeekLocationReads;

    static {
       userMasterForPeekLocationReads =  Boolean.parseBoolean(SysConfigManager.instance().getValue("userMasterForPeekLocationReads",  "false"));
    }

    public PeekLocationORMap(boolean useMaster) {
        super(useMaster);
    }

    public PeekLocationORMap() {}

	public int updateLocation(Device device) throws SQLException {
        if(logger.isTraceEnabled())
            logger.trace(String.format("updateLocation(device=%s)", String.valueOf(device)));

		logger.info("Updating location for device: " + device);

		int rc;

		PeekLocation newLocation = createPeekLocationFromDevice(device);

        PeekLocation peekLocation = new PeekLocationORMap(userMasterForPeekLocationReads).getPeekLocationByImei(device.getDeviceCode());

		if (peekLocation != null) {
			// location record exists - we just need to update it if the user's location has changed
				peekLocation.setCellId(newLocation.getCellId());
				peekLocation.setLacId(newLocation.getLacId());
				peekLocation.setMnc(newLocation.getMnc());
				peekLocation.setMcc(newLocation.getMcc());
				peekLocation.setLastUpdateTimestamp(newLocation.getLastUpdateTimestamp());
				rc = sqlMapClient.update("PeekLocation.updatePeekLocation", peekLocation);
				logger.info("Location record udpated successfully for: " + device.getDeviceCode());
		} else {
			// create new location record
			rc = sqlMapClient.update("PeekLocation.addPeekLocation", newLocation);
			logger.info("Location record created successfully for: " + device.getDeviceCode());
		}
		
		return rc;
	}

    @SuppressWarnings("unchecked")
	public PeekLocation getPeekLocationByImei(String imei) throws SQLException {
        if(logger.isTraceEnabled())
            logger.trace(String.format("getPeekLocationByImei(imei=%s)", String.valueOf(imei) ));

		HashMap<String, String> param = new HashMap<String, String>();
		param.put("imei", imei);		
        List<PeekLocation> peekLocationList = sqlMapClient.queryForList("PeekLocation.getPeekLocation", param);
        if(logger.isTraceEnabled())
            logger.trace(String.format("peekLocationList=%s", String.valueOf(peekLocationList) ));
        
        // there should be one entry per IMEI - defect 4518 implies otherwise!!
        return peekLocationList != null && peekLocationList.size() >= 1 ? peekLocationList.get(0) : null;
	}
	
	public PeekLocation getPeekLocationByEmail(String email) throws SQLException {
		HashMap<String, String> param = new HashMap<String, String>();
		param.put("name", email);		
		String imei = (String) sqlMapClient.queryForObject("PeekLocation.getDeviceCodeForAccount", param);
		if (imei != null) {
			return getPeekLocationByImei(imei);
		} else {
			logger.warn("Unable to retrieve device code (IMEI) for account " + email);
		}
		return null;
	}
	
	private PeekLocation createPeekLocationFromDevice(Device device) {
		PeekLocation peekLocation = new PeekLocation();

		peekLocation.setImei(device.getDeviceCode());
		
		String lacId = device.getLacid();
		if (lacId != null && lacId.length() > 0) { 
			peekLocation.setLacId(Integer.valueOf(lacId));
		}

		String cellId = device.getCellid();
		if (cellId != null && cellId.length() > 0) { 
			peekLocation.setCellId(Integer.valueOf(cellId));
		}
		
		/*
		 * Every mobile network (GSM, CDMA, UMTS, etc.) has a unique 5 or 6 digit number called
		 * a MNC (Mobile Network Code) that identifies the network.
		 * 
		 * The first 3 numbers of the MNC is the Mobile Country Code (MCC). The next 2 or 3 numbers
		 * is the operator specific code (ex: T-Mobile USA has a MNC 310800).
		 */
		// TODO - how is the MNC obtained from the Device?
		String mnc = null;
		
		if (mnc != null && mnc.length() > 3) {
			peekLocation.setMnc(Integer.valueOf(mnc));
			peekLocation.setMcc(Integer.valueOf(mnc.substring(0, 3)));
		} else {
			peekLocation.setMnc(DEFAULT_MOBILE_NETWORK_CODE);
			peekLocation.setMcc(DEFAULT_MOBILE_COUNTRY_CODE);
		}
		
		peekLocation.setLastUpdateTimestamp(new Timestamp(System.currentTimeMillis()));
		
		return peekLocation;
	}

    public Date getLastUpdatedTimestamp(String user_id) throws SQLException {
		HashMap<String, String> param = new HashMap<String, String>();
		param.put("user_id", user_id);
        String result = (String) sqlMapClient.queryForObject("PeekLocation.getLastUpdateTimestampByUserId", param);

        if (result == null || "".equals(result))
            return null;

        return Timestamp.valueOf(result);
	}

}