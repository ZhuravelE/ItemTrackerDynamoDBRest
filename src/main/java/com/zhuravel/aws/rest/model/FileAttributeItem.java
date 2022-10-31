package com.zhuravel.aws.rest.model;

/**
 * Evgenii Zhuravel created on 25.10.2022
 */
public class FileAttributeItem {

    private String id;
    private String date;
    private String filename;
    private String url;
    private Long size;

    public void setId (String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}
