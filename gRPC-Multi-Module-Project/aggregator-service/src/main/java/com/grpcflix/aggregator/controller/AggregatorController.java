package com.grpcflix.aggregator.controller;

import com.grpcflix.aggregator.dto.RecommendedMovie;
import com.grpcflix.aggregator.dto.UserGenre;
import com.grpcflix.aggregator.service.UserMovieService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AggregatorController {

	@Autowired
	private UserMovieService userMovieService;

	@GetMapping("/user/{loginId}")
	public List<RecommendedMovie> getMovies(@PathVariable String loginId) {
		return this.userMovieService.getUserMovieSuggestion(loginId);
	}

	@PutMapping("/user")
	public void setUserGenre(@RequestBody UserGenre userGenre) {
		this.userMovieService.setUserStub(userGenre);
	}
}
