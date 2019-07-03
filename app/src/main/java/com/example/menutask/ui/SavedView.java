package com.example.menutask.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.menutask.R;
import com.example.menutask.adapter.UniversalAdapter;
import com.example.menutask.handlers.DatabaseHandler;
import com.example.menutask.interfaces.OnButtonClickListener;
import com.example.menutask.provider.DataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SavedView extends Fragment {
    private RecyclerView recview;
    UniversalAdapter universalAdapter;
    @Nullable
    private Context context;
    @Nullable
    private DatabaseHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.saved_frame, container, false);
        context=getContext();

        db = new DatabaseHandler(context);
        recview =  rootView.findViewById(R.id.recview);
        populateSavedArticles(getSavedArticles());

        return rootView;
    }
    private HashMap<String, List<String>> getSavedArticles(){
        String[] table = {"saved_articles"};
        String[] column = {"title", "content", "section", "imageURL", "articleId"};
        return  db.getMultipleValues(false, table, column, null, null, null, null, null, null);

    }
    private void populateSavedArticles(HashMap<String, List<String>> savedArticleList) {
        final List<DataProvider> dataProviderList= new ArrayList<>();
        DataProvider dataProvider;
        String[] column={"title", "content", "section", "imageURL", "articleId"};
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        if (savedArticleList.get(column[0]) != null && !savedArticleList.get(column[0]).isEmpty()) {
            List<String> titleList = savedArticleList.get(column[0]);
            List<String> contentList = savedArticleList.get(column[1]);
            List<String> sectionList = savedArticleList.get(column[2]);
            List<String> imageURLList = savedArticleList.get(column[3]);
            List<String> articleIdList = savedArticleList.get(column[4]);
            for (int i = 0; i < titleList.size(); i++) {
                dataProvider = new DataProvider();
                dataProvider.setAttributes("articleTitle", titleList.get(i));
                dataProvider.setAttributes("sectionName", sectionList.get(i));
                dataProvider.setAttributes("textSummary", contentList.get(i));
                dataProvider.setAttributes("thumbnail", imageURLList.get(i));
                dataProvider.setAttributes("articleId", articleIdList.get(i));
                dataProviderList.add(dataProvider);
            }
            Collections.reverse(dataProviderList);
            universalAdapter = new UniversalAdapter(dataProviderList, context, "saved");
            recview.setLayoutManager(layoutManager);
            recview.setItemAnimator(new DefaultItemAnimator());
            recview.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
            recview.setAdapter(universalAdapter);

            universalAdapter.setOnButtonClickedListener(new OnButtonClickListener() {
                @Override
                public void onButtonClicked(int position) {
                    //remove from database
                    removeFromSaved("saved_articles", "articleId = ?", dataProviderList.get(position).getAttributes("articleId"));
                    universalAdapter.removeFromList(position);
                }
            });
        }
    }
    public void removeFromSaved(String tableName, String where, String articleId){
        db.deleteFromTable(tableName, where, new String[] {articleId});
    }
    @Override
    public void onPause (){
        super.onPause();
    }
    @Override
    public void onResume (){
        super.onResume();
        populateSavedArticles(getSavedArticles());
    }




}
