package com.clubloyalty.server.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public class PageResponse<T> {
    public List<T> items;
    public int page;
    public int size;
    public long totalElements;
    public int totalPages;

    public static <T> PageResponse<T> from(Page<T> page) {
        var dto = new PageResponse<T>();
        dto.items = page.getContent();
        dto.page = page.getNumber();
        dto.size = page.getSize();
        dto.totalElements = page.getTotalElements();
        dto.totalPages = page.getTotalPages();
        return dto;
    }
}
