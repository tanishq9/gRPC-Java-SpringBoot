package com.example.client.ssl;

import com.example.models.Balance;
import com.example.models.BalanceCheckRequest;
import com.example.models.BankServiceGrpc;
import com.example.models.WithdrawRequest;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import java.io.File;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SSLClientTest {

	private BankServiceGrpc.BankServiceBlockingStub blockingStub;
	private BankServiceGrpc.BankServiceStub bankServiceStub;

	@BeforeAll
	public void setup() throws SSLException {
		SslContext sslContext = GrpcSslContexts.forClient()
				.trustManager(new File("/Users/tsaluja/Documents/CA_Test/ca.cert.pem"))
				.build();

		ManagedChannel managedChannel = NettyChannelBuilder
				.forAddress("localhost", 6565)
				.sslContext(sslContext)
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
				// when setting deadline explicitly for a call then use that instead of what is added by global deadline interceptor
				.withDeadline(Deadline.after(2, TimeUnit.SECONDS))
				.getBalance(balanceCheckRequest);

		System.out.println(balance);
	}

	// Server streaming test
	@Test
	public void withdrawTest() {
		WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
				.setAccountNumber(1)
				.setAmount(50)
				.build();

		this.blockingStub
				.withDeadline(Deadline.after(5, TimeUnit.SECONDS))
				.withdraw(withdrawRequest)
				.forEachRemaining(
						amountReceived -> System.out.println("Received " + amountReceived.getValue())
				);
	}
}
