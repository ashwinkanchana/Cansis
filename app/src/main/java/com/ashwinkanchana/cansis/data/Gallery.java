package com.ashwinkanchana.cansis.data;

import java.io.Serializable;

public class Gallery implements Serializable {

    String sub_name;
    String urlk;

    public Gallery() { }



    public Gallery(String sub_name, String urlk) {
        this.sub_name = sub_name;
        this.urlk = urlk;
    }

    public String getSub_name() { return sub_name; }
    public String getUrlk() {
        return urlk;
    }


    public void setSub_name(String sub_name) {
        this.sub_name = sub_name;
    }

    public void setUrlk(String urlk) {
        this.urlk = urlk;
    }
}
