package com.archermind.txtbl.dal;

import com.archermind.txtbl.exception.TxtblException;

public class DALException extends TxtblException {

	private static final long serialVersionUID = 1L;

	public DALException(String code, String message) {
		super(code, message);
	}
	
	public DALException(String code, String message,Throwable cause) {
		super(code, message,cause);
	}
	
	public DALException(String code,Throwable cause) {
		super(code,cause);
	}
	
}
