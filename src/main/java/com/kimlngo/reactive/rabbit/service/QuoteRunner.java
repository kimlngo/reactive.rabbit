package com.kimlngo.reactive.rabbit.service;

import com.kimlngo.reactive.rabbit.config.RabbitConfig;
import com.rabbitmq.client.Delivery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.rabbitmq.Receiver;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteRunner implements CommandLineRunner {

    private final QuoteMessageSender quoteMessageSender;
    private final QuoteGeneratorService quoteGeneratorService;
    private final Receiver receiver;

    @Override
    public void run(String... args) throws Exception {
        CountDownLatch latch = new CountDownLatch(25);

        quoteGeneratorService.fetchQuoteStream(Duration.ofMillis(500))
                .take(25)
                .log("Got Quote")
                .flatMap(quoteMessageSender::sendQuoteMessage)
                .subscribe(result -> {
                    log.info("Sent Message to Rabbit");
                    latch.countDown();
                }, throwable -> {
                    log.error("Error happened", throwable);
                }, () -> {
                    log.info("DONE!!!");
                });

        latch.await(1, TimeUnit.SECONDS);

        AtomicInteger receiverCount = new AtomicInteger();

        receiver.consumeAutoAck(RabbitConfig.QUEUE)
                .log("Message Received")
                .subscribe(msg -> {
                    log.info("Received msg {} - {}", receiverCount.incrementAndGet(), new String(msg.getBody()));
                }, throwable -> {
                    log.error("Error Receiving", throwable);
                }, () -> {
                    log.info("Receiving Completed");
                });
    }
}
