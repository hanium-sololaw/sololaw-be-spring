/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hanium.sololaw.domain.precedent.entity.PrecedentBookmark;

public interface PrecedentBookmarkRepository extends JpaRepository<PrecedentBookmark, Long> {

  Page<PrecedentBookmark> findAllByUserId(Long userId, Pageable pageable);

  Optional<PrecedentBookmark> findByIdAndUserId(Long id, Long userId);

  Optional<PrecedentBookmark> findByUserIdAndSerialId(Long userId, String serialId);
}
