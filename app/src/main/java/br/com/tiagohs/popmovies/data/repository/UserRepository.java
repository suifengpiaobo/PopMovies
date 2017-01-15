package br.com.tiagohs.popmovies.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.tiagohs.popmovies.data.PopMoviesContract;
import br.com.tiagohs.popmovies.data.PopMoviesDB;
import br.com.tiagohs.popmovies.data.SQLHelper;
import br.com.tiagohs.popmovies.model.db.MovieDB;
import br.com.tiagohs.popmovies.model.db.UserDB;
import br.com.tiagohs.popmovies.model.movie.Movie;
import br.com.tiagohs.popmovies.util.PrefsUtils;

import static android.R.attr.id;

public class UserRepository {
    private static final String TAG = UserRepository.class.getSimpleName();

    private PopMoviesDB mPopMoviesDB;

    public UserRepository(Context context) {
        this.mPopMoviesDB = new PopMoviesDB(context);
    }

    public long saveUser(UserDB user, Context context) {
        SQLiteDatabase db = null;
        Log.i(TAG, "Save User Chamado.");
        long userID = 0;

        try {
            ContentValues values = getUserContentValues(user);

            boolean userJaExistente = findUserByUsername(user.getUsername()) != null;
            db = mPopMoviesDB.getWritableDatabase();

            if (userJaExistente)
                userID = db.update(PopMoviesContract.UserEntry.TABLE_NAME, values, SQLHelper.UserSQL.WHERE_USER_BY_USERNAME, new String[]{user.getUsername()});
            else
                userID = db.insert(PopMoviesContract.UserEntry.TABLE_NAME, "", values);

            PrefsUtils.setCurrentUser(user, context);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            db.close();
        }

        return userID;
    }

    private void deleteUser(String value, String where) {
        SQLiteDatabase db = mPopMoviesDB.getWritableDatabase();
        Log.i(TAG, "Delete User Chamado.");

        try {
            db.delete(PopMoviesContract.UserEntry.TABLE_NAME, where, new String[]{value});
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            db.close();
        }
    }

    public void deleteUserByID(long id) {
        deleteUser(String.valueOf(id), SQLHelper.UserSQL.WHERE_USER_BY_ID);
    }

    public void deleteUserByUsername(String username) {
        deleteUser(username, SQLHelper.UserSQL.WHERE_USER_BY_USERNAME);
    }

    public UserDB findUserByUsername(String username) {
        SQLiteDatabase db = mPopMoviesDB.getWritableDatabase();
        Log.i(TAG, "Find User Chamado.");

        try {
            Cursor c = db.query(PopMoviesContract.UserEntry.TABLE_NAME, null, SQLHelper.UserSQL.WHERE_USER_BY_USERNAME, new String[]{username}, null, null, null);
            if (c.moveToFirst()) {
                return getUserByCursor(c);
            } else {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            db.close();
        }

        return null;
    }

    public List<UserDB> findAllUsers() {
        SQLiteDatabase db = mPopMoviesDB.getWritableDatabase();
        Log.i(TAG, "findAll Users Chamado.");

        try {
            return movieCursorToList(db.query(PopMoviesContract.MoviesEntry.TABLE_NAME, null, null, null, null, null, null));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            db.close();
        }

        return null;
    }

    private List<UserDB> movieCursorToList(Cursor c) {
        List<UserDB> users = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                users.add(getUserByCursor(c));
            } while (c.moveToNext());
        }

        return users;
    }

    private UserDB getUserByCursor(Cursor c) {
        UserDB user = new UserDB();

        user.setUserID(c.getInt(c.getColumnIndex(PopMoviesContract.UserEntry._ID)));
        user.setNome(c.getString(c.getColumnIndex(PopMoviesContract.UserEntry.COLUMN_NAME)));
        user.setPicturePath(c.getString(c.getColumnIndex(PopMoviesContract.UserEntry.COLUMN_PICTURE_PATH)));
        user.setToken(c.getString(c.getColumnIndex(PopMoviesContract.UserEntry.COLUMN_TOKEN)));
        user.setTypeLogin(c.getInt(c.getColumnIndex(PopMoviesContract.UserEntry.COLUMN_TYPE_LOGIN)));
        user.setUsername(c.getString(c.getColumnIndex(PopMoviesContract.UserEntry.COLUMN_USERNAME)));
        user.setEmail(c.getString(c.getColumnIndex(PopMoviesContract.UserEntry.COLUMN_EMAIL)));
        user.setSenha(c.getString(c.getColumnIndex(PopMoviesContract.UserEntry.COLUMN_PASSWORD)));

        return user;
    }

    private ContentValues getUserContentValues(UserDB user) {
        ContentValues values = new ContentValues();

        values.put(PopMoviesContract.UserEntry.COLUMN_NAME, user.getNome());
        values.put(PopMoviesContract.UserEntry.COLUMN_PICTURE_PATH, user.getPicturePath());
        values.put(PopMoviesContract.UserEntry.COLUMN_TOKEN, user.getToken());
        values.put(PopMoviesContract.UserEntry.COLUMN_TYPE_LOGIN, user.getTypeLogin());
        values.put(PopMoviesContract.UserEntry.COLUMN_USERNAME, user.getUsername());
        values.put(PopMoviesContract.UserEntry.COLUMN_EMAIL, user.getEmail());
        values.put(PopMoviesContract.UserEntry.COLUMN_PASSWORD, user.getSenha());

        return values;
    }
}
