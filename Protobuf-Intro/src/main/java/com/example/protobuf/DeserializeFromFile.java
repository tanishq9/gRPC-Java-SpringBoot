package com.example.protobuf;

import com.example.protobuf.models.Person;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DeserializeFromFile {

	public static void main(String[] args) throws IOException {
		Person sam = Person.newBuilder()
				.setName("sam")
				.setAge(10)
				.build();

		Path path = Paths.get("sam.txt");
		Files.write(path, sam.toByteArray());

		byte[] bytes = Files.readAllBytes(path);
		Person newSam = Person.parseFrom(bytes);

		System.out.println(newSam);
	}
}
