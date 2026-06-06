package org.example.statistics.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchDownloadDTO {

    private List<Long> ids;
}
