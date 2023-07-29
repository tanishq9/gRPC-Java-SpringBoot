package com.grpcflix.aggregator.service;

import com.example.model.movie.Genre;
import com.example.model.movie.MovieSearchRequest;
import com.example.model.movie.MovieSearchResponse;
import com.example.model.movie.MovieServiceGrpc;
import com.example.model.user.UserGenreUpdateRequest;
import com.example.model.user.UserResponse;
import com.example.model.user.UserSearchRequest;
import com.example.model.user.UserServiceGrpc;
import com.grpcflix.aggregator.dto.RecommendedMovie;
import com.grpcflix.aggregator.dto.UserGenre;
import java.util.List;
import java.util.stream.Collectors;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class UserMovieService {

	@GrpcClient("user-service")
	private UserServiceGrpc.UserServiceBlockingStub userStub;

	@GrpcClient("movie-service")
	private MovieServiceGrpc.MovieServiceBlockingStub movieStub;

	public List<RecommendedMovie> getUserMovieSuggestion(String loginId) {
		UserResponse userResponse = this.userStub.getUserGenre(UserSearchRequest.newBuilder().setLoginId(loginId).build());

		System.out.println(userResponse.getGenre());

		MovieSearchResponse movieSearchResponse = this.movieStub.getMovies(MovieSearchRequest.newBuilder().setGenre(userResponse.getGenre()).build());
		return movieSearchResponse.getMovieList()
				.stream()
				.map(movieDto -> new RecommendedMovie(
						movieDto.getTitle(),
						movieDto.getYear(),
						movieDto.getRating()
				))
				.collect(Collectors.toList());
	}

	public void setUserStub(UserGenre userGenre) {
		UserResponse userResponse = this.userStub.updateUserGenre(
				UserGenreUpdateRequest.newBuilder()
						.setLoginId(userGenre.getLoginId())
						.setGenre(Genre.valueOf(userGenre.getGenre()))
						.build()
		);
		System.out.println(userResponse);
	}
}
