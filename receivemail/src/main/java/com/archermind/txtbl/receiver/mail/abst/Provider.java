package com.archermind.txtbl.receiver.mail.abst;

import com.archermind.txtbl.domain.Account;

public interface Provider {
    public abstract int receiveMail(Account account);
}
