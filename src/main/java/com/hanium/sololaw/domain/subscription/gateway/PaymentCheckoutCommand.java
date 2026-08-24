/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.gateway;

public record PaymentCheckoutCommand(
    String orderId, long amount, String orderName, String customerEmail, String customerName) {}
