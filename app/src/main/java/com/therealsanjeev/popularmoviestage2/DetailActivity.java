package com.therealsanjeev.popularmoviestage2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.ShareCompat;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.therealsanjeev.popularmoviestage2.Adapter.ReviewAdapter;
import com.therealsanjeev.popularmoviestage2.Adapter.VideoAdapter;
import com.therealsanjeev.popularmoviestage2.Data.AppDatabase;
import com.therealsanjeev.popularmoviestage2.Data.AppPreference;
import com.therealsanjeev.popularmoviestage2.NetWork.MovieService;
import com.therealsanjeev.popularmoviestage2.NetWork.RetrofitClient;
import com.therealsanjeev.popularmoviestage2.databinding.ActivityDetailBinding;
import com.therealsanjeev.popularmoviestage2.models.ApiResponse;
import com.therealsanjeev.popularmoviestage2.models.Movie;
import com.therealsanjeev.popularmoviestage2.models.MovieDetail;
import com.therealsanjeev.popularmoviestage2.models.MovieId;
import com.therealsanjeev.popularmoviestage2.models.Review;
import com.therealsanjeev.popularmoviestage2.models.Video;
import com.therealsanjeev.popularmoviestage2.utils.HorizontalItemDecoration;
import com.therealsanjeev.popularmoviestage2.utils.MovieExecutor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    public static final String DETAIL_INTENT_KEY = "com.example.android.popularmovies_2.ui.detail";
    public static final String MOVIE_NUMBER_KEY = "com.example.android.popularmovies_2.ui.movie_number";

    private static final String BUNDLE_VIDEOS = "videos";
    private static final String BUNDLE_REVIEWS = "reviews";

    private ActivityDetailBinding mBinding;
    private AppDatabase mDatabase;
    private boolean isFavorite;
    private VideoAdapter mVideoAdapter;
    private ReviewAdapter mReviewAdapter;
    private Target targetBackdrop;
    private Movie movie;
    private RetrofitClient mApiClient;
    private Executor executor;
    private int movieNumber;
    private int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // Making Collapsing Toolbar Width / Height Ratio = 3 / 2
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int width = displaymetrics.widthPixels;
            mBinding.collapsingToolbar.getLayoutParams().height = (int) Math.round(width / 1.5);
        }
        // END OF Making Collapsing Toolbar Width / Height Ratio = 3 / 2

        mDatabase = AppDatabase.getDatabase(this);

        mApiClient = MovieService.createService(RetrofitClient.class);
        executor = new MovieExecutor();

        Intent intent = getIntent();
        movieNumber = intent.getIntExtra(MOVIE_NUMBER_KEY, -1);
        movie = intent.getParcelableExtra(DETAIL_INTENT_KEY);

        mBinding.setMovie(movie);
        mBinding.setPresenter(this);

        setSupportActionBar(mBinding.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        populateUI();
        populateVideos(savedInstanceState);
        populateReviews(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(BUNDLE_VIDEOS, mVideoAdapter.getList());
        outState.putParcelableArrayList(BUNDLE_REVIEWS, mReviewAdapter.getList());
    }


    private void populateUI(){

        targetBackdrop = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mBinding.backdrop.setImageBitmap(bitmap);
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@NonNull Palette palette) {
                        color = palette.getMutedColor(R.attr.colorPrimary) | 0xFF000000;
                        mBinding.collapsingToolbar.setContentScrimColor(color);
                        mBinding.collapsingToolbar.setStatusBarScrimColor(color);
                    }
                });
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };


        Picasso.get()
                .load("http://image.tmdb.org/t/p/w780" + movie.backdropPath)
                .into(targetBackdrop);

        Picasso.get()
                .load("http://image.tmdb.org/t/p/w342" + movie.posterPath)
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.drawable.error)
                .into(mBinding.movieDetails.poster);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                MovieId movieId = mDatabase.movieDao().getMovieById(movie.movieId);

                if (movieId != null) {
                    isFavorite = true;
                    mBinding.favoriteButton.setImageResource(R.drawable.ic_favorite);
                } else {
                    isFavorite = false;

                    mBinding.favoriteButton.setImageResource(R.drawable.ic_favorite_border);
                }
            }
        });

    }
    private void populateVideos(Bundle savedInstanceState) {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.movieVideos.videosList.setLayoutManager(layoutManager);
        mBinding.movieVideos.videosList.setHasFixedSize(true);
        mBinding.movieVideos.videosList.setNestedScrollingEnabled(false);

        RecyclerView.ItemDecoration itemDecoration = new HorizontalItemDecoration(this);
        mBinding.movieVideos.videosList.addItemDecoration(itemDecoration);

        mVideoAdapter = new VideoAdapter(this);
        mBinding.movieVideos.videosList.setAdapter(mVideoAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_VIDEOS)) {
            mVideoAdapter.addVideosList(savedInstanceState.
                    <Video>getParcelableArrayList(BUNDLE_VIDEOS));
        } else {
            Call<ApiResponse<Video>> call = mApiClient.getVideos(movie.movieId);

            call.enqueue(new Callback<ApiResponse<Video>>() {
                @Override
                public void onResponse(Call<ApiResponse<Video>> call,
                                       Response<ApiResponse<Video>> response) {
                    List<Video> result = response.body().results;
                    mVideoAdapter.addVideosList(result);
                    if (result.size() == 0) {
                        mBinding.movieVideos.videosLabel.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Video>> call, Throwable t) {
                    Toast.makeText(DetailActivity.this,
                            getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    private void populateReviews(Bundle savedInstanceState) {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.movieReviews.reviewsList.setLayoutManager(layoutManager);
        mBinding.movieReviews.reviewsList.setHasFixedSize(true);
        mBinding.movieReviews.reviewsList.setNestedScrollingEnabled(false);

        RecyclerView.ItemDecoration itemDecoration = new HorizontalItemDecoration(this);
        mBinding.movieReviews.reviewsList.addItemDecoration(itemDecoration);

        mReviewAdapter = new ReviewAdapter(this);
        mBinding.movieReviews.reviewsList.setAdapter(mReviewAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_REVIEWS)) {
            mReviewAdapter.addReviewsList(savedInstanceState.<Review>getParcelableArrayList(BUNDLE_REVIEWS));
        } else {
            Call<ApiResponse<Review>> call = mApiClient.getReviews(movie.movieId);

            call.enqueue(new Callback<ApiResponse<Review>>() {
                @Override
                public void onResponse(Call<ApiResponse<Review>> call,
                                       Response<ApiResponse<Review>> response) {
                    List<Review> result = response.body().results;
                    mReviewAdapter.addReviewsList(result);
                    if (result.size() == 0) {
                        mBinding.movieReviews.reviewsLabel.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Review>> call, Throwable t) {
                    Toast.makeText(DetailActivity.this,
                            getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void onClickFavoriteButton() {
        AppPreference.setChangedMovie(this, movieNumber);

        String snackBarText;
        if (isFavorite) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    mDatabase.movieDao().delete(movie);
                }
            });
            isFavorite = false;
            mBinding.favoriteButton.setImageResource(R.drawable.ic_favorite_border);
            snackBarText = getString(R.string.remove_favorite);
        } else {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    mDatabase.movieDao().insert(movie);
                }
            });
            isFavorite = true;
            mBinding.favoriteButton.setImageResource(R.drawable.ic_favorite);
            snackBarText = getString(R.string.add_favorite);
        }
        Snackbar.make(mBinding.coordinatorLayout, snackBarText, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                String shareText = "https://www.themoviedb.org/movie/" + movie.movieId;
                ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder.from(this)
                        .setText(shareText)
                        .setType("text/plain");
                try {
                    intentBuilder.startChooser();
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, R.string.no_app, Toast.LENGTH_LONG).show();
                }
                return true;
            case android.R.id.home:
                mBinding.favoriteButton.setVisibility(View.INVISIBLE);
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        mBinding.favoriteButton.setVisibility(View.INVISIBLE);
        super.onBackPressed();
    }

    public String formatReleaseDate(String releaseDate) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = sdf.parse(releaseDate);
        } catch (ParseException e) {
            return releaseDate;
        }

        return DateFormat.getDateInstance(DateFormat.LONG).format(date);
    }

    public String getEnglishPlotSynopsis(String id) {
        Call<MovieDetail> call = mApiClient.getMovieById(id);

        call.enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {
                try {
                    String plotSynopsis = response.body().plotSynopsis;
                    mBinding.movieDetails.plotSynopsisTv.setText(plotSynopsis);
                } catch (NullPointerException e) {
                    Toast.makeText(DetailActivity.this,
                            getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                Toast.makeText(DetailActivity.this,
                        getString(R.string.connection_error), Toast.LENGTH_LONG).show();
            }
        });

        return "";
    }

    public void onClickExpand(View view, Review review) {
        Intent intent = new Intent(this, ReviewActivity.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this,
                        view,
                        ViewCompat.getTransitionName(view));
        intent.putExtra(ReviewActivity.REVIEW_INTENT_KEY, review);
        intent.putExtra(ReviewActivity.MOVIE_TITLE_KEY, movie.originalTitle);
        intent.putExtra(ReviewActivity.COLOR_ACTIONBAR_KEY, color);
        startActivity(intent, options.toBundle());
    }
}
