package com.example.protobuf;

import com.example.protobuf.models.Credentials;
import com.example.protobuf.models.EmailCredentials;
import com.example.protobuf.models.PhoneOTP;

public class OneOfDemo {
	public static void main(String[] args) {
		EmailCredentials emailCredentials = EmailCredentials.newBuilder()
				.setEmail("abc@gmail.com")
				.setPassword("abc")
				.build();

		PhoneOTP phoneOTP = PhoneOTP.newBuilder()
				.setNumber(1234567891)
				.setCode(123)
				.build();

		Credentials credentials = Credentials.newBuilder()
				.setEmailMode(emailCredentials)
				.setPhoneMode(phoneOTP) // only one value is picked in oneOf, which would be last one, like phoneMode here
				.build();

		login(credentials);
	}

	private static void login(Credentials credentials) {
		System.out.println(credentials.getEmailMode());
		System.out.println(credentials.getPhoneMode());

		if (credentials.hasEmailMode()) {
			System.out.println("EMAIL mode");
		} else if (credentials.hasPhoneMode()) {
			System.out.println("PHONE mode");
		}

		switch (credentials.getModeCase()) {
			case EMAILMODE:
				System.out.println("EMAIL mode");
				break;
			case PHONEMODE:
				System.out.println("PHONE mode");
				break;
		}
	}
}
