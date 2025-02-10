package com.digio.backend.DTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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
        private List<Relation> relations;

        public List<Calculation> getCalculations() {
            return calculations;
        }

        public void setCalculations(List<Calculation> calculations) {
            this.calculations = calculations;
        }

        public List<Relation> getRelations() {
            return relations;
        }

        public void setRelations(List<Relation> relations) {
            this.relations = relations;
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

        @Data
        @NoArgsConstructor
        public static class Relation {
            private String column1;
            private String condition;
            private String column2;
            private String condition2;

            public String getColumn1() {
                return column1;
            }

            public void setColumn1(String column1) {
                this.column1 = column1;
            }

            public String getCondition() {
                return condition;
            }

            public void setCondition(String condition) {
                this.condition = condition;
            }

            public String getColumn2() {
                return column2;
            }

            public void setColumn2(String column2) {
                this.column2 = column2;
            }

            public String getCondition2() {
                return condition2;
            }

            public void setCondition2(String condition2) {
                this.condition2 = condition2;
            }
        }
    }
}