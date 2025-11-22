package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DistrictCommunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistrictCommunityRepository extends JpaRepository<DistrictCommunity, Long> {

    Page<DistrictCommunity> findByDistrict_Id(Long districtId, Pageable pageable);

    Page<DistrictCommunity> findByDistrict_IdAndNameContainingIgnoreCase(Long districtId, String name, Pageable pageable);

}
