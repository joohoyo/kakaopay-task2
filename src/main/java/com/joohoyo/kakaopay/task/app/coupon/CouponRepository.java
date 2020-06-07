package com.joohoyo.kakaopay.task.app.coupon;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponRepository extends MongoRepository<Coupon, String> {
    Coupon findFirstByUserIdIsNull();

    List<Coupon> findByUserId(String userId);

    @Query("{'expiration': {$gte: ?0, $lte: ?1}}")
    List<Coupon> findTodayExpired(long startMillis, long endMillis);
}
