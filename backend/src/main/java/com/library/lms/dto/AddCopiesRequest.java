package com.library.lms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCopiesRequest {
    @NotNull(message = "Copies count is required")
    @Min(value = 1, message = "At least 1 copy must be added")
    private Integer copies;
}
