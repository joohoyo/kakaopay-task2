package com.joohoyo.kakaopay.task.app.coupon;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CouponGeneratorTest {

    @Nested
    @DisplayName("쿠폰 생성")
    class Make {

        CouponGenerator couponGenerator;

        @BeforeEach
        void init() {
            couponGenerator = new CouponGenerator(Clock.fixed(Instant.now(), ZoneId.systemDefault()));
        }

        @Test
        @DisplayName("특정 시각을 주면 7일뒤 만료 되는 쿠폰을 생성할 수 있다.")
        void makeCoupon() throws Exception {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            long start = sdf.parse("20200607030000").getTime();
            long end = sdf.parse("20200614030000").getTime();

            Coupon coupon = couponGenerator.make(start);

            Assertions.assertNotEquals(null, coupon.getId());
            Assertions.assertEquals(end, coupon.getExpiration());
            Assertions.assertEquals(false, coupon.isUsed());
            Assertions.assertEquals(null, coupon.getUserId());
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 1000000})
        @DisplayName("N개의 서로 다른 쿠폰을 생성할 수 있다.")
        void makeCoupons(int count) {
            List<Coupon> coupons = couponGenerator.make(count);

            Assertions.assertEquals(count, coupons.size());

            Set<String> set = coupons.stream().map(c -> c.getId()).collect(Collectors.toSet());
            Assertions.assertEquals(count, set.size());
        }

    }

}
