package com.devreport.controller.dto;

public class InsightDTO {

    private String content;

    public InsightDTO() {
    }

    public InsightDTO(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
