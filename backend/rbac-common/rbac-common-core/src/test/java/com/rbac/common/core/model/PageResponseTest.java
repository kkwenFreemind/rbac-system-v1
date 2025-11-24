package com.rbac.common.core.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PageResponse class.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
class PageResponseTest {

    @Test
    void testDefaultConstructor() {
        // Test default constructor
        PageResponse response = new PageResponse();

        assertEquals(0, response.getTotal());
        assertEquals(1, response.getCurrent());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getPages());
        assertFalse(response.isHasNext());
        assertFalse(response.isHasPrevious());
        assertEquals(0, response.getCurrentPageSize());
    }

    @Test
    void testConstructorWithParameters() {
        // Test constructor with pagination info
        PageResponse response = new PageResponse(25, 2, 10, 10);

        assertEquals(25, response.getTotal());
        assertEquals(2, response.getCurrent());
        assertEquals(10, response.getSize());
        assertEquals(3, response.getPages());
        assertTrue(response.isHasNext());
        assertTrue(response.isHasPrevious());
        assertEquals(10, response.getCurrentPageSize());
    }

    @Test
    void testOfMethodWithTotalCurrentSize() {
        // Test static factory method
        PageResponse response = PageResponse.of(25, 2, 10, 10);

        assertEquals(25, response.getTotal());
        assertEquals(2, response.getCurrent());
        assertEquals(10, response.getSize());
        assertEquals(3, response.getPages());
        assertTrue(response.isHasNext());
        assertTrue(response.isHasPrevious());
    }

    @Test
    void testOfMethodWithRecords() {
        // Test static factory method with records
        List<String> records = Arrays.asList("item1", "item2", "item3");
        PageResponse response = PageResponse.of(records, 25, 1, 10);

        assertEquals(25, response.getTotal());
        assertEquals(1, response.getCurrent());
        assertEquals(10, response.getSize());
        assertEquals(3, response.getPages());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());
        assertEquals(3, response.getCurrentPageSize());
    }

    @Test
    void testOfMethodWithNullRecords() {
        // Test static factory method with null records
        PageResponse response = PageResponse.of(null, 25, 1, 10);

        assertEquals(25, response.getTotal());
        assertEquals(0, response.getCurrentPageSize());
    }

    @Test
    void testOfMethodWithEmptyRecords() {
        // Test static factory method with empty records
        List<String> records = Collections.emptyList();
        PageResponse response = PageResponse.of(records, 0, 1, 10);

        assertEquals(0, response.getTotal());
        assertEquals(0, response.getCurrentPageSize());
        assertTrue(response.isEmpty());
    }

    @Test
    void testFirstPage() {
        // Test first page properties
        PageResponse response = new PageResponse(25, 1, 10, 10);

        assertTrue(response.isFirst());
        assertFalse(response.isLast());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());
        assertEquals(2, response.getNextPage());
        assertEquals(1, response.getPreviousPage());
        assertEquals(1, response.getStartRecord());
        assertEquals(10, response.getEndRecord());
    }

    @Test
    void testMiddlePage() {
        // Test middle page properties
        PageResponse response = new PageResponse(25, 2, 10, 10);

        assertFalse(response.isFirst());
        assertFalse(response.isLast());
        assertTrue(response.isHasNext());
        assertTrue(response.isHasPrevious());
        assertEquals(3, response.getNextPage());
        assertEquals(1, response.getPreviousPage());
        assertEquals(11, response.getStartRecord());
        assertEquals(20, response.getEndRecord());
    }

    @Test
    void testLastPage() {
        // Test last page properties
        PageResponse response = new PageResponse(25, 3, 10, 5);

        assertFalse(response.isFirst());
        assertTrue(response.isLast());
        assertFalse(response.isHasNext());
        assertTrue(response.isHasPrevious());
        assertEquals(3, response.getNextPage());
        assertEquals(2, response.getPreviousPage());
        assertEquals(21, response.getStartRecord());
        assertEquals(25, response.getEndRecord());
    }

    @Test
    void testSinglePage() {
        // Test single page (total records fit in one page)
        PageResponse response = new PageResponse(5, 1, 10, 5);

        assertTrue(response.isFirst());
        assertTrue(response.isLast());
        assertFalse(response.isHasNext());
        assertFalse(response.isHasPrevious());
        assertEquals(1, response.getPages());
        assertEquals(1, response.getStartRecord());
        assertEquals(5, response.getEndRecord());
    }

    @Test
    void testEmptyPage() {
        // Test empty page
        PageResponse response = new PageResponse(0, 1, 10, 0);

        assertTrue(response.isEmpty());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
        assertFalse(response.isHasNext());
        assertFalse(response.isHasPrevious());
        assertEquals(1, response.getPages());
        assertEquals(0, response.getStartRecord());
        assertEquals(0, response.getEndRecord());
    }

    @Test
    void testGetSummary() {
        // Test summary string
        PageResponse response1 = new PageResponse(25, 1, 10, 10);
        assertEquals("1-10 of 25", response1.getSummary());

        PageResponse response2 = new PageResponse(25, 3, 10, 5);
        assertEquals("21-25 of 25", response2.getSummary());

        PageResponse response3 = new PageResponse(0, 1, 10, 0);
        assertEquals("0 of 0", response3.getSummary());
    }

    @Test
    void testGetNavigationInfo() {
        // Test navigation info string
        PageResponse emptyResponse = new PageResponse(0, 1, 10, 0);
        assertEquals("Page 1 of 1", emptyResponse.getNavigationInfo());

        PageResponse response3 = new PageResponse(25, 3, 10, 5);
        assertEquals("Page 3 of 3", response3.getNavigationInfo());

        PageResponse emptyResponse2 = new PageResponse(0, 1, 10, 0);
        assertEquals("Page 1 of 1", emptyResponse2.getNavigationInfo());
    }

    @Test
    void testNegativeCurrentPage() {
        // Test negative current page (should default to 1)
        PageResponse response = new PageResponse(25, -1, 10, 10);
        assertEquals(1, response.getCurrent());
    }

    @Test
    void testZeroCurrentPage() {
        // Test zero current page (should default to 1)
        PageResponse response = new PageResponse(25, 0, 10, 10);
        assertEquals(1, response.getCurrent());
    }

    @Test
    void testZeroPageSize() {
        // Test zero page size (should default to 1)
        PageResponse response = new PageResponse(25, 1, 0, 0);
        assertEquals(1, response.getSize());
    }

    @Test
    void testNegativePageSize() {
        // Test negative page size (should default to 1)
        PageResponse response = new PageResponse(25, 1, -1, 0);
        assertEquals(1, response.getSize());
    }

    @Test
    void testPageCalculation() {
        // Test page count calculation
        PageResponse response1 = new PageResponse(25, 1, 10, 10);
        assertEquals(3, response1.getPages());

        PageResponse response2 = new PageResponse(30, 1, 10, 10);
        assertEquals(3, response2.getPages());

        PageResponse response3 = new PageResponse(31, 1, 10, 10);
        assertEquals(4, response3.getPages());

        PageResponse response4 = new PageResponse(10, 1, 10, 10);
        assertEquals(1, response4.getPages());
    }

    @Test
    void testIsEmpty() {
        // Test isEmpty method
        PageResponse emptyResponse = new PageResponse(0, 1, 10, 0);
        assertTrue(emptyResponse.isEmpty());

        PageResponse nonEmptyResponse = new PageResponse(25, 1, 10, 10);
        assertFalse(nonEmptyResponse.isEmpty());
    }

    @Test
    void testIsFirst() {
        // Test isFirst method
        PageResponse firstPage = new PageResponse(25, 1, 10, 10);
        assertTrue(firstPage.isFirst());

        PageResponse secondPage = new PageResponse(25, 2, 10, 10);
        assertFalse(secondPage.isFirst());
    }

    @Test
    void testIsLast() {
        // Test isLast method
        PageResponse lastPage = new PageResponse(25, 3, 10, 5);
        assertTrue(lastPage.isLast());

        PageResponse firstPage = new PageResponse(25, 1, 10, 10);
        assertFalse(firstPage.isLast());

        PageResponse middlePage = new PageResponse(25, 2, 10, 10);
        assertFalse(middlePage.isLast());
    }

    @Test
    void testGetNextPage() {
        // Test getNextPage method
        PageResponse withNextPage = new PageResponse(25, 1, 10, 10);
        assertEquals(2, withNextPage.getNextPage());

        PageResponse lastPage = new PageResponse(25, 3, 10, 5);
        assertEquals(3, lastPage.getNextPage());
    }

    @Test
    void testGetPreviousPage() {
        // Test getPreviousPage method
        PageResponse withPreviousPage = new PageResponse(25, 2, 10, 10);
        assertEquals(1, withPreviousPage.getPreviousPage());

        PageResponse firstPage = new PageResponse(25, 1, 10, 10);
        assertEquals(1, firstPage.getPreviousPage());
    }

    @Test
    void testGetStartRecord() {
        // Test getStartRecord method
        PageResponse firstPage = new PageResponse(25, 1, 10, 10);
        assertEquals(1, firstPage.getStartRecord());

        PageResponse secondPage = new PageResponse(25, 2, 10, 10);
        assertEquals(11, secondPage.getStartRecord());

        PageResponse thirdPage = new PageResponse(25, 3, 10, 5);
        assertEquals(21, thirdPage.getStartRecord());

        PageResponse emptyPage = new PageResponse(0, 1, 10, 0);
        assertEquals(0, emptyPage.getStartRecord());
    }

    @Test
    void testGetEndRecord() {
        // Test getEndRecord method
        PageResponse firstPage = new PageResponse(25, 1, 10, 10);
        assertEquals(10, firstPage.getEndRecord());

        PageResponse secondPage = new PageResponse(25, 2, 10, 10);
        assertEquals(20, secondPage.getEndRecord());

        PageResponse lastPage = new PageResponse(25, 3, 10, 5);
        assertEquals(25, lastPage.getEndRecord());

        PageResponse emptyPage = new PageResponse(0, 1, 10, 0);
        assertEquals(0, emptyPage.getEndRecord());
    }
}
