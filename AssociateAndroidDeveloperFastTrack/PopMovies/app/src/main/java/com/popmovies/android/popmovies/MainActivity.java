/*
 * Copyright (C) 2017 Thiago Piva Magalhães
 * Main activity to show grid with movies, start communication with server to get movies and
 * handler menu actions.
 */

package com.popmovies.android.popmovies;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.popmovies.android.popmovies.adapters.MovieAdapter;
import com.popmovies.android.popmovies.bo.Movie;
import com.popmovies.android.popmovies.data.PopMoviesPreferences;
import com.popmovies.android.popmovies.webservice.FetchMovies;
import com.popmovies.android.popmovies.webservice.RequestMovies;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FetchMovies.MovieTaskCallback,
        MovieAdapter.OnItemClickListener {

    // TODO fix saveInstance

    private static final String CURRENT_LIST = "current_list";
    private static final String CURRENT_SEARCH = "current_search";
    private static final String CURRENT_PAGE = "current_page";
    private static final String CURRENT_POSITION_RV = "current_position_rv";

    public static final int NUMBER_COLUMNS_LANDSCAPE = 3;
    public static final int NUMBER_COLUMNS_PORTRAIT = 2;

    public static final String SEARCH_TYPE_POPULAR = "popular";
    private static final String SEARCH_TYPE_FAVORITES = "favorites";

    private GridLayoutManager mGridLayoutManager;
    private ProgressBar mLoadingProgressBar;
    private TextView mMessageLoaginErrorTextView;
    private RecyclerView mMoviesGridRecycleView;

    private MovieAdapter mAdapter;

    private int mActualPage = 1;
    private boolean isFetching = false;
    private boolean isRestored = false;

    private ArrayList<Movie> mCurrentMovies = new ArrayList<>();

    private static String sCurrentSearchType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMoviesGridRecycleView = (RecyclerView) findViewById(R.id.rc_grid_movies);
        mLoadingProgressBar = (ProgressBar) findViewById(R.id.pb_loading_movies);
        mMessageLoaginErrorTextView = (TextView) findViewById(R.id.tv_message_error_loading);

        // check orientation to define number of columns on grid
        int numberColumns = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ?
                NUMBER_COLUMNS_LANDSCAPE : NUMBER_COLUMNS_PORTRAIT;

        mGridLayoutManager = new GridLayoutManager(this, numberColumns);

        mMoviesGridRecycleView.setLayoutManager(mGridLayoutManager);
        mAdapter = new MovieAdapter(this);

        mMoviesGridRecycleView.setAdapter(mAdapter);
        // after end of recycle view load more movies from server side.
        mMoviesGridRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    int visibleItens = mGridLayoutManager.getChildCount();
                    int totalItens = mGridLayoutManager.getItemCount();
                    int firstVisible = mGridLayoutManager.findFirstVisibleItemPosition();

                    if ((firstVisible + visibleItens) >= totalItens && !isFetching) {
                        mActualPage++;
                        fetchMovies();
                    }
                }
            }
        });

        if (!Utility.isOnline(this)) {
            showMessageError();
        } else {
            if (savedInstanceState != null) {
                mCurrentMovies = savedInstanceState.getParcelableArrayList(CURRENT_LIST);
                mAdapter.setmMovieList(mCurrentMovies);

                mMoviesGridRecycleView.getLayoutManager().scrollToPosition(savedInstanceState.getInt(CURRENT_POSITION_RV));
                isRestored = true;
            }
        }

    }

    @Override
    public void onPreExecute() {
        isFetching = true;
        showProgress();
    }

    @Override
    public void onPostExecute(List<Movie> movies) {
        isFetching = false;
        showContent();
        if (movies != null) {
            mCurrentMovies.addAll(movies);
            mAdapter.setmMovieList(mCurrentMovies);
        } else {
            showMessageError();
        }
    }

    /**
     * Show progressBar to user to inform more movies are loading.
     */
    private void showProgress() {
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        mMessageLoaginErrorTextView.setVisibility(View.INVISIBLE);
    }

    /**
     * Shows content of grid (movies) and turn invisible other UI elements.
     */
    private void showContent() {
        if (!mMoviesGridRecycleView.isShown()) {
            mMessageLoaginErrorTextView.setVisibility(View.VISIBLE);
        }

        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        mMessageLoaginErrorTextView.setVisibility(View.INVISIBLE);
    }

    /**
     * Fetch movies from the movie DB server by current search (popular or top rated) and
     * actual page of movies (server attribute).
     */
    private void fetchMovies() {
        RequestMovies.requestMovies(sCurrentSearchType, mActualPage, this, this);
    }

    /**
     * Display error message during loading movies from server.
     */
    private void showMessageError() {
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        mMessageLoaginErrorTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClicked(Movie movie) {
        Class targetClass = DetailMovieActivity.class;
        Intent detailIntent = new Intent(this, targetClass);
        detailIntent.putExtra(DetailMovieActivity.EXTRA_MOVIE, movie);
        startActivity(detailIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(CURRENT_LIST, mCurrentMovies);
        outState.putString(CURRENT_SEARCH, sCurrentSearchType);
        outState.putInt(CURRENT_PAGE, mActualPage);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        sCurrentSearchType = savedInstanceState.getString(CURRENT_SEARCH);
        mActualPage = savedInstanceState.getInt(CURRENT_PAGE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String prefSearchType = PopMoviesPreferences.getSearchType(this);
        if (!isRestored
                && !isFetching
                && !sCurrentSearchType.equals(prefSearchType)) {
            sCurrentSearchType = prefSearchType;
            // it is necessary reset
            if (SEARCH_TYPE_FAVORITES.equals(prefSearchType)) {
                // TODO make db
            } else {
                reset();
            }
        } else {
            isRestored = false;
        }
    }

    /**
     * Restart variable classes related with fetch movies from server.
     */
    private void reset() {
        if (!Utility.isOnline(this)) {
            showMessageError();
            return;
        }

        mCurrentMovies.clear();
        mAdapter.notifyDataSetChanged();
        mActualPage = 1;
        fetchMovies();
    }
}
