package com.poonehmedia.app.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class GalleryItem implements Parcelable {
    public static final Creator<GalleryItem> CREATOR = new Creator<GalleryItem>() {
        @Override
        public GalleryItem createFromParcel(Parcel in) {
            return new GalleryItem(in);
        }

        @Override
        public GalleryItem[] newArray(int size) {
            return new GalleryItem[size];
        }
    };
    private String imageUrlOrVideoThumb;
    private String videoLink;

    public GalleryItem() {
    }

    protected GalleryItem(Parcel in) {
        imageUrlOrVideoThumb = in.readString();
        videoLink = in.readString();
    }

    public String getImageUrlOrVideoThumb() {
        return imageUrlOrVideoThumb;
    }

    public void setImageUrlOrVideoThumb(String imageUrlOrVideoThumb) {
        this.imageUrlOrVideoThumb = imageUrlOrVideoThumb;
    }

    public String getVideoLink() {
        return videoLink;
    }

    public void setVideoLink(String videoLink) {
        this.videoLink = videoLink;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageUrlOrVideoThumb);
        dest.writeString(videoLink);
    }
}
