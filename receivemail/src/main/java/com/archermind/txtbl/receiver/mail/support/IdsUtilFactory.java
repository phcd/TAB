package com.archermind.txtbl.receiver.mail.support;

import com.archermind.txtbl.domain.Account;

public class IdsUtilFactory {
    public static IdsUtil getIdsUtil(Account account) {
        if (account.isXobniYahooImap()) {
            return XobniYahooIdsUtil.INSTANCE;
        }
        return UIDIdsUtil.INSTANCE;
    }
}
