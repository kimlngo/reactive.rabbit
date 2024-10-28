package com.kimlngo.reactive.rabbit.service;

import com.kimlngo.reactive.rabbit.model.Quote;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

@Service
public class QuoteGeneratorServiceImpl implements QuoteGeneratorService {

    private final MathContext mathContext = new MathContext(2);
    private final Random random = new Random();
    private final List<Quote> quotes = new ArrayList<>();

    public QuoteGeneratorServiceImpl() {
        this.quotes.add(new Quote("AAPL", 231.41));
        this.quotes.add(new Quote("TSLA", 269.19));
        this.quotes.add(new Quote("NFLX", 754.68));
        this.quotes.add(new Quote("M", 15.39));
        this.quotes.add(new Quote("F", 11.07));
        this.quotes.add(new Quote("UAL", 74.64));
        this.quotes.add(new Quote("DAL", 54.12));
        this.quotes.add(new Quote("AAL", 13.15));
    }

    @Override
    public Flux<Quote> fetchQuoteStream(Duration period) {
        return Flux.generate(() -> 0, (BiFunction<Integer, SynchronousSink<Quote>, Integer>) (index, sink) -> {
                       Quote updatedQuote = updateQuote(this.quotes.get(index));
                       sink.next(updatedQuote);
                       return ++index % this.quotes.size();
                   })
                   .zipWith(Flux.interval(period))
                   .map(t -> t.getT1())
                   .map(quote -> {
                       quote.setInstant(Instant.now());
                       return quote;
                   })
                   .log("QuoteGeneratorService");
    }

    private Quote updateQuote(Quote quote) {
        BigDecimal priceChange = quote.getPrice()
                                      .multiply(new BigDecimal(0.05 * this.random.nextDouble()), this.mathContext);
        return new Quote(quote.getTicker(), quote.getPrice()
                                                 .add(priceChange));
    }
}
