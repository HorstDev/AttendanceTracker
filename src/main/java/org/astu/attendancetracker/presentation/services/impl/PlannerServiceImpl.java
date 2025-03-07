package org.astu.attendancetracker.presentation.services.impl;

import org.astu.attendancetracker.core.application.schedule.ScheduleManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
//todo Добавить обновление текущей недели в кэше (потому что может использоваться старый номер в кэше, тогда жопа
//@Service
//public class PlannerServiceImpl {
//    private final ScheduleManager scheduleManager;
//
//    public PlannerServiceImpl(ScheduleManager scheduleManager) {
//        this.scheduleManager = scheduleManager;
//    }
//
//    // Этот метод конфликтует с vaadin, так как в методах Schedule не должно быть возвращаемых значений и Spring об этом предупреждает
//    // А в Vaadin методам Scheduled запрещено возвращать значения
//    // Закомменчу этот метод, так как он необязателен (но, если что, можно кэшировать, не возвращая значения с помощью CacheManager)
//    // Каждый понедельник в 4 часа утра загружается новый день недели в кэш
//    @Scheduled(cron = "0 0 4 * * 1")
//    @CachePut(value = "schedule", key = "'current-week-number'")
//    public CompletableFuture<Integer> updateCurrentWeekNumberInCache() {
//        return scheduleManager.getCurrentWeekNumber();
//        // Доработать. Если сервер АГТУ недоступен, вызываем метод, очищающий кэш
//        // Иначе в кэше останется старая неделя при наступившей новой
//    }
//}
