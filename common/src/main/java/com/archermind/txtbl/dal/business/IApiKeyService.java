package com.archermind.txtbl.dal.business;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.ApiKey;
import com.archermind.txtbl.domain.Partner;

public interface IApiKeyService {

    public abstract ApiKey getApiKeyById(String id)  throws DALException;

    public abstract ApiKey getApiKeyByAccessKey(String accessKey) throws DALException;
}
