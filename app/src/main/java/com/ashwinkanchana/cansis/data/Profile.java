package com.ashwinkanchana.cansis.data;


import org.apache.commons.text.WordUtils;

public class Profile {

    String s_image;
    String s_name;
    String s_contactno;
    String s_lass;
    String s_father;
    String s_address;

    public Profile() {
    }
    public String getImage() {
        return s_image;
    }


    public String getName() {

        return WordUtils.capitalizeFully(s_name).trim();
    }


    public String getContact() {
        return s_contactno;
    }


    public String getBranch() {
        s_lass = WordUtils.capitalizeFully(s_lass);
        return s_lass;
    }


    public String getFather() {
        s_father = WordUtils.capitalizeFully(s_father);
        return s_father;
    }


    public String getAddress() {
        s_address = WordUtils.capitalizeFully(s_address);
        s_address = WordUtils.capitalize(s_address,',');
        s_address = WordUtils.capitalize(s_address,'\n');
        return s_address;
    }




}
