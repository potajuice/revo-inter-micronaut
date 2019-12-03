package com.revolut.task.service;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.function.BooleanSupplier;

import javax.inject.Singleton;

import com.revolut.task.exception.AccountDoesNotExistException;
import com.revolut.task.exception.DuplicateAccountException;
import com.revolut.task.exception.InsufficientFundsException;
import com.revolut.task.model.Account;
import com.revolut.task.util.BigDecimalUtil;

@Singleton
public class AccountDataServiceImpl implements AccountDataService {

	private ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();
	
	@Override
	public Optional<BigDecimal> getAccountBalance(String accountNum) {
		return getAccount(accountNum)
				.map(Account::getBalance)
				.map(BigDecimalUtil::getScaledDecimal);
	}
	
	@Override
	public void transferFunds(String from, String to, BigDecimal sum) throws InterruptedException {
		var fromAcc = getAccount(from).orElseThrow(() -> new AccountDoesNotExistException(from));
		var toAcc = getAccount(to).orElseThrow(() -> new AccountDoesNotExistException(to));
		
		while (true) {
			boolean transfered = 
				tryToLockAccount(fromAcc.getLock(),
					() -> tryToLockAccount(toAcc.getLock(), 
							() -> transferFunds(fromAcc, toAcc, sum)));
			
			if(transfered) break;

			Thread.sleep((long) 1000 + ThreadLocalRandom.current().nextInt(1000));
		}
	}
	
	@Override
	public synchronized void createNewAccount(String accountNumber, BigDecimal balance){
		if (accounts.containsKey(accountNumber)) {
			throw new DuplicateAccountException(accountNumber);
		}	
		accounts.put(accountNumber, new Account(accountNumber, balance));
	}
	
	private Optional<Account> getAccount(String accountNum) {
		return Optional.ofNullable(accounts.get(accountNum));
	}
	
	private boolean tryToLockAccount(Lock lock, BooleanSupplier action) {
		if (lock.tryLock()) {
			try {
				return action.getAsBoolean();
			} finally {
				lock.unlock();
			}
		} else {
			return false;
		}
	}
	
	private boolean transferFunds(Account from, Account to, BigDecimal sum) {
		if (from.getBalance().compareTo(sum) < 0) {
			throw new InsufficientFundsException(from.getAccountNumber(), from.getBalance(), sum);
		}
		
		from.addBalance(sum.negate());
		to.addBalance(sum);
		
		return true;
	}
}
