package com.archermind.txtbl.taskfactory;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Server;
import com.archermind.txtbl.domain.SysConfig;

public abstract class ReceiveNoticer {

    public abstract void start();

    public abstract ReceiveNoticer updateAccount(Account account);

    public abstract void updateMSConfig(Server server);

    public abstract void updateConfig(SysConfig sysConfig);

    public abstract void pop3CollectionClear();
}
