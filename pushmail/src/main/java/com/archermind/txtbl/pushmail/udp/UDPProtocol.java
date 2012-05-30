package com.archermind.txtbl.pushmail.udp;


import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class UDPProtocol
{

    private static Logger log = Logger.getLogger(UDPProtocol.class);

    public static synchronized void topicNotify(String ip, int port, byte[] transfer)
    {
        if(log.isTraceEnabled())
            log.trace(String.format("topicNotify(ip=%s, port=%s byte=%s)", ip, String.valueOf(port), new String(transfer)));

        long start = System.nanoTime();

        log.debug(String.format("pushmail sending udp %s notification to %s:%s", transfer[0], ip, port));

        DatagramSocket ds = null;

        try
        {
            InetAddress target = InetAddress.getByName(ip);
            ds = new DatagramSocket(9990);
            ds.setSoTimeout(1000);
            DatagramPacket op = new DatagramPacket(transfer, transfer.length, target, port);
            ds.send(op);
            log.debug(String.format("pushmail sending udp %s notification to %s:%s completed", transfer[0], ip, port));
        }
        catch (SocketException e)
        {
            log.error(String.format("pushmail unable to send udp packet to %s", ip), e);
        }
        catch (IOException e)
        {
            log.error(String.format("pushmail unable to send udp packet to %s, unexpected io error", ip), e);
        }
        finally
        {
            try
            {
                if (ds != null)
                {
                    ds.close();
                }
            }
            catch (Throwable t)
            {
                log.fatal(String.format("Unable to close datagram socket during upd transfer of %s to %s:%s", transfer[0], ip, port), t);
            }

            log.info(String.format("pushmail sent udp %s notification to %s:%s in %s millis", transfer[0], ip, port, (System.nanoTime() - start) / 1000000));
        }
    }
}
