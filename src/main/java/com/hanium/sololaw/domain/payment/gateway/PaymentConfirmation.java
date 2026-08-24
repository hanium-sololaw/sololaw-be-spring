/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.payment.gateway;

import java.time.LocalDateTime;

public record PaymentConfirmation(
    String providerPaymentKey, long amount, LocalDateTime paidAt, String receiptUrl) {}
