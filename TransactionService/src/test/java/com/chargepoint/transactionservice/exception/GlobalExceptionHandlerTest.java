package com.chargepoint.transactionservice.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandlerTest.TestExceptionController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/test")
    static class TestExceptionController {

        @PostMapping("/validation")
        public String validate(@Valid @RequestBody DummyRequest request) {
            return "OK";
        }

        @GetMapping("/exception")
        public String throwException() {
            throw new RuntimeException("Simulated failure");
        }
    }

    @Data
    static class DummyRequest {
        @NotBlank(message = "name must not be blank")
        private String name;
    }

    @Test
    void shouldReturn400ForValidationError() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("name must not be blank"));
    }

    @Test
    void shouldReturn500ForUnhandledException() throws Exception {
        mockMvc.perform(get("/test/exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Simulated failure"))
                .andExpect(jsonPath("$.details", containsString("/test/exception")));
    }
}
