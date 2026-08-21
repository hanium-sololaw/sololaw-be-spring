/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hanium.sololaw.domain.subscription.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  Optional<Subscription> findByUserId(Long userId);

  /**
   * 조건부 원자 UPDATE로 저장 용량을 예약합니다. used_storage_bytes + deltaBytes가 storage_limit_bytes를 넘지 않을 때만
   * 반영되며, 영향받은 row 수가 0이면 용량 초과로 판단합니다(read-then-write가 아니라 단일 UPDATE라 동시성 문제가 없음).
   *
   * @param userId 대상 사용자 ID
   * @param deltaBytes 증가시킬 바이트 수
   * @return 영향받은 row 수(0이면 용량 초과로 반영 안 됨, 1이면 성공)
   */
  @Modifying(clearAutomatically = true)
  @Query(
      "UPDATE Subscription s SET s.usedStorageBytes = s.usedStorageBytes + :deltaBytes "
          + "WHERE s.userId = :userId AND s.usedStorageBytes + :deltaBytes <= s.storageLimitBytes")
  int tryReserveStorage(@Param("userId") Long userId, @Param("deltaBytes") Long deltaBytes);

  /**
   * 저장 용량을 반환합니다(0 미만으로 내려가지 않도록 클램프). 삭제 시 항상 성공합니다.
   *
   * @param userId 대상 사용자 ID
   * @param deltaBytes 감소시킬 바이트 수
   */
  @Modifying(clearAutomatically = true)
  @Query(
      "UPDATE Subscription s SET s.usedStorageBytes = "
          + "CASE WHEN s.usedStorageBytes - :deltaBytes < 0 THEN 0 ELSE s.usedStorageBytes - :deltaBytes END "
          + "WHERE s.userId = :userId")
  void releaseStorage(@Param("userId") Long userId, @Param("deltaBytes") Long deltaBytes);
}
