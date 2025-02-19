package com.digio.backend.Service;

import com.digio.backend.Validate.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TemplateService {

    @Autowired
    ObjectMapper objectMapper;

    private final Map<Pattern, Function<String, String>> validationRules = new HashMap<>();

    public TemplateService() {
        initializeDefaultValidationRules();
    }

    public List<Map<String, Object>> handleUploadWithTemplate(
            MultipartFile file, List<String> expectedHeaders, List<String> calculater, List<String> relation) {

        validateFile(file);

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            validateSheet(sheet);

            List<List<String>> parsedCalculations = parseCalculations(calculater);
            List<List<String>> parsedRelations = parseRelations(relation);
            List<String> flatHeaders = cleanHeaders(expectedHeaders);

            System.out.println(parsedCalculations);
            System.out.println(parsedRelations);

            return processSheet(sheet, flatHeaders, parsedRelations,  parsedCalculations);

        } catch (IOException e) {
            throw new IllegalArgumentException("ไม่สามารถอ่านไฟล์ Excel ได้", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("ไฟล์ว่างเปล่า ไม่สามารถอ่านข้อมูลได้");
        }
    }

    private void validateSheet(Sheet sheet) {
        if (isRowsEmpty(sheet)) {
            throw new IllegalArgumentException("ไฟล์นี้ไม่มีข้อมูล");
        }
    }

    private List<String> cleanHeaders(List<String> headers) {
        return headers.stream()
                .map(header -> header.replace("[", "").replace("]", "").replace("\"", ""))
                .collect(Collectors.toList());
    }

    private List<List<String>> parseCalculations(List<String> calculater) {
        List<List<String>> parsedCalculations = new ArrayList<>();
        if (calculater == null || calculater.isEmpty()) {
            return parsedCalculations;
        }

        List<String> currentCalculation = new ArrayList<>();
        for (String item : calculater) {
            item = item.replace("\"", "").replace("[", "").replace("]", "").trim();
            if (!item.isEmpty()) {
                if (item.equals("+") || item.equals("-") || item.equals("x") || item.equals("/")) {
                    if (!currentCalculation.isEmpty()) {
                        parsedCalculations.add(new ArrayList<>(currentCalculation));
                    }
                    currentCalculation.clear();
                }
                currentCalculation.add(item);
            }
        }
        if (!currentCalculation.isEmpty()) {
            parsedCalculations.add(currentCalculation);
        }

        return parsedCalculations;
    }

    private static List<List<String>> parseRelations(List<String> relations) {
        List<List<String>> parsedRelations = new ArrayList<>();
        if (relations == null || relations.isEmpty()) {
            return parsedRelations;
        }

        List<String> currentRelation= new ArrayList<>();
        for (String item : relations) {
            item = item.replace("\"", "").replace("[", "").replace("]", "").trim();
            if (!item.isEmpty()) {
                currentRelation.add(item);
            }
        }
        if (!currentRelation.isEmpty()) {
            parsedRelations.add(currentRelation);
        }

        return parsedRelations;
    }

    private List<Map<String, Object>> processSheet(Sheet sheet, List<String> headers, List<List<String>> relation, List<List<String>> calculations) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Map<String, Object>> lastErrorList = new ArrayList<>();

        if (calculations != null && !calculations.isEmpty() && relation != null && !relation.isEmpty()) {
            for (List<String> calc : calculations) {
                for (List<String> rela : relation) {
                    List<Map<String, Object>> tempResult = processRowsAndCalculations(sheet, headers, null, rela, calc);
                    if (!tempResult.isEmpty() && tempResult.get(0).containsKey("summary")) {
                        lastErrorList = tempResult;
                    } else {
                        resultList.addAll(tempResult);
                    }
                }
            }
        } else if (calculations != null && !calculations.isEmpty()) {
            for (List<String> calc : calculations) {
                List<Map<String, Object>> tempResult = processRowsAndCalculations(sheet, headers, null, null, calc);
                if (!tempResult.isEmpty() && tempResult.get(0).containsKey("summary")) {
                    lastErrorList = tempResult;
                } else {
                    resultList.addAll(tempResult);
                }
            }
        } else if (relation != null && !relation.isEmpty()) {
            for (List<String> rela : relation) {
                List<Map<String, Object>> tempResult = processRowsAndCalculations(sheet, headers, null, rela, null);
                if (!tempResult.isEmpty() && tempResult.get(0).containsKey("summary")) {
                    lastErrorList = tempResult;
                } else {
                    resultList.addAll(tempResult);
                }
            }
        } else {
            resultList = processRowsAndCalculations(sheet, headers, null, null, null);
        }

        return !lastErrorList.isEmpty() ? lastErrorList : (resultList.isEmpty() ? Collections.emptyList() : resultList);
    }

    private List<Map<String, Object>> processRowsAndCalculations(Sheet sheet, List<String> headers, List<Integer> selectedIndices, List<String> relation, List<String> calculation) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Map<String, Object>> errorList = new ArrayList<>();
        Map<Integer, String> errorSummaryMap = new TreeMap<>();

        Map<String, Integer> headerIndexMap = getHeaderIndexMap(sheet.getRow(0));
        boolean hasCalculation = calculation != null && calculation.size() == 4;
        String operator = hasCalculation ? calculation.get(0).trim() : null;
        String addend = hasCalculation ? calculation.get(1).trim() : null;
        String operand = hasCalculation ? calculation.get(2).trim() : null;
        String resultKey = hasCalculation ? calculation.get(3).trim() : null;

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            Map<String, Object> rowData = new HashMap<>();
            StringBuilder errorBuilder = new StringBuilder();

            processRowValidation(row, headers, selectedIndices, errorList, errorBuilder, headerIndexMap);
            if (relation != null && !relation.isEmpty()) checkRelation(row, relation, errorList, errorBuilder, headerIndexMap);
            if (hasCalculation) processCalculation(row, operator, addend, operand, resultKey, rowData, errorList, errorBuilder, headerIndexMap);

            if (!errorBuilder.isEmpty()) {
                errorSummaryMap.put(row.getRowNum() + 1, errorBuilder.toString().trim());
            } else {
                resultList.add(rowData);
            }
        }

        if (!errorList.isEmpty()) return generateErrorResponse(errorList, errorSummaryMap);
        return resultList.isEmpty() ? Collections.emptyList() : resultList;
    }

    private Map<String, Integer> getHeaderIndexMap(Row headerRow) {
        Map<String, Integer> headerIndexMap = new HashMap<>();
        for (Cell cell : headerRow) {
            headerIndexMap.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
        }
        return headerIndexMap;
    }

    private void processRowValidation(Row row, List<String> headers, List<Integer> selectedIndices, List<Map<String, Object>> errorList, StringBuilder errorBuilder, Map<String, Integer> headerIndexMap) {
        for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
            if (selectedIndices != null && !selectedIndices.contains(colIndex)) continue;

            String header = headers.get(colIndex);
            String cellValue = Optional.ofNullable(getCellValue(row.getCell(colIndex))).map(String::trim).orElse("");
            String errorMessage = validateCellAndGetMessage(header, cellValue);

            if (!"success".equals(errorMessage)) {
                addErrorDetails(row, colIndex, header, errorMessage, errorList);
                errorBuilder.append(errorMessage).append("; ");
            }
        }
    }

    private void checkRelation(Row row, List<String> relation, List<Map<String, Object>> errorList, StringBuilder errorBuilder, Map<String, Integer> headerIndexMap) {
        String column1 = relation.get(0).trim();
        String condition = relation.get(1).trim();
        String column2 = relation.get(2).trim();

        String value1 = getCell(row, headerIndexMap.get(column1));
        String value2 = getCell(row, headerIndexMap.get(column2));

        if (!checkRelation(value1, condition, value2)) {
            String relationError = "ไม่ตรงกับความสัมพันธ์: " + column1 + " " + condition + " " + column2;
            addErrorDetails(row, headerIndexMap.get(column1), column1, relationError, errorList);
            errorBuilder.append(relationError).append("; ");
        }
    }

    private void processCalculation(Row row, String operator, String addend, String operand, String resultKey, Map<String, Object> rowData, List<Map<String, Object>> errorList, StringBuilder errorBuilder, Map<String, Integer> headerIndexMap) {
        double addendValue = getValue(row, headerIndexMap.get(addend));
        double operandValue = getValue(row, headerIndexMap.get(operand));
        double result = performCalculation(operator, addendValue, operandValue);

        if (result != 0.0 && headerIndexMap.containsKey(resultKey)) {
            double expectedBalance = getValue(row, headerIndexMap.get(resultKey));
            if (result != expectedBalance) {
                String calcError = resultKey + ": คาดหวัง " + result + " แต่ในไฟล์ได้ " + expectedBalance;
                addErrorDetails(row, headerIndexMap.get(resultKey), resultKey, calcError, errorList);
                errorBuilder.append(calcError).append("; ");
            } else {
                rowData.put(resultKey, result);
            }
        }
    }

    private double performCalculation(String operator, double addendValue, double operandValue) {
        switch (operator) {
            case "+":
                return addendValue + operandValue;
            case "-":
                return addendValue - operandValue;
            case "x":
                return addendValue * operandValue;
            case "/":
                return operandValue != 0 ? addendValue / operandValue : 0.0;
            default:
                throw new IllegalArgumentException("ไม่รองรับเครื่องหมายการคำนวณ: " + operator);
        }
    }

    private void addErrorDetails(Row row, int colIndex, String header, String errorMessage, List<Map<String, Object>> errorList) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("row", row.getRowNum());
        errorDetails.put("column", colIndex);
        errorDetails.put("header", header);
        errorDetails.put("message", errorMessage);
        errorList.add(errorDetails);
    }

    private List<Map<String, Object>> generateErrorResponse(List<Map<String, Object>> errorList, Map<Integer, String> errorSummaryMap) {
        Set<Map<String, Object>> uniqueErrors = new HashSet<>(errorList);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("summary", "Errors found");
        errorResponse.put("errorList", new ArrayList<>(uniqueErrors));
        errorResponse.put("errorDetails", formatErrorMessages(errorSummaryMap));

        return List.of(errorResponse);
    }

    private boolean checkRelation(String value1, String condition, String value2) {
        System.out.println("ตรวจสอบค่าในแถว: " + value1 + " และ " + value2);

        switch (condition.toLowerCase()) {
            case "notempty":
                return !value1.isEmpty() && !value2.isEmpty();
            case "exists":
                return checkExistence(value1, value2);
            default:
                throw new IllegalArgumentException("Unsupported condition: " + condition);
        }
    }

    private boolean checkExistence(String column1Value, String column2Value) {
        Map<String, List<String>> provinceToDistrictMap = getProvinceToDistrictMap();

        List<String> districts = provinceToDistrictMap.get(column1Value);
        return districts != null && districts.contains(column2Value);
    }

    private Map<String, List<String>> getProvinceToDistrictMap() {

        Map<String, List<String>> provinceToDistrictMap = new HashMap<>();
        provinceToDistrictMap.put("อุดรธานี", Arrays.asList("เมืองอุดรธานี", "หนองหาน", "กุมภวาปี"));
        provinceToDistrictMap.put("กรุงเทพมหานคร", Arrays.asList("บางรัก", "พระนคร", "ดินแดง"));

        return provinceToDistrictMap;
    }

    private boolean isRowsEmpty(Sheet sheet) {
        for (int rowIndex = 1; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                for (Cell cell : row) {
                    if (cell != null && !getCellValue(cell).trim().isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private String getCell(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private double getValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (cell != null) {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    try {
                        return Double.parseDouble(cell.getStringCellValue().trim());
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                case FORMULA:
                    FormulaEvaluator evaluator = row.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                    return evaluator.evaluate(cell).getNumberValue();
                default:
                    return 0.0;
            }
        }
        return 0.0;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";

        FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return cell.getNumericCellValue() % 1 == 0
                        ? String.valueOf((long) cell.getNumericCellValue())
                        : String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                CellValue cellValue = evaluator.evaluate(cell);
                switch (cellValue.getCellType()) {
                    case STRING:
                        return cellValue.getStringValue();
                    case NUMERIC:
                        return String.valueOf(cellValue.getNumberValue());
                    case BOOLEAN:
                        return String.valueOf(cellValue.getBooleanValue());
                    default:
                        return "";
                }
            default:
                return "";
        }
    }

    private String validateCellAndGetMessage(String header, String cellValue) {
        return validationRules.entrySet().stream()
                .filter(entry -> entry.getKey().matcher(header).matches())
                .findFirst()
                .map(entry -> entry.getValue().apply(cellValue))
                .orElse("ไม่สามารถตรวจสอบหัวข้อนี้ได้: " + header);
    }

    private List<String> formatErrorMessages(Map<Integer, String> errorMap) {
        return errorMap.entrySet().stream()
                .map(entry -> "แถวที่ " + entry.getKey() + ": " + entry.getValue())
                .toList();
    }

    private void initializeDefaultValidationRules() {
        validationRules.put(Pattern.compile("^(ชื่อ|name|ชื่อนามสกุล|fullname).*"), NameValidator::validate);
        validationRules.put(Pattern.compile("^(อีเมล|email).*$"), EmailValidate::validate);
        validationRules.put(Pattern.compile("^(บัตรประชาชน|citizenid).*$"), CitizenIdValidator::validate);
        validationRules.put(Pattern.compile("^(เบอร์โทร|phone).*$"), PhoneValidator::validate);
        validationRules.put(Pattern.compile("^(ที่อยู่|address).*$"), AddressValidator::validate);
        validationRules.put(Pattern.compile("^(อายุ|age).*$"), AgeValidator::validateDateOfBirth);
        validationRules.put(Pattern.compile("^(เพศ|gender).*$"), GenderValidator::validateGender);
        validationRules.put(
                Pattern.compile("^(จำนวนเงิน|balance|amount|transactionAmount|deposit|withdrawal|credit|debit|transferAmount|loanAmount|paymentAmount|fundAmount|accountBalance|currentBalance).*$"),
                BalanceValidator::validate
        );
    }
}