package com.example.menutask.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menutask.R;
import com.example.menutask.interfaces.OnBottomReachedListener;
import com.example.menutask.interfaces.OnButtonClickListener;
import com.example.menutask.interfaces.OnPinButtonClickListener;
import com.example.menutask.provider.DataProvider;
import com.example.menutask.ui.Article;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PinnedArticlesAdapter extends RecyclerView.Adapter {
    private List<DataProvider> dataProvList;
    private Context context;
    private OnButtonClickListener onButtonClickListener;
    private OnPinButtonClickListener onPinButtonClickListener;



    public class ViewHolder  extends RecyclerView.ViewHolder {
        ImageView article_image_view;
        TextView article_title_view, section_textview;
        ImageButton favorite_image_button, remove_pin_button_view;

        ViewHolder (@NonNull View v) {
            super(v);
            article_title_view =  v.findViewById(R.id.article_title_view);
            section_textview =  v.findViewById(R.id.section_textview);
            article_image_view =  v.findViewById(R.id.article_image_view);
            favorite_image_button=v.findViewById(R.id.favorite_image_button);
            remove_pin_button_view=v.findViewById(R.id.remove_pin_button_view);
            final String[] attributes = {"articleTitle", "textSummary", "sectionName", "thumbnail",  "articleId"};
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(@NonNull View v) {
                    ArrayList<String> titleIdList=new ArrayList<>();
                    for(String attr: attributes){
                        titleIdList.add(dataProvList.get(getAdapterPosition()).getAttributes(attr));
                    }
                    Intent intent = new Intent(v.getContext(), Article.class);
                    intent.putStringArrayListExtra("titleIdList", titleIdList);
                    v.getContext().startActivity(intent);
                }
            });
        }
    }
    public PinnedArticlesAdapter(List<DataProvider> dataProvList, Context context) {
        this.dataProvList = dataProvList;
        this.context=context;
    }
    // OnCreateViewHolder, where we are creating the ViewHolder as per the ViewType
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_list_horizontal_view, parent, false);
        return  new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final DataProvider dataProvider= dataProvList.get(position);

        String articleTitle= dataProvider.getAttributes("articleTitle");
        String sectionTitle= dataProvider.getAttributes("sectionName");
        String url = dataProvider.getAttributes("thumbnail");

        ((ViewHolder) holder).article_title_view.setText(articleTitle);
        ((ViewHolder) holder).section_textview.setText(sectionTitle);
        if(url!=null){
            if (url.contains(".jpg")) {
                Picasso.with(context)
                        .load(url)
                        .fit()
                        .centerCrop()
                        .into(((ViewHolder) holder).article_image_view);
            } else {
                Bitmap myBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.android_icon);
                ((ViewHolder) holder).article_image_view.setImageBitmap(myBitmap);
            }
        }
        else {
            Bitmap myBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.android_icon);
            ((ViewHolder) holder).article_image_view.setImageBitmap(myBitmap);
        }

        ((ViewHolder) holder).favorite_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onButtonClickListener != null) {
                    onButtonClickListener.onButtonClicked(position);
                }
            }
        });
        ((ViewHolder) holder).remove_pin_button_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onPinButtonClickListener != null) {
                    onPinButtonClickListener.onButtonClicked(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataProvList.size();
    }

    public void setOnButtonClickedListener(OnButtonClickListener onButtonClickedListener){
        this.onButtonClickListener = onButtonClickedListener;
    }
    public void setOnPinButtonClickListener(OnPinButtonClickListener onPinButtonClickListener){
        this.onPinButtonClickListener = onPinButtonClickListener;
    }

    // Method to remove items from the list
    public void remove(int position){
        dataProvList.remove(position);
        notifyDataSetChanged();
    }
}