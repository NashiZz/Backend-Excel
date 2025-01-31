package com.digio.backend.DTO;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddend() {
        return addend;
    }

    public void setAddend(String addend) {
        this.addend = addend;
    }

    public String getOperand() {
        return operand;
    }

    public void setOperand(String operand) {
        this.operand = operand;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

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


