package com.gepapant.enersense.device;

public record DeviceResponse(
    Long id,
    String name,
    DeviceType type,
    DeviceStatus status,
    Long facilityId,
    String facilityName
) {

    public static DeviceResponse from(Device device) {
        return new DeviceResponse(
            device.getId(),
            device.getName(),
            device.getType(),
            device.getStatus(),
            device.getFacility().getId(),
            device.getFacility().getName()
        );
    }
}