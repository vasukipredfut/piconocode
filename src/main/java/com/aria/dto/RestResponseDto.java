package com.aria.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestResponseDto {

    @JsonProperty("errorCode")
    public String errorCode;

    @JsonProperty("errorMessage")
    public String errorMessage;

    @JsonProperty("referenceDetails")
    public String referenceDetails;
}
