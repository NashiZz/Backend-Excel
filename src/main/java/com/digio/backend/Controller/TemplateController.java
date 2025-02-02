package com.digio.backend.Controller;

import com.digio.backend.DTO.TemplateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

@RestController
@RequestMapping("/api/save/templates")
public class TemplateController {

    @PostMapping
    public ResponseEntity<Object> saveTemplate(@RequestBody TemplateRequest templateRequest) {
        try {
            File templateFile = createYamlFile(templateRequest);
            saveFile(templateFile);

            return ResponseEntity.ok(new HashMap<String, String>() {{
                put("message", "Template saved successfully as YAML");
            }});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new HashMap<String, String>() {{
                        put("error", "Error saving template");
                    }});
        }
    }

    private File createYamlFile(TemplateRequest templateRequest) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        File file = new File("D:/Project/ProjectExcel/template/template.yaml");
        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(templateRequest, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private void saveFile(File file) {
        try {
            Path path = Paths.get("D:/Project/ProjectExcel/template/template.yaml");
            Files.copy(file.toPath(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}