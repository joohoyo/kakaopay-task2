package com.joohoyo.kakaopay.task.app.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joohoyo.kakaopay.task.app.user.UserRepository;
import com.joohoyo.kakaopay.task.helper.CouponHelper;
import com.joohoyo.kakaopay.task.helper.UserHelper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class CouponControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Nested
    @DisplayName("쿠폰 생성 요청")
    class Make {
        @Test
        @DisplayName("정상적인 n")
        public void valid() throws Exception {
            mockMvc.perform(post("/coupon").content("n=1").contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        @DisplayName("n이 없을 때")
        public void invalidParamter() throws Exception {
            mockMvc.perform(post("/coupon").content("nn=1").contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().is4xxClientError())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("사용자에게 쿠폰 지급")
    class Give {

        @BeforeEach
        public void init() {
            userRepository.save(UserHelper.getTestUser());
        }

        @AfterEach
        public void tearDown() {
            userRepository.deleteAll();
        }

        @Test
        @DisplayName("없는 유저에게 기존의 쿠폰 지급시 에러 확인")
        public void inValidUser() throws Exception {
            mockMvc.perform(put("/coupon/" + UserHelper.INVALID_ID).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().string(CouponController.ERROR_INVALID_USERID))
                    .andDo(print());
        }

        @Test
        @DisplayName("줄 수 있는 쿠폰이 없는 경우 에러 확인")
        public void invalidCoupon() throws Exception {
            mockMvc.perform(put("/coupon/" + UserHelper.VALID_ID).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().string(CouponService.ERROR_NO_MORE_COUPON))
                    .andDo(print());
        }

        @Test
        @DisplayName("정상 유저에게 정상 쿠폰 지급")
        public void validCoupon() throws Exception {
            Coupon coupon = couponRepository.save(CouponHelper.getTestCoupon());

            mockMvc.perform(put("/coupon/" + UserHelper.VALID_ID).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(content().string(coupon.getId()))
                    .andDo(print());

            couponRepository.deleteAll();
        }
    }

    @Nested
    @DisplayName("사용자에게 지급된 쿠폰 조회")
    class Search {

        @BeforeEach
        public void init() {
            userRepository.save(UserHelper.getTestUser());
        }

        @AfterEach
        public void tearDown() {
            userRepository.deleteAll();
        }

        @Test
        @DisplayName("없는 유저 조회")
        public void inValidUser() throws Exception {
            mockMvc.perform(get("/coupon/" + UserHelper.INVALID_ID).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().string(CouponController.ERROR_INVALID_USERID))
                    .andDo(print());
        }

        @Test
        @DisplayName("지급된 쿠폰이 없는 사용자 조회")
        public void noCoupon() throws Exception {
            String expectedBody = objectMapper.writeValueAsString(new ArrayList<String>());

            mockMvc.perform(get("/coupon/" + UserHelper.VALID_ID).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(content().string(expectedBody))
                    .andDo(print());
        }

        @Test
        @DisplayName("지급된 쿠폰이 1개 있는 사용자 조회")
        public void oneCoupon() throws Exception {
            couponRepository.save(CouponHelper.getGivenTestCoupon());

            MvcResult result = mockMvc.perform(get("/coupon/" + UserHelper.VALID_ID).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andReturn();

            String actualBody = result.getResponse().getContentAsString();
            List<String> coupons = objectMapper.readValue(actualBody, ArrayList.class);
            Assertions.assertEquals(1, coupons.size());

            couponRepository.deleteAll();
        }
    }

    @Nested
    @DisplayName("지급된 쿠폰 중 하나 사용")
    class Use {

        private static final String COUPON_USE_URI = "/coupon/use";
        private static final String INVALID_PARAMETER = "couponId=" + CouponHelper.INVALID_ID;
        private static final String VALID_PARAMETER = "couponId=" + CouponHelper.VALID_ID;

        @BeforeEach
        public void init() {
            userRepository.save(UserHelper.getTestUser());
        }

        @AfterEach
        public void tearDown() {
            userRepository.deleteAll();
            couponRepository.deleteAll();
        }

        @Test
        @DisplayName("없는 쿠폰 사용")
        public void invalidCoupon() throws Exception {
            mockMvc.perform(put(COUPON_USE_URI).content(INVALID_PARAMETER).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().string(CouponController.ERROR_INVALID_COUPONID))
                    .andDo(print());
        }

        @Test
        @DisplayName("탈퇴한 유저의 쿠폰 사용")
        public void inValidUser() throws Exception {
            couponRepository.save(CouponHelper.getGivenTestCoupon());
            userRepository.deleteAll();

            mockMvc.perform(put(COUPON_USE_URI).content(VALID_PARAMETER).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().string(CouponService.ERROR_INVALID_COUPON))
                    .andDo(print());
        }

        @Test
        @DisplayName("사용된 쿠폰 사용")
        public void usedCoupon() throws Exception {
            Coupon coupon = CouponHelper.getGivenTestCoupon();
            coupon.setUsed(true);
            couponRepository.save(coupon);

            mockMvc.perform(put(COUPON_USE_URI).content(VALID_PARAMETER).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().string(CouponService.ERROR_INVALID_COUPON))
                    .andDo(print());
        }

        @Test
        @DisplayName("지급된 적 없는 쿠폰 사용")
        public void notGiveCoupon() throws Exception {
            couponRepository.save(CouponHelper.getTestCoupon());

            mockMvc.perform(put(COUPON_USE_URI).content(VALID_PARAMETER).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().string(CouponService.ERROR_INVALID_COUPON))
                    .andDo(print());
        }

        @Test
        @DisplayName("쿠폰 사용")
        public void use() throws Exception {
            couponRepository.save(CouponHelper.getGivenTestCoupon());

            mockMvc.perform(put(COUPON_USE_URI).content(VALID_PARAMETER).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

    }

    @Nested
    @DisplayName("사용된 쿠폰 중 하나 취소")
    class Cancel {
        private static final String COUPON_CANCEL_URI = "/coupon/cancel";
        private static final String VALID_PARAMETER = "couponId=" + CouponHelper.VALID_ID;
        private static final String INVALID_PARAMETER = "couponId=" + CouponHelper.INVALID_ID;

        @BeforeEach
        public void init() {
            userRepository.save(UserHelper.getTestUser());
            couponRepository.save(CouponHelper.getGivenTestCoupon());
        }

        @AfterEach
        public void tearDown() {
            userRepository.deleteAll();
            couponRepository.deleteAll();
        }

        @Test
        @DisplayName("없는 쿠폰 취소")
        public void invalidCoupon() throws Exception {
            mockMvc.perform(put(COUPON_CANCEL_URI).content(INVALID_PARAMETER).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().string(CouponController.ERROR_INVALID_COUPONID))
                    .andDo(print());
        }

        @Test
        @DisplayName("사용되지 않은 쿠폰 취소")
        public void unusedCoupon() throws Exception {
            mockMvc.perform(put(COUPON_CANCEL_URI).content(VALID_PARAMETER).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().string(CouponService.ERROR_UNUSED_COUPON))
                    .andDo(print());
        }

        @Test
        @DisplayName("사용된 쿠폰 취소")
        public void validCoupon() throws Exception {
            couponRepository.save(CouponHelper.getUsedTestCoupon());

            mockMvc.perform(put(COUPON_CANCEL_URI).content(VALID_PARAMETER).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("당일 만료된 전체 쿠폰 목록을 조회")
    class Expire {
        private static final String COUPON_EXPIRED_URI = "/coupon/expired";

        @Test
        @DisplayName("당일 만료된 전체 쿠폰 목록을 조회")
        public void expire() throws Exception {
            mockMvc.perform(get(COUPON_EXPIRED_URI))
                    .andExpect(status().isOk())
                    .andDo(print());
        }
    }

    // 7. 발급된 쿠폰중 만료 3일전 사용자에게 메세지(“쿠폰이 3일 후 만료됩니다.”)를 발송하는 기능을 구현하세요. (실제 메세지를 발송하는 것이 아닌 stdout 등으로 출력하시면 됩니다.)

}
