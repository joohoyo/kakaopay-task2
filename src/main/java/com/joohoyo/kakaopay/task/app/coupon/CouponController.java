package com.joohoyo.kakaopay.task.app.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joohoyo.kakaopay.task.app.user.User;
import com.joohoyo.kakaopay.task.app.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/coupon")
public class CouponController {

    public static final String ERROR_INVALID_COUPONID = "invalid couponid";
    public static final String ERROR_INVALID_USERID = "invalid userid";

    CouponService couponService;
    UserService userService;
    ObjectMapper objectMapper;

    public CouponController(CouponService couponService, UserService userService, ObjectMapper objectMapper) {
        this.couponService = couponService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    // 1. 랜덤한 코드의 쿠폰을 N개 생성하여 데이터베이스에 보관하는 API를 구현하세요. input : N
    @PostMapping
    public void make(@RequestParam Integer n) {
        couponService.makeCoupon(n);
        return;
    }

    // 2. 생성된 쿠폰중 하나를 사용자에게 지급하는 API를 구현하세요. output : 쿠폰번호(XXXXX-XXXXXX-XXXXXXXX)
    @PutMapping(value = "/{userId}")
    public ResponseEntity<?> give(@PathVariable String userId) {
        Optional<User> user = userService.get(userId);
        if (user.isPresent() == false) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR_INVALID_USERID);
        }

        String couponId = "";
        try {
            couponId = couponService.give(userId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(couponId);
    }

    // 3. 사용자에게 지급된 쿠폰을 조회하는 API를 구현하세요.
    @GetMapping(value = "/{userId}")
    public ResponseEntity<?> search(@PathVariable String userId) throws Exception {
        Optional<User> user = userService.get(userId);
        if (user.isPresent() == false) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR_INVALID_USERID);
        }

        List<Coupon> coupons = couponService.search(user.get());
        return ResponseEntity.status(HttpStatus.OK).body(new ObjectMapper().writeValueAsString(coupons));
    }

    // 4. 지급된 쿠폰중 하나를 사용하는 API를 구현하세요. (쿠폰 재사용은 불가) input : 쿠폰번호
    @PutMapping(value = "/use")
    public ResponseEntity<?> use(@RequestParam String couponId) throws Exception {
        Optional<Coupon> coupon = couponService.get(couponId);
        if (coupon.isPresent() == false) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR_INVALID_COUPONID);
        }

        try {
            couponService.use(coupon.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    // 5. 지급된 쿠폰중 하나를 사용 취소하는 API를 구현하세요. (취소된 쿠폰 재사용 가능) input : 쿠폰번호
    @PutMapping(value = "/cancel")
    public ResponseEntity<?> cancel(@RequestParam String couponId) throws Exception {
        Optional<Coupon> coupon = couponService.get(couponId);
        if (coupon.isPresent() == false) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR_INVALID_COUPONID);
        }

        try {
            couponService.cancel(coupon.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    // 6. 발급된 쿠폰중 당일 만료된 전체 쿠폰 목록을 조회하는 API를 구현하세요.
    @GetMapping(value = "/expired")
    public ResponseEntity<?> getExpired() throws Exception {
        List<Coupon> coupons = couponService.getExpired();
        return ResponseEntity.status(HttpStatus.OK).body(objectMapper.writeValueAsString(coupons));
    }

}
