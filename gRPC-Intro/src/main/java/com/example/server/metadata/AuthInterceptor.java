package com.example.server.metadata;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import java.util.Objects;

public class AuthInterceptor implements ServerInterceptor {
	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
		System.out.println("Inside AuthInterceptor");
		//String clientToken = metadata.get(ServerConstants.TOKEN);
		String clientToken = metadata.get(ServerConstants.USER_TOKEN);
		System.out.println("Token value is: " + clientToken);
		if (validate(clientToken)) {
			return serverCallHandler.startCall(serverCall, metadata);
		} else {
			Status status = Status.UNAUTHENTICATED.withDescription("invalid token");
			serverCall.close(status, metadata);
		}
		return new ServerCall.Listener<ReqT>() {
		};
	}

	private boolean validate(String token) {
		// return Objects.nonNull(token) && token.equals("bank-client-secret");
		return Objects.nonNull(token) && (token.equals("user-secret-3") || token.equals("user-secret-2"));
	}
}
