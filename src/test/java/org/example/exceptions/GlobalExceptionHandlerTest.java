package org.example.exceptions;

import org.example.controllers.PublicController;
import org.example.services.ExchangeRateService;
import org.example.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.eq;

@WebMvcTest(PublicController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExchangeRateService exchangeRateService;

    @MockitoBean
    private UserService userService;

    @Test
    void catchAllGlobalExceptionHandlerTest() throws Exception {
        when(exchangeRateService.getCurrencyPair(eq("USD"), any()))
                        .thenThrow(new RuntimeException("чет не так"));

        mockMvc.perform(get("/api/v1/rate")
                        .param("code", "USD"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("Проблемы на сервере"));

    }
}
