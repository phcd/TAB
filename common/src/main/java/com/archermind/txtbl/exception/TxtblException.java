package com.archermind.txtbl.exception;


public class TxtblException extends Exception {
	private static final long serialVersionUID = -8721605697905737696L;

	private String code;

        public TxtblException(String code, String message) {
        	super(message);
        	this.code = code;
        }

        public TxtblException(String code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
        }

        public TxtblException(String code, Throwable cause) {
            super(cause);
            this.code = code;
        }

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}
}