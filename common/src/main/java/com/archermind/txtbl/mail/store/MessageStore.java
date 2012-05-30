package com.archermind.txtbl.mail.store;

import com.archermind.txtbl.domain.Country;
import com.archermind.txtbl.utils.StopWatch;

import java.util.Set;

public interface MessageStore
{
    public boolean hasMessage(Integer accountId, String bucket, String messageId, Country country) throws MessageStoreException;

    public void addMessageInBulk(Integer accountId, String bucket, String messageIds, Country country) throws MessageStoreException;

    public boolean reconcileIds(Integer accountId, String bucket, String messageIds, Country country) throws MessageStoreException;

    public boolean addMessage(Integer accountId, String bucket, String messageId, Country country) throws MessageStoreException;

    public void deleteMessage(Integer accountId, String bucket, String messageId, Country country) throws MessageStoreException;

    public void deleteAllMessages(Integer accountId, Country country) throws MessageStoreException;

    public Set<String> getMessages(Integer accountId, Country country, String context, StopWatch watch) throws MessageStoreException;

    public String getBucket(Integer accountId);

    public void reconcile(Integer accountId);
}
