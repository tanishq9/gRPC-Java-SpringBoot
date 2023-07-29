package com.grpcflix.user.service;

import com.example.model.movie.Genre;
import com.example.model.user.UserGenreUpdateRequest;
import com.example.model.user.UserResponse;
import com.example.model.user.UserSearchRequest;
import com.example.model.user.UserServiceGrpc;
import com.grpcflix.user.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import javax.transaction.Transactional;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class UserService extends UserServiceGrpc.UserServiceImplBase {

	@Autowired
	private UserRepository userRepository;

	@Override
	public void getUserGenre(UserSearchRequest request, StreamObserver<UserResponse> responseObserver) {
		UserResponse.Builder builder = UserResponse.newBuilder();

		System.out.println(request);

		this.userRepository.findById(request.getLoginId())
				.ifPresent(user -> {

					System.out.println(user);

					builder
							.setLoginId(user.getLogin())
							.setName(user.getName())
							.setGenre(Genre.valueOf(user.getGenre()));
				});

		responseObserver.onNext(builder.build());
		responseObserver.onCompleted();
	}

	@Override
	@Transactional
	public void updateUserGenre(UserGenreUpdateRequest request, StreamObserver<UserResponse> responseObserver) {
		UserResponse.Builder builder = UserResponse.newBuilder();

		this.userRepository.findById(request.getLoginId())
				.ifPresent(user -> {
					user.setGenre(request.getGenre().toString());
					builder
							.setLoginId(user.getLogin())
							.setName(user.getName())
							.setGenre(Genre.valueOf(user.getGenre()));
				});

		responseObserver.onNext(builder.build());
		responseObserver.onCompleted();
	}
}
