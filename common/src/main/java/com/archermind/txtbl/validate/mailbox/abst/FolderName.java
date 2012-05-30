package com.archermind.txtbl.validate.mailbox.abst;

import com.archermind.txtbl.domain.Account;

public class FolderName {
    public String name;
    public String excludeFolder;

    public static final FolderName DEFAULT_FOLDER_NAME = new FolderName(Account.DEFAULT_FOLDER);

    public FolderName(String name) {
        this.name = name;
    }

    public FolderName(String name, String excludeFolder) {
        this.name = name;
        this.excludeFolder = excludeFolder;
    }
}
