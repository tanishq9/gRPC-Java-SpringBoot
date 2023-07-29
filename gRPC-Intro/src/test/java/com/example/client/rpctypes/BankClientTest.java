package com.example.client.rpctypes;

import com.example.models.Balance;
import com.example.models.BalanceCheckRequest;
import com.example.models.BankServiceGrpc;
import com.example.models.DepositRequest;
import com.example.models.Money;
import com.example.models.TransferRequest;
import com.example.models.TransferResponse;
import com.example.models.TransferServiceGrpc;
import com.example.models.WithdrawRequest;
import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BankClientTest {

	private BankServiceGrpc.BankServiceBlockingStub blockingStub;
	private BankServiceGrpc.BankServiceStub bankServiceStub;
	private TransferServiceGrpc.TransferServiceStub transferServiceStub;

	@BeforeAll
	public void setup() {
		ManagedChannel managedChannel = ManagedChannelBuilder
				.forAddress("localhost", 6565)
				.usePlaintext()
				.build();

		this.blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
		this.bankServiceStub = BankServiceGrpc.newStub(managedChannel);
		this.transferServiceStub = TransferServiceGrpc.newStub(managedChannel);
	}

	@Test
	public void balanceTest() {
		BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
				.setAccountNumber(123)
				.build();

		Balance balance = this.blockingStub.getBalance(balanceCheckRequest);

		System.out.println(balance);
	}

	@Test
	public void withdrawTest() {
		WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
				.setAccountNumber(1)
				.setAmount(40)
				.build();

		this.blockingStub
				.withdraw(withdrawRequest)
				.forEachRemaining(
						amountReceived -> System.out.println("Received " + amountReceived.getValue())
				);
	}

	@Test
	public void withdrawAsyncTest() {
		WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
				.setAccountNumber(1)
				.setAmount(30)
				.build();

		// The 2nd argument is a callback which invoked as and when server responds with some event
		this.bankServiceStub
				.withdraw(withdrawRequest, new StreamObserver<Money>() {
					@Override
					public void onNext(Money money) {
						System.out.println("I have received: " + money.getValue());
					}

					@Override
					public void onError(Throwable throwable) {
						System.out.println("Error: " + throwable.getMessage());
					}

					@Override
					public void onCompleted() {
						System.out.println("Completed transfer");
					}
				});

		// CountDownLatch is a better alternative to above, use that only wherever applicable.
		Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
	}

	@Test
	public void withdrawAsyncTestUsingCountDownLatch() throws InterruptedException {
		CountDownLatch countDownLatch = new CountDownLatch(1);

		WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
				.setAccountNumber(1)
				.setAmount(30)
				.build();

		// The 2nd argument is a callback which invoked as and when server responds with some event
		this.bankServiceStub
				.withdraw(withdrawRequest, new StreamObserver<Money>() {
					@Override
					public void onNext(Money money) {
						System.out.println("I have received: " + money.getValue());
					}

					@Override
					public void onError(Throwable throwable) {
						System.out.println("Error: " + throwable.getMessage());
						// countDown here
						countDownLatch.countDown();
					}

					@Override
					public void onCompleted() {
						System.out.println("Completed transfer");
						// countDown here
						countDownLatch.countDown();
					}
				});

		// Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
		countDownLatch.await();
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

		requestStreamObserver.onNext(
				DepositRequest.newBuilder().setAmount(10).build()
		);

		requestStreamObserver.onNext(
				DepositRequest.newBuilder().setAmount(20).build()
		);

		requestStreamObserver.onNext(
				DepositRequest.newBuilder().setAmount(30).build()
		);

		requestStreamObserver.onNext(
				DepositRequest.newBuilder().setAmount(40).build()
		);

		requestStreamObserver.onCompleted();

		countDownLatch.await();
	}

	@Test
	public void transferStreamingResponse() throws InterruptedException {
		CountDownLatch countDownLatch = new CountDownLatch(1);

		StreamObserver<TransferRequest> transferRequestStreamObserver = this.transferServiceStub.transfer(new StreamObserver<TransferResponse>() {
			@Override
			public void onNext(TransferResponse transferResponse) {
				System.out.println("Server onNext() called, received: " + transferResponse.getAccounts(0));
			}

			@Override
			public void onError(Throwable throwable) {
				countDownLatch.countDown();
			}

			@Override
			public void onCompleted() {
				System.out.println("Server onCompleted() called.");
				countDownLatch.countDown();
			}
		});

		transferRequestStreamObserver.onNext(
				TransferRequest.newBuilder()
						.setFromAccount(1)
						.setToAccount(2)
						.setAmount(10)
						.build()
		);

		transferRequestStreamObserver.onNext(
				TransferRequest.newBuilder()
						.setFromAccount(1)
						.setToAccount(2)
						.setAmount(20)
						.build()
		);

		transferRequestStreamObserver.onNext(
				TransferRequest.newBuilder()
						.setFromAccount(1)
						.setToAccount(2)
						.setAmount(30)
						.build()
		);

		transferRequestStreamObserver.onNext(
				TransferRequest.newBuilder()
						.setFromAccount(1)
						.setToAccount(2)
						.setAmount(40)
						.build()
		);

		transferRequestStreamObserver.onCompleted();

		countDownLatch.await();
	}
}
