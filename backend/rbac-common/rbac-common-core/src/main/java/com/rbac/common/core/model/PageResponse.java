package com.rbac.common.core.model;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Pagination response metadata.
 *
 * This class provides metadata about paginated query results,
 * including total counts, page information, and navigation flags.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
@Data
public class PageResponse {

    /**
     * Total number of records across all pages.
     */
    private long total = 0;

    /**
     * Current page number (1-based).
     */
    private int current = 1;

    /**
     * Number of records per page.
     */
    private int size = 10;

    /**
     * Total number of pages.
     */
    private int pages = 0;

    /**
     * Whether there are more pages after this one.
     */
    private boolean hasNext = false;

    /**
     * Whether there are pages before this one.
     */
    private boolean hasPrevious = false;

    /**
     * Number of records in the current page.
     */
    private int currentPageSize = 0;

    /**
     * Default constructor.
     */
    public PageResponse() {
    }

    /**
     * Constructor with pagination info.
     *
     * @param total total number of records
     * @param current current page number (1-based)
     * @param size page size
     * @param currentPageSize number of records in current page
     */
    public PageResponse(long total, int current, int size, int currentPageSize) {
        this.total = total;
        this.current = Math.max(1, current);
        this.size = Math.max(1, size);
        this.currentPageSize = currentPageSize;
        this.pages = calculatePages(total, size);
        this.hasNext = current < this.pages;
        this.hasPrevious = current > 1;
    }

    /**
     * Create a PageResponse from pagination parameters.
     *
     * @param total total number of records
     * @param current current page number (1-based)
     * @param size page size
     * @param currentPageSize number of records in current page
     * @return PageResponse instance
     */
    public static PageResponse of(long total, int current, int size, int currentPageSize) {
        return new PageResponse(total, current, size, currentPageSize);
    }

    /**
     * Create a PageResponse from a list and pagination info.
     *
     * @param records the list of records in current page
     * @param total total number of records
     * @param current current page number (1-based)
     * @param size page size
     * @return PageResponse instance
     */
    public static PageResponse of(List<?> records, long total, int current, int size) {
        int currentPageSize = records != null ? records.size() : 0;
        return new PageResponse(total, current, size, currentPageSize);
    }

    /**
     * Calculate total number of pages.
     *
     * @param total total number of records
     * @param size page size
     * @return total number of pages
     */
    private int calculatePages(long total, int size) {
        if (size <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / size);
    }

    /**
     * Check if this page is empty.
     *
     * @return true if no records in current page, false otherwise
     */
    public boolean isEmpty() {
        return currentPageSize == 0;
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
    public int getNextPage() {
        return hasNext ? current + 1 : current;
    }

    /**
     * Get the previous page number.
     *
     * @return previous page number, or current page if no previous page
     */
    public int getPreviousPage() {
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
        return (long) (current - 1) * size + 1;
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
        long end = (long) current * size;
        return Math.min(end, total);
    }

    /**
     * Get a summary string of the pagination info.
     *
     * @return summary string like "1-10 of 100"
     */
    public String getSummary() {
        if (total == 0) {
            return "0 of 0";
        }
        return getStartRecord() + "-" + getEndRecord() + " of " + total;
    }

    /**
     * Get navigation info as a string.
     *
     * @return navigation string like "Page 1 of 10"
     */
    public String getNavigationInfo() {
        return "Page " + current + " of " + pages;
    }
}