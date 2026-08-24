/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.payment.gateway;

public record PaymentCheckoutCommand(
    String orderId, long amount, String orderName, String customerEmail, String customerName) {}
