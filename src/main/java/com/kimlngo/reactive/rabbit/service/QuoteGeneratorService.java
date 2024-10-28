package com.kimlngo.reactive.rabbit.service;

import com.kimlngo.reactive.rabbit.model.Quote;
import reactor.core.publisher.Flux;

import java.time.Duration;

public interface QuoteGeneratorService {

    Flux<Quote> fetchQuoteStream(Duration period);
}
