package org.xinyo.subtitle.entity;

import lombok.Data;

@Data
public class UploadFile {
    private Long fileId;
    private String sourceName;
    private String fileName;
    private String fullPath;
    private String businessId;
}
