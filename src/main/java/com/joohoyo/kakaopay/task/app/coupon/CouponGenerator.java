package com.joohoyo.kakaopay.task.app.coupon;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

@Component
public class CouponGenerator {
    public static final long EXPIRATION = 1000 * 60 * 60 * 24 * 7;
    private final int couponStringLength = 32;
    Clock clock;
    private char[] couponStrings = new char[]{'c', 'x', 'b', '3', 'k', 'h', 'q', 'f', 'i', 't', 'r', 'w', 'u', 'j', 'e', 'p', '5', 'z', 'v', 'g', '2', '9', '7', '8', 'a', '6', '4', 's', 'y', 'd', 'm', 'n'};

    public CouponGenerator(Clock clock) {
        this.clock = clock;
    }

    Coupon make(long currentMillis) {
        return make(currentMillis, 1);
    }

    Coupon make(long currentMillis, int count) {
        Coupon coupon = new Coupon();
        coupon.setId(getCouponId(currentMillis, count));
        coupon.setExpiration(currentMillis + EXPIRATION);
        coupon.setUsed(false);
        return coupon;
    }

    private String getCouponId(long currentMillis, int count) {
        // 초 단위 랜덤 번호 + 카운트
        // 5자리 : max 33554432
        return Long.toHexString(currentMillis / 1000) + "-" + getCouponPart(count);
    }

    private String getCouponPart(int count) {
        StringBuffer couponString = new StringBuffer();

        count += 10000000; // 5자리를 맞추기 위한 padding
        while (count > 0) {
            couponString.append(couponStrings[count % couponStringLength]);
            count /= couponStringLength;
        }

        return couponString.toString();
    }

    List<Coupon> make(int count) {
        List<Coupon> coupons = new ArrayList<>();

        long currentMillis = clock.millis();
        for (int i = 0; i < count; i++) {
            coupons.add(make(currentMillis, i));
        }

        return coupons;
    }

}
