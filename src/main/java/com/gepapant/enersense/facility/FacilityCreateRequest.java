package com.gepapant.enersense.facility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FacilityCreateRequest(

    @NotBlank
    @Size(max = 120)
    String name,

    @NotBlank
    @Size(max = 200)
    String location

) {
}