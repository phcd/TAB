package com.archermind.txtbl.dal.business;

import java.util.List;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.SimCode;

public interface ISimCodeService {

	public List<SimCode> viewDeviceSimcode(String simCode) throws DALException;

}
