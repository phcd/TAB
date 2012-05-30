package com.archermind.txtbl.domain;

public enum ExchangeDiagnostics {
    CONNECTION("Connection"), MESSAGES("Messages"), CONTACTS("Contacts"), ATTACHMENTS("Attachments"), SEND("Send");
    private String description;

    ExchangeDiagnostics(String description) {
        this.description = description;
    }

    public String toString() {
        return description;
    }
}
