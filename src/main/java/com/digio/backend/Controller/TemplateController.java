package com.digio.backend.Controller;

import com.digio.backend.DTO.TemplateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.DumperOptions;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/api/save/templates")
public class TemplateController {

    private static final String FILE_PATH = "/Users/prasopchocksancharoen/Documents/Intern-Poonsap/ProjectExcel/backend/template/template.yaml";

    @PostMapping
    public ResponseEntity<Object> saveTemplate(@RequestBody TemplateRequest templateRequest) {
        System.out.println("📌 Mapped Request: " + templateRequest);

        if (templateRequest.getCondition() == null) {
            System.out.println("❌ Condition is NULL!");
            templateRequest.setCondition(new TemplateRequest.Condition());
        }
        if (templateRequest.getHeaders() == null) {
            System.out.println("❌ Headers are NULL!");
            templateRequest.setHeaders(new ArrayList<>());
        }

        try {
            File templateFile = createYamlFile(templateRequest);
            return ResponseEntity.ok(Map.of("message", "✅ Template saved successfully as YAML"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "❌ Error saving template", "details", e.getMessage()));
        }
    }

    private File createYamlFile(TemplateRequest templateRequest) throws IOException {
        File file = new File(FILE_PATH);
        Map<String, Map<String, List<Map<String, Object>>>> yamlData = new HashMap<>();

        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setAllowRecursiveKeys(true);

        if (file.exists() && file.length() > 0) {
            try (FileReader reader = new FileReader(file)) {
                Yaml yaml = new Yaml(new Constructor(Map.class, loaderOptions));
                Object loadedData = yaml.load(reader);

                if (loadedData instanceof Map) {
                    yamlData = (Map<String, Map<String, List<Map<String, Object>>>>) loadedData;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Error reading YAML file: " + e.getMessage());
                yamlData = new HashMap<>();
            }
        }

        String userToken = templateRequest.getUserToken();
        yamlData.putIfAbsent(userToken, new HashMap<>());
        yamlData.get(userToken).putIfAbsent("templates", new ArrayList<>());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> templateMap = objectMapper.convertValue(templateRequest, Map.class);

        List<Map<String, Object>> templates = yamlData.get(userToken).get("templates");

        boolean updated = false;
        for (int i = 0; i < templates.size(); i++) {
            Map<String, Object> existingTemplate = templates.get(i);
            if (existingTemplate.get("templatename").equals(templateRequest.getTemplatename())) {
                templates.set(i, templateMap);
                updated = true;
                break;
            }
        }

        if (!updated) {
            templates.add(templateMap);
        }

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setExplicitStart(true);

        Yaml yaml = new Yaml(options);
        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(yamlData, writer);
        }

        return file;
    }
}