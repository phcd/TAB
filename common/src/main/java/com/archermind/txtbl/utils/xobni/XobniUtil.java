package com.archermind.txtbl.utils.xobni;

import com.archermind.txtbl.domain.Account;

public class XobniUtil {
    public static boolean isInitialFetchInProgress(Account account, int maxMessagesToProcess,int xobniBatchSize) {
        Integer folderDepth = account.getFolder_depth();
        int noOfEmailsToBeFetched = (folderDepth == null || folderDepth > maxMessagesToProcess) ? maxMessagesToProcess : folderDepth;
        return noOfEmailsToBeFetched > (account.getMessage_count() + xobniBatchSize + 1);
    }

    public static String getReceiver(Account account) {
        //TODO - Paul - needs to be relooked at
        if(account.isXobniImapIdle() || account.isXobniOauthIdle() || account.isXobniImap() || account.isXobniOauth()) {
            return "gmail";
        } else if(account.isXobniYahooImap()) {
            return "yahoo";
        }
        return null;        
    }
}
