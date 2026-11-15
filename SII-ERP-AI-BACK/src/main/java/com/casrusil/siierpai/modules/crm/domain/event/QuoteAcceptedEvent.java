package com.casrusil.siierpai.modules.crm.domain.event;

import com.casrusil.siierpai.modules.crm.domain.model.Quote;
import org.springframework.context.ApplicationEvent;

public class QuoteAcceptedEvent extends ApplicationEvent {
    private final Quote quote;

    public QuoteAcceptedEvent(Object source, Quote quote) {
        super(source);
        this.quote = quote;
    }

    public Quote getQuote() {
        return quote;
    }
}
