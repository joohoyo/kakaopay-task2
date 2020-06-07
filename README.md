# 카카오페이 사전과제

Rest API 기반 쿠폰시스템

## 개발환경

- Java 14
    - Spring Boot 2.3.0
    - Junit 5
    - Spring Boot Test
- MongoDB 4.2.1
- Maven 3.6.3
- IntelliJ IDEA

## 빌드 및 실행 방법

Git, Java, Maven, MongoDB는 설치되어 있다고 가정. MongoDB는 auth 설정을 하지 않고 개발.

```
$ git clone https://github.com/joohoyo/kakaopay-task2.git
$ cd kakaopay-task2
$ ./mvnw spring-boot:run 
```

## 문제 해결 전략

- 쿠폰 구조

```
{
   id : (String) 쿠폰번호,
   expiration : (long) 만료시각(millisecond). 생성일자 기준 7일 뒤,
   userId : (String) 지급된 사용자ID,
   used : (boolean) 사용여부,
}
```  

- 쿠폰 생성
    - 생성 시각 + 카운트로 쿠폰번호 생성하여 여러개의 쿠폰을 생성하여도 겹치지 않도록 함.
    - 생성시각의 millisecond를 초단위로 잘라 16진수로 변환하고, 카운트는 미리 구성된 문자셋(알파벳+숫자 총 32개)을 이용하여 5자리의 쿠폰 번호를 만듬.

- 쿠폰 지급
    - 만들어진 쿠폰 중에서 사용자에게 배정이 되지 않은 (coupon.userId == null) 아무 쿠폰을 찾아, userId를 할당하여 지급처리
    - userId에 인덱스 설정

- 사용자에게 지급된 쿠폰 조회
    - 저장되어 있는 쿠폰 중 입력된 userId와 일치하는 쿠폰 조회

- 쿠폰 사용
    - 쿠폰 번호를 받으면 해당 쿠폰을 찾아 used를 true로 설정

- 쿠폰 취소
    - 해당 쿠폰을 찾아 used를 false로 업데이트

- 당일 만료된 쿠폰 목록 조회
    - 자정 ~ 조회 시점까지의 expiration을 갖는 쿠폰 찾기
    - 만료 시점 DB 조회를 위하여 expiration에 인덱스 설정

- 만료 3일전 사용자에게 알려주기
    - 우선 3일 후의 expiration을 갖는 쿠폰을 찾고, 사용자에게 할당되었고, 사용되지 않은 쿠폰을 조회.


## 더 나은 쿠폰 시스템을 위한 TODO
- 사용할 때, expiration 검사하기
- coupon에 type 필드 추가. 다양한 종류의 쿠폰 지원하기
- custom exception 만들어 처리하기
- 대량으로 coupon 가져올 때, paging 기능 이용하기
