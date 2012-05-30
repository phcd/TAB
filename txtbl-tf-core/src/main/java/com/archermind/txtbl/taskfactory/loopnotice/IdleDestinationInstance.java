package com.archermind.txtbl.taskfactory.loopnotice;

import com.archermind.txtbl.domain.Account;

public class IdleDestinationInstance extends DestinationInstance {
    private int accountIdIncrement;

    public IdleDestinationInstance(String configuration, String[] protocols, String protocol, int accountIdIncrement) {
        super(configuration, protocols, protocol);
        this.accountIdIncrement = accountIdIncrement;
    }

    @Override
    protected String[] getDestinationUrls(String url) {
        return url.split(",");
    }

    @Override
    protected String getDestinationUrl(Account account) {
        int noOfNodes = this.destinationUrls.length;
        if(noOfNodes == 1) {
            return destinationUrls[0];
        }
        return destinationUrls[getIndex(account, noOfNodes)];
    }

    private int getIndex(Account account, int noOfNodes) {
        int id = account.getId();
        id = (int) (id/(float)accountIdIncrement);
        return id % noOfNodes;
    }

    @Override
    protected String getDestinationName(Account account) {
        int noOfNodes = this.destinationUrls.length;
        if(noOfNodes == 1) {
            return this.destinationName;
        }
        int index = getIndex(account, noOfNodes);
        return destinationName + "-" + index;
    }
}
