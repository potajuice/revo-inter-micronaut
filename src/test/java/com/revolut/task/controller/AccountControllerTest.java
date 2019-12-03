package com.revolut.task.controller;

import static com.revolut.task.util.BigDecimalUtil.getScaledDecimal;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.awaitility.Awaitility;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.revolut.task.exception.InsufficientFundsException;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MicronautTest;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccountControllerTest {
	
	@Inject
    @Client("/account")
    private RxHttpClient client;
	
	private static final String ACCOUNT_NUM1 = "1";
	private static final String ACCOUNT_NUM2 = "2";
	private static final String ACCOUNT_NUM3 = "3";
	private static final String ACCOUNT_NUM4 = "4";
	private static final String ACCOUNT_NUM5 = "5";
	
	@BeforeAll
	void init() {
		client.toBlocking().exchange(formCreateRequest(ACCOUNT_NUM1, 100.54));
		client.toBlocking().exchange(formCreateRequest(ACCOUNT_NUM2, 187.78));
		client.toBlocking().exchange(formCreateRequest(ACCOUNT_NUM3, 5234.35));
		client.toBlocking().exchange(formCreateRequest(ACCOUNT_NUM4, 51.42));
		client.toBlocking().exchange(formCreateRequest(ACCOUNT_NUM5, 10.89));
	}
	
	@Test
	void shouldTransferFundsToAnotherAccount() {
		BlockingHttpClient blockingClient = client.toBlocking();

		blockingClient.exchange(formTransferRequest(ACCOUNT_NUM1, ACCOUNT_NUM2, 10));
		
		JSONObject jsonResp = getAccount(ACCOUNT_NUM1);
		Assertions.assertEquals(90.54, jsonResp.getDouble("balance"));
		
		jsonResp = getAccount(ACCOUNT_NUM2);
		Assertions.assertEquals(197.78, jsonResp.getDouble("balance"));
	}
	
	@Test
	void shouldCreateAccount() {
		Assertions.assertFalse(getAccount(ACCOUNT_NUM1).isEmpty());
		Assertions.assertFalse(getAccount(ACCOUNT_NUM2).isEmpty());
	}
	
	@Test
	void shouldNotLockUp() throws InterruptedException {
		ExecutorService threadPool = Executors.newFixedThreadPool(5);
		try {
			threadPool.submit(getTransferRunnable(ACCOUNT_NUM1, ACCOUNT_NUM2, 5.48));
			threadPool.submit(getTransferRunnable(ACCOUNT_NUM2, ACCOUNT_NUM1, 6.49));
			threadPool.submit(getTransferRunnable(ACCOUNT_NUM3, ACCOUNT_NUM4, 7.21));
			threadPool.submit(getTransferRunnable(ACCOUNT_NUM5, ACCOUNT_NUM3, 9.54));
			threadPool.submit(getTransferRunnable(ACCOUNT_NUM4, ACCOUNT_NUM5, 6.84));
		} finally {
			threadPool.shutdown();
		}
		Awaitility.await().atMost(20, TimeUnit.SECONDS).until(threadPool::isTerminated);
		
		BigDecimal totalAmount = getBalanceFromResponse(ACCOUNT_NUM1)
				.add(getBalanceFromResponse(ACCOUNT_NUM2))
						.add(getBalanceFromResponse(ACCOUNT_NUM3)
								.add(getBalanceFromResponse(ACCOUNT_NUM4)
										.add(getBalanceFromResponse(ACCOUNT_NUM5))));
		
		Assertions.assertEquals(5584.98, totalAmount.doubleValue());
	}
	
	private BigDecimal getBalanceFromResponse(String accountNum) {
		return getScaledDecimal(getAccount(accountNum).getBigDecimal("balance"));
	}
	
	private Runnable getTransferRunnable(String from, String to, double amount) {
		return () -> 
			IntStream.range(0, 500).forEach(i -> {
				try {
					client.exchange(formTransferRequest(from, to, amount));
				} catch (InsufficientFundsException e) {}
			});
	}
	
	private MutableHttpRequest<String> formCreateRequest(String accountNum, double amount) {
		return HttpRequest.PUT(String.format("/%s/balance/%.2f", accountNum, amount), "");
	}
	
	private MutableHttpRequest<String> formTransferRequest(String from, String to, double amount) {
		return HttpRequest.POST(String.format("/%s/to/%s/amount/%.2f", from, to, 10f), "");
	}
	
	private JSONObject getAccount(String accountNum) {
		String response = client.toBlocking().retrieve(HttpRequest.GET("/" + accountNum));
		return new JSONObject(response);
	}
}
