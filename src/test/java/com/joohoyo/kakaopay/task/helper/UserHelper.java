package com.joohoyo.kakaopay.task.helper;

import com.joohoyo.kakaopay.task.app.user.User;

public class UserHelper {
    public static final String VALID_ID = "1";
    public static final String INVALID_ID = "9";

    public static User getTestUser() {
        User user = new User();
        user.setId(VALID_ID);
        return user;
    }
}
