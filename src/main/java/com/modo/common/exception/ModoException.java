package com.modo.common.exception;

public class ModoException extends RuntimeException{

	public ModoException(String message) {
		super(message);
	}

	public ModoException(String message, Throwable cause) {
		super(message, cause);
	}


}
