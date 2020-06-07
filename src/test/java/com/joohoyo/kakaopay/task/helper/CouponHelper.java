package com.joohoyo.kakaopay.task.helper;

import com.joohoyo.kakaopay.task.app.coupon.Coupon;

public class CouponHelper {
    public static final String VALID_ID = "valid_couponId";
    public static final String INVALID_ID = "invalid_couponId";

    public static Coupon getTestCoupon() {
        Coupon coupon = new Coupon();
        coupon.setId(VALID_ID);
        coupon.setUsed(false);
        coupon.setExpiration(0);
        return coupon;
    }

    public static Coupon getGivenTestCoupon() {
        Coupon coupon = getTestCoupon();
        coupon.setUserId(UserHelper.VALID_ID);
        return coupon;
    }

    public static Coupon getUsedTestCoupon() {
        Coupon coupon = getGivenTestCoupon();
        coupon.setUsed(true);
        return coupon;
    }
}
