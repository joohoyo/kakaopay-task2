package com.joohoyo.kakaopay.task.app.user;

import org.springframework.data.annotation.Id;

public class User {

    @Id
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
