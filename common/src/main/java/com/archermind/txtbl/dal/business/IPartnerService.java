package com.archermind.txtbl.dal.business;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Partner;

public interface IPartnerService {

    public abstract Partner getPartnerById(String id)  throws DALException;
}
