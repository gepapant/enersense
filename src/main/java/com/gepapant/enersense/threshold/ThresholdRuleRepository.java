package com.gepapant.enersense.threshold;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ThresholdRuleRepository
    extends JpaRepository<ThresholdRule, Long> {

    List<ThresholdRule>
        findByDeviceIdOrderByIdAsc(
            Long deviceId
        );

    List<ThresholdRule>
        findByDeviceIdAndActiveTrueOrderByIdAsc(
            Long deviceId
        );
}