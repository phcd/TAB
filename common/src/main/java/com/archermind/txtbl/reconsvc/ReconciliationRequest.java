package com.archermind.txtbl.reconsvc;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Set;


public class ReconciliationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer accountId;
    private Set<String> messageIds;
    private String context;

    public ReconciliationRequest(Integer accountId, Set<String> messageIds, String context) {
        this.accountId = accountId;
        this.messageIds = messageIds;
        this.context = context;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public Set<String> getMessageIds() {
        return messageIds;
    }

    public String getContext() {
        return context;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(accountId).append(context).append("messageIds.length", messageIds != null ? messageIds.size() : 0);
        return builder.toString();
    }
}
