package com.example.server.rpctypes;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class GrpcServer {
	public static void main(String[] args) throws IOException, InterruptedException {
		Server server = ServerBuilder.forPort(6565)
				.addService(new BankService())
				.addService(new TransferService())
				.build();

		server.start();

		// To not let the main method exit immediately
		server.awaitTermination();
	}
}
