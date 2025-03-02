package org.astu.attendancetracker.presentation.services.impl;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmailSenderService {

    private final RabbitTemplate rabbitTemplate;

    public EmailSenderService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

//    // Для теста
////    @Scheduled(fixedRate = 20000)
//    public void sendEmailToQueue() {
//        for (int i = 0; i < 10; i++) {
//            String email = UUID.randomUUID() + "@gmail.com";
//            System.out.println("Отправка почты в очередь: " + email);
//            // Помещаем почту в очередь
//            rabbitTemplate.convertAndSend("email_exchange", "email_routing", email);
//        }
//    }

//    // Слушатель очереди
//    @RabbitListener(queues = "email_queue")
//    public void receiveEmailFromQueue(String email) {
//        try {
//            System.out.println("Из очереди получен email: " + email);
//        } catch(AmqpException ex) {
//            System.out.println(ex.getMessage());
//        }
//    }
}
