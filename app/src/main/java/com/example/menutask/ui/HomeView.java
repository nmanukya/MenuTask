package com.example.menutask.ui;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;


import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.menutask.R;
import com.example.menutask.adapter.PinnedArticlesAdapter;
import com.example.menutask.adapter.UniversalAdapter;
import com.example.menutask.handlers.DatabaseHandler;
import com.example.menutask.interfaces.AsyncResponse;
import com.example.menutask.interfaces.OnBottomReachedListener;
import com.example.menutask.interfaces.OnButtonClickListener;
import com.example.menutask.interfaces.OnPinButtonClickListener;
import com.example.menutask.provider.DataProvider;
import com.example.menutask.utility.GetJson;
import com.example.menutask.utility.NotificationWorker;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class HomeView extends Fragment  implements LifecycleObserver {

    private RecyclerView recview, horizontal_recview;
    @Nullable
    private UniversalAdapter universalAdapter;
    private PinnedArticlesAdapter pinnedArticlesAdapter;
    private List<DataProvider> dataProviderList= new ArrayList<>();
    @Nullable
    private Context context;
    @Nullable
    private DatabaseHandler db;

    private int pageNumber=1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_frame, container, false);
        context=getContext();

        db = new DatabaseHandler(context);
        horizontal_recview=  rootView.findViewById(R.id.horizontal_recview);
        /** getting pinned articles from sqlite db*/
        populatePinnedArticles(getPinnedArticles());
        recview =  rootView.findViewById(R.id.recview);

        final String url="https://content.guardianapis.com/search?page-size=10&show-fields=thumbnail&show-blocks=body&type=article&api-key="+getString(R.string.api_key)+"&page=";
        final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        /** checking if we have network connection to load articles*/
        NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            getArticles(url);
            checkForNewArticles();
        }
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        return rootView;
    }

    private void getArticles(final String url){
        /* page number 1 contains latest articles, starting from there*/
        String myurl=url+pageNumber;
        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        new GetJson(myurl, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                try {
                    JSONObject obj = new JSONObject(output);
                    String status=obj.getJSONObject("response").getString("status");

                    if(status.equals("ok")){
                        dataProviderList= populateProvider(obj.getJSONObject("response"));
                        universalAdapter = new UniversalAdapter(dataProviderList, context, "home");
                        recview.setLayoutManager(mLayoutManager);
                        recview.setItemAnimator(new DefaultItemAnimator());
                        recview.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
                        recview.setAdapter(universalAdapter);
                        ViewCompat.setNestedScrollingEnabled(recview, false);
                        /** if we scrolled to the bottom of the current view load more articles from API*/
                        universalAdapter.setOnBottomReachedListener(new OnBottomReachedListener() {
                            @Override
                            public void onBottomReached(int position) {
                                pageNumber++;
                               addArticlesToList(url, pageNumber);
                            }
                        });
                        /** save article button is clicked , saving it for offline use*/
                        universalAdapter.setOnButtonClickedListener(new OnButtonClickListener() {
                            @Override
                            public void onButtonClicked(int position) {
                                insertToSaved(position, dataProviderList);
                            }
                        });
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }).execute();
    }
    /** save articles for offline use*/
    private void insertToSaved(int position, List<DataProvider> dataProviderList){
        ContentValues insertValues = new ContentValues();
        insertValues.put("title",dataProviderList.get(position).getAttributes("articleTitle") );
        insertValues.put("content",dataProviderList.get(position).getAttributes("textSummary") );
        insertValues.put("section",dataProviderList.get(position).getAttributes("sectionName") );
        insertValues.put("articleId",dataProviderList.get(position).getAttributes("articleId") );
        insertValues.put("imageURL",dataProviderList.get(position).getAttributes("thumbnail") );
        long id =db.insertIntoTableorThrow("saved_articles", null, insertValues );
        /** if the data is already exists in saved table, remove it from the db */
        if(id == 0){
            Toast.makeText(getActivity(), "You have already saved this article", Toast.LENGTH_LONG).show();
            removeFromFavorites("saved_articles", "articleId=?", dataProviderList.get(position).getAttributes("articleId"));
        }
    }
    /** adding articles to the current list */
    private void addArticlesToList(String url, final int pageNumber){
        String urlWithPage="";
        if(pageNumber > 0){
            urlWithPage=url+pageNumber;
        }
        else{
            urlWithPage=url ;
        }

        new GetJson(urlWithPage, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                List<DataProvider> dataProviderList = new ArrayList<>();
                try {
                    JSONObject obj = new JSONObject(output);
                    String status=obj.getJSONObject("response").getString("status");
                    if(status.equals("ok")) {
                        dataProviderList = populateProvider(obj.getJSONObject("response"));
                    }
                    else{
                        dataProviderList.clear();
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
                if(pageNumber == 0){
                    /** if we have new articles add them at the top of the list*/
                    universalAdapter.addToList(dataProviderList);
                }
                /** adding articles to the current list upon scrolling down*/
                universalAdapter.update(dataProviderList);

            }
        }).execute();
    }
    public HashMap<String, List<String>> getPinnedArticles(){
        String[] table = {"pinned_articles"};
        String[] column = {"title", "content", "section", "imageURL", "articleId"};
        return  db.getMultipleValues(false, table, column, null, null, null, null, null, null);

    }
    private void populatePinnedArticles(final HashMap<String, List<String>> pinnedArticleList) {
        final List<DataProvider> dataProviderList = new ArrayList<>();
        DataProvider dataProvider;
        String[] column = {"title", "content", "section", "imageURL", "articleId"};
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false );

        if (pinnedArticleList.get(column[0]) != null && !pinnedArticleList.get(column[0]).isEmpty()) {
            List<String> titleList = pinnedArticleList.get(column[0]);
            List<String> contentList = pinnedArticleList.get(column[1]);
            List<String> sectionList = pinnedArticleList.get(column[2]);
            List<String> urlList = pinnedArticleList.get(column[3]);
            List<String> idList = pinnedArticleList.get(column[4]);
            for (int i = 0; i < titleList.size(); i++) {
                dataProvider = new DataProvider();
                dataProvider.setAttributes("articleTitle",titleList.get(i));
                dataProvider.setAttributes("sectionName",sectionList.get(i));
                dataProvider.setAttributes("textSummary",contentList.get(i));
                dataProvider.setAttributes("thumbnail",urlList.get(i));
                dataProvider.setAttributes("articleId",idList.get(i));
                dataProviderList.add(dataProvider);
            }
            Collections.reverse(dataProviderList);
            pinnedArticlesAdapter = new PinnedArticlesAdapter(dataProviderList, context);
            horizontal_recview.setLayoutManager(layoutManager);

            horizontal_recview.setAdapter(pinnedArticlesAdapter);
            ViewCompat.setNestedScrollingEnabled(horizontal_recview, false);
            /** if remove pin button is clicked the article is removed from the list and db*/
            pinnedArticlesAdapter.setOnPinButtonClickListener(new OnPinButtonClickListener() {
                @Override
                public void onButtonClicked(int position) {
                    removeFromPinned("pinned_articles", "articleId=?", dataProviderList.get(position).getAttributes("articleId"));
                    pinnedArticlesAdapter.remove(position);
                }
            });
            pinnedArticlesAdapter.setOnButtonClickedListener(new OnButtonClickListener() {
                @Override
                public void onButtonClicked(int position) {
                    insertToSaved(position, dataProviderList);
                }
            });
        }
    }
    private List<DataProvider>  populateProvider(@NonNull JSONObject obj) {
        List<DataProvider> dataProviderList1 = new ArrayList<>();;
        final String [] keys={"webTitle", "sectionName", "fields", "blocks"};
        try {
            JSONArray jsonarray= obj.getJSONArray("results");
            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject jsonobject = jsonarray.getJSONObject(i);
                DataProvider dataProvider = new DataProvider();
                /** parsing json result by the fields which are interesting for us*/
                for (String key: keys){
                    String value = jsonobject.getString(key);
                        switch (key){
                            case "blocks":
                                JSONObject blockobj = new JSONObject(value);
                                JSONArray blockarray= blockobj.getJSONArray("body");
                                dataProvider.setAttributes("textSummary", blockarray.getJSONObject(0).getString("bodyTextSummary"));
                                dataProvider.setAttributes("articleId", blockarray.getJSONObject(0).getString("id"));
                                break;
                            case "fields":
                                JSONObject fieldobj = new JSONObject(value);
                                dataProvider.setAttributes("thumbnail",fieldobj.getString("thumbnail"));
                                break;
                            case "webTitle":
                                dataProvider.setAttributes("articleTitle", value);
                                break;
                            case "sectionName":
                                dataProvider.setAttributes("sectionName", value);
                                break;
                        }
                    }
                    dataProviderList1.add(dataProvider);
                }
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return dataProviderList1;
    }
    private void checkForNewArticles(){
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        final Handler handler = new Handler();
        Timer timer = new Timer();
        /** scheduling the checking of the new articles by the TimerTask on the background thread, each 30 seconds
         * thread starts after 2 seconds of delay*/
        TimerTask checkTask = new TimerTask() {
            @Override
            public void run() {
                handler.postDelayed(new Runnable() {
                    public void run() {
                        try {
                            Calendar calendar = Calendar.getInstance();
                            /** Checking for the new articles published in last minute*/
                            calendar.add(Calendar.MINUTE,-1);
                            String fromDate=sdf.format(calendar.getTime());
                            String checkingURL="https://content.guardianapis.com/search?show-fields=thumbnail&show-blocks=body&type=article&api-key=521507b1-6b35-4656-8c03-de57542895f0&from-date="+fromDate;
                            addArticlesToList(checkingURL, 0);

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                }, 2000);
            }
        };
        timer.schedule(checkTask , 0, 30000); //execute in every 30000 ms*/
    }

    public void removeFromFavorites(String tableName, String where, String articleId){
        db.deleteFromTable(tableName, where, new String[] {articleId});
    }
    public void removeFromPinned(String tableName, String where, String articleId){
        db.deleteFromTable(tableName, where, new String[] {articleId});
    }
    @Override
    public void  onResume(){
        super.onResume();
        /** to make sure that we will see our last pinned article in the list*/
        populatePinnedArticles(getPinnedArticles());
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        //System.out.println("we are on background");
        Constraints constraints = new Constraints.Builder()
                /** do checking only if there is a network connection, no point to do it otherwise */
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest saveRequest =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 20, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance()
                .enqueue(saveRequest);
    }
}
