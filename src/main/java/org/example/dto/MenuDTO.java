package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class MenuDTO {
    private Long id;
    private String name;
    private String path;
    private Long parentId;
}
