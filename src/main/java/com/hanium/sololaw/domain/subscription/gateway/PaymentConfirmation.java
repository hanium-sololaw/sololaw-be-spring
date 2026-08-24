/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.gateway;

import java.time.LocalDateTime;

public record PaymentConfirmation(
    String providerPaymentKey, long amount, LocalDateTime paidAt, String receiptUrl) {}
