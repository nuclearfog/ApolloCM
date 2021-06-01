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

package com.andrew.apollo.model;


/**
 * A class that represents a playlist.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class Playlist extends Music {

    /**
     * Constructor of <code>Genre</code>
     *
     * @param playlistId   The Id of the playlist
     * @param playlistName The playlist name
     */
    public Playlist(long playlistId, String playlistName) {
        super(playlistId, playlistName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + (int) id;
        result = prime * result + name.hashCode();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Playlist) {
            Playlist playlist = (Playlist) obj;
            return id == playlist.id && name.equals(playlist.name);
        }
        return false;
    }
}