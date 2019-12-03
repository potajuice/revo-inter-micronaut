package com.revolut.task.exception;

public class AccountDoesNotExistException extends RuntimeException {
	
	private static final long serialVersionUID = -9129103547371306521L;

	public AccountDoesNotExistException(String account) {
		super(String.format("Account %s does not exist", account));
	}
}
