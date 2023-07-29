package com.example.server.rpctypes;

import com.example.models.Account;
import com.example.models.TransferRequest;
import com.example.models.TransferResponse;
import com.example.models.TransferServiceGrpc;
import com.example.models.TransferStatus;
import io.grpc.stub.StreamObserver;

public class TransferService extends TransferServiceGrpc.TransferServiceImplBase {
	@Override
	public StreamObserver<TransferRequest> transfer(StreamObserver<TransferResponse> responseObserver) {
		return new StreamObserver<TransferRequest>() {
			@Override
			public void onNext(TransferRequest transferRequest) {
				System.out.println("Client onNext() called, received: " + transferRequest.getFromAccount());

				int fromAccount = transferRequest.getFromAccount();
				int toAccount = transferRequest.getToAccount();
				int amount = transferRequest.getAmount();
				TransferStatus transferStatus = TransferStatus.FAILED;
				if (amount > 100 && fromAccount != toAccount) {
					transferStatus = TransferStatus.SUCCESS;
				}

				TransferResponse transferResponse = TransferResponse.newBuilder()
						.setStatus(transferStatus)
						.addAccounts(
								Account.newBuilder()
										.setAccountNumber(fromAccount)
										.build()
						)
						.addAccounts(
								Account.newBuilder()
										.setAccountNumber(toAccount)
										.build()
						)
						.build();

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				responseObserver.onNext(transferResponse);
			}

			@Override
			public void onError(Throwable throwable) {
				responseObserver.onError(throwable);
			}

			@Override
			public void onCompleted() {
				System.out.println("Client onCompleted() called.");

				// When client notifies onCompleted, server also does the same.
				responseObserver.onCompleted();
			}
		};
	}
}
