package me.thesquare.models;

/**
 * Created by larsh on 20-6-2017.
 */

public class StreamModel {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String id;
    private String title;
}
