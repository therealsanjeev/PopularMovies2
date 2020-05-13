package com.therealsanjeev.popularmoviestage2.Adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.therealsanjeev.popularmoviestage2.R;
import com.therealsanjeev.popularmoviestage2.databinding.ItemVideoBinding;
import com.therealsanjeev.popularmoviestage2.models.Video;

import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoAdapterViewHolder> {
    private final Context mContext;
    private List<Video> mList;

    public VideoAdapter(Context context) {
        this.mContext = context;
    }
    @NonNull
    @Override
    public VideoAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        ItemVideoBinding binding = ItemVideoBinding.inflate(layoutInflater, parent, false);
        return new VideoAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoAdapterViewHolder holder, int position) {
        Video video = mList.get(position);
        holder.bind(video);
    }

    @Override
    public int getItemCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    public void addVideosList(List<Video> videosList) {
        mList = videosList;
        notifyDataSetChanged();
    }

    public ArrayList<Video> getList() {
        return (ArrayList<Video>) mList;
    }

    public class VideoAdapterViewHolder extends RecyclerView.ViewHolder {

        ItemVideoBinding binding;

        VideoAdapterViewHolder(ItemVideoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Video video) {
            binding.setVideo(video);
            binding.setPresenter(this);

            String photoUrl = String.format("https://img.youtube.com/vi/%s/0.jpg", video.videoUrl);
            Glide.with(binding.videoIv.getContext())
                    .load(photoUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.drawable.error)
                    .into(binding.videoIv);

        }

        public void onClickVideo(String videoUrl) {
            Intent appIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("vnd.youtube:" + videoUrl));

            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=" + videoUrl));
            try {
                mContext.startActivity(appIntent);
            } catch (ActivityNotFoundException ex) {
                mContext.startActivity(webIntent);
            }
        }

    }
}
