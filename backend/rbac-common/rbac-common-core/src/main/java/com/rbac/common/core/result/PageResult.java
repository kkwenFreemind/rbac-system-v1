package com.rbac.common.core.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Paginated result wrapper.
 *
 * This class provides a standardized structure for paginated API responses,
 * including pagination metadata and the actual data list.
 *
 * @param <T> the type of items in the page
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResult<T> {

    /**
     * List of items in this page.
     */
    private List<T> records = Collections.emptyList();

    /**
     * Total number of records across all pages.
     */
    private long total = 0;

    /**
     * Current page number (1-based).
     */
    private long current = 1;

    /**
     * Number of records per page.
     */
    private long size = 10;

    /**
     * Total number of pages.
     */
    private long pages = 0;

    /**
     * Whether there are more pages after this one.
     */
    private boolean hasNext = false;

    /**
     * Whether there are pages before this one.
     */
    private boolean hasPrevious = false;

    /**
     * Default constructor.
     */
    public PageResult() {
    }

    /**
     * Constructor with all fields.
     *
     * @param records list of items in this page
     * @param total total number of records
     * @param current current page number (1-based)
     * @param size page size
     */
    public PageResult(List<T> records, long total, long current, long size) {
        this.records = records != null ? records : Collections.emptyList();
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = calculatePages(total, size);
        this.hasNext = current < this.pages;
        this.hasPrevious = current > 1;
    }

    /**
     * Create a PageResult from a list and pagination info.
     *
     * @param records list of items
     * @param total total number of records
     * @param current current page number (1-based)
     * @param size page size
     * @param <T> the type of items
     * @return PageResult instance
     */
    public static <T> PageResult<T> of(List<T> records, long total, long current, long size) {
        return new PageResult<>(records, total, current, size);
    }

    /**
     * Create an empty PageResult.
     *
     * @param <T> the type of items
     * @return empty PageResult instance
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(Collections.emptyList(), 0, 1, 10);
    }

    /**
     * Create a PageResult with custom page size.
     *
     * @param records list of items
     * @param total total number of records
     * @param current current page number (1-based)
     * @param size page size
     * @param <T> the type of items
     * @return PageResult instance
     */
    public static <T> PageResult<T> of(List<T> records, long total, int current, int size) {
        return new PageResult<>(records, total, current, size);
    }

    /**
     * Calculate total number of pages.
     *
     * @param total total number of records
     * @param size page size
     * @return total number of pages
     */
    private long calculatePages(long total, long size) {
        if (size <= 0) {
            return 0;
        }
        return (total + size - 1) / size; // Ceiling division
    }

    /**
     * Get the number of records in the current page.
     *
     * @return number of records in current page
     */
    public int getCurrentPageSize() {
        return records != null ? records.size() : 0;
    }

    /**
     * Check if this page is empty.
     *
     * @return true if no records in current page, false otherwise
     */
    public boolean isEmpty() {
        return records == null || records.isEmpty();
    }

    /**
     * Check if this is the first page.
     *
     * @return true if this is the first page, false otherwise
     */
    public boolean isFirst() {
        return current == 1;
    }

    /**
     * Check if this is the last page.
     *
     * @return true if this is the last page, false otherwise
     */
    public boolean isLast() {
        return current >= pages;
    }

    /**
     * Get the next page number.
     *
     * @return next page number, or current page if no next page
     */
    public long getNextPage() {
        return hasNext ? current + 1 : current;
    }

    /**
     * Get the previous page number.
     *
     * @return previous page number, or current page if no previous page
     */
    public long getPreviousPage() {
        return hasPrevious ? current - 1 : current;
    }

    /**
     * Get the start record number (1-based) for the current page.
     *
     * @return start record number
     */
    public long getStartRecord() {
        if (current <= 0 || size <= 0) {
            return 0;
        }
        return (current - 1) * size + 1;
    }

    /**
     * Get the end record number (1-based) for the current page.
     *
     * @return end record number
     */
    public long getEndRecord() {
        if (current <= 0 || size <= 0) {
            return 0;
        }
        long end = current * size;
        return Math.min(end, total);
    }

    /**
     * Convert this PageResult to a Result wrapper.
     *
     * @return Result containing this PageResult
     */
    public Result<PageResult<T>> toResult() {
        return Result.success(this);
    }

    /**
     * Convert this PageResult to a Result wrapper with custom message.
     *
     * @param message the success message
     * @return Result containing this PageResult
     */
    public Result<PageResult<T>> toResult(String message) {
        return Result.success(this, message);
    }
}