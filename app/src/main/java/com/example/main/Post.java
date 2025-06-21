package com.example.main;

public class Post {
    private String title;

    public Post() {} // Firebase에서 사용하려면 기본 생성자 필요

    public Post(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}

