/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.andrew.apollo.ui.activities;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;

import com.andrew.apollo.Config;
import com.andrew.apollo.R;
import com.andrew.apollo.format.Capitalize;
import com.andrew.apollo.loaders.AsyncHandler;
import com.andrew.apollo.loaders.SearchLoader;
import com.andrew.apollo.model.Song;
import com.andrew.apollo.utils.MusicUtils;
import com.andrew.apollo.utils.MusicUtils.ServiceToken;

import java.util.List;

import static com.andrew.apollo.Config.MIME_TYPE;
import static com.andrew.apollo.ui.activities.ProfileActivity.PAGE_FAVORIT;
import static com.andrew.apollo.ui.activities.ProfileActivity.PAGE_MOST_PLAYED;

/**
 * This class is opened when the user touches a Home screen shortcut or album
 * art in an app-wdget, and then carries out the proper action. It is also
 * responsible for processing voice queries and playing the spoken artist,
 * album, song, playlist, or genre.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class ShortcutActivity extends AppCompatActivity implements ServiceConnection, LoaderCallbacks<List<Song>> {

    /**
     * ID of the loader
     */
    private static final int LOADER_ID = 0x32942390;
    /**
     * If true, this class will begin playback and open
     * {@link AudioPlayerActivity}, false will close the class after playback,
     * which is what happens when a user starts playing something from an
     * app-widget
     */
    public static final String OPEN_AUDIO_PLAYER = null;
    /**
     * Service token
     */
    private ServiceToken mToken;
    /**
     * Gather the intent action and extras
     */
    private Intent mIntent;
    /**
     * The list of songs to play
     */
    private long[] mList = {};
    /**
     * Used to shuffle the tracks or play them in order
     */
    private boolean mShouldShuffle;
    /**
     * Search query from a voice action
     */
    private String mVoiceQuery;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fade it in
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        // Bind Apollo's service
        mToken = MusicUtils.bindToService(this, this);
        // Initialize the intent
        mIntent = getIntent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        final Context context = getApplicationContext();
        MusicUtils.connectService(service);
        // Check for a voice query
        if (mIntent.getAction() != null && mIntent.getAction().equals(Config.PLAY_FROM_SEARCH)) {
            LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
        } else if (MusicUtils.isConnected()) {
            AsyncHandler.post(new Runnable() {
                @Override
                public void run() {
                    String requestedMimeType = "";
                    if (mIntent.getExtras() != null) {
                        requestedMimeType = mIntent.getExtras().getString(MIME_TYPE);
                    }
                    // First, check the artist MIME type
                    if (MediaStore.Audio.Artists.CONTENT_TYPE.equals(requestedMimeType)) {
                        // Shuffle the artist track list
                        mShouldShuffle = true;
                        // Get the artist song list
                        mList = MusicUtils.getSongListForArtist(context, getId());
                    } else if (MediaStore.Audio.Albums.CONTENT_TYPE.equals(requestedMimeType)) {
                        // Shuffle the album track list
                        mShouldShuffle = true;
                        // Get the album song list
                        mList = MusicUtils.getSongListForAlbum(context, getId());
                    } else if (MediaStore.Audio.Genres.CONTENT_TYPE.equals(requestedMimeType)) {
                        // Shuffle the genre track list
                        mShouldShuffle = true;
                        // Get the genre song list
                        mList = MusicUtils.getSongListForGenre(context, getId());
                    } else if (MediaStore.Audio.Playlists.CONTENT_TYPE.equals(requestedMimeType)) {
                        // Don't shuffle the playlist track list
                        mShouldShuffle = false;
                        // Get the playlist song list
                        mList = MusicUtils.getSongListForPlaylist(context, getId());
                    } else if (PAGE_FAVORIT.equals(requestedMimeType)) {
                        // Don't shuffle the Favorites track list
                        mShouldShuffle = false;
                        // Get the Favorites song list
                        mList = MusicUtils.getSongListForFavorites(context);
                    } else if (PAGE_MOST_PLAYED.equals(requestedMimeType)) {
                        // Don't shuffle the popular track list
                        mShouldShuffle = false;
                        // Get the popular song list
                        mList = MusicUtils.getPopularSongList(context);
                    } else if (getString(R.string.playlist_last_added).equals(requestedMimeType)) {
                        // Don't shuffle the last added track list
                        mShouldShuffle = false;
                        // Get the Last added song list
                        mList = MusicUtils.getSongListForLastAdded(context);
                    }
                    // Finish up
                    allDone();
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        MusicUtils.disconnectService();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (MusicUtils.isConnected()) {
            MusicUtils.unbindFromService(mToken);
            mToken = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        // Get the voice search query
        mVoiceQuery = Capitalize.capitalize(mIntent.getStringExtra(SearchManager.QUERY));
        return new SearchLoader(ShortcutActivity.this, mVoiceQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(@NonNull Loader<List<Song>> loader, List<Song> data) {
        // disable loader
        LoaderManager.getInstance(this).destroyLoader(LOADER_ID);
        // If the user searched for a playlist or genre, this list will
        // return empty
        if (data.isEmpty()) {
            // Before running the playlist loader, try to play the
            // "Favorites" playlist
            if (isFavorite()) {
                MusicUtils.playFavorites(ShortcutActivity.this);
            }
            // Finish up
            allDone();
            return;
        }
        // What's about to happen is similar to the above process. Apollo
        // runs a
        // series of checks to see if anything comes up. When it does, it
        // assumes (pretty accurately) that it should begin to play that
        // thing.
        // The fancy search query used in {@link SearchLoader} is the key to
        // this. It allows the user to perform very specific queries. i.e.
        // "Listen to Ethio

        String song = data.get(0).getName();
        String album = data.get(0).getAlbum();
        String artist = data.get(0).getArtist();
        // This tripes as the song, album, and artist Id
        long id = data.get(0).getId();
        // First, try to play a song
        if (song != null) {
            mList = new long[]{id};
        } else {
            if (album != null) {
                // Second, try to play an album
                mList = MusicUtils.getSongListForAlbum(ShortcutActivity.this, id);
            } else if (artist != null) {
                // Third, try to play an artist
                mList = MusicUtils.getSongListForArtist(ShortcutActivity.this, id);
            }
        }
        // Finish up
        allDone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoaderReset(@NonNull Loader<List<Song>> loader) {
    }

    /**
     * Used to find the Id supplied
     *
     * @return The Id passed into the activity
     */
    private long getId() {
        Bundle param = mIntent.getExtras();
        if (param != null)
            return param.getLong(Config.ID);
        return -1;
    }

    /**
     * @return True if the user searched for the favorites playlist
     */
    private boolean isFavorite() {
        if (PAGE_FAVORIT.equals(mVoiceQuery)) {
            return true;
        }
        // Check to see if the user spoke the word "Favorite"
        String favorite = getString(R.string.playlist_favorite);
        return favorite.equals(mVoiceQuery);
    }

    /**
     * Starts playback, open {@link AudioPlayerActivity} and finishes this one
     */
    private void allDone() {
        boolean shouldOpenAudioPlayer = mIntent.getBooleanExtra(OPEN_AUDIO_PLAYER, true);
        // Play the list
        if (mList.length > 0) {
            MusicUtils.playAll(mList, 0, mShouldShuffle);
        }
        // Open the now playing screen
        if (shouldOpenAudioPlayer) {
            Intent intent = new Intent(this, AudioPlayerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        // All done
        finish();
    }
}