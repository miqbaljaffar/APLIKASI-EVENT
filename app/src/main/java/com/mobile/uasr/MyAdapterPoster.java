package com.mobile.uasr;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MyAdapterPoster extends BaseAdapter {

    private ArrayList<DataClassPoster> dataList;
    private Context context;
    LayoutInflater layoutInflater;

    public MyAdapterPoster(Context context, ArrayList<DataClassPoster> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int i) {
        return dataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (layoutInflater == null) {
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (view == null) {
            view = layoutInflater.inflate(R.layout.grid_item, null);
        }

        ImageView gridImage = view.findViewById(R.id.gridImage);
        TextView gridTitle = view.findViewById(R.id.gridTitle);

        DataClassPoster dataClass = dataList.get(i);

        Glide.with(context).load(dataClass.getImageUrl()).into(gridImage);
        gridTitle.setText(dataClass.getTitle());

        view.setOnClickListener(v -> {
            Intent intent = new Intent(context, PosterViewActivity.class);
            intent.putExtra("imageUrl", dataClass.getImageUrl());
            intent.putExtra("imageDescription", dataClass.getDescription());
            intent.putExtra("title", dataClass.getTitle());  // Tambahkan judul poster
            context.startActivity(intent);
        });


        return view;
    }
}
