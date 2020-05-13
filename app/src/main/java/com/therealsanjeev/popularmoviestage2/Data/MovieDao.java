package com.therealsanjeev.popularmoviestage2.Data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.therealsanjeev.popularmoviestage2.models.Movie;
import com.therealsanjeev.popularmoviestage2.models.MovieId;

import java.util.List;

@Dao
public interface MovieDao {
    @Query("SELECT * FROM movie")
    LiveData<List<Movie>> getAll();

    @Query("SELECT movieId FROM movie WHERE movieId = :id")
    MovieId getMovieById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Movie movie);

    @Delete
    void delete(Movie movie);
}