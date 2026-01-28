package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ICDCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICDCategoryRepository extends JpaRepository<ICDCategory, String> {

    Page<ICDCategory> findByIcdCodingAndParentCategoryCodeIsNull(String icdCoding, Pageable pageable);

    Page<ICDCategory> findByIcdCodingAndParentCategoryCode(String icdCoding, String parentCategoryCode, Pageable pageable);
}
