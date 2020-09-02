package com.example.ai.dlibdemo;

import android.app.Application;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumConfig;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.AlbumLoader;

import java.util.Locale;


public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        initAlbum();
    }


    /**
     * @description 初始化Album框架
     **/
    private void initAlbum() {
        Album.initialize(AlbumConfig.newBuilder(this)
                .setAlbumLoader(new AlbumLoader() {
                    @Override
                    public void load(ImageView imageView, AlbumFile albumFile) {
                        load(imageView, albumFile.getPath());
                    }

                    @Override
                    public void load(ImageView imageView, String url) {
                        Glide.with(imageView.getContext())
                                .load(url)
                                .into(imageView);

                    }
                }).setLocale(Locale.getDefault())
                .build());


    }
}
