package com.grpcflix.movie.repository;

import com.grpcflix.movie.entity.Movie;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
	List<Movie> getMovieByGenreOrderByReleaseYearDesc(String genre);
}
