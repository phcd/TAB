package com.archermind.txtbl.utils.xobni;

public class XobniResponse {
    private int returnCode;
    private String response;

    public XobniResponse(int returnCode, String response) {
        this(returnCode);
        this.response = response;
    }

    public XobniResponse(int returnCode) {
        this.returnCode = returnCode;
    }

    public String getResponse() {
        return response;
    }

    public int getReturnCode() {
        return returnCode;
    }
}
