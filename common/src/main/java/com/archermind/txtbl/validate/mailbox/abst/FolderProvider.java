package com.archermind.txtbl.validate.mailbox.abst;

import com.archermind.txtbl.domain.Account;

import javax.mail.Store;
import java.util.Arrays;
import java.util.List;

public class FolderProvider {
    public List<FolderName> getFolderNames(Account account, Store store) {
        return Arrays.asList(FolderName.DEFAULT_FOLDER_NAME);
    }
}
