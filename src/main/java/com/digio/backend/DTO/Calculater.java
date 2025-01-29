package com.digio.backend.DTO;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Calculater {
    @JsonProperty("type")
    private String type;

    @JsonProperty("addend")
    private String addend;

    @JsonProperty("operand")
    private String operand;

    @JsonProperty("result")
    private String result;

    @Override
    public String toString() {
        return "Calculater{" +
                "type='" + type + '\'' +
                ", addend='" + addend + '\'' +
                ", operand='" + operand + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}


