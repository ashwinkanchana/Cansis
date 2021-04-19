package com.ashwinkanchana.cansis.data;

import java.io.Serializable;

public class Attendance implements Serializable {
    private int pos;
    private String id;
    private String name;
    private String value;
    private boolean isLoading;
    private boolean isRetry;

    public Attendance(String id,String name,int pos) {
        this.pos = pos;
        this.id = id;
        this.name = name;
        this.isLoading = false;
        this.isRetry = false;
        this.value = " ";
    }
    public Attendance(String id,String name,String value,int pos) {
        this.pos = pos;
        this.value = value;
        this.id = id;
        this.name = name;
        this.isLoading = false;
        this.isRetry = false;
    }

    public int getPos() {
        return pos;
    }

    public String getId(){return id;}

    public String getName() {
        return name;
    }

    public String getValue() {
        return value.replace(" ","");
    }


    public boolean getLoading() {
        return isLoading;
    }

    public boolean getRetry() {
        return isRetry;
    }

    public void setValue(String newValue) {
        this.value = newValue;
    }

    public void setLoading(boolean loading){this.isLoading = loading;}

    public void setRetry(boolean retry){this.isRetry = retry;}



}
