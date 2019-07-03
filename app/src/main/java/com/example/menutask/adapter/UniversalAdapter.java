package com.example.menutask.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menutask.R;
import com.example.menutask.interfaces.OnBottomReachedListener;
import com.example.menutask.interfaces.OnButtonClickListener;

import com.example.menutask.provider.DataProvider;
import com.example.menutask.ui.Article;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UniversalAdapter extends RecyclerView.Adapter {
    private List<DataProvider> dataProvList;
    private Context context;
    private OnButtonClickListener onButtonClickListener;
    private OnBottomReachedListener onBottomReachedListener;
    private String activityName;

    public class ViewHolder  extends RecyclerView.ViewHolder {
        ImageView article_image_view;
        TextView article_title_view, section_textview;
        ImageButton favorite_image_button;

        ViewHolder (@NonNull View v) {
            super(v);
            article_title_view =  v.findViewById(R.id.article_title_view);
            section_textview =  v.findViewById(R.id.section_textview);
            article_image_view =  v.findViewById(R.id.article_image_view);
            favorite_image_button=v.findViewById(R.id.favorite_image_button);
            final String[] attributes = {"articleTitle", "textSummary", "sectionName", "thumbnail",  "articleId"};
            /** redirecting to Article view, which shows article content*/
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
    public UniversalAdapter(List<DataProvider> dataProvList, Context context, String activityName) {
        this.dataProvList = dataProvList;
        this.context=context;
        this.activityName=activityName;
    }
    // OnCreateViewHolder, where we are creating the ViewHolder as per the ViewType
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_list_view, parent, false);
        return  new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
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

        if (position == dataProvList.size() - 1) {
            if (onBottomReachedListener != null) {
                onBottomReachedListener.onBottomReached(position);
            }
        }
        /** if the activity is the SavedView presenting favorites/saving button as already saved*/
        if(activityName.equals("saved")){
            ((ViewHolder) holder).favorite_image_button.setBackgroundResource(R.drawable.favorite);
        }
        ((ViewHolder) holder).favorite_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onButtonClickListener != null) {
                    onButtonClickListener.onButtonClicked(position);
                }
                /* changing favorites icon on click */
                Drawable.ConstantState savededIcon = context.getDrawable(R.drawable.favorite).getConstantState();
                Drawable.ConstantState unsavedIcon = context.getDrawable(R.drawable.favorites_white).getConstantState();
                Drawable.ConstantState curDraw =  ((ViewHolder) holder).favorite_image_button.getBackground().getConstantState();
                if (curDraw.equals(savededIcon)) {
                    ((ViewHolder) holder).favorite_image_button.setBackgroundResource(R.drawable.favorites_white);

                }
                else if (curDraw.equals(unsavedIcon)){
                    ((ViewHolder) holder).favorite_image_button.setBackgroundResource(R.drawable.favorite);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataProvList.size();
    }

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener){
        this.onBottomReachedListener = onBottomReachedListener;
    }
    public void setOnButtonClickedListener(OnButtonClickListener onButtonClickedListener){
        this.onButtonClickListener = onButtonClickedListener;
    }

    // Adding more items to the list
    public void update(@NonNull List<DataProvider> newItems){
        for (DataProvider dataProvider: newItems){
            if(!dataProvList.contains(dataProvider)){
                dataProvList.add(dataProvider);
            }
        }
        notifyDataSetChanged();
    }
    // Adding latest articles on the top of the list
    public void addToList(@NonNull List<DataProvider> newItems){
        for (int i=0; i< newItems.size(); i++){
            String oldArticleId=dataProvList.get(i).getAttributes("articleId");
            String newArticleId=newItems.get(i).getAttributes("articleId");
            if(!oldArticleId.equals(newArticleId)){
                dataProvList.add(i, newItems.get(i));
                notifyDataSetChanged();
            }
        }
    }
    public void removeFromList(int position){
        dataProvList.remove(position);
        notifyDataSetChanged();
    }

}