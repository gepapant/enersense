package com.gepapant.enersense.alert;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository
    extends JpaRepository<Alert, Long> {

    List<Alert> findAllByOrderByCreatedAtDesc();
}