package com.digio.backend.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class NewDataRequest {
    private String userToken;
    private String templateId;
    private String updateAt;
    private List<Map<String, Object>> records;

}

