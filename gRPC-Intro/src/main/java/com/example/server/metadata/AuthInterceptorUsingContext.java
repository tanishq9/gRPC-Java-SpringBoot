package com.example.server.metadata;

import static com.example.server.metadata.UserRole.FREE;
import static com.example.server.metadata.UserRole.PREMIUM;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import java.util.Objects;

public class AuthInterceptorUsingContext implements ServerInterceptor {
	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
		System.out.println("Inside AuthInterceptorUsingContext");
		String clientToken = metadata.get(ServerConstants.USER_TOKEN);
		System.out.println("Token value is: " + clientToken);
		if (validate(clientToken)) {
			// Assign role on basis of token value
			UserRole role = getUserRole(clientToken);
			Context context = Context.current().withValue(
					ServerConstants.CTX_USER_ROLE,
					role
			);
			// To pass context info to the service layer
			return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
			// return serverCallHandler.startCall(serverCall, metadata);
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

	public UserRole getUserRole(String token) {
		if ("user-secret-3".equals(token)) {
			return PREMIUM;
		} else if ("user-secret-2".equals(token)) {
			return FREE;
		}
		return null;
	}
}
