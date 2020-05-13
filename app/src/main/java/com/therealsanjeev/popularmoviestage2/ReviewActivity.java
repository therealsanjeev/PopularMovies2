package com.therealsanjeev.popularmoviestage2;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.therealsanjeev.popularmoviestage2.databinding.ActivityReviewBinding;
import com.therealsanjeev.popularmoviestage2.models.Review;

public class ReviewActivity extends AppCompatActivity {

    public static final String REVIEW_INTENT_KEY = "com.example.android.popularmovies_2.ui.review";
    public static final String MOVIE_TITLE_KEY = "com.example.android.popularmovies_2.ui.movie_title";
    public static final String COLOR_ACTIONBAR_KEY = "com.example.android.popularmovies_2.ui.color_actionbar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityReviewBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_review);

        Intent intent = getIntent();
        Review review = intent.getParcelableExtra(REVIEW_INTENT_KEY);
        String movieTitle = intent.getStringExtra(MOVIE_TITLE_KEY);
        int color = intent.getIntExtra(COLOR_ACTIONBAR_KEY, ContextCompat.getColor(this, R.color.colorPrimary));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(String.format("%s - %s", getString(R.string.review), movieTitle));
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }

        binding.setReview(review);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
