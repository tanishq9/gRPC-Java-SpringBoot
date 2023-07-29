package com.grpcflix.movie.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;
import lombok.ToString;

@Entity
@ToString
@Data
public class Movie {

	@Id
	private int id;
	private String title;

	// @Column(name = "release_year")
	// private int year;
	// Use either above or above way
	private int releaseYear;

	private double rating;
	private String genre;
}
