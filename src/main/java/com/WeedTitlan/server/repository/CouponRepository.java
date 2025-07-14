package com.WeedTitlan.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.WeedTitlan.server.model.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, String> {
}
