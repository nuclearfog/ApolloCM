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

package com.andrew.apollo.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio.Artists.Albums;

import com.andrew.apollo.model.Album;
import com.andrew.apollo.utils.PreferenceUtils;

import java.util.LinkedList;
import java.util.List;

import static com.andrew.apollo.loaders.AlbumLoader.ALBUM_COLUMN;
import static com.andrew.apollo.loaders.AlbumLoader.ALBUM_URI;

/**
 * Used to query {@link Albums} and return the albums
 * for a particular artist.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class ArtistAlbumLoader extends WrappedAsyncTaskLoader<List<Album>> {

    /**
     * The Id of the artist the albums belong to.
     */
    private long mArtistID;

    /**
     * Constructor of <code>ArtistAlbumHandler</code>
     *
     * @param context  The {@link Context} to use.
     * @param artistId The Id of the artist the albums belong to.
     */
    public ArtistAlbumLoader(Context context, Long artistId) {
        super(context);
        mArtistID = artistId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Album> loadInBackground() {
        List<Album> result = new LinkedList<>();
        // Create the Cursor
        Cursor mCursor = makeArtistAlbumCursor();
        // Gather the dataS
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    // Copy the album id
                    long id = mCursor.getLong(0);
                    // Copy the album name
                    String albumName = mCursor.getString(1);
                    // Copy the artist name
                    String artist = mCursor.getString(2);
                    // Copy the number of songs
                    int songCount = mCursor.getInt(3);
                    // Copy the release year
                    String year = mCursor.getString(4);

                    long test = mCursor.getLong(5);

                    // Create a new album
                    Album album = new Album(id, albumName, artist, songCount, year);
                    // Add everything up
                    result.add(album);
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        }
        return result;
    }

    /**
     * Create cursor
     *
     * @return sql cursor
     */
    private Cursor makeArtistAlbumCursor() {
        Uri media = Albums.getContentUri("external", mArtistID);
        String select = null;//ALBUM_COLUMN[5] + '=' + mArtistID;
        String order = PreferenceUtils.getInstance(getContext()).getArtistAlbumSortOrder();
        return getContext().getContentResolver().query(ALBUM_URI, ALBUM_COLUMN, select, null, order);
    }
}