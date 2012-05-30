package com.archermind.txtbl.utils;

public interface IMailer {
    void sendMail(String from, String to, String cc, String bcc, String message, String subject);
}
