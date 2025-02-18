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

        Row headerRow = sheet.getRow(0);
        Map<String, Integer> headerIndexMap = new HashMap<>();
        for (Cell cell : headerRow) {
            String header = cell.getStringCellValue().trim();
            headerIndexMap.put(header, cell.getColumnIndex());
        }

        boolean hasCalculation = calculation != null && calculation.size() == 4;
        String operator = null, addend = null, operand = null, resultKey = null;

        if (hasCalculation) {
            operator = calculation.get(0).trim();
            addend = calculation.get(1).trim();
            operand = calculation.get(2).trim();
            resultKey = calculation.get(3).trim();

            System.out.println("ตรวจสอบการคำนวณ:");
            System.out.println("Addend: " + addend);
            System.out.println("Operand: " + operand);
            System.out.println("Header Index Map: " + headerIndexMap);

            if (!headerIndexMap.containsKey(addend) || !headerIndexMap.containsKey(operand)) {
                System.out.println("ไม่พบหัวข้อที่ใช้คำนวณใน headerIndexMap");
                throw new IllegalArgumentException("หัวข้อที่ใช้คำนวณไม่ตรงกับข้อมูลในไฟล์");
            }
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            Map<String, Object> rowData = new HashMap<>();
            StringBuilder errorBuilder = new StringBuilder();

            for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
                if (selectedIndices != null && !selectedIndices.contains(colIndex)) continue;

                String header = headers.get(colIndex);
                String cellValue = getCellValue(row.getCell(colIndex));

                String errorMessage = validateCellAndGetMessage(header, cellValue);
                if (!errorMessage.equals("success")) {
                    Map<String, Object> errorDetails = new HashMap<>();
                    errorDetails.put("row", row.getRowNum());
                    errorDetails.put("column", colIndex);
                    errorDetails.put("header", header);
                    errorDetails.put("message", errorMessage);

                    errorList.add(errorDetails);
                    errorBuilder.append(errorMessage).append("; ");
                }
            }

            if (relation != null && !relation.isEmpty()) {
                System.out.println("ตรวจสอบความสัมพันธ์ใน relation:");

                String column1 = relation.get(0).trim();
                String condition = relation.get(1).trim();
                String column2 = relation.get(2).trim();

                System.out.println("Column1: " + column1);
                System.out.println("Condition: " + condition);
                System.out.println("Column2: " + column2);

                String value1 = getCell(row, headerIndexMap.get(column1));
                String value2 = getCell(row, headerIndexMap.get(column2));

                System.out.println("Value1: " + value1);
                System.out.println("Value2: " + value2);

                if (!checkRelation(value1, condition, value2)) {
                    String relationError = "ไม่ตรงกับความสัมพันธ์: " + column1 + " " + condition + " " + column2;

                    Map<String, Object> errorDetails = new HashMap<>();

                    errorDetails.put("row", row.getRowNum());
                    errorDetails.put("column", headerIndexMap.get(column1));
                    errorDetails.put("header", headerIndexMap.get(column1));
                    errorDetails.put("message", relationError);

                    errorList.add(errorDetails);

                    errorBuilder.append(relationError).append("; ");
                    errorSummaryMap.put(row.getRowNum() + 1, errorBuilder.toString().trim());
                }
            }

            if (hasCalculation) {
                double addendValue = getValue(row, headerIndexMap.get(addend));
                double operandValue = getValue(row, headerIndexMap.get(operand));
                double result = 0.0;

                if (addendValue != 0 && operandValue != 0) {
                    System.out.println("คำนวณสำหรับแถว " + row.getRowNum() + ": ");
                    System.out.println("Addend: " + addendValue + ", Operand: " + operandValue);

                    if ("+".equals(operator)) {
                        result = addendValue + operandValue;
                        System.out.println("ผลลัพธ์ ( + ): " + result);
                    } else if ("-".equals(operator)) {
                        result = addendValue - operandValue;
                        System.out.println("ผลลัพธ์ ( - ): " + result);
                    } else if ("x".equals(operator)) {
                        result = addendValue * operandValue;
                        System.out.println("ผลลัพธ์ ( x ): " + result);
                    } else if ("/".equals(operator)) {
                        result = addendValue / operandValue;
                        System.out.println("ผลลัพธ์ ( / ): " + result);
                    } else {
                        String calcError = "ไม่รองรับเครื่องหมายการคำนวณ: " + operator;
                        System.out.println(calcError);
                        throw new IllegalArgumentException(calcError);
                    }

                    if (headerIndexMap.containsKey(resultKey)) {
                        double expectedBalance = getValue(row, headerIndexMap.get(resultKey));
                        if (result != expectedBalance) {
                            String calcError = resultKey + ": คาดหวัง " + result + " แต่ในไฟล์ได้ " + expectedBalance;

                            Map<String, Object> errorDetails = new HashMap<>();
                            errorDetails.put("row", row.getRowNum());
                            errorDetails.put("column", headerIndexMap.get(resultKey));
                            errorDetails.put("header", resultKey);
                            errorDetails.put("message", calcError);

                            errorList.add(errorDetails);
                            errorBuilder.append(calcError).append("; ");

                            errorSummaryMap.put(row.getRowNum() + 1, errorBuilder.toString().trim());
                            System.out.println("ข้อผิดพลาด: " + calcError);
                        }
                    }

                    if (errorBuilder.isEmpty()) {
                        rowData.put(resultKey, result);
                    }
                }
            }

            if (!errorBuilder.isEmpty()) {
                errorSummaryMap.put(row.getRowNum() + 1, errorBuilder.toString().trim());
            } else {
                resultList.add(rowData);
            }
        }

        List<String> errorSummaryList = formatErrorMessages(errorSummaryMap);

        System.out.println("Error Summary List:");
        for (String error : errorSummaryList) {
            System.out.println(error);
        }

        if (!errorList.isEmpty()) {
            Set<Map<String, Object>> uniqueErrors = new HashSet<>(errorList);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("summary", "Errors found");
            errorResponse.put("errorList", new ArrayList<>(uniqueErrors));
            errorResponse.put("errorDetails", errorSummaryList);

            return List.of(errorResponse);
        }

        return resultList.isEmpty() || resultList.stream().allMatch(Map::isEmpty) ? Collections.emptyList() : resultList;
    }

    private boolean checkRelation(String value1, String condition, String value2) {
        System.out.println("ตรวจสอบค่าในแถว: " + value1 + " และ " + value2);

        switch (condition.toLowerCase()) {
            case "notempty":
                return !value1.isEmpty() && !value2.isEmpty();  // ตรวจสอบไม่ให้ค่าว่าง
            case "exists":
                return checkExistence(value1, value2);  // ตรวจสอบว่าเป็นค่า "exists"
            default:
                throw new IllegalArgumentException("Unsupported condition: " + condition);
        }
    }

    private boolean checkExistence(String column1Value, String column2Value) {
        // ตัวอย่างการตรวจสอบว่า value2 (อำเภอ) ต้องอยู่ใน column1 (จังหวัด)
        // สมมติว่าคุณมีฐานข้อมูลจังหวัดและอำเภอที่สามารถใช้ในการตรวจสอบนี้
        // เช่น สามารถใช้ Map หรือฐานข้อมูลเพื่อเช็คว่าอำเภอนั้นๆ อยู่ในจังหวัดที่ระบุ
        Map<String, List<String>> provinceToDistrictMap = getProvinceToDistrictMap(); // สมมติว่าเป็นฐานข้อมูลของจังหวัดและอำเภอ

        // ตรวจสอบว่า value2 (อำเภอ) อยู่ในจังหวัด value1
        List<String> districts = provinceToDistrictMap.get(column1Value);
        return districts != null && districts.contains(column2Value);
    }

    private Map<String, List<String>> getProvinceToDistrictMap() {
        // ตัวอย่างการสร้างข้อมูลจังหวัดและอำเภอ
        // ควรจะดึงข้อมูลจริงจากฐานข้อมูลหรือแหล่งข้อมูลที่เหมาะสม
        Map<String, List<String>> provinceToDistrictMap = new HashMap<>();
        provinceToDistrictMap.put("อุดรธานี", Arrays.asList("เมืองอุดรธานี", "หนองหาน", "กุมภวาปี"));
        provinceToDistrictMap.put("กรุงเทพมหานคร", Arrays.asList("บางรัก", "พระนคร", "ดินแดง"));
        // เพิ่มจังหวัดและอำเภอในลักษณะนี้

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
        if (cell == null) return null;

        FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC ->
                    cell.getNumericCellValue() % 1 == 0
                            ? String.valueOf((long) cell.getNumericCellValue())
                            : String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                CellValue cellValue = evaluator.evaluate(cell);
                yield switch (cellValue.getCellType()) {
                    case STRING -> cellValue.getStringValue();
                    case NUMERIC -> String.valueOf(cellValue.getNumberValue());
                    case BOOLEAN -> String.valueOf(cellValue.getBooleanValue());
                    default -> null;
                };
            }
            default -> null;
        };
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