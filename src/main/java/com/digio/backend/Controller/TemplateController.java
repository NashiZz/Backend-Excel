package com.digio.backend.Controller;

import com.digio.backend.DTO.TemplateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/api/save/templates")
public class TemplateController {

    // D:/Project/ProjectExcel/template/template.yaml
    // /Users/prasopchocksancharoen/Documents/Intern-Poonsap/ProjectExcel/backend/template/template.yaml
    private static final String FILE_PATH = "src/main/resources/template/template.yaml";

    @GetMapping("/{userToken}")
    public ResponseEntity<Object> getTemplatesByUserToken(@PathVariable String userToken) {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "❌ No templates found for user " + userToken));
        }

        Map<String, Map<String, List<Map<String, Object>>>> yamlData;
        try (FileReader reader = new FileReader(file)) {
            Yaml yaml = new Yaml(new Constructor(HashMap.class, new LoaderOptions()));
            Object loadedData = yaml.load(reader);

            yamlData = (loadedData instanceof Map)
                    ? (Map<String, Map<String, List<Map<String, Object>>>>) loadedData
                    : new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "❌ Error reading templates", "details", e.getMessage()));
        }

        Map<String, List<Map<String, Object>>> userTemplates = yamlData.get(userToken);
        if (userTemplates == null || userTemplates.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "❌ No templates found for user " + userToken));
        }

        return ResponseEntity.ok(userTemplates);
    }

    @DeleteMapping("/{userToken}/{templateName}")
    public ResponseEntity<Object> deleteTemplate(@PathVariable String userToken, @PathVariable String templateName) {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "❌ No templates found"));
        }

        Map<String, Map<String, List<Map<String, Object>>>> yamlData;
        try (FileReader reader = new FileReader(file)) {
            Yaml yaml = new Yaml(new Constructor(HashMap.class, new LoaderOptions()));
            Object loadedData = yaml.load(reader);
            yamlData = (loadedData instanceof Map) ? (Map<String, Map<String, List<Map<String, Object>>>>) loadedData : new HashMap<>();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "❌ Error reading templates", "details", e.getMessage()));
        }

        if (!yamlData.containsKey(userToken) || yamlData.get(userToken).get("templates") == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "❌ No templates found for user " + userToken));
        }

        List<Map<String, Object>> templates = yamlData.get(userToken).get("templates");
        boolean removed = templates.removeIf(t -> templateName.equals(t.get("templatename")));

        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "❌ Template not found"));
        }

        saveYamlFile(yamlData);
        return ResponseEntity.ok(Map.of("message", "✅ Template deleted successfully"));
    }

    @PutMapping("/{userToken}/{templateName}")
    public ResponseEntity<Object> updateTemplate(@PathVariable String userToken, @PathVariable String templateName,
                                                 @RequestBody TemplateRequest templateRequest) {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "❌ No templates found"));
        }

        Map<String, Map<String, List<Map<String, Object>>>> yamlData;
        try (FileReader reader = new FileReader(file)) {
            Yaml yaml = new Yaml(new Constructor(HashMap.class, new LoaderOptions()));
            Object loadedData = yaml.load(reader);
            yamlData = (loadedData instanceof Map) ? (Map<String, Map<String, List<Map<String, Object>>>>) loadedData : new HashMap<>();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "❌ Error reading templates", "details", e.getMessage()));
        }

        if (!yamlData.containsKey(userToken) || yamlData.get(userToken).get("templates") == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "❌ No templates found for user " + userToken));
        }

        List<Map<String, Object>> templates = yamlData.get(userToken).get("templates");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> updatedTemplate = objectMapper.convertValue(templateRequest, Map.class);

        boolean updated = false;
        for (int i = 0; i < templates.size(); i++) {
            if (templateName.equals(templates.get(i).get("templatename"))) {
                templates.set(i, updatedTemplate);
                updated = true;
                break;
            }
        }

        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "❌ Template not found"));
        }

        saveYamlFile(yamlData);
        return ResponseEntity.ok(Map.of("message", "✅ Template updated successfully"));
    }

    private void saveYamlFile(Map<String, Map<String, List<Map<String, Object>>>> yamlData) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setExplicitStart(true);

        Representer representer = new Representer(options);
        representer.addClassTag(Map.class, Tag.MAP);

        Yaml yaml = new Yaml(representer, options);
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            yaml.dump(yamlData, writer);
        } catch (IOException e) {
            throw new RuntimeException("❌ Error saving YAML file", e);
        }
    }

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
        loaderOptions.setAllowRecursiveKeys(false);

        if (file.exists() && file.length() > 0) {
            try (FileReader reader = new FileReader(file)) {
                Yaml yaml = new Yaml(new Constructor(HashMap.class, loaderOptions));
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

        Representer representer = new Representer(options);
        representer.addClassTag(Map.class, Tag.MAP);

        Yaml yaml = new Yaml(representer, options);
        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(yamlData, writer);
        }

        return file;
    }
}