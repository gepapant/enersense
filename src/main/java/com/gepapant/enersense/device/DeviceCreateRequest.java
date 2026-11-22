package com.gepapant.enersense.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeviceCreateRequest(

    @NotBlank
    @Size(max = 120)
    String name,

    @NotNull
    DeviceType type,

    @NotNull
    Long facilityId

) {
}