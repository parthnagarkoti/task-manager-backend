package com.taskmanagement.dto;

import com.taskmanagement.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private TaskStatus status;
}
