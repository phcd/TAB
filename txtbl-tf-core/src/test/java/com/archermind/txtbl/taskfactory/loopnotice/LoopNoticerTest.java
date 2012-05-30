package com.archermind.txtbl.taskfactory.loopnotice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.archermind.txtbl.domain.SysConfig;
import com.archermind.txtbl.taskfactory.test.Assert2Array;
import com.archermind.txtbl.taskfactory.test.AssertArray;
import com.archermind.txtbl.taskfactory.test.PrivateMethod;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class LoopNoticerTest extends TestCase {

	public void testRemoveSubscribeProtocols() {
		List<String> nameOfProtocols = new ArrayList<String>();
		nameOfProtocols.add("pop3");
		nameOfProtocols.add("imap");
		nameOfProtocols.add("yahooimap");
		nameOfProtocols.add("yahoo");
		nameOfProtocols.add("hotmail");
		nameOfProtocols.add("hotmailmsp");
		nameOfProtocols.add("gmailpop3");
		nameOfProtocols.add("new");

		List<String> nameOfProtocolsResult = new ArrayList<String>();
		nameOfProtocolsResult.add("imap");
		nameOfProtocolsResult.add("yahooimap");
		nameOfProtocolsResult.add("hotmailmsp");
		nameOfProtocolsResult.add("gmailpop3");
		nameOfProtocolsResult.add("new");
		String[] subscribeArray = new String[] { "hotmail:8", "msp,yahoo:9",
				"pop3:7" };

		LoopNoticer loopNoticer = new LoopNoticer();
		PrivateMethod.doTest(LoopNoticer.class, "removeSubscribeProtocols",
				loopNoticer, new Class[] { List.class, String[].class },
				new Object[] { nameOfProtocols, subscribeArray });
		assertEquals(nameOfProtocols, nameOfProtocolsResult);

		subscribeArray = new String[] { "hotmail:8" };

		nameOfProtocols.clear();
		nameOfProtocols.add("pop3");
		nameOfProtocols.add("imap");
		nameOfProtocols.add("yahooimap");
		nameOfProtocols.add("yahoo");
		nameOfProtocols.add("hotmail");
		nameOfProtocols.add("hotmailmsp");
		nameOfProtocols.add("gmailpop3");
		nameOfProtocols.add("new");

		nameOfProtocolsResult.clear();
		nameOfProtocolsResult.add("pop3");
		nameOfProtocolsResult.add("imap");
		nameOfProtocolsResult.add("yahooimap");
		nameOfProtocolsResult.add("yahoo");
		nameOfProtocolsResult.add("hotmailmsp");
		nameOfProtocolsResult.add("gmailpop3");
		nameOfProtocolsResult.add("new");

		PrivateMethod.doTest(LoopNoticer.class, "removeSubscribeProtocols",
				loopNoticer, new Class[] { List.class, String[].class },
				new Object[] { nameOfProtocols, subscribeArray });
		assertEquals(nameOfProtocols, nameOfProtocolsResult);
	}

	@SuppressWarnings("unchecked")
	public void testGetNameOfProtocols() {
		//
		String[] allProtocols = new String[] { "pop3", "imap", "test",
				"yahooimap", "yahoo", "hotmail", "hotmailmsp", "gmailpop3",
				"new" };
		String[] subscribeArray = new String[] { "test,pop4:5",
				"gmailpop3:333", "yahoo,hotmail,gmailpop3:444",
				"no,meiyou,haha,heihei:77" };
		LoopNoticer loopNoticer = new LoopNoticer();

		@SuppressWarnings("unused")
		List<String> nameOfProtocols = (List<String>) PrivateMethod.doTest(
				LoopNoticer.class, "getNameOfProtocols", loopNoticer,
				new Class[] { String[].class, String[].class }, new Object[] {
						allProtocols, subscribeArray });

		List<String> exception = new ArrayList<String>();
		exception.add("pop3");
		exception.add("imap");
		exception.add("yahooimap");
		exception.add("hotmailmsp");
		exception.add("new");

		assertEquals(nameOfProtocols, exception);

		//
		subscribeArray = new String[] { "imap:10" };

		nameOfProtocols = (List<String>) PrivateMethod.doTest(
				LoopNoticer.class, "getNameOfProtocols", loopNoticer,
				new Class[] { String[].class, String[].class }, new Object[] {
						allProtocols, subscribeArray });

		exception.clear();
		exception.add("pop3");
		exception.add("test");
		exception.add("yahooimap");
		exception.add("yahoo");
		exception.add("hotmail");
		exception.add("hotmailmsp");
		exception.add("gmailpop3");
		exception.add("new");

		assertEquals(nameOfProtocols, exception);

		// allProtocols=null
		allProtocols = null;
		subscribeArray = new String[] { "imap:10" };

		nameOfProtocols = (List<String>) PrivateMethod.doTest(
				LoopNoticer.class, "getNameOfProtocols", loopNoticer,
				new Class[] { String[].class, String[].class }, new Object[] {
						allProtocols, subscribeArray });

		assertNull(nameOfProtocols);

		// subscribeArray=null
		allProtocols = new String[] { "pop3", "imap", "test", "yahooimap",
				"yahoo", "hotmail", "hotmailmsp", "gmailpop3", "new" };
		subscribeArray = null;

		nameOfProtocols = (List<String>) PrivateMethod.doTest(
				LoopNoticer.class, "getNameOfProtocols", loopNoticer,
				new Class[] { String[].class, String[].class }, new Object[] {
						allProtocols, subscribeArray });

		exception.clear();
		exception.add("pop3");
		exception.add("imap");
		exception.add("test");
		exception.add("yahooimap");
		exception.add("yahoo");
		exception.add("hotmail");
		exception.add("hotmailmsp");
		exception.add("gmailpop3");
		exception.add("new");

		assertEquals(exception, nameOfProtocols);

		// allProtocols=null&&subscribeArray=null
		allProtocols = null;
		subscribeArray = null;

		nameOfProtocols = (List<String>) PrivateMethod.doTest(
				LoopNoticer.class, "getNameOfProtocols", loopNoticer,
				new Class[] { String[].class, String[].class }, new Object[] {
						allProtocols, subscribeArray });

		assertNull(nameOfProtocols);
	}

	public void testFiltGroups() {
		//
		String[][] groups = new String[][] { { "pop3" }, { "imap" },
				{ "yahooimap", "yahoo" }, { "hotmail", "hotmailmsp" },
				{ "gmailpop3" }, { "new" } };
		String[] subscribeArray = new String[] { "hotmail:8", "msp,yahoo:9",
				"pop3:7" };
		String[][] exception = new String[][] { { "imap" }, { "yahooimap" },
				{ "hotmailmsp" }, { "gmailpop3" }, { "new" } };

		LoopNoticer loopNoticer = new LoopNoticer();
		String[][] result = (String[][]) PrivateMethod.doTest(
				LoopNoticer.class, "filtGroups", loopNoticer, new Class[] {
						String[][].class, String[].class }, new Object[] {
						groups, subscribeArray });

		Assert2Array.assertEquals(exception, result);

		// groups=null
		groups = null;
		subscribeArray = new String[] { "hotmail:8", "msp,yahoo:9", "pop3:7" };
		loopNoticer = new LoopNoticer();
		result = (String[][]) PrivateMethod.doTest(LoopNoticer.class,
				"filtGroups", loopNoticer, new Class[] { String[][].class,
						String[].class },
				new Object[] { groups, subscribeArray });

		assertNull(result);

		// subscribeArray=null
		groups = new String[][] { { "pop3" }, { "imap" },
				{ "yahooimap", "yahoo" }, { "hotmail", "hotmailmsp" },
				{ "gmailpop3" }, { "new" } };
		exception = new String[][] { { "pop3" }, { "imap" },
				{ "yahooimap", "yahoo" }, { "hotmail", "hotmailmsp" },
				{ "gmailpop3" }, { "new" } };
		subscribeArray = null;
		loopNoticer = new LoopNoticer();
		result = (String[][]) PrivateMethod.doTest(LoopNoticer.class,
				"filtGroups", loopNoticer, new Class[] { String[][].class,
						String[].class },
				new Object[] { groups, subscribeArray });

		Assert2Array.assertEquals(exception, result);

		// groups=null&&subscribeArray=null
		groups = null;
		subscribeArray = null;
		loopNoticer = new LoopNoticer();
		result = (String[][]) PrivateMethod.doTest(LoopNoticer.class,
				"filtGroups", loopNoticer, new Class[] { String[][].class,
						String[].class },
				new Object[] { groups, subscribeArray });
		assertNull(result);

		// groups
		groups = new String[][] { {}, { "" }, { "", "" }, { "", "", "msp" },
				{ "", "", "msp", "bbb" }, { "pop3" }, { "imap" },
				{ "yahooimap", "yahoo" }, { "hotmail", "hotmailmsp" },
				{ "gmailpop3" }, { "new" } };
		subscribeArray = new String[] { "hotmail:8", "msp,yahoo:9", "pop3:7" };
		exception = new String[][] { { "bbb" }, { "imap" }, { "yahooimap" },
				{ "hotmailmsp" }, { "gmailpop3" }, { "new" } };

		loopNoticer = new LoopNoticer();
		result = (String[][]) PrivateMethod.doTest(LoopNoticer.class,
				"filtGroups", loopNoticer, new Class[] { String[][].class,
						String[].class },
				new Object[] { groups, subscribeArray });

		Assert2Array.assertEquals(exception, result);
	}

	public void testGetGroups() {
		//
		String groupOfProtocols = "pop3;imap;yahooimap,yahoo;hotmail,hotmailmsp;gmailpop3,new";
		String[] subscribeArray = new String[] { "msp:9", "imap,yahoo,aol:0",
				"hotmail,gmail:7" };
		String[][] exception = new String[][] { { "pop3" }, { "yahooimap" },
				{ "hotmailmsp" }, { "gmailpop3", "new" } };
		LoopNoticer loopNoticer = new LoopNoticer();

		String[][] result = (String[][]) PrivateMethod.doTest(
				LoopNoticer.class, "getGroups", loopNoticer, new Class[] {
						String.class, String[].class }, new Object[] {
						groupOfProtocols, subscribeArray });
		Assert2Array.assertEquals(exception, result);

		// groupOfProtocols=null
		groupOfProtocols = null;
		subscribeArray = new String[] { "msp:9", "imap,yahoo,aol:0",
				"hotmail,gmail:7" };
		loopNoticer = new LoopNoticer();

		result = (String[][]) PrivateMethod.doTest(LoopNoticer.class,
				"getGroups", loopNoticer, new Class[] { String.class,
						String[].class }, new Object[] { groupOfProtocols,
						subscribeArray });
		assertNull(result);

		// subscribeArray=null
		groupOfProtocols = "pop3;imap;yahooimap,yahoo;hotmail,hotmailmsp;gmailpop3,new";
		subscribeArray = null;
		exception = new String[][] { { "pop3" }, { "imap" },
				{ "yahooimap", "yahoo" }, { "hotmail", "hotmailmsp" },
				{ "gmailpop3", "new" } };
		loopNoticer = new LoopNoticer();

		result = (String[][]) PrivateMethod.doTest(LoopNoticer.class,
				"getGroups", loopNoticer, new Class[] { String.class,
						String[].class }, new Object[] { groupOfProtocols,
						subscribeArray });
		Assert2Array.assertEquals(exception, result);

		// ";;"
		groupOfProtocols = "pop3;;yahooimap,yahoo;hotmail,hotmailmsp;gmailpop3,new";
		subscribeArray = new String[] { "pop3:8", "new:0" };
		exception = new String[][] { { "yahooimap", "yahoo" },
				{ "hotmail", "hotmailmsp" }, { "gmailpop3" } };
		loopNoticer = new LoopNoticer();

		result = (String[][]) PrivateMethod.doTest(LoopNoticer.class,
				"getGroups", loopNoticer, new Class[] { String.class,
						String[].class }, new Object[] { groupOfProtocols,
						subscribeArray });
		Assert2Array.assertEquals(exception, result);

		//
		groupOfProtocols = "pop3;";
		subscribeArray = new String[] { "pop3:8", "new:0" };
		exception = new String[][] {};
		loopNoticer = new LoopNoticer();

		result = (String[][]) PrivateMethod.doTest(LoopNoticer.class,
				"getGroups", loopNoticer, new Class[] { String.class,
						String[].class }, new Object[] { groupOfProtocols,
						subscribeArray });
		Assert2Array.assertEquals(exception, result);

	}

	public void testGetGroupsNameValue() {
		//
		String[][] groups = new String[][] { { "pop3" }, { "imap" },
				{ "yahooimap", "yahoo" }, { "hotmail", "hotmailmsp" },
				{ "gmailpop3", "new" } };
		List<SysConfig> destinationGroup = new ArrayList<SysConfig>();

		SysConfig sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.default");
		sysConfig.setValue("1;127.0.0.1:1099;8");
		destinationGroup.add(sysConfig);

		sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.imap");
		sysConfig.setValue("1;127.0.0.2:1099;8");
		destinationGroup.add(sysConfig);

		sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.hotmail");
		sysConfig.setValue("1;127.0.0.3:1099;8");
		destinationGroup.add(sysConfig);

		String[][] exception = new String[][] { { null, null },
				{ "imap", "1;127.0.0.2:1099;8" }, { null, null },
				{ "hotmail", "1;127.0.0.3:1099;8" }, { null, null } };
		LoopNoticer loopNoticer = new LoopNoticer();

		String[][] result = (String[][]) PrivateMethod.doTest(
				LoopNoticer.class, "getGroupsNameValue", loopNoticer,
				new Class[] { String[][].class, List.class }, new Object[] {
						groups, destinationGroup });

		Assert2Array.assertEquals(exception, result);

		// groups=null
		groups = null;
		destinationGroup = new ArrayList<SysConfig>();

		sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.default");
		sysConfig.setValue("1;127.0.0.1:1099;8");
		destinationGroup.add(sysConfig);

		sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.imap");
		sysConfig.setValue("1;127.0.0.2:1099;8");
		destinationGroup.add(sysConfig);

		sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.hotmail");
		sysConfig.setValue("1;127.0.0.3:1099;8");
		destinationGroup.add(sysConfig);

		exception = new String[][] { { null, null },
				{ "imap", "1;127.0.0.2:1099;8" }, { null, null },
				{ "hotmail", "1;127.0.0.3:1099;8" }, { null, null } };
		loopNoticer = new LoopNoticer();

		result = (String[][]) PrivateMethod.doTest(LoopNoticer.class,
				"getGroupsNameValue", loopNoticer, new Class[] {
						String[][].class, List.class }, new Object[] { groups,
						destinationGroup });

		assertNull(result);

		// groups={}
		groups = new String[][] {};
		destinationGroup = new ArrayList<SysConfig>();

		sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.default");
		sysConfig.setValue("1;127.0.0.1:1099;8");
		destinationGroup.add(sysConfig);

		sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.imap");
		sysConfig.setValue("1;127.0.0.2:1099;8");
		destinationGroup.add(sysConfig);

		sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.hotmail");
		sysConfig.setValue("1;127.0.0.3:1099;8");
		destinationGroup.add(sysConfig);

		exception = new String[][] { { null, null },
				{ "imap", "1;127.0.0.2:1099;8" }, { null, null },
				{ "hotmail", "1;127.0.0.3:1099;8" }, { null, null } };
		loopNoticer = new LoopNoticer();

		result = (String[][]) PrivateMethod.doTest(LoopNoticer.class,
				"getGroupsNameValue", loopNoticer, new Class[] {
						String[][].class, List.class }, new Object[] { groups,
						destinationGroup });

		assertNull(result);

		// groups={ {}, {}, {} }
		groups = new String[][] { {}, {}, {} };
		destinationGroup = new ArrayList<SysConfig>();

		sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.default");
		sysConfig.setValue("1;127.0.0.1:1099;8");
		destinationGroup.add(sysConfig);

		sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.imap");
		sysConfig.setValue("1;127.0.0.2:1099;8");
		destinationGroup.add(sysConfig);

		sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.hotmail");
		sysConfig.setValue("1;127.0.0.3:1099;8");
		destinationGroup.add(sysConfig);

		exception = new String[][] { { null, null }, { null, null },
				{ null, null } };
		loopNoticer = new LoopNoticer();

		result = (String[][]) PrivateMethod.doTest(LoopNoticer.class,
				"getGroupsNameValue", loopNoticer, new Class[] {
						String[][].class, List.class }, new Object[] { groups,
						destinationGroup });

		Assert2Array.assertEquals(exception, result);

		// destinationGroup=null
		groups = new String[][] { { "pop3" }, { "imap" },
				{ "yahooimap", "yahoo" }, { "hotmail", "hotmailmsp" },
				{ "gmailpop3", "new" } };
		destinationGroup = null;

		exception = new String[][] { { null, null },
				{ "imap", "1;127.0.0.2:1099;8" }, { null, null },
				{ "hotmail", "1;127.0.0.3:1099;8" }, { null, null } };
		loopNoticer = new LoopNoticer();

		result = (String[][]) PrivateMethod.doTest(LoopNoticer.class,
				"getGroupsNameValue", loopNoticer, new Class[] {
						String[][].class, List.class }, new Object[] { groups,
						destinationGroup });

		assertNull(result);

		groups = new String[][] { { "pop3" }, { "yahooimap", "yahoo" },
				{ "imap", "hotmail", "hotmailmsp" }, { "gmailpop3", "new" } };
		destinationGroup = new ArrayList<SysConfig>();

		sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.default");
		sysConfig.setValue("1;127.0.0.1:1099;8");
		destinationGroup.add(sysConfig);

		sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.imap");
		sysConfig.setValue("1;127.0.0.2:1099;8");
		destinationGroup.add(sysConfig);

		sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.hotmail");
		sysConfig.setValue("1;127.0.0.3:1099;8");
		destinationGroup.add(sysConfig);

		exception = new String[][] { { null, null }, { null, null },
				{ "hotmail", "1;127.0.0.3:1099;8" }, { null, null } };
		loopNoticer = new LoopNoticer();

		result = (String[][]) PrivateMethod.doTest(LoopNoticer.class,
				"getGroupsNameValue", loopNoticer, new Class[] {
						String[][].class, List.class }, new Object[] { groups,
						destinationGroup });

		Assert2Array.assertEquals(exception, result);
	}
/* The test crashes (DB)
	@SuppressWarnings("unchecked")
	public void testCreateDestination() {
		String[] allProtocols = new String[] { "imap", "yahoo", "hotmail",
				"msp", "gmail", "pop3", "new", "yahooimap" };
		String[] subscribeArray = new String[] { "msp,yahoo:8", "pop3:9" };
		LoopNoticer loopNoticer = new LoopNoticer();

		@SuppressWarnings("unused")
		List<String> nameOfProtocols = (List<String>) PrivateMethod.doTest(
				LoopNoticer.class, "getNameOfProtocols", loopNoticer,
				new Class[] { String[].class, String[].class }, new Object[] {
						allProtocols, subscribeArray });

		String groupOfProtocols = new String(
				"imap,yahoo,hotmail;msp,gmail;pop3;new;yahooimap");

		String[][] groups = (String[][]) PrivateMethod.doTest(
				LoopNoticer.class, "getGroups", loopNoticer, new Class[] {
						String.class, String[].class }, new Object[] {
						groupOfProtocols, subscribeArray });

		List<SysConfig> destinationGroup = new ArrayList<SysConfig>();

		SysConfig sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.default");
		sysConfig.setValue("1;127.0.0.1:1099;8");
		destinationGroup.add(sysConfig);

		sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.imap");
		sysConfig.setValue("1;127.0.0.2:1099;8");
		destinationGroup.add(sysConfig);

		sysConfig = new SysConfig();
		sysConfig.setName("destination.target.group.hotmail");
		sysConfig.setValue("1;127.0.0.3:1099;8");
		destinationGroup.add(sysConfig);

		String[][] groupsNameValue = (String[][]) PrivateMethod.doTest(
				LoopNoticer.class, "getGroupsNameValue", loopNoticer,
				new Class[] { String[][].class, List.class }, new Object[] {
						groups, destinationGroup });

		HashMap<String, DestinationInstance> instanceStub = new HashMap<String, DestinationInstance>();
		HashMap<String, String[]> protocolsStub = new HashMap<String, String[]>();
		String defaultConfig = "1;127.0.0.1:1099;8";

		PrivateMethod.doTest(LoopNoticer.class, "createDestination",
				loopNoticer, new Class[] { List.class, String[][].class,
						String[][].class, String.class, HashMap.class,
						HashMap.class }, new Object[] { nameOfProtocols,
						groups, groupsNameValue, defaultConfig, instanceStub,
						protocolsStub });

		assertEquals(instanceStub.size(), 2);
		assertEquals(protocolsStub.size(), 2);

		String[] proKeySetException = new String[] { "hotmail", "default" };
		Iterator<String> protocolsIterator = protocolsStub.keySet().iterator();
		for (int i = 0; protocolsIterator.hasNext(); i++) {
			assertEquals(protocolsIterator.next(), proKeySetException[i]);
		}

		Iterator<String[]> protocolsValuesIt = protocolsStub.values()
				.iterator();
		String[][] proValuesExceptio = new String[][] { { "imap", "hotmail" },
				{ "gmail", "new", "yahooimap" } };
		for (int i = 0; protocolsValuesIt.hasNext(); i++) {
			AssertArray.assertEquals(protocolsValuesIt.next(),
					proValuesExceptio[i]);
		}

		Iterator<String> instanceStubKeyValuesIt = instanceStub.keySet()
				.iterator();
		String[] instanceKeys = new String[] { "hotmail", "default" };
		for (int i = 0; instanceStubKeyValuesIt.hasNext(); i++) {
			assertEquals(instanceStubKeyValuesIt.next(), instanceKeys[i]);
		}
		
		
	}
*/
	public static Test suite() {
		return new TestSuite(LoopNoticerTest.class);
	}

	/**
	 * @param args
	 */

	public static void main(String[] args) {
		return;
		// junit.textui.TestRunner.run(suite());
		// List<String> list1 = new ArrayList<String>();
		// list1.add("you");
		// list1.add("lian");
		// list1.add("jie");
		// List<String> list2 = new ArrayList<String>();
		// list2.add("jie");
		// list2.add("lian");
		// list2.add("you");
		// list1.removeAll(list2);
		// System.out.println(list1.equals(list2));
	}

}
