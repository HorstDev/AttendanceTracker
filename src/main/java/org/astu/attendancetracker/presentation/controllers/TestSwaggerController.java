package org.astu.attendancetracker.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/api/swagger-test")
public class TestSwaggerController {

    @Operation(summary = "Возвращает рандомное число в определенном диапазоне")
    @ApiResponse(responseCode = "200", description = "Рандомное число успешно вернулось")
    @GetMapping("random-in-range")
    public int returnIntInRange() {
        Random rand = new Random();
        return rand.nextInt(2, 44);
    }

    @Operation(summary = "Возвращает рандомное число")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Рандомное число успешно вернулось"),
            @ApiResponse(responseCode = "404", description = "Рандомное число не найдено")
    })
    @GetMapping("random")
    public int returnInt() {
        Random rand = new Random();
        return rand.nextInt();
    }
}
