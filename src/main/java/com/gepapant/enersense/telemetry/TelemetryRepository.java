package com.gepapant.enersense.telemetry;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TelemetryRepository
    extends MongoRepository<TelemetryReading, String> {

    List<TelemetryReading>
        findByDeviceIdOrderByRecordedAtDesc(
            Long deviceId
        );
}