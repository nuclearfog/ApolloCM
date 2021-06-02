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

package com.andrew.apollo.ui.fragments.profile;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;

import com.andrew.apollo.R;
import com.andrew.apollo.adapters.ProfileSongAdapter;
import com.andrew.apollo.loaders.LastAddedLoader;
import com.andrew.apollo.menu.CreateNewPlaylist;
import com.andrew.apollo.menu.FragmentMenuItems;
import com.andrew.apollo.model.Song;
import com.andrew.apollo.provider.FavoritesStore;
import com.andrew.apollo.utils.MusicUtils;
import com.andrew.apollo.utils.NavUtils;

import java.util.List;

import static com.andrew.apollo.adapters.ProfileSongAdapter.DISPLAY_DEFAULT_SETTING;

/**
 * This class is used to display all of the songs the user put on their device
 * within the last four weeks.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class LastAddedFragment extends ProfileFragment implements LoaderCallbacks<List<Song>> {

    /**
     * Used to keep context menu items from bleeding into other fragments
     */
    private static final int GROUP_ID = 0x461834C5;

    /**
     * LoaderCallbacks identifier
     */
    private static final int LOADER_ID = 0x4D492A47;

    /**
     * The adapter for the list
     */
    private ProfileSongAdapter mAdapter;

    /**
     * Selected track
     */
    @Nullable
    private Song mSong;


    @Override
    protected void init() {
        // Enable the options menu
        setHasOptionsMenu(true);
        // Start the loader
        LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
    }


    @Override
    protected ListAdapter getAdapter() {
        // Create the adapter
        mAdapter = new ProfileSongAdapter(requireContext(), DISPLAY_DEFAULT_SETTING, false);
        return mAdapter;
    }


    @Override
    protected void onItemClick(View view, int position, long id) {
        MusicUtils.playAllFromUserItemClick(mAdapter, position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (menuInfo instanceof AdapterContextMenuInfo) {
            // Get the position of the selected item
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
            // Creat a new song
            mSong = mAdapter.getItem(info.position);
            // Play the song
            menu.add(GROUP_ID, FragmentMenuItems.PLAY_SELECTION, Menu.NONE, R.string.context_menu_play_selection);
            // Play next
            menu.add(GROUP_ID, FragmentMenuItems.PLAY_NEXT, Menu.NONE, R.string.context_menu_play_next);
            // Add the song to the queue
            menu.add(GROUP_ID, FragmentMenuItems.ADD_TO_QUEUE, Menu.NONE, R.string.add_to_queue);
            // Add the song to a playlist
            SubMenu subMenu = menu.addSubMenu(GROUP_ID, FragmentMenuItems.ADD_TO_PLAYLIST, Menu.NONE, R.string.add_to_playlist);
            MusicUtils.makePlaylistMenu(requireContext(), GROUP_ID, subMenu, false);
            // View more content by the song artist
            menu.add(GROUP_ID, FragmentMenuItems.MORE_BY_ARTIST, Menu.NONE, R.string.context_menu_more_by_artist);
            // Make the song a ringtone
            menu.add(GROUP_ID, FragmentMenuItems.USE_AS_RINGTONE, Menu.NONE, R.string.context_menu_use_as_ringtone);
            // Delete the song
            menu.add(GROUP_ID, FragmentMenuItems.DELETE, Menu.NONE, R.string.context_menu_delete);
        } else {
            // remove selection if an error occurs
            mSong = null;
        }
    }


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getGroupId() == GROUP_ID && mSong != null) {
            long[] trackId = {mSong.getId()};

            switch (item.getItemId()) {
                case FragmentMenuItems.PLAY_SELECTION:
                    MusicUtils.playAll(trackId, 0, false);
                    return true;

                case FragmentMenuItems.PLAY_NEXT:
                    MusicUtils.playNext(trackId);
                    return true;

                case FragmentMenuItems.ADD_TO_QUEUE:
                    MusicUtils.addToQueue(requireActivity(), trackId);
                    return true;

                case FragmentMenuItems.ADD_TO_FAVORITES:
                    FavoritesStore.getInstance(requireContext()).addSongId(mSong);
                    return true;

                case FragmentMenuItems.NEW_PLAYLIST:
                    CreateNewPlaylist.getInstance(trackId).show(getParentFragmentManager(), "CreatePlaylist");
                    return true;

                case FragmentMenuItems.PLAYLIST_SELECTED:
                    long mPlaylistId = item.getIntent().getLongExtra("playlist", 0);
                    MusicUtils.addToPlaylist(requireActivity(), trackId, mPlaylistId);
                    return true;

                case FragmentMenuItems.MORE_BY_ARTIST:
                    NavUtils.openArtistProfile(requireActivity(), mSong.getArtist());
                    return true;

                case FragmentMenuItems.USE_AS_RINGTONE:
                    MusicUtils.setRingtone(requireActivity(), mSong.getId());
                    return true;

                case FragmentMenuItems.DELETE:
                    MusicUtils.openDeleteDialog(requireActivity(), mSong.getName(), trackId);
                    LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, this);
                    return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        return new LastAddedLoader(requireContext());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(@NonNull Loader<List<Song>> loader, @NonNull List<Song> data) {
        // disable loader
        LoaderManager.getInstance(this).destroyLoader(LOADER_ID);
        // Start fresh
        mAdapter.clear();
        // Add the data to the adpater
        for (Song song : data) {
            mAdapter.add(song);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoaderReset(@NonNull Loader<List<Song>> loader) {
        // Clear the data in the adapter
        mAdapter.clear();
    }


    @Override
    public void refresh() {
        LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, this);
    }
}