package com.archermind.txtbl.dal.business;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.MspToken;

import java.util.List;

public interface IMspTokenService {
	public int setMspToken(MspToken mspToken) throws DALException;

	public MspToken getMspToken(String user_id, String sName) throws DALException;

	public List<MspToken> getAllMspToken() throws DALException;
}
