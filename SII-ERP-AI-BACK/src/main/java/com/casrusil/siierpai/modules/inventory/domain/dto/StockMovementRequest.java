package com.casrusil.siierpai.modules.inventory.domain.dto;

import com.casrusil.siierpai.modules.inventory.domain.model.MovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class StockMovementRequest {

    private Long productId;

    @NotNull(message = "El tipo de movimiento es obligatorio (ENTRADA, SALIDA, AJUSTE)")
    private MovementType type;

    @NotNull(message = "La cantidad del movimiento es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La cantidad debe ser mayor a 0")
    private BigDecimal quantity;

    @DecimalMin(value = "0.0", message = "El costo unitario no puede ser negativo")
    private BigDecimal unitCost;

    private LocalDateTime movementDate;

    @Size(max = 100, message = "El documento de referencia no puede superar 100 caracteres")
    private String referenceDocument;

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String description;
}
