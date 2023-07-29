package com.example.client.loadbalancing;

import com.example.models.Balance;
import com.example.models.BalanceCheckRequest;
import com.example.models.BankServiceGrpc;
import com.example.models.DepositRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NginxTestClient {

	private BankServiceGrpc.BankServiceBlockingStub blockingStub;
	private BankServiceGrpc.BankServiceStub bankServiceStub;

	@BeforeAll
	public void setup() {
		// 8585 port is where nginx is listening for HTTP/2 connections
		ManagedChannel managedChannel = ManagedChannelBuilder
				.forAddress("localhost", 8585)
				.usePlaintext()
				.build();

		this.blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
		this.bankServiceStub = BankServiceGrpc.newStub(managedChannel);
	}

	@Test
	public void balanceTest() {
		for (int i = 0; i < 100; i++) {
			BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
					.setAccountNumber(i)
					.build();

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Balance balance = this.blockingStub.getBalance(balanceCheckRequest);

			System.out.println(balance);
		}
	}

	@Test
	public void cashStreamingRequest() throws InterruptedException {
		CountDownLatch countDownLatch = new CountDownLatch(1);

		StreamObserver<DepositRequest> requestStreamObserver = this.bankServiceStub.deposit(
				new StreamObserver<Balance>() {
					@Override
					public void onNext(Balance balance) {
						System.out.println("Final balance is: " + balance.getAmount());
					}

					@Override
					public void onError(Throwable throwable) {
						System.out.println(throwable.getMessage());
						countDownLatch.countDown();
					}

					@Override
					public void onCompleted() {
						System.out.println("Server Side Done");
						countDownLatch.countDown();
					}
				}
		);

		for (int i = 0; i < 10; i++) {
			DepositRequest depositRequest = DepositRequest.newBuilder()
					.setAccountNumber(i)
					.setAmount(10).build();

			requestStreamObserver.onNext(depositRequest);
		}

		requestStreamObserver.onCompleted();

		countDownLatch.await();
	}
}
