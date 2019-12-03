package com.revolut.task.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

	private static final long serialVersionUID = 6307133082076897066L;

	public InsufficientFundsException(String account, BigDecimal balance, BigDecimal transfer) {
		super(String.format("Insufficient funds in %s, balance %f, transfer amount %f", account, balance.doubleValue(), transfer.doubleValue()));
	}
}
