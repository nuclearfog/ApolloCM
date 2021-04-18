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

package com.andrew.apollo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.andrew.apollo.adapters.MusicHolder.DataHolder;
import com.andrew.apollo.model.Song;
import com.andrew.apollo.ui.fragments.QueueFragment;
import com.andrew.apollo.ui.fragments.SongFragment;
import com.andrew.apollo.utils.MusicUtils;

import java.util.ArrayList;

/**
 * This {@link ArrayAdapter} is used to display all of the songs on a user's
 * device for {@link SongFragment}. It is also used to show the queue in
 * {@link QueueFragment}.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class SongAdapter extends ArrayAdapter<Song> {

    /**
     * The resource Id of the layout to inflate
     */
    private int mLayoutId;

    /**
     * Used to cache the song info
     */
    private ArrayList<DataHolder> mData = new ArrayList<>();

    /**
     * Constructor of <code>SongAdapter</code>
     *
     * @param context  The {@link Context} to use.
     * @param layoutId The resource Id of the view to inflate.
     */
    public SongAdapter(Context context, int layoutId) {
        super(context, 0);
        // Get the layout Id
        mLayoutId = layoutId;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Recycle ViewHolder's items
        MusicHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mLayoutId, parent, false);
            holder = new MusicHolder(convertView);
            // Hide the third line of text
            holder.mLineThree.setVisibility(View.GONE);
            convertView.setTag(holder);
        } else {
            holder = (MusicHolder) convertView.getTag();
        }
        // Retrieve the data holder
        DataHolder dataHolder = mData.get(position);
        // Set each song name (line one)
        holder.mLineOne.setText(dataHolder.mLineOne);
        // Set the song duration (line one, right)
        holder.mLineOneRight.setText(dataHolder.mLineOneRight);
        // Set the album name (line two)
        holder.mLineTwo.setText(dataHolder.mLineTwo);
        return convertView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }


    @Override
    public void clear() {
        super.clear();
        mData.clear();
    }

    /**
     * Method used to cache the data used to populate the list or grid. The idea
     * is to cache everything before {@code #getView(int, View, ViewGroup)} is
     * called.
     */
    public void buildCache() {
        mData.ensureCapacity(getCount());
        for (int i = 0; i < getCount(); i++) {
            // Build the song
            Song song = getItem(i);
            if (song != null) {
                // Build the data holder
                DataHolder holder = new DataHolder();
                // Song Id
                holder.mItemId = song.mSongId;
                // Song names (line one)
                holder.mLineOne = song.mSongName;
                // Song duration (line one, right)
                holder.mLineOneRight = MusicUtils.makeTimeString(getContext(), song.mDuration);
                // Album names (line two)
                holder.mLineTwo = song.mAlbumName;
                mData.add(holder);
            }
        }
    }
}