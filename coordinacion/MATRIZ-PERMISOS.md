# Matriz de permisos rol × endpoint

> Generada desde el código en `test/flyway-y-permisos` (base `integracion`, 51e8335).
> Fuente: anotaciones `@PreAuthorize` efectivas (método sobre clase) y `SecurityConfig`;
> el test `PermissionMatrixTest` recorre los handlers reales con MockMvc y los 8 roles del enum.

**Leyenda:** `P` = público; `A` = autenticado sin rol específico; `S` = permitido; `—` = denegado.

**Roles del dominio:** ADMIN, ACCOUNTANT, TREASURER, HR, SALES, OPERATIONS, AUDITOR, USER.
`B2B_CLIENT` y `TECHNICIAN` aparecen en `@PreAuthorize` pero **no existen en `UserRole`**; se muestran en la matriz estática y quedan como dudas para el dueño (no se pueden probar con token real).

**Endpoints públicos permitidos:** `POST /api/v1/auth/login`, `POST /api/v1/auth/register`, `POST /api/v1/auth/logout`;
`GET /api/v1/auth/me` exige autenticación. Actuator: solo `health/**` e `info`.

| # | Método | Endpoint | Controller:línea | ADMIN | ACCOUNTANT | TREASURER | HR | SALES | OPERATIONS | AUDITOR | USER | B2B_CLIENT | TECHNICIAN |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | GET | `/api/v1/accounting/accounts` | `AccountingController.java:215` | S | S | — | — | — | — | — | — | — | — |
| 2 | POST | `/api/v1/accounting/accounts` | `AccountingController.java:202` | S | S | — | — | — | — | — | — | — | — |
| 3 | GET | `/api/v1/accounting/accounts/tree` | `AccountingController.java:152` | S | S | — | — | — | — | — | — | — | — |
| 4 | GET | `/api/v1/accounting/audit/sii-mirror` | `SiiAuditController.java:22` | S | S | — | — | — | — | S | — | — | — |
| 5 | POST | `/api/v1/accounting/audit/sii-sync` | `SiiAuditController.java:30` | S | S | — | — | — | — | S | — | — | — |
| 6 | GET | `/api/v1/accounting/balance-sheet` | `AccountingController.java:221` | S | S | — | — | — | — | — | — | — | — |
| 7 | POST | `/api/v1/accounting/cleanup/november-rcv` | `DataCleanupController.java:33` | S | — | — | — | — | — | — | — | — | — |
| 8 | GET | `/api/v1/accounting/entries` | `AccountingController.java:132` | S | S | — | — | — | — | — | — | — | — |
| 9 | GET | `/api/v1/accounting/entries/by-invoice/{invoiceId}` | `AccountingController.java:138` | S | S | — | — | S | — | — | — | — | — |
| 10 | GET | `/api/v1/accounting/f29` | `AccountingController.java:231` | S | S | — | — | — | — | — | — | — | — |
| 11 | GET | `/api/v1/accounting/ledger/partners` | `PartnerLedgerController.java:26` | S | S | — | — | — | — | — | — | — | — |
| 12 | GET | `/api/v1/accounting/ledger/partners/{rut}/movements` | `PartnerLedgerController.java:33` | S | S | — | — | — | — | — | — | — | — |
| 13 | GET | `/api/v1/accounting/ledger/{accountCode}` | `AccountingController.java:191` | S | S | — | — | — | — | — | — | — | — |
| 14 | POST | `/api/v1/accounting/opening-balance` | `AccountingController.java:124` | S | S | — | — | — | — | — | — | — | — |
| 15 | POST | `/api/v1/accounting/opening-balance/upload` | `AccountingController.java:108` | S | S | — | — | — | — | — | — | — | — |
| 16 | POST | `/api/v1/accounting/periods/close` | `AccountingController.java:241` | S | S | — | — | — | — | — | — | — | — |
| 17 | GET | `/api/v1/accounting/periods/closed` | `AccountingController.java:252` | S | S | — | — | — | — | — | — | — | — |
| 18 | GET | `/api/v1/accounting/periods/{period}/status` | `AccountingController.java:259` | S | S | — | — | — | — | — | — | — | — |
| 19 | GET | `/api/v1/accounting/reports/income-statement` | `AccountingController.java:94` | S | S | — | — | — | — | — | — | — | — |
| 20 | POST | `/api/v1/ai/conversations` | `AiAssistantController.java:48` | S | S | S | — | S | — | S | — | — | — |
| 21 | POST | `/api/v1/ai/conversations/{conversationId}/messages` | `AiAssistantController.java:57` | S | S | S | — | S | — | S | — | — | — |
| 22 | POST | `/api/v1/ai/knowledge` | `KnowledgeBaseController.java:52` | S | — | — | — | — | — | — | — | — | — |
| 23 | GET | `/api/v1/ai/knowledge/search` | `KnowledgeBaseController.java:89` | S | — | — | — | — | — | — | — | — | — |
| 24 | POST | `/api/v1/annual-close/f22/calculate/{fiscalYear}` | `AnnualCloseController.java:19` | S | S | — | — | — | — | — | — | — | — |
| 25 | GET | `/api/v1/audit/alerts` | `AuditController.java:38` | S | — | — | — | — | — | S | — | — | — |
| 26 | GET | `/api/v1/audit/duplicates` | `AuditController.java:74` | S | — | — | — | — | — | S | — | — | — |
| 27 | GET | `/api/v1/audit/report` | `AuditController.java:133` | S | — | — | — | — | — | S | — | — | — |
| 28 | GET | `/api/v1/audit/suspicious` | `AuditController.java:85` | S | — | — | — | — | — | S | — | — | — |
| 29 | POST | `/api/v1/auth/login` | `AuthController.java:155` | P | P | P | P | P | P | P | P | P | P |
| 30 | POST | `/api/v1/auth/logout` | `AuthController.java:169` | P | P | P | P | P | P | P | P | P | P |
| 31 | GET | `/api/v1/auth/me` | `AuthController.java:102` | A | A | A | A | A | A | A | A | A | A |
| 32 | POST | `/api/v1/auth/register` | `AuthController.java:139` | P | P | P | P | P | P | P | P | P | P |
| 33 | POST | `/api/v1/b2b/orders` | `CustomerOrderController.java:47` | S | — | — | — | — | — | — | — | S | — |
| 34 | GET | `/api/v1/b2b/orders/my-orders` | `CustomerOrderController.java:35` | S | — | — | — | — | — | — | — | S | — |
| 35 | POST | `/api/v1/b2b/orders/{id}/pay` | `CustomerOrderController.java:62` | S | — | — | — | — | — | — | — | S | — |
| 36 | GET | `/api/v1/banking/connections` | `BankingSyncController.java:28` | S | — | S | — | — | — | — | — | — | — |
| 37 | POST | `/api/v1/banking/connections` | `BankingSyncController.java:34` | S | — | S | — | — | — | — | — | — | — |
| 38 | POST | `/api/v1/banking/sync` | `BankingSyncController.java:40` | S | — | S | — | — | — | — | — | — | — |
| 39 | GET | `/api/v1/banking/transactions` | `BankingSyncController.java:47` | S | — | S | — | — | — | — | — | — | — |
| 40 | POST | `/api/v1/banking/transactions/{id}/ignore` | `BankingSyncController.java:59` | S | — | S | — | — | — | — | — | — | — |
| 41 | POST | `/api/v1/banking/transactions/{id}/reconcile` | `BankingSyncController.java:53` | S | — | S | — | — | — | — | — | — | — |
| 42 | POST | `/api/v1/company/onboarding` | `CompanyOnboardingController.java:46` | S | — | — | — | — | — | — | — | — | — |
| 43 | POST | `/api/v1/company/onboarding/certificate` | `CompanyOnboardingController.java:63` | S | — | — | — | — | — | — | — | — | — |
| 44 | GET | `/api/v1/company/profile` | `CompanyController.java:62` | S | — | — | — | — | — | — | — | — | — |
| 45 | PUT | `/api/v1/company/profile` | `CompanyController.java:42` | S | — | — | — | — | — | — | — | — | — |
| 46 | GET | `/api/v1/company/users` | `CompanyController.java:72` | S | — | — | — | — | — | — | — | — | — |
| 47 | GET | `/api/v1/compliance/dashboard` | `ComplianceController.java:79` | S | S | — | — | — | — | — | — | — | — |
| 48 | GET | `/api/v1/compliance/f29/alerts` | `F29AlertController.java:32` | S | S | — | — | — | — | — | — | — | — |
| 49 | GET | `/api/v1/crm/leads` | `CrmController.java:35` | S | — | — | — | S | — | — | — | — | — |
| 50 | POST | `/api/v1/crm/leads` | `CrmController.java:41` | S | — | — | — | S | — | — | — | — | — |
| 51 | PUT | `/api/v1/crm/leads/{id}/status` | `CrmController.java:48` | S | — | — | — | S | — | — | — | — | — |
| 52 | GET | `/api/v1/crm/quotes` | `CrmController.java:56` | S | — | — | — | S | — | — | — | — | — |
| 53 | POST | `/api/v1/crm/quotes` | `CrmController.java:62` | S | — | — | — | S | — | — | — | — | — |
| 54 | POST | `/api/v1/crm/quotes/{id}/accept` | `CrmController.java:82` | S | — | — | — | S | — | — | — | — | — |
| 55 | POST | `/api/v1/crm/quotes/{id}/reject` | `CrmController.java:87` | S | — | — | — | S | — | — | — | — | — |
| 56 | GET | `/api/v1/dashboard/alerts` | `DashboardController.java:71` | S | — | — | — | — | S | — | — | — | — |
| 57 | POST | `/api/v1/dashboard/alerts` | `DashboardController.java:119` | S | — | — | — | — | S | — | — | — | — |
| 58 | GET | `/api/v1/dashboard/alerts/severity/{severity}` | `DashboardController.java:95` | S | — | — | — | — | S | — | — | — | — |
| 59 | GET | `/api/v1/dashboard/alerts/station/{stationId}` | `DashboardController.java:107` | S | — | — | — | — | S | — | — | — | — |
| 60 | GET | `/api/v1/dashboard/alerts/statistics` | `DashboardController.java:172` | S | — | — | — | — | S | — | — | — | — |
| 61 | GET | `/api/v1/dashboard/alerts/unacknowledged` | `DashboardController.java:83` | S | — | — | — | — | S | — | — | — | — |
| 62 | PUT | `/api/v1/dashboard/alerts/{id}/acknowledge` | `DashboardController.java:143` | S | — | — | — | — | S | — | — | — | — |
| 63 | PUT | `/api/v1/dashboard/alerts/{id}/resolve` | `DashboardController.java:159` | S | — | — | — | — | S | — | — | — | — |
| 64 | GET | `/api/v1/dashboard/metrics` | `DashboardController.java:44` | S | — | — | — | — | S | — | — | — | — |
| 65 | GET | `/api/v1/dashboard/metrics/{metricType}` | `DashboardController.java:54` | S | — | — | — | — | S | — | — | — | — |
| 66 | GET | `/api/v1/dashboard/recent-activity` | `RecentActivityController.java:40` | S | S | — | — | S | S | — | — | — | — |
| 67 | GET | `/api/v1/dms` | `DmsController.java:55` | S | S | — | — | — | — | — | — | — | — |
| 68 | POST | `/api/v1/dms` | `DmsController.java:30` | S | S | — | — | — | — | — | — | — | — |
| 69 | POST | `/api/v1/dms/ai-invoicing/process` | `AiInvoicingController.java:27` | S | S | — | — | — | — | — | — | — | — |
| 70 | POST | `/api/v1/dms/{id}/analyze` | `DmsController.java:60` | S | S | — | — | — | — | — | — | — | — |
| 71 | GET | `/api/v1/energy/alerts` | `EnergyController.java:80` | S | — | — | — | — | S | — | — | — | S |
| 72 | GET | `/api/v1/energy/charges` | `EnergyController.java:94` | S | — | — | — | — | S | — | — | — | S |
| 73 | GET | `/api/v1/energy/metrics` | `EnergyController.java:57` | S | — | — | — | — | S | — | — | — | S |
| 74 | GET | `/api/v1/energy/reports/energy` | `EnergyReportingController.java:59` | S | — | — | — | — | S | S | — | — | — |
| 75 | GET | `/api/v1/energy/reports/energy/daily` | `EnergyReportingController.java:72` | S | — | — | — | — | S | S | — | — | — |
| 76 | GET | `/api/v1/energy/reports/energy/export/csv` | `EnergyReportingController.java:97` | S | — | — | — | — | S | S | — | — | — |
| 77 | GET | `/api/v1/energy/reports/energy/monthly` | `EnergyReportingController.java:84` | S | — | — | — | — | S | S | — | — | — |
| 78 | GET | `/api/v1/energy/reports/forecast` | `EnergyReportingController.java:183` | S | — | — | — | — | S | S | — | — | — |
| 79 | GET | `/api/v1/energy/reports/forecast/capacity-plan` | `EnergyReportingController.java:201` | S | — | — | — | — | S | S | — | — | — |
| 80 | GET | `/api/v1/energy/reports/forecast/load-balancing` | `EnergyReportingController.java:219` | S | — | — | — | — | S | S | — | — | — |
| 81 | GET | `/api/v1/energy/reports/revenue` | `EnergyReportingController.java:116` | S | — | — | — | — | S | S | — | — | — |
| 82 | GET | `/api/v1/energy/reports/revenue/by-station` | `EnergyReportingController.java:132` | S | — | — | — | — | S | S | — | — | — |
| 83 | GET | `/api/v1/energy/reports/revenue/profit-analysis` | `EnergyReportingController.java:147` | S | — | — | — | — | S | S | — | — | — |
| 84 | POST | `/api/v1/energy/reports/schedule` | `EnergyReportingController.java:240` | S | — | — | — | — | S | S | — | — | — |
| 85 | GET | `/api/v1/energy/reports/usage-patterns` | `EnergyReportingController.java:163` | S | — | — | — | — | S | S | — | — | — |
| 86 | GET | `/api/v1/energy/stations` | `EnergyController.java:43` | S | — | — | — | — | S | — | — | — | S |
| 87 | GET | `/api/v1/fees` | `FeeQueryController.java:45` | S | S | — | — | — | — | — | — | — | — |
| 88 | POST | `/api/v1/fees/import` | `FeeImportController.java:23` | S | S | — | — | — | — | — | — | — | — |
| 89 | GET | `/api/v1/financial-shield/health` | `FinancialShieldController.java:23` | S | S | S | — | — | — | — | — | — | — |
| 90 | GET | `/api/v1/fixed-assets` | `FixedAssetController.java:33` | S | S | — | — | — | — | — | — | — | — |
| 91 | POST | `/api/v1/fixed-assets` | `FixedAssetController.java:28` | S | S | — | — | — | — | — | — | — | — |
| 92 | POST | `/api/v1/fixed-assets/run-depreciation` | `FixedAssetController.java:43` | S | S | — | — | — | — | — | — | — | — |
| 93 | GET | `/api/v1/fixed-assets/{id}` | `FixedAssetController.java:38` | S | S | — | — | — | — | — | — | — | — |
| 94 | GET | `/api/v1/fleet/metrics` | `FleetController.java:29` | S | — | — | — | — | S | — | — | — | — |
| 95 | GET | `/api/v1/integration/indicators/today` | `EconomicIndicatorsController.java:36` | A | A | A | A | A | A | A | A | A | A |
| 96 | GET | `/api/v1/inventory/products` | `InventoryController.java:66` | S | — | — | — | — | S | — | — | — | — |
| 97 | POST | `/api/v1/inventory/products` | `InventoryController.java:41` | S | — | — | — | — | S | — | — | — | — |
| 98 | DELETE | `/api/v1/inventory/products/{id}` | `InventoryController.java:55` | S | — | — | — | — | S | — | — | — | — |
| 99 | GET | `/api/v1/inventory/products/{id}` | `InventoryController.java:61` | S | — | — | — | — | S | — | — | — | — |
| 100 | PUT | `/api/v1/inventory/products/{id}` | `InventoryController.java:48` | S | — | — | — | — | S | — | — | — | — |
| 101 | GET | `/api/v1/inventory/products/{productId}/movements` | `InventoryController.java:79` | S | — | — | — | — | S | — | — | — | — |
| 102 | POST | `/api/v1/inventory/products/{productId}/movements` | `InventoryController.java:71` | S | — | — | — | — | S | — | — | — | — |
| 103 | GET | `/api/v1/invoices` | `InvoiceController.java:103` | S | S | — | — | S | — | — | — | — | — |
| 104 | POST | `/api/v1/invoices` | `InvoiceController.java:74` | S | S | — | — | S | — | — | — | — | — |
| 105 | POST | `/api/v1/invoices/import` | `InvoiceImportController.java:58` | S | S | — | — | S | — | — | — | — | — |
| 106 | GET | `/api/v1/invoices/{id}` | `InvoiceController.java:109` | S | S | — | — | S | — | — | — | — | — |
| 107 | PUT | `/api/v1/invoices/{id}` | `InvoiceController.java:114` | S | S | — | — | S | — | — | — | — | — |
| 108 | GET | `/api/v1/invoices/{id}/audit-trail` | `InvoiceController.java:120` | S | S | — | — | S | — | — | — | — | — |
| 109 | POST | `/api/v1/invoices/{id}/send` | `InvoiceController.java:129` | S | S | — | — | S | — | — | — | — | — |
| 110 | GET | `/api/v1/learning/rules` | `LearningController.java:35` | S | S | — | — | — | — | — | — | — | — |
| 111 | POST | `/api/v1/learning/rules` | `LearningController.java:92` | S | S | — | — | — | — | — | — | — | — |
| 112 | DELETE | `/api/v1/learning/rules/{id}` | `LearningController.java:81` | S | S | — | — | — | — | — | — | — | — |
| 113 | GET | `/api/v1/learning/rules/{id}` | `LearningController.java:51` | S | S | — | — | — | — | — | — | — | — |
| 114 | PUT | `/api/v1/learning/rules/{id}` | `LearningController.java:64` | S | S | — | — | — | — | — | — | — | — |
| 115 | GET | `/api/v1/maintenance/summary/{stationId}` | `MaintenanceController.java:221` | S | — | — | — | — | S | — | — | — | — |
| 116 | GET | `/api/v1/maintenance/tasks` | `MaintenanceController.java:164` | S | — | — | — | — | S | — | — | — | — |
| 117 | POST | `/api/v1/maintenance/tasks` | `MaintenanceController.java:48` | S | — | — | — | — | S | — | — | — | — |
| 118 | GET | `/api/v1/maintenance/tasks/overdue` | `MaintenanceController.java:206` | S | — | — | — | — | S | — | — | — | — |
| 119 | GET | `/api/v1/maintenance/tasks/upcoming` | `MaintenanceController.java:190` | S | — | — | — | — | S | — | — | — | — |
| 120 | PUT | `/api/v1/maintenance/tasks/{id}/cancel` | `MaintenanceController.java:121` | S | — | — | — | — | S | — | — | — | — |
| 121 | PUT | `/api/v1/maintenance/tasks/{id}/complete` | `MaintenanceController.java:94` | S | — | — | — | — | S | — | — | — | — |
| 122 | PUT | `/api/v1/maintenance/tasks/{id}/fail` | `MaintenanceController.java:139` | S | — | — | — | — | S | — | — | — | — |
| 123 | PUT | `/api/v1/maintenance/tasks/{id}/start` | `MaintenanceController.java:68` | S | — | — | — | — | S | — | — | — | — |
| 124 | GET | `/api/v1/metadata/manufacturers` | `ChargerMetadataController.java:59` | S | — | — | — | — | S | — | — | — | — |
| 125 | POST | `/api/v1/metadata/manufacturers` | `ChargerMetadataController.java:43` | S | — | — | — | — | S | — | — | — | — |
| 126 | GET | `/api/v1/metadata/manufacturers/{id}` | `ChargerMetadataController.java:71` | S | — | — | — | — | S | — | — | — | — |
| 127 | PUT | `/api/v1/metadata/manufacturers/{id}` | `ChargerMetadataController.java:84` | S | — | — | — | — | S | — | — | — | — |
| 128 | PUT | `/api/v1/metadata/manufacturers/{id}/deactivate` | `ChargerMetadataController.java:106` | S | — | — | — | — | S | — | — | — | — |
| 129 | GET | `/api/v1/metadata/models` | `ChargerMetadataController.java:139` | S | — | — | — | — | S | — | — | — | — |
| 130 | POST | `/api/v1/metadata/models` | `ChargerMetadataController.java:121` | S | — | — | — | — | S | — | — | — | — |
| 131 | GET | `/api/v1/metadata/models/manufacturer/{manufacturerId}` | `ChargerMetadataController.java:164` | S | — | — | — | — | S | — | — | — | — |
| 132 | GET | `/api/v1/metadata/models/ocpp/{version}` | `ChargerMetadataController.java:177` | S | — | — | — | — | S | — | — | — | — |
| 133 | GET | `/api/v1/metadata/models/{id}` | `ChargerMetadataController.java:151` | S | — | — | — | — | S | — | — | — | — |
| 134 | PUT | `/api/v1/metadata/models/{id}` | `ChargerMetadataController.java:189` | S | — | — | — | — | S | — | — | — | — |
| 135 | PUT | `/api/v1/metadata/models/{id}/firmware` | `ChargerMetadataController.java:211` | S | — | — | — | — | S | — | — | — | — |
| 136 | GET | `/api/v1/mrp/bom` | `MrpController.java:34` | S | — | — | — | — | S | — | — | — | — |
| 137 | POST | `/api/v1/mrp/bom` | `MrpController.java:29` | S | — | — | — | — | S | — | — | — | — |
| 138 | GET | `/api/v1/mrp/work-order` | `MrpController.java:44` | S | — | — | — | — | S | — | — | — | — |
| 139 | POST | `/api/v1/mrp/work-order` | `MrpController.java:39` | S | — | — | — | — | S | — | — | — | — |
| 140 | POST | `/api/v1/mrp/work-order/{id}/complete` | `MrpController.java:49` | S | — | — | — | — | S | — | — | — | — |
| 141 | GET | `/api/v1/notifications/preferences` | `NotificationController.java:40` | A | A | A | A | A | A | A | A | A | A |
| 142 | GET | `/api/v1/notifications/subscribe` | `NotificationController.java:29` | A | A | A | A | A | A | A | A | A | A |
| 143 | POST | `/api/v1/ocpp/remote/availability/change` | `OcppRemoteController.java:118` | S | — | — | — | — | S | — | — | — | — |
| 144 | POST | `/api/v1/ocpp/remote/charging-profile/clear` | `OcppRemoteController.java:146` | S | — | — | — | — | S | — | — | — | — |
| 145 | GET | `/api/v1/ocpp/remote/charging-profile/schedule` | `OcppRemoteController.java:162` | S | — | — | — | — | S | — | — | — | — |
| 146 | POST | `/api/v1/ocpp/remote/charging-profile/set` | `OcppRemoteController.java:132` | S | — | — | — | — | S | — | — | — | — |
| 147 | POST | `/api/v1/ocpp/remote/clear-cache` | `OcppRemoteController.java:249` | S | — | — | — | — | S | — | — | — | — |
| 148 | POST | `/api/v1/ocpp/remote/config/change` | `OcppRemoteController.java:91` | S | — | — | — | — | S | — | — | — | — |
| 149 | GET | `/api/v1/ocpp/remote/config/get` | `OcppRemoteController.java:105` | S | — | — | — | — | S | — | — | — | — |
| 150 | POST | `/api/v1/ocpp/remote/diagnostics/get` | `OcppRemoteController.java:177` | S | — | — | — | — | S | — | — | — | — |
| 151 | POST | `/api/v1/ocpp/remote/firmware/update` | `OcppRemoteController.java:192` | S | — | — | — | — | S | — | — | — | — |
| 152 | POST | `/api/v1/ocpp/remote/reservation/cancel` | `OcppRemoteController.java:222` | S | — | — | — | — | S | — | — | — | — |
| 153 | POST | `/api/v1/ocpp/remote/reservation/reserve` | `OcppRemoteController.java:206` | S | — | — | — | — | S | — | — | — | — |
| 154 | POST | `/api/v1/ocpp/remote/reset` | `OcppRemoteController.java:78` | S | — | — | — | — | S | — | — | — | — |
| 155 | POST | `/api/v1/ocpp/remote/start` | `OcppRemoteController.java:38` | S | — | — | — | — | S | — | — | — | — |
| 156 | POST | `/api/v1/ocpp/remote/stop` | `OcppRemoteController.java:52` | S | — | — | — | — | S | — | — | — | — |
| 157 | POST | `/api/v1/ocpp/remote/trigger-message` | `OcppRemoteController.java:235` | S | — | — | — | — | S | — | — | — | — |
| 158 | POST | `/api/v1/ocpp/remote/unlock` | `OcppRemoteController.java:65` | S | — | — | — | — | S | — | — | — | — |
| 159 | GET | `/api/v1/organizations` | `OrganizationController.java:68` | S | — | — | — | — | — | — | — | — | — |
| 160 | POST | `/api/v1/organizations` | `OrganizationController.java:38` | S | — | — | — | — | — | — | — | — | — |
| 161 | GET | `/api/v1/organizations/active` | `OrganizationController.java:77` | S | — | — | — | — | — | — | — | — | — |
| 162 | GET | `/api/v1/organizations/{id}` | `OrganizationController.java:86` | S | — | — | — | — | — | — | — | — | — |
| 163 | PUT | `/api/v1/organizations/{id}` | `OrganizationController.java:111` | S | — | — | — | — | — | — | — | — | — |
| 164 | PUT | `/api/v1/organizations/{id}/activate` | `OrganizationController.java:139` | S | — | — | — | — | — | — | — | — | — |
| 165 | GET | `/api/v1/organizations/{id}/hierarchy` | `OrganizationController.java:97` | S | — | — | — | — | — | — | — | — | — |
| 166 | PUT | `/api/v1/organizations/{id}/suspend` | `OrganizationController.java:128` | S | — | — | — | — | — | — | — | — | — |
| 167 | POST | `/api/v1/organizations/{parentId}/children` | `OrganizationController.java:52` | S | — | — | — | — | — | — | — | — | — |
| 168 | PUT | `/api/v1/partners/{rut}/email` | `PartnerController.java:36` | S | S | — | — | S | — | — | — | — | — |
| 169 | POST | `/api/v1/payroll/calculate` | `PayrollController.java:40` | S | — | — | S | — | — | — | — | — | — |
| 170 | GET | `/api/v1/payroll/employee/payslips` | `PayrollController.java:86` | S | — | — | S | — | — | — | S | — | — |
| 171 | GET | `/api/v1/payroll/employees` | `PayrollController.java:71` | S | — | — | S | — | — | — | — | — | — |
| 172 | POST | `/api/v1/payroll/periods` | `PayrollController.java:33` | S | — | — | S | — | — | — | — | — | — |
| 173 | POST | `/api/v1/payroll/previred/export` | `PayrollController.java:47` | S | — | — | S | — | — | — | — | — | — |
| 174 | POST | `/api/v1/period-close/audit` | `PeriodCloseController.java:22` | S | S | — | — | — | — | — | — | — | — |
| 175 | POST | `/api/v1/period-close/execute` | `PeriodCloseController.java:29` | S | S | — | — | — | — | — | — | — | — |
| 176 | GET | `/api/v1/pos/products/search` | `PosController.java:40` | S | — | — | — | S | — | — | — | — | — |
| 177 | POST | `/api/v1/pos/sync` | `PosController.java:85` | S | — | — | — | S | — | — | — | — | — |
| 178 | GET | `/api/v1/pos/transactions` | `PosController.java:67` | S | — | — | — | S | — | — | — | — | — |
| 179 | POST | `/api/v1/pos/transactions` | `PosController.java:73` | S | — | — | — | S | — | — | — | — | — |
| 180 | GET | `/api/v1/pricing/campaigns` | `EnhancedPricingController.java:183` | S | — | — | — | — | S | — | — | — | — |
| 181 | POST | `/api/v1/pricing/campaigns` | `EnhancedPricingController.java:151` | S | — | — | — | — | S | — | — | — | — |
| 182 | GET | `/api/v1/pricing/campaigns/{code}` | `EnhancedPricingController.java:192` | S | — | — | — | — | S | — | — | — | — |
| 183 | PUT | `/api/v1/pricing/campaigns/{id}/activate` | `EnhancedPricingController.java:201` | S | — | — | — | — | S | — | — | — | — |
| 184 | PUT | `/api/v1/pricing/campaigns/{id}/deactivate` | `EnhancedPricingController.java:214` | S | — | — | — | — | S | — | — | — | — |
| 185 | GET | `/api/v1/pricing/current/{stationId}` | `EnhancedPricingController.java:65` | S | — | — | — | — | S | — | — | — | — |
| 186 | GET | `/api/v1/pricing/demand-rules` | `EnhancedPricingController.java:110` | S | — | — | — | — | S | — | — | — | — |
| 187 | POST | `/api/v1/pricing/demand-rules` | `EnhancedPricingController.java:85` | S | — | — | — | — | S | — | — | — | — |
| 188 | POST | `/api/v1/pricing/preview` | `EnhancedPricingController.java:41` | S | — | — | — | — | S | — | — | — | — |
| 189 | GET | `/api/v1/pricing/seasonal-rates` | `EnhancedPricingController.java:140` | S | — | — | — | — | S | — | — | — | — |
| 190 | POST | `/api/v1/pricing/seasonal-rates` | `EnhancedPricingController.java:121` | S | — | — | — | — | S | — | — | — | — |
| 191 | GET | `/api/v1/procurement/orders` | `PurchaseOrderController.java:31` | S | — | — | — | — | S | — | — | — | — |
| 192 | POST | `/api/v1/procurement/orders` | `PurchaseOrderController.java:42` | S | — | — | — | — | S | — | — | — | — |
| 193 | GET | `/api/v1/procurement/orders/{id}` | `PurchaseOrderController.java:37` | S | — | — | — | — | S | — | — | — | — |
| 194 | POST | `/api/v1/procurement/orders/{id}/received` | `PurchaseOrderController.java:66` | S | — | — | — | — | S | — | — | — | — |
| 195 | POST | `/api/v1/procurement/orders/{id}/sent` | `PurchaseOrderController.java:61` | S | — | — | — | — | S | — | — | — | — |
| 196 | GET | `/api/v1/projects` | `ProjectController.java:28` | S | — | — | — | — | S | — | — | — | — |
| 197 | POST | `/api/v1/projects` | `ProjectController.java:39` | S | — | — | — | — | S | — | — | — | — |
| 198 | GET | `/api/v1/projects/{id}` | `ProjectController.java:34` | S | — | — | — | — | S | — | — | — | — |
| 199 | PUT | `/api/v1/projects/{id}/status` | `ProjectController.java:46` | S | — | — | — | — | S | — | — | — | — |
| 200 | GET | `/api/v1/reports/balance-sheet` | `ReportingController.java:148` | S | S | — | — | — | — | S | — | — | — |
| 201 | POST | `/api/v1/reports/f29/submit` | `ReportingController.java:167` | S | S | — | — | — | — | S | — | — | — |
| 202 | GET | `/api/v1/reports/income-statement` | `ReportingController.java:89` | S | S | — | — | — | — | S | — | — | — |
| 203 | GET | `/api/v1/reports/sales/excel` | `ReportingController.java:114` | S | S | — | — | — | — | S | — | — | — |
| 204 | POST | `/api/v1/reports/weekly/generate` | `ReportingController.java:75` | S | S | — | — | — | — | S | — | — | — |
| 205 | GET | `/api/v1/roles` | `RoleController.java:57` | S | — | — | — | — | — | — | — | — | — |
| 206 | POST | `/api/v1/roles` | `RoleController.java:39` | S | — | — | — | — | — | — | — | — | — |
| 207 | GET | `/api/v1/roles/organization/{organizationId}` | `RoleController.java:75` | S | — | — | — | — | — | — | — | — | — |
| 208 | GET | `/api/v1/roles/system` | `RoleController.java:66` | S | — | — | — | — | — | — | — | — | — |
| 209 | GET | `/api/v1/roles/{id}` | `RoleController.java:84` | S | — | — | — | — | — | — | — | — | — |
| 210 | PUT | `/api/v1/roles/{id}` | `RoleController.java:95` | S | — | — | — | — | — | — | — | — | — |
| 211 | PUT | `/api/v1/roles/{id}/permissions` | `RoleController.java:112` | S | — | — | — | — | — | — | — | — | — |
| 212 | GET | `/api/v1/sii/audit/report` | `TaxAuditController.java:24` | S | S | — | — | — | — | S | — | — | — |
| 213 | GET | `/api/v1/sii/caf` | `CafController.java:54` | S | S | — | — | — | — | — | — | — | — |
| 214 | POST | `/api/v1/sii/caf` | `CafController.java:61` | S | S | — | — | — | — | — | — | — | — |
| 215 | POST | `/api/v1/sii/dte/{dteId}/send` | `SiiDteController.java:35` | S | S | — | — | — | — | — | — | — | — |
| 216 | GET | `/api/v1/sii/dte/{tipo}/{folio}` | `SiiDteController.java:29` | S | S | — | — | — | — | — | — | — | — |
| 217 | POST | `/api/v1/sii/ops/fetch-rcv` | `SiiIntegrationController.java:60` | S | S | — | — | — | — | — | — | — | — |
| 218 | GET | `/api/v1/sii/status` | `SiiSyncController.java:38` | S | S | — | — | — | — | — | — | — | — |
| 219 | POST | `/api/v1/sii/sync` | `SiiSyncController.java:52` | S | S | — | — | — | — | — | — | — | — |
| 220 | POST | `/api/v1/sso/users` | `SsoUserController.java:48` | S | — | — | — | — | — | — | — | — | — |
| 221 | POST | `/api/v1/sso/users/invite` | `SsoUserController.java:65` | S | — | — | — | — | — | — | — | — | — |
| 222 | GET | `/api/v1/sso/users/invite/validate/{token}` | `SsoUserController.java:75` | S | — | — | — | — | — | — | — | — | — |
| 223 | POST | `/api/v1/sso/users/register-invite` | `SsoUserController.java:83` | S | — | — | — | — | — | — | — | — | — |
| 224 | PUT | `/api/v1/sso/users/{id}/role` | `SsoUserController.java:59` | S | — | — | — | — | — | — | — | — | — |
| 225 | GET | `/api/v1/sustainability/corfo-simulator` | `SustainabilityController.java:102` | S | — | — | — | — | S | S | — | — | — |
| 226 | GET | `/api/v1/sustainability/esg-report` | `SustainabilityController.java:96` | S | — | — | — | — | S | S | — | — | — |
| 227 | GET | `/api/v1/sustainability/metrics/score` | `SustainabilityMetricsController.java:66` | S | — | — | — | — | S | S | — | — | — |
| 228 | GET | `/api/v1/sustainability/score` | `SustainabilityController.java:70` | S | — | — | — | — | S | S | — | — | — |
| 229 | GET | `/api/v1/treasury/ai-forecast` | `TreasuryController.java:64` | S | S | S | — | — | — | — | — | — | — |
| 230 | GET | `/api/v1/treasury/cash-entries` | `TreasuryController.java:82` | S | S | S | — | — | — | — | — | — | — |
| 231 | POST | `/api/v1/treasury/cash-entries` | `TreasuryController.java:94` | S | S | S | — | — | — | — | — | — | — |
| 232 | POST | `/api/v1/treasury/cash-entry` | `TreasuryController.java:71` | S | S | S | — | — | — | — | — | — | — |
| 233 | GET | `/api/v1/treasury/cash-flow-daily` | `TreasuryController.java:41` | S | S | S | — | — | — | — | — | — | — |
| 234 | GET | `/api/v1/treasury/financial-ratios` | `TreasuryController.java:53` | S | S | S | — | — | — | — | — | — | — |
| 235 | GET | `/api/v1/users` | `UserController.java:69` | S | — | — | — | — | — | — | — | — | — |
| 236 | POST | `/api/v1/users` | `UserController.java:40` | S | — | — | — | — | — | — | — | — | — |
| 237 | POST | `/api/v1/users/invite` | `UserController.java:55` | S | — | — | — | — | — | — | — | — | — |
| 238 | GET | `/api/v1/users/organization/{organizationId}` | `UserController.java:78` | S | — | — | — | — | — | — | — | — | — |
| 239 | GET | `/api/v1/users/{id}` | `UserController.java:88` | S | — | — | — | — | — | — | — | — | — |
| 240 | PUT | `/api/v1/users/{id}/activate` | `UserController.java:128` | S | — | — | — | — | — | — | — | — | — |
| 241 | PUT | `/api/v1/users/{id}/profile` | `UserController.java:99` | S | — | — | — | — | — | — | — | — | — |
| 242 | PUT | `/api/v1/users/{id}/suspend` | `UserController.java:144` | S | — | — | — | — | — | — | — | — | — |
| 243 | PUT | `/api/v1/users/{id}/verify-email` | `UserController.java:117` | S | — | — | — | — | — | — | — | — | — |
| 244 | DELETE | `/api/v1/users/{userId}/roles/{roleId}` | `UserController.java:171` | S | — | — | — | — | — | — | — | — | — |
| 245 | POST | `/api/v1/users/{userId}/roles/{roleId}` | `UserController.java:155` | S | — | — | — | — | — | — | — | — | — |
| 246 | GET | `/api/v1/wms/locations` | `WmsController.java:33` | S | — | — | — | — | S | — | — | — | — |
| 247 | POST | `/api/v1/wms/locations` | `WmsController.java:28` | S | — | — | — | — | S | — | — | — | — |
| 248 | GET | `/api/v1/wms/picking` | `WmsController.java:43` | S | — | — | — | — | S | — | — | — | — |
| 249 | POST | `/api/v1/wms/picking` | `WmsController.java:38` | S | — | — | — | — | S | — | — | — | — |
| 250 | POST | `/api/v1/wms/picking/{id}/complete` | `WmsController.java:48` | S | — | — | — | — | S | — | — | — | — |

## Resumen

- Endpoints inventariados: **250** en 60 controllers (el mapa previo hablaba de 51/226; el código creció).
- Públicos: **3** (login, register, logout).
- Solo autenticados, sin rol específico: **4**.

| Rol | Endpoints permitidos (incluye autenticados) |
|---|---:|
| ADMIN | 247 |
| ACCOUNTANT | 73 |
| TREASURER | 19 |
| HR | 9 |
| SALES | 27 |
| OPERATIONS | 110 |
| AUDITOR | 34 |
| USER | 5 |
| B2B_CLIENT | 7 |
| TECHNICIAN | 8 |

## Discrepancias contra `coordinacion/rediseno/MAPA-FUNCIONAL.md`

| # | Hallazgo | Evidencia | Estado |
|---:|---|---|---|
| 1 | El portal del empleado ya no está roto para USER. | `PayrollController.java:86-91` permite `ADMIN,HR,USER`; MAPA §2.5 lo daba solo ADMIN/HR. | **Corregido en integracion** (verificar en despliegue). |
| 2 | El portal B2B sí tiene restricción de rol. | `CustomerOrderController.java:30` permite `ADMIN,B2B_CLIENT`; MAPA §7 decía "sin @PreAuthorize". | **Corregido en integracion**, pero `B2B_CLIENT` no está en `UserRole.java:30-38` → rol inalcanzable por token. **Dudoso para el dueño.** |
| 3 | El chat IA tiene roles explícitos. | `AiAssistantController.java:39` permite `ADMIN,ACCOUNTANT,SALES,TREASURER,AUDITOR`; MAPA §7 lo listaba sin restricción. | **Corregido en integracion**. |
| 4 | Notificaciones exige autenticación, pero cualquier rol. | `NotificationController.java:16-17` usa `isAuthenticated()`; MAPA §7 (R-23) lo marcaba sin restricción. | **Parcial**: ya no es público, pero no hay rol de notificaciones. **Dudoso para el dueño.** |
| 5 | Indicadores económicos ahora declara autenticación explícita. | `EconomicIndicatorsController.java:21` (`isAuthenticated()`); antes dependía solo de `SecurityConfig` (`anyRequest().authenticated()`). | **Corregido en esta rama**. |
| 6 | Energía referencia `TECHNICIAN`, que no existe en el enum. | `EnergyController.java:32` (`hasAnyRole('ADMIN','OPERATIONS','TECHNICIAN')`); `UserRole.java:30-38` solo tiene 8 roles. | **Dudoso para el dueño**: el rol nunca podrá entrar; decidir si se agrega al enum o se retira de la regla. |
| 7 | Compliance mezcla regla de clase y método. | `ComplianceController.java:45` incluye AUDITOR; `:80` restringe el dashboard a ADMIN/ACCOUNTANT. | Coincide con MAPA §7 (auditor no ve dashboard), pero la clase sugiere otra cosa. **Dudoso**: unificar en una sola regla. |
| 8 | Centro de Costos y Conciliación Bancaria siguen sin ACCOUNTANT. | `ProjectController.java:19` (`ADMIN,OPERATIONS`), `BankingSyncController.java:19` (`ADMIN,TREASURER`). | Coincide con MAPA §7; **decisión de producto pendiente**. |
| 9 | `/accounting/f29` no permite AUDITOR. | `AccountingController.java:56` (`ADMIN,ACCOUNTANT`); MAPA §2.2/§7 lo señala. | Coincide con MAPA; **decisión de producto pendiente** (Auditoría Tributaria la usa). |

## Auditoría de endpoints públicos

- El test anónimo (`unauthenticatedRequestsAreRejectedOutsideWhitelist`) recorre los 250 endpoints: todos los no públicos responden 401/403 sin token.
- `SecurityConfig.java:60-71` deja públicos solo `OPTIONS`, `/api/v1/auth/**` (login/register/logout/me), `/actuator/health/**`, `/actuator/info` y `/error`.
- `GET /api/v1/auth/me` está cubierto por el matcher `authenticated()` anterior al `permitAll` de `/api/v1/auth/**`.
- No se encontró ningún endpoint de negocio público por error.

## Cómo reproducir

```bash
cd SII-ERP-AI-BACK
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew test --tests "*PermissionMatrixTest"
```

El test falla si: (a) un endpoint no público no tiene `@PreAuthorize` ni la regla global, (b) un endpoint no público acepta anónimos, (c) un rol permitido recibe 403, o (d) un rol denegado recibe 2xx.
