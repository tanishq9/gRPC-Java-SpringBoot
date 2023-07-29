package com.example.client.metadata;

import com.example.models.Balance;
import com.example.models.BalanceCheckRequest;
import com.example.models.BankServiceGrpc;
import com.example.models.WithdrawRequest;
import com.example.models.WithdrawalError;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.MetadataUtils;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MetadataClientTest {

	private BankServiceGrpc.BankServiceBlockingStub blockingStub;
	private BankServiceGrpc.BankServiceStub bankServiceStub;

	@BeforeAll
	public void setup() {
		ManagedChannel managedChannel = ManagedChannelBuilder
				.forAddress("localhost", 6565)
				.intercept(MetadataUtils.newAttachHeadersInterceptor(ClientConstants.getClientToken()))
				.usePlaintext()
				.build();

		this.blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
		this.bankServiceStub = BankServiceGrpc.newStub(managedChannel);
	}

	@Test
	public void balanceTest() {
		BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
				.setAccountNumber(1)
				.build();

		Balance balance = this.blockingStub
				// CallCredentials will be attached as CallOption i.e. with every request since the credentials are user/request specific.
				.withCallCredentials(new UserSessionToken("user-secret-2"))
				// .withCallCredentials(new UserSessionToken("user-secret-3"))
				.getBalance(balanceCheckRequest);

		System.out.println(balance);
	}

	// Server streaming test
	@Test
	public void withdrawTest() {
		WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
				.setAccountNumber(1)
				.setAmount(110)
				.build();

		try {
			this.blockingStub
					.withDeadline(Deadline.after(5, TimeUnit.SECONDS))
					.withdraw(withdrawRequest)
					.forEachRemaining(
							amountReceived -> System.out.println("Received " + amountReceived.getValue())
					);
		} catch (StatusRuntimeException statusRuntimeException) {
			// Status status = statusRuntimeException.getStatus();
			// System.out.println(status);

			Metadata trailers = statusRuntimeException.getTrailers();
			Metadata.Key<WithdrawalError> withdrawalErrorKey = ProtoUtils.keyForProto(WithdrawalError.getDefaultInstance());

			System.out.println(trailers.get(withdrawalErrorKey));
		}
	}
}
