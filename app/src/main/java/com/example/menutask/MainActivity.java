package com.example.menutask;

import android.content.ContentValues;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.work.Constraints;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.menutask.adapter.PagerAdapter;
import com.example.menutask.adapter.UniversalAdapter;
import com.example.menutask.handlers.DatabaseHandler;
import com.example.menutask.provider.DataProvider;

import com.example.menutask.utility.NotificationWorker;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements LifecycleObserver {

    private String tab1, tab2;

    private Context context;


    private DatabaseHandler db;
    @NonNull



    public Bundle b;
    final int notificationId=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=getApplicationContext();
        db = new DatabaseHandler(this);
        db.openDataBase();
        tab1 = getResources().getString(R.string.home);
        tab2 = getResources().getString(R.string.saved);

        final TabLayout tabLayout =  findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_home_black_24dp));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.favorite));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
       // b=new Bundle();
       // b.putSerializable("pinnedArticles", getPinnedArticles());
        final ViewPager viewPager = findViewById(R.id.pager);


        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {  }
        });
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), null);
        viewPager.setAdapter(adapter);
        viewPager.getAdapter().getItemPosition(0);
        viewPager.getAdapter().notifyDataSetChanged();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }
    /** checking if we are in background to check for new articles and notify*/
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
       /* //System.out.println("we are on background");
        Constraints constraints = new Constraints.Builder()
                *//** do checking only if there is a network connection, no point to do it otherwise *//*
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest saveRequest =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 20, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance()
                .enqueue(saveRequest);*/
    }


}
