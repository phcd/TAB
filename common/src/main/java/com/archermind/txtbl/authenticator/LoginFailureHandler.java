package com.archermind.txtbl.authenticator;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Account;

public interface LoginFailureHandler {
    public void handleLoginFailures(String context, Account account) throws DALException;
}
