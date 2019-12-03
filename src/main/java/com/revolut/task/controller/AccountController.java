package com.revolut.task.controller;

import java.math.BigDecimal;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.json.JSONObject;

import com.revolut.task.exception.AccountDoesNotExistException;
import com.revolut.task.service.AccountDataService;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.validation.Validated;

@Validated
@Controller("/account")
public class AccountController {
	
    @Inject
    private AccountDataService accountService;

	@Get(uri = "/{accountNum}", produces = MediaType.APPLICATION_JSON) 
	public String getAccount(@NotBlank String accountNum) {
		return accountService.getAccountBalance(accountNum)
			.map(balance -> new JSONObject()
					.put("accountNumber", accountNum)
					.put("balance", balance)
					.toString())
			.orElseThrow(() -> new AccountDoesNotExistException(accountNum));
	}
	
	@Put(uri = "/{accountNum}/balance/{balance}", single = true) 
	public void createAccount(@NotBlank String accountNum, @NotEmpty double balance) {
		accountService.createNewAccount(accountNum, BigDecimal.valueOf(balance));
	}
	
	@Post(uri = "/{from}/to/{to}/amount/{amount}")
	public void transferMoney(@NotBlank String from, @NotBlank String to, @NotNull double amount) throws InterruptedException {
		accountService.transferFunds(from, to, BigDecimal.valueOf(amount));
	}
}
