package com.joohoyo.kakaopay.task.app.coupon;

import com.joohoyo.kakaopay.task.app.user.User;
import com.joohoyo.kakaopay.task.app.user.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CouponService {

    public static final String ERROR_NO_MORE_COUPON = "no more coupon";
    public static final String ERROR_NO_COUPON = "no coupon";
    public static final String ERROR_INVALID_COUPON = "invalid coupon";
    public static final String ERROR_UNUSED_COUPON = "not used coupon";
    private final long DAY_MILLIS = 1000 * 60 * 60 * 24;
    private final int NOTIFY_DAYS_TERM = 3;

    CouponRepository couponRepository;
    CouponGenerator couponGenerator;
    UserRepository userRepository;
    Clock clock;

    public CouponService(CouponRepository couponRepository, CouponGenerator couponGenerator, UserRepository userRepository, Clock clock) {
        this.couponRepository = couponRepository;
        this.couponGenerator = couponGenerator;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    Optional<Coupon> get(String couponId) {
        return couponRepository.findById(couponId);
    }

    void makeCoupon(int count) {
        List<Coupon> coupons = couponGenerator.make(count);
        couponRepository.saveAll(coupons);
    }

    public String give(String userId) throws Exception {
        Coupon coupon = couponRepository.findFirstByUserIdIsNull();
        if (coupon == null) {
            throw new Exception(ERROR_NO_MORE_COUPON);
        }

        coupon.setUserId(userId);
        couponRepository.save(coupon);

        return coupon.getId();
    }

    public List<Coupon> search(User user) {
        return couponRepository.findByUserId(user.getId());
    }

    public void use(Coupon coupon) throws Exception {
        if (coupon.isUsed() || coupon.getUserId() == null) {
            throw new Exception(ERROR_INVALID_COUPON);
        }

        User user = userRepository.findById(coupon.getUserId()).orElseThrow(() -> new Exception(ERROR_INVALID_COUPON));
        coupon.setUsed(true);
        couponRepository.save(coupon);
    }

    public void cancel(Coupon coupon) throws Exception {
        if (coupon.isUsed() == false) {
            throw new Exception(ERROR_UNUSED_COUPON);
        }

        User user = userRepository.findById(coupon.getUserId()).orElseThrow(() -> new Exception(ERROR_INVALID_COUPON));
        coupon.setUsed(false);
        couponRepository.save(coupon);
    }

    public List<Coupon> getExpired() {
        return getExpired(clock.millis());
    }

    private List<Coupon> getExpired(long now) {
        long startMillis = (now / DAY_MILLIS) * DAY_MILLIS;

        return couponRepository.findTodayExpired(startMillis, now);
    }

    // 발급된 쿠폰중 만료 3일전 사용자에게 메세지(“쿠폰이 3일 후 만료됩니다.”)를 발송하는 기능을 구현하 세요. (실제 메세지를 발송하는것이 아닌 stdout 등으로 출력하시면 됩니다.)
    @Scheduled(cron = "0 0 9 * * *")
    public void notifyExpire() {
        for (Coupon coupon : getNotifyCoupons()) {
            Optional<User> user = userRepository.findById(coupon.getUserId());
            if (user.isPresent() == false) {
                continue;
            }

            System.out.println(user.get().getId() + "님. 쿠폰이 " + NOTIFY_DAYS_TERM + "일 후 만료됩니다.");
        }
    }

    List<Coupon> getNotifyCoupons() {
        long current = clock.millis();
        long expireIn3Days = current + (DAY_MILLIS * NOTIFY_DAYS_TERM);

        return getExpired(expireIn3Days).stream()
                .filter(c -> c.getUserId() != null && c.isUsed() == false)
                .collect(Collectors.toList());
    }

}

