package com.devreport.domain;

public class Insight implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private final String content;

    public Insight(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
