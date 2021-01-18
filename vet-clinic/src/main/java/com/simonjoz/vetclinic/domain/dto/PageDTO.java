package com.simonjoz.vetclinic.domain.dto;

import lombok.Value;

import java.util.List;

@Value
public class PageDTO<T> {
    int totalPages;
    long totalElements;
    boolean first;
    boolean last;
    boolean empty;
    List<T> content;
}
