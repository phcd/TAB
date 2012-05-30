package com.archermind.txtbl.dal.business;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Client;

public interface IClientService {
    public abstract Client getClientById(String id)  throws DALException;
}
