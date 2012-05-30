package com.archermind.txtbl.taskfactory.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TFConfigHelpIMPTest extends TestCase {
	TFConfigHelpIMP help = new TFConfigHelpIMP();

	public void testGetSubscribeProtocols() {
		//
		String[] subscribeArray = new String[] { "msp,hotmail:6", "gmail:5",
				"gmailpop3,yahoo,imap,yahooimap:2" };
		List<String> exception = new ArrayList<String>();
		exception.addAll(Arrays.asList(new String[] { "msp", "hotmail",
				"gmail", "gmailpop3", "yahoo", "imap", "yahooimap" }));
		List<String> result = help.getSubscribeProtocols(subscribeArray);
		assertEquals(result, exception);

		// "aaa,:
		subscribeArray = new String[] { "msp,hotmail:6", "gmail:5",
				"gmailpop3,yahoo,,:2" };
		exception = new ArrayList<String>();
		exception.addAll(Arrays.asList(new String[] { "msp", "hotmail",
				"gmail", "gmailpop3", "yahoo" }));
		result = help.getSubscribeProtocols(subscribeArray);
		assertEquals(result, exception);

		//
		subscribeArray = new String[] { "msp,hotmail", "gmail",
				"gmailpop3,yahoo:2" };
		exception = new ArrayList<String>();
		exception.addAll(Arrays.asList(new String[] { "msp", "hotmail",
				"gmail", "gmailpop3", "yahoo" }));
		result = help.getSubscribeProtocols(subscribeArray);
		assertEquals(result, exception);

		// 
		subscribeArray = new String[] { "msp,hotmail", "gmail",
				"gmailpop3,yahoo,," };
		exception = new ArrayList<String>();
		exception.addAll(Arrays.asList(new String[] { "msp", "hotmail",
				"gmail", "gmailpop3", "yahoo" }));
		result = help.getSubscribeProtocols(subscribeArray);
		assertEquals(result, exception);
	}

	public static Test suite() {
		return new TestSuite(TFConfigHelpIMPTest.class);
	}

	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite());
	}
}
