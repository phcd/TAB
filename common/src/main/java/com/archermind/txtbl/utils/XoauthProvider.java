package com.archermind.txtbl.utils;

import java.security.Provider;

public final class XoauthProvider extends Provider {
    public XoauthProvider() {
        super("Google Xoauth Provider", 1.0, "Provides the Xoauth experimental SASL Mechanism");
        put("SaslClientFactory.XOAUTH", "com.archermind.txtbl.utils.XoauthSaslClientFactory");
    }
}