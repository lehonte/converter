package org.example.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.example.dto.ExchangeRateResponseDto;
import org.example.dto.UserDto;
import org.example.services.ExchangeRateService;
import jakarta.validation.constraints.Pattern;
import org.example.services.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Общедоступное", description = "Действия доступные для получения информации любым пользователем")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class PublicController {

    private final ExchangeRateService exchangeRateService;
    private final UserService userService;

    @Operation(summary = "Вход")
    @PostMapping("/login")
    public UserDto login(@NotBlank(message = "Логин не может быть пустым")
                                @RequestParam String username,
                         @NotBlank(message = "Пароль не может быть пустым")
                                @Size(min = 5, message = "Пароль должен содержать минимум 5 символов")
                                @RequestParam String password) {
        return userService.login(username, password);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Курс найден",
                    content = @Content(schema = @Schema(implementation = ExchangeRateResponseDto.class))),
            @ApiResponse(responseCode = "404", ref="NotFound"),
            @ApiResponse(responseCode = "400", ref="BadRequestError")
    })
    @Operation(summary = "Получить курс")
    @GetMapping("/rate")
    public ExchangeRateResponseDto getCurrencyPair(@NotBlank(message = "Код валюты не должен быть пустым")
                                                       @Pattern(regexp = "^[A-Z]{3}$", message = "Неверный тип данных")
                                                       @RequestParam String code,
                                                   @RequestParam(required = false)LocalDate date) {
        return exchangeRateService.getCurrencyPair(code, date);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Курсы найдены",
                    content = @Content(schema = @Schema(implementation = ExchangeRateResponseDto.class))),
            @ApiResponse(responseCode = "404", ref="NotFound"),
            @ApiResponse(responseCode = "400", ref="BadRequestError")
    })
    @Operation(summary = "Получить курсы")
    @GetMapping("/rates/all")
    public List<ExchangeRateResponseDto> getAllCurrencies(@RequestParam(required = false) LocalDate date) {
        return exchangeRateService.getAllCurrencies(date);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Курсы найдены",
                    content = @Content(schema = @Schema(implementation = ExchangeRateResponseDto.class))),
            @ApiResponse(responseCode = "404", ref="NotFound"),
            @ApiResponse(responseCode = "400", ref="BadRequestError")
    })
    @Operation(summary = "Получить курсы по интервалу времени")
    @GetMapping("/rates/history")
    public List<ExchangeRateResponseDto> getAllCurrenciesForTime(@NotBlank(message = "Код валюты не должен быть пустым")
                                                                     @Pattern(regexp = "^[A-Z]{3}$", message = "Неверный тип данных")
                                                                     @RequestParam String code,
                                                                 @RequestParam("from") LocalDate fromDate,
                                                                 @RequestParam("to") LocalDate toDate) {
        return exchangeRateService.getAllCurrenciesForTime(code, fromDate, toDate);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Курс найден",
                    content = @Content(schema = @Schema(implementation = ExchangeRateResponseDto.class))),
            @ApiResponse(responseCode = "404", ref="NotFound"),
            @ApiResponse(responseCode = "400", ref="BadRequestError")
    })
    @Operation(summary = "Конвертор валют")
    @GetMapping("/convert")
    public ExchangeRateResponseDto getExchangeRateBetweenTwoCurrencies(@NotBlank(message = "Код валюты не должен быть пустым")
                                                                           @Pattern(regexp = "^[A-Z]{3}$", message = "Неверный тип данных")
                                                                           @RequestParam("first") String firstCode,
                                                                       @NotBlank(message = "Код валюты не должен быть пустым")
                                                                       @Pattern(regexp = "^[A-Z]{3}$", message = "Неверный тип данных")
                                                                       @RequestParam("second") String secondCode,
                                                                       @RequestParam(required = false) LocalDate date
                                                                       ) {
        return exchangeRateService.getExchangeRateBetweenTwoCurrencies(firstCode, secondCode, date);
    }
}
