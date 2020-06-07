package com.joohoyo.kakaopay.task.app.coupon;

import com.joohoyo.kakaopay.task.app.user.User;
import com.joohoyo.kakaopay.task.app.user.UserRepository;
import com.joohoyo.kakaopay.task.helper.CouponHelper;
import com.joohoyo.kakaopay.task.helper.UserHelper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CouponServiceTest {

    private final static long DAY_MILLIS = 1000 * 60 * 60 * 24;
    @Autowired
    CouponService couponService;
    @Autowired
    CouponRepository couponRepository;
    @Autowired
    UserRepository userRepository;

    @BeforeEach
    public void init() {
        userRepository.save(UserHelper.getTestUser());
    }

    @AfterEach
    public void tearDown() {
        couponRepository.deleteAll();
        userRepository.deleteAll();
    }

    List<Coupon> get2weeksCoupons(long now) {
        Clock fixedClock = Clock.systemDefaultZone();
        CouponGenerator couponGenerator = new CouponGenerator(fixedClock);

        long millis = now - (DAY_MILLIS * 14);
        List<Coupon> coupons = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            coupons.add(couponGenerator.make(millis));
            millis += DAY_MILLIS;
        }

        return coupons;
    }

    @Nested
    @DisplayName("랜덤한 코드의 쿠폰을 N개 생성하여 데이터베이스에 보관한다.")
    class Make {
        @ParameterizedTest
        @ValueSource(ints = {1, 2, 100})
        @DisplayName("쿠폰 N개 정상 저장 확인")
        public void makeCoupon(int count) {
            couponService.makeCoupon(count);

            assertEquals(count, couponRepository.count());
        }
    }

    @Nested
    @DisplayName("생성된 쿠폰 중 하나를 사용자에게 지급한다.")
    class Give {
        @Test
        @DisplayName("정상 지급")
        public void giveCoupon() throws Exception {
            couponService.makeCoupon(1);
            Coupon coupon = couponRepository.findAll().get(0);

            String givenId = couponService.give(UserHelper.VALID_ID);

            Coupon givenCoupon = couponRepository.findById(givenId).get();
            assertEquals(UserHelper.VALID_ID, givenCoupon.getUserId());
        }
    }

    @Nested
    @DisplayName("사용자에게 지급된 쿠폰을 조회한다.")
    class Search {
        @Test
        @DisplayName("지급받은 쿠폰이 없는 경우.")
        public void searchCouponWithoutGive() {
            couponService.makeCoupon(2);
            User user = UserHelper.getTestUser();

            List<Coupon> givenCoupons = couponService.search(user);

            assertEquals(0, givenCoupons.size());
        }

        @Test
        @DisplayName("지급받은 쿠폰이 있는 경우.")
        public void searchGivenCoupon() throws Exception {
            couponService.makeCoupon(2);
            List<Coupon> coupons = couponRepository.findAll();
            String givenCouponId = couponService.give(UserHelper.VALID_ID);
            User user = UserHelper.getTestUser();

            List<Coupon> searchedCoupons = couponService.search(user);

            assertEquals(1, searchedCoupons.size());
            assertEquals(givenCouponId, searchedCoupons.get(0).getId());
        }
    }

    @Nested
    @DisplayName("지급된 쿠폰중 하나를 사용")
    class Use {
        @Test
        @DisplayName("쿠폰 사용")
        public void useCoupon() throws Exception {
            couponService.makeCoupon(1);
            String givenCouponId = couponService.give(UserHelper.VALID_ID);
            Coupon coupon = couponService.get(givenCouponId).get();

            couponService.use(coupon);
            Coupon usedCoupon = couponService.get(givenCouponId).get();

            assertEquals(true, usedCoupon.isUsed());
        }
    }

    @Nested
    @DisplayName("사용된 쿠폰중 하나를 취소")
    class Cancel {
        @Test
        @DisplayName("쿠폰 취소")
        public void cancelCoupon() throws Exception {
            userRepository.save(UserHelper.getTestUser());
            couponRepository.save(CouponHelper.getUsedTestCoupon());

            Coupon coupon = couponService.get(CouponHelper.VALID_ID).get();
            couponService.cancel(coupon);

            Coupon canceledCoupon = couponRepository.findById(CouponHelper.VALID_ID).get();
            assertEquals(false, canceledCoupon.isUsed());
        }
    }

    @Nested
    @DisplayName("당일 만료된 전체 쿠폰 목록을 조회")
    class Expire {

        private final static long TEST_MILLIS = 1590948000000L;
        private final static long DAY_MILLIS = 1000 * 60 * 60 * 24;

        @BeforeEach
        public void init() throws Exception {
            couponRepository.saveAll(get2weeksCoupons(TEST_MILLIS));
        }

        @Test
        @DisplayName("당일 만료된 쿠폰이 1개 있는 경우")
        public void get() {
            couponService.clock = Clock.fixed(Instant.ofEpochMilli(TEST_MILLIS + 1), ZoneId.systemDefault());

            List<Coupon> expiredCoupons = couponService.getExpired();

            assertEquals(1, expiredCoupons.size());
        }

        @Test
        @DisplayName("당일 만료된 쿠폰이 없는 경우")
        public void getEmptyList() {
            couponService.clock = Clock.fixed(Instant.ofEpochMilli(TEST_MILLIS - 1), ZoneId.systemDefault());

            List<Coupon> expiredCoupons = couponService.getExpired();

            assertEquals(0, expiredCoupons.size());
        }
    }

    @Nested
    @DisplayName("쿠폰 만료 3일전 사용자에게 알려주기")
    class Notify {
        private final static long TEST_MILLIS = 1590948000000L;

        @BeforeEach
        public void init() {
            couponService.clock = Clock.fixed(Instant.ofEpochMilli(TEST_MILLIS), ZoneId.systemDefault());
        }

        @Test
        @DisplayName("사용자에게 지급이 안된 쿠폰은 알려주지 않음")
        public void emptyExpireCoupons() {
            couponRepository.saveAll(get2weeksCoupons(TEST_MILLIS));

            List<Coupon> coupons = couponService.getNotifyCoupons();

            assertEquals(0, coupons.size());
        }

        @Test
        @DisplayName("사용자에게 지급된 쿠폰 중 3일 후 만료되는 것이 있는지 확인")
        public void expireCoupon() {
            List<Coupon> coupons = get2weeksCoupons(TEST_MILLIS).stream()
                    .map(c -> {
                        c.setUserId(UserHelper.VALID_ID);
                        return c;
                    })
                    .collect(Collectors.toList());
            couponRepository.saveAll(coupons);


            List<Coupon> notifyCoupons = couponService.getNotifyCoupons();

            assertEquals(1, notifyCoupons.size());
        }

        @Test
        @DisplayName("3일 후 만료되는 쿠폰 중 사용한 쿠폰은 알려주지 않음")
        public void emptyUsedCoupons() {
            List<Coupon> coupons = get2weeksCoupons(TEST_MILLIS).stream()
                    .map(c -> {
                        c.setUserId(UserHelper.VALID_ID);
                        c.setUsed(true);
                        return c;
                    })
                    .collect(Collectors.toList());
            couponRepository.saveAll(coupons);

            List<Coupon> notifyCoupons = couponService.getNotifyCoupons();

            assertEquals(0, notifyCoupons.size());
        }
    }
}