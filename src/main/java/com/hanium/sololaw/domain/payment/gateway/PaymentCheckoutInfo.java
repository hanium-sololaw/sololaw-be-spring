/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.payment.gateway;

/**
 * 결제 시작 정보. clientKey/redirectUrl 중 활성 PG가 채우지 않는 필드는 null이다(Toss는 clientKey만, Stripe Checkout류는
 * redirectUrl만 채운다).
 */
public record PaymentCheckoutInfo(
    String orderId,
    long amount,
    String orderName,
    String customerEmail,
    String customerName,
    String clientKey,
    String redirectUrl) {}
