package com.asset.itassetsystem.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class BatchUpdateDTO {
    private List<Long> assetIds;
    private Map<String, Object> fields;
}
