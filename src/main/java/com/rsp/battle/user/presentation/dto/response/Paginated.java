package com.rsp.battle.user.presentation.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record Paginated<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static <T> Paginated<T> from(Page<T> page) {
        return new Paginated<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
