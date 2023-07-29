package com.grpcflix.movie.service;

import com.example.model.movie.MovieDto;
import com.example.model.movie.MovieSearchRequest;
import com.example.model.movie.MovieSearchResponse;
import com.example.model.movie.MovieServiceGrpc;
import com.grpcflix.movie.repository.MovieRepository;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.stream.Collectors;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class MovieService extends MovieServiceGrpc.MovieServiceImplBase {

	@Autowired
	private MovieRepository movieRepository;

	@Override
	public void getMovies(MovieSearchRequest request, StreamObserver<MovieSearchResponse> responseObserver) {
		List<MovieDto> movieDtoList = this.movieRepository.getMovieByGenreOrderByReleaseYearDesc(request.getGenre().toString())
				.stream()
				.map(movie -> MovieDto.newBuilder()
						.setTitle(movie.getTitle())
						.setYear(movie.getReleaseYear())
						.setRating(movie.getRating())
						.build())
				.collect(Collectors.toList());

		MovieSearchResponse movieSearchResponse = MovieSearchResponse.newBuilder()
				.addAllMovie(movieDtoList)
				.build();

		responseObserver.onNext(movieSearchResponse);
		responseObserver.onCompleted();
	}
}
