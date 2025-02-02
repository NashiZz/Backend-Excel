package com.digio.backend.DTO;

import lombok.Data;

import java.util.List;

@Data
public class TemplateRequest {
    private String userToken;
    private String templatename;
    private List<Header> headers;
    private int maxRows;
    private Condition condition;

    @Data
    public static class Header {
        private String name;
        private String condition;

    }
    @Data
    public static class Condition {
        private List<Calculation> calculations;
        @Data
        public static class Calculation {
            private String type;
            private String addend;
            private String operand;
            private String result;
        }
    }
}

