package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.Device;
import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class DeviceORMap extends BaseORMap {

    private static final Logger logger = Logger.getLogger(DeviceORMap.class);

    public DeviceORMap(BaseORMap another) {
        super(another);
    }

    public DeviceORMap(boolean useMaster) {
        super(useMaster);
    }

    public DeviceORMap() {
    }

    public int addDevice(Device device) throws SQLException {
        return this.sqlMapClient.update("Device.addDevice", device);
    }

    public String getDeviceCode(HashMap param) throws SQLException {
        return (String) this.sqlMapClient.queryForObject("Device.getCode", param);
    }

    @SuppressWarnings("unchecked")
    public List<Device> selectDevice(String user_id) throws SQLException {
        return this.sqlMapClient.queryForList("Device.selectDevices", user_id);
    }

    public Device getDeviceByUserid(String param) throws SQLException {
        return (Device) this.sqlMapClient.queryForObject(
                "Device.getDeviceByUserid", param);
    }

    public Device getDeviceByid(String param) throws SQLException {
        return (Device) this.sqlMapClient.queryForObject("Device.getDeviceByid",
                param);
    }

    public int modifyDevice(Device param) throws SQLException {
        return this.sqlMapClient.update("Device.modifyDevice", param);
    }

    public int modifyIdDevices(Device param) throws SQLException {
        return this.sqlMapClient.update("Device.modifyIdDevices", param);
    }

    @SuppressWarnings("unchecked")
    public List<Device> getDeviceByIDvcIDSim(String devicecode, String simcode) throws SQLException {
        HashMap<String, String> paras = new HashMap<String, String>();
        paras.put("devicecode", devicecode);
        paras.put("simcode", simcode);
        return this.sqlMapClient.queryForList("Device.getDeviceByIDvcIDSim", paras);
    }

    public String getUserIdByDeviceCode(String deviceCode) throws SQLException {
        HashMap<String, String> param = new HashMap<String, String>();
        param.put("deviceCode", deviceCode);
        return (String) this.sqlMapClient.queryForObject("Device.getUserIdByDeviceCode", param);
    }

    public int modifyDevicePin(HashMap param) throws SQLException {
        return this.sqlMapClient.update("Device.modifyDevicePin", param);
    }

    public String getUserId(Device device) throws SQLException {
        return (String) this.sqlMapClient.queryForObject("Device.getUserId", device);
    }
}