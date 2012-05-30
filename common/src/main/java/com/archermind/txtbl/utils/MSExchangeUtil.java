package com.archermind.txtbl.utils;

import com.webalgorithm.exchange.ConnectionPick;
import com.webalgorithm.exchange.ExchangeClient;
import com.webalgorithm.exchange.ExchangeClientFactory;
import com.webalgorithm.exchange.ExchangeConnectionMode;

public class MSExchangeUtil {

    public static ConnectionPick getExchangeClient(String mode, String host, String portString, String ts, String email, String loginName, String password, String prefix, String fbaPath) {
        if (!StringUtils.isEmpty(mode)) {
            ExchangeConnectionMode connectionMode = ExchangeConnectionMode.modes.get(mode);
            ExchangeClient exchangeClient = getExchangeClient(connectionMode, host, portString, ts, email, loginName, password, prefix, fbaPath);
            return new ConnectionPick(exchangeClient, connectionMode);
        } else {
            return MSExchangeUtil.pickConnection(host, portString, ts, email, loginName, password, prefix, fbaPath);
        }
    }

    public static ConnectionPick pickConnection(String host, String portString, String ts, String email, String loginName, String password, String prefix, String fbaPath) {
        int port = getPort(portString);
        boolean useSSL = useSSL(ts);

        return ExchangeClientFactory.pickConnection(host, port, useSSL, email, loginName, password, prefix, fbaPath);

    }

    public static boolean useSSL(String ts) {
        return "ssl".equals(ts);
    }

    public static int getPort(String portString) {
        int port = 443;
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException ignored) {
        }
        return port;
    }

    public static ExchangeClient getExchangeClient(ExchangeConnectionMode mode, String host, String portString, String ts, String email, String loginName, String password, String prefix, String fbaPath) {
        int port = getPort(portString);
        boolean useSSL = useSSL(ts);

        return mode.getExchangeClient(host, port, useSSL, email, loginName, password, prefix, fbaPath);
    }

}
