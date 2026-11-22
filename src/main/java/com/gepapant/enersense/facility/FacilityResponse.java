package com.gepapant.enersense.facility;

public record FacilityResponse(
    Long id,
    String name,
    String location
) {

    public static FacilityResponse from(
        Facility facility
    ) {
        return new FacilityResponse(
            facility.getId(),
            facility.getName(),
            facility.getLocation()
        );
    }
}