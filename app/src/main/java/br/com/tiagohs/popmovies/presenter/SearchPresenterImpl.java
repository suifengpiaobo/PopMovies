package br.com.tiagohs.popmovies.presenter;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.tiagohs.popmovies.App;
import br.com.tiagohs.popmovies.data.repository.MovieRepository;
import br.com.tiagohs.popmovies.interceptor.SearchMoviesInterceptor;
import br.com.tiagohs.popmovies.interceptor.SearchMoviesInterceptorImpl;
import br.com.tiagohs.popmovies.interceptor.SearchPersonInterceptor;
import br.com.tiagohs.popmovies.interceptor.SearchPersonInterceptorImpl;
import br.com.tiagohs.popmovies.model.credits.MediaBasic;
import br.com.tiagohs.popmovies.model.db.MovieDB;
import br.com.tiagohs.popmovies.model.movie.Movie;
import br.com.tiagohs.popmovies.model.movie.MovieDetails;
import br.com.tiagohs.popmovies.model.person.PersonFind;
import br.com.tiagohs.popmovies.model.response.GenericListResponse;
import br.com.tiagohs.popmovies.server.ResponseListener;
import br.com.tiagohs.popmovies.server.methods.MoviesServer;
import br.com.tiagohs.popmovies.util.MovieUtils;
import br.com.tiagohs.popmovies.util.PrefsUtils;
import br.com.tiagohs.popmovies.util.enumerations.SearchType;
import br.com.tiagohs.popmovies.view.SearchMoviesView;
import br.com.tiagohs.popmovies.view.SearchPersonsView;
import br.com.tiagohs.popmovies.view.SearchView;

public class SearchPresenterImpl implements SearchPresenter, SearchPersonInterceptor.onSearchPersonListener,
                                            SearchMoviesInterceptor.onSearchMoviesListener, ResponseListener<MovieDetails> {

    private SearchMoviesInterceptor mSearchMoviesInterceptor;
    private SearchPersonInterceptor mSearchPersonInterceptor;
    private MovieRepository mMovieRepository;
    private MoviesServer mMoviesServer;

    private int mMovieCurrentPage;
    private int mMovieTotalPages;
    private int mPersonCurrentPage;
    private int mPersonTotalPages;
    private Context mContext;

    private SearchMoviesView mSearchMoviesView;
    private SearchPersonsView mSearchPersonsView;

    private boolean mIsNewMovieSearch;
    private boolean mIsNewPersonSearch;

    private boolean mButtonStage;

    public SearchPresenterImpl() {
        mMovieCurrentPage = mPersonCurrentPage =1;

        mMoviesServer = new MoviesServer();
        mSearchMoviesInterceptor = new SearchMoviesInterceptorImpl(this);
        mSearchPersonInterceptor = new SearchPersonInterceptorImpl(this);
    }

    @Override
    public void setView(SearchView view) {

    }

    public void setMovieView(SearchMoviesView searchMoviesView) {
        mSearchMoviesView = searchMoviesView;
    }

    public void setPersonView(SearchPersonsView searchPersonsView) {
        mSearchPersonsView = searchPersonsView;
    }

    @Override
    public void setContext(Context context) {
        mContext = context;
        mMovieRepository = new MovieRepository(mContext);
    }

    @Override
    public void onCancellRequest(Activity activity, String tag) {
        ((App) activity.getApplication()).cancelAll(tag);
    }

    public void searchMovies(String query,
                             Boolean includeAdult,
                             Integer searchYear, String tag,
                             Integer primaryReleaseYear, boolean isNewSearch) {
        mIsNewMovieSearch = isNewSearch;

        if (mSearchMoviesView.isInternetConnected()) {
            mSearchMoviesInterceptor.searchMovies(query, includeAdult,
                                                  searchYear, primaryReleaseYear, tag,
                    mIsNewMovieSearch ? mMovieCurrentPage : ++mMovieCurrentPage);
        } else {
            if (mSearchMoviesView.isAdded())
                mSearchMoviesView.onError("Sem Conexão");

            mSearchMoviesView.setProgressVisibility(View.GONE);
        }

    }

    public void searchPersons(String query,
                              Boolean includeAdult, String tag,
                              SearchType searchType, boolean isNewSearch) {
        mIsNewPersonSearch = isNewSearch;

        if (mSearchPersonsView.isInternetConnected()) {
            mSearchPersonsView.setProgressVisibility(View.VISIBLE);
            mSearchPersonInterceptor.searchPersons(query, includeAdult, searchType, tag,
                                                   mIsNewPersonSearch ? mMovieCurrentPage : ++mMovieCurrentPage);
        } else {
            if (mSearchPersonsView.isAdded())
                mSearchPersonsView.onError("Sem Conexão");

            mSearchPersonsView.setProgressVisibility(View.GONE);
        }

    }


    @Override
    public void onSearchMoviesRequestSucess(GenericListResponse<Movie> response) {
        mMovieCurrentPage = response.getPage();
        mMovieTotalPages = response.getTotalPage();
        mSearchMoviesView.setNenhumFilmeEncontradoVisibility(View.GONE);

        if (mIsNewMovieSearch) {
            if (response.getResults().isEmpty()) {
                mSearchMoviesView.setNenhumFilmeEncontradoVisibility(View.VISIBLE);
                setupResponseMovies(new ArrayList<Movie>());
            } else {
                setupResponseMovies(response.getResults());
            }
        } else {
            mSearchMoviesView.addAllMovies(response.getResults(), hasMoreMoviePages());
            mSearchMoviesView.updateAdapter();
        }

        mSearchMoviesView.setProgressVisibility(View.GONE);
    }

    public void getMovieDetails(int movieID, boolean buttonStage, String tag) {
        this.mButtonStage = buttonStage;

        if (mSearchMoviesView.isInternetConnected()) {
            mMoviesServer.getMovieDetails(movieID, new String[]{}, tag, this);
        } else {
            if (mSearchMoviesView.isAdded())
                mSearchMoviesView.onError("Sem Conexão");

            mSearchMoviesView.setProgressVisibility(View.GONE);
        }
    }

    public boolean isJaAssistido(int movieID) {
        return mMovieRepository.findMovieByServerID(movieID, PrefsUtils.getCurrentProfile(mContext).getProfileID()) != null;
    }

    private void setupResponseMovies(List<Movie> movies) {
        mSearchMoviesView.setListMovies(movies, hasMoreMoviePages());
        mSearchMoviesView.setupRecyclerView();
    }

    private boolean hasMoreMoviePages() {
        return mMovieCurrentPage < mMovieTotalPages;
    }

    @Override
    public void onSearchMoviesRequestError(VolleyError error) {
        mSearchMoviesView.setNenhumFilmeEncontradoVisibility(View.VISIBLE);
        mSearchMoviesView.setProgressVisibility(View.GONE);

        if (error.networkResponse != null) {
            switch (error.networkResponse.statusCode) {
                case 422:
                    setupResponseMovies(new ArrayList<Movie>());
                    break;
                default:
                    if (mSearchMoviesView.isAdded())
                        mSearchMoviesView.onError("Sem Conexão");
            }
        }


    }

    @Override
    public void onSearchPersonRequestSucess(GenericListResponse<PersonFind> personFind) {
        mPersonCurrentPage = personFind.getPage();
        mPersonTotalPages = personFind.getTotalPage();
        mSearchPersonsView.setNenhumaPessoaEncontradoVisibility(View.GONE);

        if (mIsNewPersonSearch) {
            if (personFind.getResults().isEmpty()) {
                mSearchPersonsView.setNenhumaPessoaEncontradoVisibility(View.VISIBLE);
                setupResponsePerson(new ArrayList<PersonFind>());
            } else {
                setupResponsePerson(personFind.getResults());
            }
        } else {
            mSearchPersonsView.addAllPersons(personFind.getResults(), hasMorePersonPages());
            mSearchPersonsView.updateAdapter();
        }

        mSearchPersonsView.setProgressVisibility(View.GONE);
    }

    private boolean hasMorePersonPages() {
        return mPersonCurrentPage < mPersonTotalPages;
    }

    private void setupResponsePerson(List<PersonFind> movies) {
        mSearchPersonsView.setListPersons(movies, hasMorePersonPages());
        mSearchPersonsView.setupRecyclerView();
    }

    @Override
    public void onSearchPersonRequestError(VolleyError error) {
        mSearchPersonsView.setNenhumaPessoaEncontradoVisibility(View.VISIBLE);
        mSearchPersonsView.setProgressVisibility(View.GONE);

        if (error.networkResponse != null) {
            switch (error.networkResponse.statusCode) {
                case 422:
                    setupResponsePerson(new ArrayList<PersonFind>());
                    break;
                default:
                    if (mSearchPersonsView.isAdded())
                        mSearchPersonsView.onError("Sem Conexão");
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(MovieDetails movie) {
        if (mButtonStage)
            mMovieRepository.saveMovie(new MovieDB(movie.getId(), MovieDB.STATUS_WATCHED, movie.getRuntime(), movie.getPosterPath(),
                    movie.getTitle(), movie.isFavorite(), movie.getVoteCount(), PrefsUtils.getCurrentProfile(mContext).getProfileID(),
                    Calendar.getInstance(), MovieUtils.formateStringToCalendar(movie.getReleaseDate()),
                    MovieUtils.getYearByDate(movie.getReleaseDate()), MovieUtils.genreToGenreDB(movie.getGenres())));
        else
            mMovieRepository.deleteMovieByServerID(movie.getId(), PrefsUtils.getCurrentProfile(mContext).getProfileID());
    }
}
