package com.archermind.txtbl.dal.business;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.RandomKey;

public interface IRandomKeyService {

	public RandomKey getRandomKey(int iKeyId) throws DALException;

	public RandomKey getMaxRandomKey() throws DALException;
}
