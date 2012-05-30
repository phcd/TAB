package com.archermind.txtbl.receiver.mail.bean;

@SuppressWarnings("unused")
public class MSPSubscribeBean {
    private String transactionID;
    private String webServiceCustomerID;
    private String response;

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public String getWebServiceCustomerID() {
        return webServiceCustomerID;
    }

    public void setWebServiceCustomerID(String webServiceCustomerID) {
        this.webServiceCustomerID = webServiceCustomerID;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
