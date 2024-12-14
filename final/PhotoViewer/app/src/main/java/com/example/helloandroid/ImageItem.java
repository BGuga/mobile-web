package com.example.helloandroid;

import android.graphics.Bitmap;

public class ImageItem {
    private Bitmap image;
    private String title;

    public ImageItem(Bitmap image, String title) {
        this.image = image;
        this.title = title;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }
}
