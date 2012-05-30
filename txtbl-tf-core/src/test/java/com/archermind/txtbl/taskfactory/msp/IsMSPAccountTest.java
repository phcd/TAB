package com.archermind.txtbl.taskfactory.msp;

import com.archermind.txtbl.domain.Account;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

//
public class IsMSPAccountTest extends TestCase {
	private IsMSPAccount isMSPAccount = new IsMSPAccountIMP(new String[] {
			"hotmail:10", "yahoo,imap:8" });

	public IsMSPAccountTest(String name) {
		super(name);
	}

	//
	public void testIsMSP0() {
		Account account = new Account();
		assertFalse(isMSPAccount.isMSP(account));
	}

	// hotmail
	public void testIsMSP1() {
		Account account = new Account();
		account.setReceiveProtocolType("hotmail");
		assertTrue(isMSPAccount.isMSP(account));
	}

	// Hotmail
	public void testIsMSP2() {
		Account account = new Account();
		account.setReceiveProtocolType("Hotmail");
		assertFalse(isMSPAccount.isMSP(account));
	}

	// serverid=8
	public void testIsMSP3() {
		Account account = new Account();
		account.setServer_id(8);
		assertFalse(isMSPAccount.isMSP(account));
	}

	// serverid=123456789
	public void testIsMSP4() {
		Account account = new Account();
		account.setServer_id(123456789);
		assertFalse(isMSPAccount.isMSP(account));
	}

	// ReceiveProtocolType=null
	public void testIsMSP5() {
		Account account = new Account();
		account.setReceiveProtocolType(null);
		account.setServer_id(123456789);
		assertFalse(isMSPAccount.isMSP(account));
	}

	// ReceiveProtocolType=yahoo
	public void testIsMSP6() {
		Account account = new Account();
		account.setReceiveProtocolType("yahoo");
		assertTrue(isMSPAccount.isMSP(account));
	}

	// ReceiveProtocolType=imap
	public void testIsMSP7() {
		Account account = new Account();
		account.setReceiveProtocolType("imap");
		assertTrue(isMSPAccount.isMSP(account));
	}

	public static Test suite() {
		return new TestSuite(IsMSPAccountTest.class);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
