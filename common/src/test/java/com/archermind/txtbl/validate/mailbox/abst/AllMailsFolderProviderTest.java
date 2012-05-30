package com.archermind.txtbl.validate.mailbox.abst;

import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AllMailsFolderProviderTest {
    @Test
    public void getAllMailsFolder() {
        AllMailsFolderProvider mailsFolderProvider = new AllMailsFolderProvider();
        Assert.assertNull(mailsFolderProvider.getAllMailsFolder(null));
        Assert.assertNull(mailsFolderProvider.getAllMailsFolder(new ArrayList<String>()));

        List<String> folderTags = Arrays.asList("(\\HasNoChildren) \"/\" \"Duiken\"",
        "(\\HasNoChildren \\Inbox) \"/\" \"Postvak IN\"",
        "(\\HasNoChildren) \"/\" \"Magic\"",
        "(\\HasNoChildren) \"/\" \"Notes\"",
        "(\\Noselect \\HasChildren) \"/\" \"[Gmail]\"",
        "(\\HasChildren \\HasNoChildren \\AllMail) \"/\" \"[Gmail]/Alle berichten\"",
        "(\\HasNoChildren \\Drafts) \"/\" \"[Gmail]/Concepten\"",
        "(\\HasNoChildren \\Starred) \"/\" \"[Gmail]/Met ster\"",
        "(\\HasChildren \\HasNoChildren \\Trash) \"/\" \"[Gmail]/Prullenbak\"",
        "(\\HasNoChildren \\Spam) \"/\" \"[Gmail]/Spam\"",
        "(\\HasNoChildren \\Sent) \"/\" \"[Gmail]/Verzonden berichten\"",
        "(\\HasNoChildren) \"/\" \"[Gmail]Prullenbak\"",
        "Success");

        FolderName folerName = mailsFolderProvider.getAllMailsFolder(folderTags);
        Assert.assertEquals("[Gmail]/Alle berichten", folerName.name);
        Assert.assertEquals("[Gmail]/Concepten", folerName.excludeFolder);

        folderTags = Arrays.asList("(\\HasNoChildren) \"/\" \"Duiken\"",
        "(\\HasNoChildren \\Inbox) \"/\" \"Postvak IN\"",
        "(\\HasNoChildren) \"/\" \"Magic\"",
        "(\\HasNoChildren) \"/\" \"Notes\"",
        "(\\Noselect \\HasChildren) \"/\" \"[Gmail]\"",
        "(\\HasChildren \\HasNoChildren \\AllMail) \"/\" \"[Gmail]/Alle berichten\"",
        "(\\HasNoChildren \\Starred) \"/\" \"[Gmail]/Met ster\"",
        "(\\HasChildren \\HasNoChildren \\Trash) \"/\" \"[Gmail]/Prullenbak\"",
        "(\\HasNoChildren \\Spam) \"/\" \"[Gmail]/Spam\"",
        "(\\HasNoChildren \\Sent) \"/\" \"[Gmail]/Verzonden berichten\"",
        "(\\HasNoChildren) \"/\" \"[Gmail]Prullenbak\"",
        "Success");

        folerName = mailsFolderProvider.getAllMailsFolder(folderTags);
        Assert.assertEquals("[Gmail]/Alle berichten", folerName.name);
        Assert.assertNull(folerName.excludeFolder);

        folderTags = Arrays.asList("(\\HasNoChildren) \"/\" \"Duiken\"",
        "(\\HasNoChildren \\Inbox) \"/\" \"Postvak IN\"",
        "(\\HasNoChildren) \"/\" \"Magic\"",
        "(\\HasNoChildren) \"/\" \"Notes\"",
        "(\\Noselect \\HasChildren) \"/\" \"[Gmail]\"",
        "(\\HasNoChildren \\Drafts) \"/\" \"[Gmail]/Concepten\"",
        "(\\HasNoChildren \\Starred) \"/\" \"[Gmail]/Met ster\"",
        "(\\HasChildren \\HasNoChildren \\Trash) \"/\" \"[Gmail]/Prullenbak\"",
        "(\\HasNoChildren \\Spam) \"/\" \"[Gmail]/Spam\"",
        "(\\HasNoChildren \\Sent) \"/\" \"[Gmail]/Verzonden berichten\"",
        "(\\HasNoChildren) \"/\" \"[Gmail]Prullenbak\"",
        "Success");

        Assert.assertNull(mailsFolderProvider.getAllMailsFolder(folderTags));
        
    }
}
