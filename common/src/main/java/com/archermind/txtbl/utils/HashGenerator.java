package com.archermind.txtbl.utils;

import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;

public class HashGenerator {

    public static String genHash(String target) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return CipherTools.getHash(target, CipherTools.SHA_256);
    }

}
