package com.casrusil.siierpai.modules.inventory.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "El SKU del producto es obligatorio")
    @Size(max = 50, message = "El SKU no puede superar 50 caracteres")
    private String sku;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String name;

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String description;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio de venta debe ser mayor a 0")
    private BigDecimal salePrice;

    @Size(max = 100, message = "La categoría no puede superar 100 caracteres")
    private String category;

    @DecimalMin(value = "0.0", inclusive = true, message = "El stock mínimo no puede ser negativo")
    @Builder.Default
    private BigDecimal minStock = BigDecimal.valueOf(5);
}
