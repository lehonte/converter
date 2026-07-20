package org.example.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.services.ExchangeRateService;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Администрирование", description = "Загрузка курсов из НБРБ")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ExchangeRateService exchangeRateService;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Курсы успешно загружены и сохранены"),
            @ApiResponse(responseCode = "500", description = "Ошибка при загрузке курсов",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Загрузить курсы из НБРБ",
            description = "Ручной запуск загрузки актуальных курсов валют из API НБРБ и сохранения их в базу данных. Обычно выполняется автоматически по расписанию.")
    @PostMapping("/rates/upload")
    public void dataLoading() {
        exchangeRateService.dataLoading();
    }

}
