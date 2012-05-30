package com.archermind.txtbl.pushmail.redis;

import com.archermind.txtbl.utils.SysConfigManager;
import org.jboss.logging.Logger;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 11-07-18
 * Time: 2:11 PM
 */
public class JedisPushmailPoolFactory {
    private static final Logger log = Logger.getLogger(JedisPushmailPoolFactory.class);
    public static JedisPool jedisPool;
    public static final String redisHost;
    public static final Integer redisPort;
    public static final String redisPushmailChannel;

    static {
        redisHost = SysConfigManager.instance().getValue("redisPushmailHost");
        redisPort = Integer.parseInt(SysConfigManager.instance().getValue("redisPushmailPort"));
        redisPushmailChannel = SysConfigManager.instance().getValue("redisPushmailChannel");
        jedisPool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort);
        log.info("Jedis connection pool initialiazed");
    }


    public static JedisPool getJedisPool() {
        return jedisPool;
    }
}