package org.astu.attendancetracker.presentation.services.impl;

import org.astu.attendancetracker.core.application.schedule.ScheduleManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class PlannerServiceImpl {
    private final ScheduleManager scheduleManager;

    public PlannerServiceImpl(ScheduleManager scheduleManager) {
        this.scheduleManager = scheduleManager;
    }

    // Каждый понедельник в 4 часа утра загружается новый день недели в кэш
    @Scheduled(cron = "0 0 4 * * 1")
    @CachePut(value = "schedule", key = "'current-week-number'")
    public CompletableFuture<Integer> updateCurrentWeekNumberInCache() {
        return scheduleManager.getCurrentWeekNumber();
        // Доработать. Если сервер АГТУ недоступен, вызываем метод, очищающий кэш
        // Иначе в кэше останется старая неделя при наступившей новой
    }
}
