package com.gepapant.enersense.alert;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {

    private final AlertRepository repository;

    public AlertService(
        AlertRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> findAll() {
        return repository
            .findAllByOrderByCreatedAtDesc()
            .stream()
            .map(AlertResponse::from)
            .toList();
    }

    @Transactional
    public AlertResponse acknowledge(
        Long alertId
    ) {
        Alert alert = repository
            .findById(alertId)
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Alert not found"
                )
            );

        alert.acknowledge();

        return AlertResponse.from(alert);
    }
}