package com.example.client.metadata;

import io.grpc.Metadata;

public class ClientConstants {

	public static final Metadata.Key<String> USER_TOKEN = Metadata.Key.of("user-token", Metadata.ASCII_STRING_MARSHALLER);
	private static final Metadata METADATA = new Metadata();

	static {
		// Metadata represents the headers
		// The value can byte array, protobuf object, etc.
		METADATA.put(
				// key name, how to marshal/unmarshal the value
				Metadata.Key.of("client-token", Metadata.ASCII_STRING_MARSHALLER),
				"bank-client-secret"
		);
	}

	public static Metadata getClientToken() {
		return METADATA;
	}
}
