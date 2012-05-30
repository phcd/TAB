package com.archermind.txtbl.utils;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.parser.MessageParser;

import javax.mail.Message;

public class EmailUtil {
    public static boolean isFromSameAsAccount(Account account, Message message) {
        return new MessageParser().parseMsgAddress(message, "FROM", false).contains(account.getName());
    }
}
