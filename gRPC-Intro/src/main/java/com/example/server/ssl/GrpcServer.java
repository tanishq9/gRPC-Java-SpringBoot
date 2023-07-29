package com.example.server.ssl;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import java.io.File;
import java.io.IOException;

public class GrpcServer {
	public static void main(String[] args) throws IOException, InterruptedException {
		SslContext sslContext = GrpcSslContexts.forServer(
				new File("/Users/tsaluja/Documents/CA_Test/localhost.crt"),
				new File("/Users/tsaluja/Documents/CA_Test/localhost.pem")
		).build();

		Server server = NettyServerBuilder.forPort(6565)
				.sslContext(sslContext)
				.addService(new BankService())
				.build();

		server.start();

		// To not let the main method exit immediately
		server.awaitTermination();
	}
}
