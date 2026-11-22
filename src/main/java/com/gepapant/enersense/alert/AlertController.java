package com.gepapant.enersense.alert;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService service;

    public AlertController(
        AlertService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<AlertResponse> findAll() {
        return service.findAll();
    }

    @PatchMapping("/{alertId}/acknowledge")
    public AlertResponse acknowledge(
        @PathVariable Long alertId
    ) {
        return service.acknowledge(alertId);
    }
}