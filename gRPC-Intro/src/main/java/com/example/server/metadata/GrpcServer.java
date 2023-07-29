package com.example.server.metadata;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class GrpcServer {
	public static void main(String[] args) throws IOException, InterruptedException {
		Server server = ServerBuilder.forPort(6565)
				//.intercept(new AuthInterceptor())
				//.intercept(new AuthInterceptorUsingContext())
				.addService(new MetadataService())
				.build();

		server.start();

		// To not let the main method exit immediately
		server.awaitTermination();
	}
}
