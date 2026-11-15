package com.casrusil.siierpai.modules.financial_shield.domain.port.out;

import java.util.Optional;

public interface GreenSupplierRepository {
    Optional<String> findGreenAlternativeByCategory(String category);
}
