package com.gepapant.enersense.threshold;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api")
public class ThresholdRuleController {

    private final ThresholdRuleService
        thresholdRuleService;

    public ThresholdRuleController(
        ThresholdRuleService
            thresholdRuleService
    ) {
        this.thresholdRuleService =
            thresholdRuleService;
    }

    @PostMapping(
        "/devices/{deviceId}/thresholds"
    )
    public ResponseEntity<
        ThresholdRuleResponse
    > create(
        @PathVariable
        Long deviceId,

        @Valid
        @RequestBody
        ThresholdRuleCreateRequest request
    ) {

        ThresholdRuleResponse response =
            thresholdRuleService.create(
                deviceId,
                request
            );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping(
        "/devices/{deviceId}/thresholds"
    )
    public List<
        ThresholdRuleResponse
    > findByDevice(
        @PathVariable
        Long deviceId
    ) {

        return thresholdRuleService
            .findByDevice(deviceId);
    }

    @PatchMapping(
        "/thresholds/{ruleId}/active"
    )
    public ThresholdRuleResponse
        updateActive(
            @PathVariable
            Long ruleId,

            @Valid
            @RequestBody
            ThresholdRuleActiveRequest request
        ) {

        return thresholdRuleService
            .updateActive(
                ruleId,
                request
            );
    }

    @DeleteMapping(
        "/thresholds/{ruleId}"
    )
    public ResponseEntity<Void>
        delete(
            @PathVariable
            Long ruleId
        ) {

        thresholdRuleService.delete(
            ruleId
        );

        return ResponseEntity
            .noContent()
            .build();
    }
}