package com.archermind.txtbl.dal.business;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.ClientFeature;

public interface IClientFeatureService {
    public abstract ClientFeature getClientFeatureById(String id)  throws DALException;
}
