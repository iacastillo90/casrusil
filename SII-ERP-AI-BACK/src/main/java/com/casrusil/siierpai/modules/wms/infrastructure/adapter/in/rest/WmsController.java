package com.casrusil.siierpai.modules.wms.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.wms.application.service.WmsServiceImpl;
import com.casrusil.siierpai.modules.wms.domain.model.PickingTask;
import com.casrusil.siierpai.modules.wms.domain.model.WarehouseLocation;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wms")
@PreAuthorize("hasAnyRole('ADMIN','OPERATIONS')")
public class WmsController {
    
    private final WmsServiceImpl wmsService;

    public WmsController(WmsServiceImpl wmsService) {
        this.wmsService = wmsService;
    }

    @PostMapping("/locations")
    public ResponseEntity<WarehouseLocation> createLocation(@Valid @RequestBody LocationReq req) {
        return ResponseEntity.ok(wmsService.createLocation(CompanyContext.requireCompanyId(), req.code(), req.description(), req.zone()));
    }

    @GetMapping("/locations")
    public ResponseEntity<List<WarehouseLocation>> getLocations() {
        return ResponseEntity.ok(wmsService.getLocations(CompanyContext.requireCompanyId()));
    }

    @PostMapping("/picking")
    public ResponseEntity<PickingTask> createPickingTask(@Valid @RequestBody PickingReq req) {
        return ResponseEntity.ok(wmsService.createPickingTask(CompanyContext.requireCompanyId(), req.orderId(), req.items()));
    }

    @GetMapping("/picking")
    public ResponseEntity<List<PickingTask>> getPickingTasks() {
        return ResponseEntity.ok(wmsService.getPickingTasks(CompanyContext.requireCompanyId()));
    }

    @PostMapping("/picking/{id}/complete")
    public ResponseEntity<Void> completePickingTask(@PathVariable UUID id) {
        wmsService.completePickingTask(CompanyContext.requireCompanyId(), id);
        return ResponseEntity.ok().build();
    }

    public record LocationReq(String code, String description, String zone) {}
    public record PickingReq(String orderId, List<PickingTask.PickingItem> items) {}
}