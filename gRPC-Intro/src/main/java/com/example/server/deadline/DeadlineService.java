package com.example.server.deadline;

import com.example.models.Balance;
import com.example.models.BalanceCheckRequest;
import com.example.models.BankServiceGrpc;
import com.example.models.DepositRequest;
import com.example.models.Money;
import com.example.models.WithdrawRequest;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class DeadlineService extends BankServiceGrpc.BankServiceImplBase {

	@Override
	public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {
		int accountNumber = request.getAccountNumber();

		System.out.println("Received the request for: " + accountNumber);

		Balance balance = Balance.newBuilder()
				.setAmount(accountNumber * 10)
				.build();

		// simulate time-consuming call
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		responseObserver.onNext(balance);
		responseObserver.onCompleted();
	}

	@Override
	public void withdraw(WithdrawRequest request, StreamObserver<Money> responseObserver) {
		int accountNumber = request.getAccountNumber();
		int amount = request.getAmount();

		// Testing throwing exceptions
		if (amount < 0 || amount >= 100) {
			Status status = Status.FAILED_PRECONDITION.withDescription("Invalid amount");
			responseObserver.onError(status.asRuntimeException());
		}

		for (int i = 0; i < (amount / 10); i++) {
			Money money = Money.newBuilder().setValue(10).build();
			// simulate time-consuming call
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// check if rpc call was canceled by client
			if (!Context.current().isCancelled()) {
				responseObserver.onNext(money);
				System.out.println("Delivered 10$");
			} else {
				break;
			}
		}
		System.out.println("Completed");
		responseObserver.onCompleted();
	}

	@Override
	public StreamObserver<DepositRequest> deposit(StreamObserver<Balance> responseObserver) {
		// Where will be getting the streaming request
		// Server has to provide an implementation for the streaming request
		return new StreamObserver<DepositRequest>() {
			@Override
			public void onNext(DepositRequest depositRequest) {
				System.out.println("Received deposit request for: " + depositRequest.getAccountNumber());
				// System.out.println("Got DepositRequest with Amount: " + depositRequest.getAmount());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable throwable) {
				System.out.println("Error: " + throwable.getMessage());
			}

			@Override
			public void onCompleted() {
				System.out.println("Request Streaming Done from Client Side");
				Balance balance = Balance.newBuilder()
						.setAmount(100)
						.build();
				responseObserver.onNext(balance);
				// Only one response can be sent by server
				responseObserver.onCompleted();
			}
		};
	}
}
