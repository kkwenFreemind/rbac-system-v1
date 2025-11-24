package com.rbac.common.web.handler;

import com.rbac.common.core.exception.BusinessException;
import com.rbac.common.core.exception.RbacException;
import com.rbac.common.core.result.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void handleRbacException_ShouldReturnErrorResult() throws Exception {
        mockMvc.perform(get("/test/rbac-exception")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_ERROR"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void handleException_ShouldReturnInternalServerError() throws Exception {
        mockMvc.perform(get("/test/exception")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    @RestController
    static class TestController {
        @GetMapping("/test/rbac-exception")
        public void throwRbacException() {
            throw new BusinessException("BUSINESS_ERROR", "User not found");
        }

        @GetMapping("/test/exception")
        public void throwException() {
            throw new RuntimeException("Unexpected error");
        }
    }
}
