package com.archermind.txtbl.pushmail.redis;

import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Device;
import com.archermind.txtbl.domain.PartnerCode;
import com.archermind.txtbl.domain.User;
import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 11-08-03
 * Time: 11:07 AM
 */
public class NewPushManager {
    private static final String redisUuidKeyPrefix = "uuid:";
    private static final String redisMessageValuePrefix = "uuid:";
    private static final String redisMailcheckKeyName = "last_mailcheck";

    private static final Logger log = Logger.getLogger(NewPushManager.class);
    private static final String redisPushmailChannel;
    private static final String supportedClientSwVersions;
    private static JedisPool jedisPool;


    static {
        redisPushmailChannel = SysConfigManager.instance().getValue("redisPushmailChannel");
        supportedClientSwVersions = SysConfigManager.instance().getValue("pushmail.clientsw.version.new");
        jedisPool = JedisPushmailPoolFactory.getJedisPool();
    }

    public boolean isNewClient(String uuid) {
        UserService userservice = new UserService();
        User user = userservice.getPeekAccountIdByID(uuid);
        if (user == null) return false;
        PartnerCode code = user.getPartnerCode();
        log.debug("UUID=" + uuid + " Code=" + code);
        if (code == PartnerCode.qcom || code == PartnerCode.peekint)
            return true;

        Device device = userservice.getDeviceByUserId(uuid);
        String deviceClientswVersion = device.getClientsw() != null ? device.getClientsw().trim() : "";
        log.debug("checking if " + deviceClientswVersion + " is a valid client string for new pushmail");
        return com.archermind.txtbl.utils.StringUtils.isVersionSupported(deviceClientswVersion, supportedClientSwVersions);
    }

    public void sendChar(String uuid, char msg) {
        String userKey = redisUuidKeyPrefix + uuid;
        String messageVal = uuid + ":" + msg;
        log.info(String.format("publishing %s to %s channel for key %s ", messageVal, redisPushmailChannel, userKey));

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.publish(redisPushmailChannel, messageVal);
        } catch (JedisConnectionException e) {
            log.fatal("Cannot connect to Redis instance: ", e);
        } catch (JedisException e) {
            log.error("Redis error: ", e);
        } catch (Exception e) {
            log.error("Error: " + e.getClass() + ":" + e.getMessage(), e);
        } finally {
            if (jedis != null)
                jedisPool.returnResource(jedis);
        }
    }


    public void renewUuid(String uuid) {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss zzzzzzzz");

        String userKey = redisUuidKeyPrefix + uuid;
        String dateValue = format.format(now);
        log.debug(String.format("updating %s with mailcheck value %s", userKey, dateValue));

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (jedis.exists(userKey)) {
                jedis.hset(userKey, redisMailcheckKeyName, dateValue);
            } else {
                log.debug(String.format("Key %s does not have a mapping, not setting mailcheck date", userKey));
            }
        } catch (JedisConnectionException e) {
            log.fatal("Cannot connect to Redis instance: ", e);
        } catch (JedisException e) {
            log.error("Redis error: ", e);
        } catch (Exception e) {
            log.error("Error: " + e.getClass() + ":" + e.getMessage(), e);
        } finally {
            if (jedis != null)
                jedisPool.returnResource(jedis);
        }
    }

}
