package com.example.menutask.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menutask.R;
import com.example.menutask.adapter.PinnedArticlesAdapter;
import com.example.menutask.handlers.DatabaseHandler;
import com.example.menutask.provider.DataProvider;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Article extends AppCompatActivity {
    ArrayList<String> titleIdList;
    Context context;
    ImageView article_image_view;
    private ImageButton pin_button_view, favorite_image_button;
    @Nullable
    private DatabaseHandler db;
    @NonNull
    private ContentValues insertValues = new ContentValues();

    TextView article_title_view, article_text_view, section_textview;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_view);
        Intent intent = getIntent();
        titleIdList=intent.getStringArrayListExtra("titleIdList");
        context=getApplicationContext();
        db = new DatabaseHandler(context);

        pin_button_view=findViewById(R.id.pin_button_view);
        favorite_image_button=findViewById(R.id.favorite_image_button);
        article_image_view=findViewById(R.id.article_image_view);
        article_title_view=findViewById(R.id.article_title_view);
        article_text_view=findViewById(R.id.article_text_view);
        section_textview=findViewById(R.id.section_textview);

        article_title_view.setText(titleIdList.get(0));
        article_text_view.setText(titleIdList.get(1));

        section_textview.setText(titleIdList.get(2));
        final String thumbnailURL=titleIdList.get(3);
        final String articleId=titleIdList.get(4);
        if(thumbnailURL!=null){
            if (thumbnailURL.contains(".jpg")) {
                Picasso.with(context)
                        .load(thumbnailURL)
                        .into(article_image_view);
            } else {
                Bitmap myBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.android_icon);
                article_image_view.setImageBitmap(myBitmap);
            }
        }
        else {
            Bitmap myBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.android_icon);
            article_image_view.setImageBitmap(myBitmap);
        }
        pin_button_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertValues.put("title",titleIdList.get(0) );
                insertValues.put("content",titleIdList.get(1) );
                insertValues.put("section",titleIdList.get(2) );
                insertValues.put("imageURL",thumbnailURL );
                insertValues.put("articleId",articleId );
                long id =db.insertIntoTableorThrow("pinned_articles", null, insertValues );
                if(id == 0){
                    Toast.makeText(context, "You have already pinned this article", Toast.LENGTH_LONG).show();
                }
            }
        });
        favorite_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertValues.put("title",titleIdList.get(0) );
                insertValues.put("content",titleIdList.get(1) );
                insertValues.put("section",titleIdList.get(2) );
                insertValues.put("imageURL",thumbnailURL );
                insertValues.put("articleId",articleId );
                long id =db.insertIntoTableorThrow("saved_articles", null, insertValues );
                if(id == 0){
                    Toast.makeText(context, "You have already saved this article", Toast.LENGTH_LONG).show();
                }
            }
        });

    }


}

