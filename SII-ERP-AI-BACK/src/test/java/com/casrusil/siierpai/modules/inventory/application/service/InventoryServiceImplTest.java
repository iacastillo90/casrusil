package com.casrusil.siierpai.modules.inventory.application.service;

import com.casrusil.siierpai.modules.inventory.domain.model.MovementType;
import com.casrusil.siierpai.modules.inventory.domain.model.Product;
import com.casrusil.siierpai.modules.inventory.domain.model.StockMovement;
import com.casrusil.siierpai.modules.inventory.domain.port.ProductRepositoryPort;
import com.casrusil.siierpai.modules.inventory.domain.port.StockMovementRepositoryPort;
import com.casrusil.siierpai.shared.domain.event.StockMovedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio de Inventario.
 * Se verifica especialmente el algoritmo de Precio Medio Ponderado (PMP)
 * y la emisión correcta de eventos de dominio hacia la contabilidad.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private ProductRepositoryPort productRepository;

    @Mock
    private StockMovementRepositoryPort stockMovementRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .companyId("comp-1")
                .sku("SKU-100")
                .name("Producto Prueba")
                .quantity(10)
                .unitCost(new BigDecimal("1000.00")) // Costo inicial 1000
                .totalValue(new BigDecimal("10000.00"))
                .build();
    }

    @Test
    void testRegisterMovement_In_ShouldRecalculatePMP() {
        // Arrange
        // Simulamos una nueva compra de 5 unidades a 1500 cada una
        StockMovement movement = StockMovement.builder()
                .productId(1L)
                .companyId("comp-1")
                .type(MovementType.IN)
                .quantity(5)
                .unitCost(new BigDecimal("1500.00"))
                .totalCost(new BigDecimal("7500.00"))
                .movementDate(LocalDateTime.now())
                .build();

        when(productRepository.findByIdAndCompanyId(1L, "comp-1")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        // Llamamos al método bajo prueba
        Object response = inventoryService.registerMovement(movement);
        // Cast or reflection if the return type is a Response DTO

        // Assert
        // El nuevo PMP debería ser: ((10 * 1000) + (5 * 1500)) / 15 = 17500 / 15 = 1166.6667
        assertEquals(15, testProduct.getQuantity(), "La cantidad total debe ser 15");
        assertEquals(new BigDecimal("1166.6667"), testProduct.getUnitCost(), "El PMP recalculado debe ser 1166.6667");
        
        // Verificar que el evento StockMovedEvent se haya disparado para la contabilidad
        verify(eventPublisher, times(1)).publishEvent(any(StockMovedEvent.class));
    }
}
