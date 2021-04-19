package com.ashwinkanchana.cansis.data;

public class MarksSubject {

   private String s_m_no;
   private String s_m_sub;


    public MarksSubject() {
    }
    public MarksSubject(String no,String sub) {
        this.s_m_no = no;
        this.s_m_sub = sub;
    }
    public String getSubjectName() {
        return s_m_sub;
    }
    public String getSubjectNo() {
        return s_m_no;
    }





}
