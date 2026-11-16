package com.casrusil.siierpai.modules.accounting.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AccountNodeDto(
        String code,
        String name,
        String type,
        List<AccountNodeDto> children,
        BigDecimal balance,
        @JsonProperty("isLeaf") boolean isLeaf) {
}
