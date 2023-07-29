package com.example.server.deadline;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class GrpcServer {
	public static void main(String[] args) throws IOException, InterruptedException {
		Server server = ServerBuilder.forPort(6565)
				.addService(new DeadlineService())
				.build();

		server.start();

		// To not let the main method exit immediately
		server.awaitTermination();
	}
}
