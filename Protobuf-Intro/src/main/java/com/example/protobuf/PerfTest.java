package com.example.protobuf;

import com.example.json.JPerson;
import com.example.protobuf.models.Person;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;

public class PerfTest {

	public static void main(String[] args) {
		// json
		JPerson jPerson = new JPerson();
		jPerson.setName("sam");
		jPerson.setAge(10);
		ObjectMapper mapper = new ObjectMapper();

		Runnable json = () -> {
			try {
				// serialising to string
				byte[] bytes = mapper.writeValueAsBytes(jPerson);
				// de-serialising to object
				JPerson jPerson1 = mapper.readValue(bytes, JPerson.class);
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		};

		// protobuf
		Person sam = Person.newBuilder()
				.setName("sam")
				.setAge(10)
				.build();

		Runnable proto = () -> {
			try {
				byte[] bytes = sam.toByteArray();
				Person person = Person.parseFrom(bytes);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		};

		runPerfTest(json, "JSON");
		runPerfTest(proto, "PROTO");
	}

	private static void runPerfTest(Runnable runnable, String method) {
		long time1 = System.currentTimeMillis();
		for (int i = 0; i < 1_000_000; i++) {
			runnable.run();
		}
		long time2 = System.currentTimeMillis();
		System.out.println(method + ": " + (time2 - time1) + " ms");
	}
}
