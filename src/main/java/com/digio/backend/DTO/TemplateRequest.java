package com.digio.backend.DTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor  // ✅ เพิ่ม constructor เปล่า
public class TemplateRequest {
    private String userToken;
    private String templatename;
    private List<Header> headers;
    private Condition condition;
    private int maxRows;

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getTemplatename() {
        return templatename;
    }

    public void setTemplatename(String templatename) {
        this.templatename = templatename;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    @Data
    @NoArgsConstructor
    public static class Header {
        private String name;
        private String condition;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }
    }

    @Data
    @NoArgsConstructor
    public static class Condition {
        private List<Calculation> calculations;


        public List<Calculation> getCalculations() {
            return calculations;
        }

        public void setCalculations(List<Calculation> calculations) {
            this.calculations = calculations;
        }

        @Data
        @NoArgsConstructor
        public static class Calculation {
            private String type;
            private String addend;
            private String operand;
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
        }
    }
}