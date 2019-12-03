package com.revolut.task.model;

import static com.revolut.task.util.BigDecimalUtil.getScaledDecimal;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Account {
	
	private final Lock lock = new ReentrantLock();
	
	private volatile BigDecimal balance;
	
	private String accountNumber;
	
	public Account(String accountNumber, BigDecimal balance) {
		this.accountNumber = accountNumber;
		this.balance = balance;
	}
	
	public Lock getLock() {
		return lock;
	}
	
	public void addBalance(BigDecimal balance) {
		this.balance = this.balance.add(getScaledDecimal(balance));
	}
	
	public BigDecimal getBalance() {
		return balance;
	}
	
	public String getAccountNumber() {
		return accountNumber;
	}
}