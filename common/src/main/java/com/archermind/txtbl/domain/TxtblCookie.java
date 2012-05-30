package com.archermind.txtbl.domain;

public class TxtblCookie {
    private int id = 0;
    private String emailAccount = "";
    private String cookiesName = "";
    private String cookiesValue = "";
    private String domain = "";
    private String path = "";
    private String expiryDate = "";
    private boolean secure = true;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmailAccount() {
        return emailAccount;
    }

    public void setEmailAccount(String emailAccount) {
        this.emailAccount = emailAccount;
    }

    public String getCookiesName() {
        return cookiesName;
    }

    public void setCookiesName(String cookiesName) {
        this.cookiesName = cookiesName;
    }

    public String getCookiesValue() {
        return cookiesValue;
    }

    public void setCookiesValue(String cookiesValue) {
        this.cookiesValue = cookiesValue;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean getSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

}
