package org.acme.sample.quarkussocial.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;




@Data
public class CreateUserRequest  {

    @NotNull(message = "Age is Required")
    private Integer age;

    @NotBlank(message = "Name is Required")
    private String name;



}
