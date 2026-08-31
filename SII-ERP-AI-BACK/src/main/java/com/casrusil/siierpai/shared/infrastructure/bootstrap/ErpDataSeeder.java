package com.casrusil.siierpai.shared.infrastructure.bootstrap;

import com.casrusil.siierpai.modules.dms.application.service.DmsServiceImpl;
import com.casrusil.siierpai.modules.fixed_assets.application.service.FixedAssetServiceImpl;
import com.casrusil.siierpai.modules.mrp.application.service.MrpServiceImpl;
import com.casrusil.siierpai.modules.mrp.domain.model.BillOfMaterials;
import com.casrusil.siierpai.modules.wms.application.service.WmsServiceImpl;
import com.casrusil.siierpai.modules.wms.domain.model.PickingTask;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;


import com.casrusil.siierpai.modules.compliance.infrastructure.persistence.entity.TaxParameterEntity;
import com.casrusil.siierpai.modules.compliance.infrastructure.persistence.repository.TaxParameterJpaRepository;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.List;

@Component
public class ErpDataSeeder {

    private final MrpServiceImpl mrpService;
    private final FixedAssetServiceImpl faService;
    private final WmsServiceImpl wmsService;
        private final DmsServiceImpl dmsService;
    private final TaxParameterJpaRepository taxRepo;

    

    public ErpDataSeeder(MrpServiceImpl mrpService, FixedAssetServiceImpl faService, WmsServiceImpl wmsService, DmsServiceImpl dmsService, TaxParameterJpaRepository taxRepo) {
        this.mrpService = mrpService;
        this.faService = faService;
        this.wmsService = wmsService;
                this.dmsService = dmsService;
        this.taxRepo = taxRepo;
    }


    @PostConstruct
    public void seed() {
        CompanyId compId = CompanyId.fromString("00000000-0000-0000-0000-000000000001"); // Seed company

        try {

            // Tax Parameters 2024
            if (taxRepo.count() == 0) {
                TaxParameterEntity p = new TaxParameterEntity();
                p.setId("YEAR_2024");
                p.setUtmValue(new BigDecimal("64793"));
                p.setUfValue(new BigDecimal("36900"));
                p.setBheRetentionRate(new BigDecimal("13.75"));
                taxRepo.save(p);
            }

            // MRP
            if (mrpService.getBoms(compId).isEmpty()) {
                var bom1 = mrpService.createBom(compId, "PROD-MESA", "Mesa de Roble", List.of(
                        new BillOfMaterials.BomItem("MAT-MADERA", new BigDecimal("5.0")),
                        new BillOfMaterials.BomItem("MAT-CLAVOS", new BigDecimal("20.0"))
                ));
                mrpService.createWorkOrder(compId, bom1.id(), new BigDecimal("10"));
            }

            // Fixed Assets
            if (faService.getAssets(compId).isEmpty()) {
                faService.registerAsset(compId, "Camioneta Reparto Ford", "VEHICLE", new BigDecimal("15000000"), LocalDate.now().minusMonths(6));
                faService.registerAsset(compId, "Servidor Dell", "IT", new BigDecimal("3500000"), LocalDate.now().minusMonths(2));
            }

            // WMS
            if (wmsService.getLocations(compId).isEmpty()) {
                wmsService.createLocation(compId, "PAS-A-01", "Pasillo A Estante 1", "SECO");
                wmsService.createLocation(compId, "PAS-F-01", "Cámara Frigorífica 1", "FRIO");
                
                wmsService.createPickingTask(compId, "PED-9991", List.of(
                        new PickingTask.PickingItem("PROD-MANZANA", 50, "PAS-F-01"),
                        new PickingTask.PickingItem("PROD-HARINA", 20, "PAS-A-01")
                ));
            }

            

            // DMS
            if (dmsService.getDocuments(compId).isEmpty()) {
                dmsService.uploadDocument(compId, "Contrato Arriendo Local Centro", "CONTRACT", "https://s3.aws.com/doc.pdf");
                dmsService.uploadDocument(compId, "Poder Notarial Representante", "DEED", "https://s3.aws.com/poder.pdf");
            }
        } catch (Exception e) {
            System.err.println("Seeding error: " + e.getMessage());
        }
    }
}