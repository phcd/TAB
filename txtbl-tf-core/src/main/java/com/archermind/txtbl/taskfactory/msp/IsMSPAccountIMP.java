package com.archermind.txtbl.taskfactory.msp;

import java.util.List;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.taskfactory.common.TFConfigHelpIMP;

public class IsMSPAccountIMP implements IsMSPAccount {
	private List<String> SUBSCRIBE_PROTOCOLS = null;
	private TFConfigHelpIMP tfConfigHelp = new TFConfigHelpIMP();

	public IsMSPAccountIMP(String[] subscribeArray) {
		SUBSCRIBE_PROTOCOLS = getSubscribeProtocols(subscribeArray);
	}

	private List<String> getSubscribeProtocols(String[] subscribeArray) {
		return tfConfigHelp.getSubscribeProtocols(subscribeArray);
	}

	public boolean isMSP(Account account) {
        return SUBSCRIBE_PROTOCOLS != null && SUBSCRIBE_PROTOCOLS.contains(account.getReceiveProtocolType());
    }
}
