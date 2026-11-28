package com.casrusil.siierpai.shared.infrastructure.web;

import java.util.UUID;

/** Ids de correlación cortos para cruzar un error mostrado al usuario con el log. */
public final class CorrelationIds {

    private CorrelationIds() {
    }

    public static String newId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
