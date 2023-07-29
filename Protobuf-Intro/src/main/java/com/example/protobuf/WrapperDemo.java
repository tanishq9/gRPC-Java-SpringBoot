package com.example.protobuf;

import com.example.protobuf.models.PersonAlter;
import com.google.protobuf.Int32Value;

public class WrapperDemo {
	public static void main(String[] args) {
		PersonAlter personAlter = PersonAlter.newBuilder()
				.setAge(
						Int32Value.newBuilder()
								.setValue(10)
								.build()
				)
				.build();

		System.out.println(personAlter);

		System.out.println(personAlter.hasAge());
	}
}
