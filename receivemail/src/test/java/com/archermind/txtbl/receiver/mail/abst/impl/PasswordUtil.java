package com.archermind.txtbl.receiver.mail.abst.impl;

import com.archermind.txtbl.utils.CipherTools;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

public class PasswordUtil
{

    public static String encode(String key, String name, String password) throws UnsupportedEncodingException
    {
        return new String(Base64.encodeBase64(CipherTools
                .RC4Encrypt(password, key + name)), "utf-8");
    }

    public static String decode(String key, String name, String password) throws UnsupportedEncodingException
    {
        return CipherTools.RC4Decrype(Base64.decodeBase64(password.getBytes("utf-8")), key + name);
    }

    public static void main(String[] args) throws UnsupportedEncodingException
    {
        String name = "dan.morel@gmail.com";
        String key = "fQrDPJByHwgYeHnIXMKC";
       // String key="df";
        String password = "txtbl123";


        String str = PasswordUtil.encode(key, name, password);

        System.out.println(str);
        System.out.println(PasswordUtil.decode(key, name, str));

    }
}
