package com.revolut.task.service;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountDataService {

	Optional<BigDecimal> getAccountBalance(String accountNum);

	void createNewAccount(String accountNumber, BigDecimal balance);

	void transferFunds(String from, String to, BigDecimal sum) throws InterruptedException;

}