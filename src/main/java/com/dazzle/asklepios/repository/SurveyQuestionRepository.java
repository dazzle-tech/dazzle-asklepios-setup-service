package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.SurveyQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {

    /** Questions can share an order: among them the oldest (lowest id) comes first */
    Page<SurveyQuestion> findBySectionIdOrderByDisplayOrderAscIdAsc(Long sectionId, Pageable pageable);
}
