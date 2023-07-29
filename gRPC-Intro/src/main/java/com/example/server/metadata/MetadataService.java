package com.example.server.metadata;

import com.example.models.Balance;
import com.example.models.BalanceCheckRequest;
import com.example.models.BankServiceGrpc;
import com.example.models.DepositRequest;
import com.example.models.ErrorMessage;
import com.example.models.Money;
import com.example.models.WithdrawRequest;
import com.example.models.WithdrawalError;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;

public class MetadataService extends BankServiceGrpc.BankServiceImplBase {

	@Override
	public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {
		int accountNumber = request.getAccountNumber();

		System.out.println("Received the request for: " + accountNumber);

		// Similar to ThreadLocal, only current thread can store and get this info, so whatever info current rpc stored it will be getting it
		UserRole userRole = ServerConstants.CTX_USER_ROLE.get();

		System.out.println(userRole);

		Balance balance = Balance.newBuilder()
				.setAmount(accountNumber * 10)
				.build();

		responseObserver.onNext(balance);
		responseObserver.onCompleted();
	}

	@Override
	public void withdraw(WithdrawRequest request, StreamObserver<Money> responseObserver) {
		int accountNumber = request.getAccountNumber();
		int amount = request.getAmount();

		if (amount % 10 != 0) {
			Metadata metadata = new Metadata();

			Metadata.Key<WithdrawalError> withdrawalErrorKey = ProtoUtils.keyForProto(WithdrawalError.getDefaultInstance());

			WithdrawalError withdrawalError = WithdrawalError.newBuilder()
					.setErrorMessage(ErrorMessage.ONLY_TEN_MULTIPLES)
					.build();

			metadata.put(withdrawalErrorKey, withdrawalError);

			Status status = Status.FAILED_PRECONDITION.withDescription("Invalid amount, not in multiples of 10");

			responseObserver.onError(status.asRuntimeException(metadata));
		}

		// Testing throwing exceptions
		if (amount < 0 || amount >= 100) {
			Metadata metadata = new Metadata();

			Metadata.Key<WithdrawalError> withdrawalErrorKey = ProtoUtils.keyForProto(WithdrawalError.getDefaultInstance());

			WithdrawalError withdrawalError = WithdrawalError.newBuilder()
					.setErrorMessage(ErrorMessage.INSUFFICIENT_BALANCE)
					.build();

			metadata.put(withdrawalErrorKey, withdrawalError);

			// Status status = Status.FAILED_PRECONDITION.withDescription("Invalid amount, either less than 0 or greater than 100");

			responseObserver.onError( Status.FAILED_PRECONDITION.asRuntimeException(metadata));
		}

		for (int i = 0; i < (amount / 10); i++) {
			Money money = Money.newBuilder().setValue(10).build();
			responseObserver.onNext(money);
			System.out.println("Delivered 10$");
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
