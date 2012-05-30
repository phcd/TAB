package com.archermind.txtbl.utils;

import org.junit.Test;
import org.junit.Assert;
import com.archermind.txtbl.domain.Server;
import com.archermind.txtbl.domain.Protocol;
import com.archermind.txtbl.domain.Country;

import java.util.ArrayList;
import java.util.List;

public class UtilsToolsTest {
    @Test
    public void filterXobniServers() {

        ArrayList<Server> servers = new ArrayList<Server>();
        Server genericServer1 = new Server();
        Server genericServer2 = new Server();
        Server xobniServer1 = new Server();
        xobniServer1.setReceiveProtocolType(Protocol.XOBNI_IMAP_IDLE);
        Server xobniServer2 = new Server();
        xobniServer2.setReceiveProtocolType(Protocol.XOBNI_OAUTH_IDLE);
        servers.add(genericServer1);
        servers.add(genericServer2);
        servers.add(xobniServer1);
        servers.add(xobniServer2);

        List<Server> filteredServerList = UtilsTools.filterXobniServers(servers, Country.US);
        Assert.assertEquals(2, filteredServerList.size());
        Assert.assertEquals(genericServer1, filteredServerList.get(0));
        Assert.assertEquals(genericServer2, filteredServerList.get(1));

        filteredServerList = UtilsTools.filterXobniServers(servers, Country.Xobni);
        Assert.assertEquals(4, filteredServerList.size());
        Assert.assertEquals(xobniServer1, filteredServerList.get(0));
        Assert.assertEquals(xobniServer2, filteredServerList.get(1));
        Assert.assertEquals(genericServer1, filteredServerList.get(2));
        Assert.assertEquals(genericServer2, filteredServerList.get(3));
    }

    @Test
    public void containsIdleProtocol() {
        String[] protocols = new String[]{Protocol.EXCHANGE, Protocol.GMAIL_IMAP};
        Assert.assertFalse(UtilsTools.containsIdleProtocol(protocols));

        protocols = new String[]{Protocol.EXCHANGE, Protocol.GMAIL_IMAP, Protocol.IMAP_IDLE};
        Assert.assertTrue(UtilsTools.containsIdleProtocol(protocols));
    }

}
