package com.therealsanjeev.popularmoviestage2.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.therealsanjeev.popularmoviestage2.data.AppDatabase;
import com.therealsanjeev.popularmoviestage2.data.AppPreference;
import com.therealsanjeev.popularmoviestage2.DetailActivity;
import com.therealsanjeev.popularmoviestage2.MainActivity;
import com.therealsanjeev.popularmoviestage2.R;
import com.therealsanjeev.popularmoviestage2.databinding.ItemMovieBinding;
import com.therealsanjeev.popularmoviestage2.models.Movie;
import com.therealsanjeev.popularmoviestage2.models.MovieId;
import com.therealsanjeev.popularmoviestage2.utils.MovieExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private Activity mActivity;
    private AppDatabase mDatabase;
    private List<Movie> mList;
    private Executor executor;

    public MovieAdapter(Activity activity) {
        this.mActivity = activity;
        this.mDatabase = AppDatabase.getDatabase(activity);
        this.executor = new MovieExecutor();
    }


    @NonNull
    @Override
    public MovieAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
        ItemMovieBinding binding = ItemMovieBinding.inflate(layoutInflater, parent, false);
        return new MovieAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieAdapterViewHolder holder, int position) {
        Movie movie = mList.get(position);
        holder.bind(movie);
    }

    @Override
    public int getItemCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    public void clearList() {
        if (mList == null) {
            mList = new ArrayList<>();
        } else {
            int itemCount = mList.size();
            mList.clear();
            notifyItemRangeRemoved(0, itemCount);
        }
    }

    public void addMoviesList(List<Movie> moviesList) {
        int positionStart = mList.size();
        mList.clear();

        mList.addAll(moviesList);
        notifyItemRangeInserted(positionStart, moviesList.size() - positionStart);
    }

    // using for refreshing favorite star when back from detail ui
    public void refreshFavorite() {
        int movieNumber = AppPreference.getChangedMovie(mActivity);
        if (movieNumber != -1) {
            notifyItemChanged(movieNumber);
            AppPreference.setChangedMovie(mActivity, -1);
        }
    }






    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder {
        ItemMovieBinding binding;
        boolean isFavorite;

        MovieAdapterViewHolder(ItemMovieBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Movie movie) {
            binding.setMovie(movie);
            binding.setPresenter(this);


            Glide.with(binding.movieItemIv.getContext())
                    .load("http://image.tmdb.org/t/p/w342" + movie.posterPath)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.drawable.error)
                    .into(binding.movieItemIv);

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    final MovieId miniMovie = mDatabase.movieDao().getMovieById(movie.movieId);

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (miniMovie != null) {
                                binding.favoriteIv.setImageResource(R.drawable.ic_favorite);
                                isFavorite = true;
                            } else {
                                binding.favoriteIv.setImageResource(R.drawable.ic_favorite_border);
                                isFavorite = false;
                            }
                        }
                    });
                }
            });
        }

        public void openMovieDetail(Movie movie) {
            int movieNumber = getAdapterPosition();

            Intent intent = new Intent(mActivity, DetailActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(mActivity,
                            binding.movieItemIv,
                            ViewCompat.getTransitionName(binding.movieItemIv));
            intent.putExtra(DetailActivity.DETAIL_INTENT_KEY, movie);
            intent.putExtra(DetailActivity.MOVIE_NUMBER_KEY, movieNumber);
            mActivity.startActivity(intent, options.toBundle());
        }

        public void onClickFavorite(View view) {
            String snackBarText;
            int position = getAdapterPosition();
            final Movie movie = mList.get(position);

            if (isFavorite) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        mDatabase.movieDao().delete(movie);
                    }
                });
                isFavorite = false;
                binding.favoriteIv.setImageResource(R.drawable.ic_favorite_border);
                snackBarText = mActivity.getString(R.string.remove_favorite);

                if (AppPreference.getSorting(mActivity) == MainActivity.FAVORITES) {
                    mList.remove(position);
                    notifyItemRemoved(position);
                }

            } else {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        mDatabase.movieDao().insert(movie);
                    }
                });
                isFavorite = true;
                binding.favoriteIv.setImageResource(R.drawable.ic_favorite);
                snackBarText = mActivity.getString(R.string.add_favorite);
            }
            Snackbar.make(view, snackBarText, Snackbar.LENGTH_SHORT).show();
        }


    }
}
