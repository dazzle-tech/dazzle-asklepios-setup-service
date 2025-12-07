package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CommunityArea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityAreaRepository extends JpaRepository<CommunityArea, Long> {

    Page<CommunityArea> findByCommunity_Id(Long communityId, Pageable pageable);

    Page<CommunityArea> findByCommunity_IdAndNameContainingIgnoreCase(Long communityId, String name, Pageable pageable);
}
