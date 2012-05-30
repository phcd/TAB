package com.archermind.txtbl.taskfactory.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TFConfigHelpIMP implements TFConfigHelp {

	public List<String> getSubscribeProtocols(String[] subscribeArray) {
		ArrayList<String> subscribeProtocols = new ArrayList<String>();
		if (subscribeArray != null) {
			for (int i = 0; i < subscribeArray.length; i++) {
				subscribeArray[i] = subscribeArray[i].split(":")[0];
				String[] tmp = subscribeArray[i].split(",");
                subscribeProtocols.addAll(Arrays.asList(tmp));
			}
		}

		return subscribeProtocols;
	}

}
