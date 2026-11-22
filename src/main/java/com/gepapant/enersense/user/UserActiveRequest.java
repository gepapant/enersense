package com.gepapant.enersense.user;

import jakarta.validation.constraints.NotNull;

public record UserActiveRequest(

    @NotNull
    Boolean active

) {
}