package com.simonjoz.vetclinic.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class PageReqUtils {

    public final static String PAGE_ZERO = "0";
    public final static String DEFAULT_SORT_BY = "id";
    public final static String DEFAULT_SIZE = "10";
    public final static String DESC_FALSE = "false";


    public static PageRequest getPageRequest(int page, int size, String sortBy, boolean desc) {
        if (page < 0) {
            throw new IllegalArgumentException("Pages index must not be less than zero !");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must not be less than one!");
        }

        PageRequest pageRequest = PageRequest.of(page, size); // default sort will by performed
        if (sortBy != null && !"".equals(sortBy.trim())) {
            if (desc) {
                pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
            } else {
                pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortBy));
            }
        }
        return pageRequest;
    }
}
