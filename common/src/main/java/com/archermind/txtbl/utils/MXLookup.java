package com.archermind.txtbl.utils;

import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.DirContext;

public class MXLookup
{
    private static final Logger log = Logger.getLogger(MXLookup.class.getName());

    public static void main(String args[])
    {
        for (String mailHost : lookupMailHosts("wiki.com"))
        {
            System.out.println(mailHost);
        }
    }

    // returns a String array of mail exchange servers (mail hosts)
    // sorted from most preferred to least preferred
    public static String[] lookupMailHosts(String domainName)
    {
        if(log.isTraceEnabled())
            log.trace(String.format("lookupMailHosts(domainName=%s)", domainName));
        try
        {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            env.put("com.sun.jndi.dns.timeout.initial", "1000");
            env.put("com.sun.jndi.dns.timeout.retries", "2");
            DirContext iDirC = new InitialDirContext(env);
            // get the MX records from the default DNS directory service provider
            // NamingException thrown if no DNS record found for domainName
            Attributes attributes = iDirC.getAttributes("dns:/" + domainName, new String[]{"MX"});
            // attributeMX is an attribute ('list') of the Mail Exchange(MX)
            // Resource Records(RR)
            Attribute attributeMX = attributes.get("MX");

            // if there are no MX RRs then default to domainName (see: RFC 974)
            if (attributeMX == null)
            {
                return (new String[]{domainName});
            }

            // split MX RRs into Preference Values(pvhn[0]) and Host Names(pvhn[1])
            String[][] pvhn = new String[attributeMX.size()][2];
            for (int i = 0; i < attributeMX.size(); i++)
            {
                pvhn[i] = ("" + attributeMX.get(i)).split("\\s+");
            }

            // sort the MX RRs by RR value (lower is preferred)
            Arrays.sort(pvhn, new Comparator<String[]>()
            {
                public int compare(String[] o1, String[] o2)
                {
                    return (Integer.parseInt(o1[0]) - Integer.parseInt(o2[0]));
                }
            });

            // put sorted host names in an array, get rid of any trailing '.'
            String[] sortedHostNames = new String[pvhn.length];
            for (int i = 0; i < pvhn.length; i++)
            {
                sortedHostNames[i] = pvhn[i][1].endsWith(".") ? pvhn[i][1].substring(0, pvhn[i][1].length() - 1) : pvhn[i][1];
            }
            return sortedHostNames;
        }
        catch (NamingException e)
        {
            return new String[0];
        }
    }

}
