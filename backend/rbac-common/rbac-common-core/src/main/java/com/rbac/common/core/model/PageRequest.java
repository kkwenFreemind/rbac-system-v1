package com.rbac.common.core.model;

import com.rbac.common.core.constant.CommonConstant;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Pagination request parameters.
 *
 * This class encapsulates the parameters needed for paginated queries,
 * providing validation and default values for page navigation.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
@Data
public class PageRequest {

    /**
     * Page number (1-based).
     */
    @Min(value = 1, message = "Page number must be greater than 0")
    private int pageNum = CommonConstant.DEFAULT_PAGE_NUM;

    /**
     * Number of records per page.
     */
    @Min(value = 1, message = "Page size must be greater than 0")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private int pageSize = CommonConstant.DEFAULT_PAGE_SIZE;

    /**
     * Sort field name.
     */
    private String sortField;

    /**
     * Sort direction (asc/desc).
     */
    private String sortOrder = "asc";

    /**
     * Default constructor.
     */
    public PageRequest() {
    }

    /**
     * Constructor with page number and size.
     *
     * @param pageNum page number (1-based)
     * @param pageSize number of records per page
     */
    public PageRequest(int pageNum, int pageSize) {
        this.pageNum = Math.max(1, pageNum);
        this.pageSize = Math.max(1, Math.min(pageSize, CommonConstant.MAX_PAGE_SIZE));
    }

    /**
     * Constructor with page number, size, and sorting.
     *
     * @param pageNum page number (1-based)
     * @param pageSize number of records per page
     * @param sortField sort field name
     * @param sortOrder sort direction (asc/desc)
     */
    public PageRequest(int pageNum, int pageSize, String sortField, String sortOrder) {
        this(pageNum, pageSize);
        this.sortField = sortField;
        this.sortOrder = sortOrder != null ? sortOrder.toLowerCase() : "asc";
    }

    /**
     * Create a PageRequest with default values.
     *
     * @return PageRequest instance
     */
    public static PageRequest of() {
        return new PageRequest();
    }

    /**
     * Create a PageRequest with page number and size.
     *
     * @param pageNum page number (1-based)
     * @param pageSize number of records per page
     * @return PageRequest instance
     */
    public static PageRequest of(int pageNum, int pageSize) {
        return new PageRequest(pageNum, pageSize);
    }

    /**
     * Create a PageRequest with page number, size, and sorting.
     *
     * @param pageNum page number (1-based)
     * @param pageSize number of records per page
     * @param sortField sort field name
     * @param sortOrder sort direction (asc/desc)
     * @return PageRequest instance
     */
    public static PageRequest of(int pageNum, int pageSize, String sortField, String sortOrder) {
        return new PageRequest(pageNum, pageSize, sortField, sortOrder);
    }

    /**
     * Get the offset for database queries (0-based).
     *
     * @return offset value
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }

    /**
     * Validate the sort order.
     *
     * @return true if sort order is valid, false otherwise
     */
    public boolean isValidSortOrder() {
        return "asc".equalsIgnoreCase(sortOrder) || "desc".equalsIgnoreCase(sortOrder);
    }

    /**
     * Normalize the sort order to lowercase.
     */
    public void normalizeSortOrder() {
        if (sortOrder != null) {
            sortOrder = sortOrder.toLowerCase();
        }
    }

    /**
     * Check if sorting is requested.
     *
     * @return true if sort field is specified, false otherwise
     */
    public boolean hasSorting() {
        return sortField != null && !sortField.trim().isEmpty();
    }

    /**
     * Get the validated page size (ensures it's within bounds).
     *
     * @return validated page size
     */
    public int getValidatedPageSize() {
        if (pageSize < CommonConstant.MIN_PAGE_SIZE) {
            return CommonConstant.MIN_PAGE_SIZE;
        }
        if (pageSize > CommonConstant.MAX_PAGE_SIZE) {
            return CommonConstant.MAX_PAGE_SIZE;
        }
        return pageSize;
    }

    /**
     * Get the validated page number (ensures it's positive).
     *
     * @return validated page number
     */
    public int getValidatedPageNum() {
        return Math.max(1, pageNum);
    }

    /**
     * Create a copy of this PageRequest with different page number.
     *
     * @param newPageNum new page number
     * @return new PageRequest instance
     */
    public PageRequest withPageNum(int newPageNum) {
        return new PageRequest(newPageNum, pageSize, sortField, sortOrder);
    }

    /**
     * Create a copy of this PageRequest with different page size.
     *
     * @param newPageSize new page size
     * @return new PageRequest instance
     */
    public PageRequest withPageSize(int newPageSize) {
        return new PageRequest(pageNum, newPageSize, sortField, sortOrder);
    }

    /**
     * Create a copy of this PageRequest with different sorting.
     *
     * @param newSortField new sort field
     * @param newSortOrder new sort order
     * @return new PageRequest instance
     */
    public PageRequest withSorting(String newSortField, String newSortOrder) {
        return new PageRequest(pageNum, pageSize, newSortField, newSortOrder);
    }
}