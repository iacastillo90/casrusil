package com.casrusil.siierpai.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.YearMonth;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifica las migraciones de datos V3 (plan de cuentas) y V4 (pay_periods)
 * contra PostgreSQL real, partiendo de V1/V2.
 */
class FlywayMigrationsTest {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("flyway_test")
            .withUsername("test")
            .withPassword("test");

    private static Connection connection;

    @BeforeAll
    static void start() throws Exception {
        POSTGRES.start();
        connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    @AfterAll
    static void stop() throws Exception {
        if (connection != null) connection.close();
        POSTGRES.stop();
    }

    @Test
    void chartMigrationKeepsBalancesAndPayPeriodBecomesVarchar() throws Exception {
        migrateTo("2");

        UUID companyId = UUID.randomUUID();
        try (PreparedStatement ps = connection.prepareStatement(
                "insert into sso.companies (id, rut, razon_social, email, is_active, created_at) values (?,?,?,?,?,now())")) {
            ps.setObject(1, companyId);
            ps.setString(2, "90000000-1");
            ps.setString(3, "Balance Test SpA");
            ps.setString(4, "balance@test.cl");
            ps.setBoolean(5, true);
            ps.executeUpdate();
        }
        insertAccount(companyId, "1.1.01.001", "Caja canónica", "ASSET");
        insertAccount(companyId, "110101", "Caja detallada", "ASSET");
        UUID entryId = UUID.randomUUID();
        try (PreparedStatement ps = connection.prepareStatement(
                "insert into accounting.accounting_entries (id, company_id, description, occurred_on, type) values (?,?,?,now(),'SALE')")) {
            ps.setObject(1, entryId);
            ps.setObject(2, companyId);
            ps.setString(3, "Asiento de prueba");
            ps.executeUpdate();
        }
        insertLine(entryId, "1.1.01.001", "100.00", "0.00");
        insertLine(entryId, "110101", "50.00", "0.00");

        BigDecimal[] before = balances(companyId);
        migrateTo("3");
        BigDecimal[] after = balances(companyId);

        assertEquals(0, before[0].compareTo(after[0]), "débitos deben mantenerse");
        assertEquals(0, before[1].compareTo(after[1]), "créditos deben mantenerse");

        byte[] serialized = serialize(YearMonth.of(2026, 11));
        try (PreparedStatement ps = connection.prepareStatement(
                "insert into public.pay_periods (company_id, is_closed, period, uf_value, utm_value) values (?,false,?,?,?)")) {
            ps.setString(1, "balance-test");
            ps.setBytes(2, serialized);
            ps.setBigDecimal(3, new BigDecimal("38000"));
            ps.setBigDecimal(4, new BigDecimal("67000"));
            ps.executeUpdate();
        }

        migrateTo("4");

        try (PreparedStatement ps = connection.prepareStatement(
                "select period from public.pay_periods where company_id = 'balance-test'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            assertEquals("2026-11", rs.getString(1));
        }
    }

    private static void migrateTo(String version) {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .target(version)
                .load()
                .migrate();
    }

    private static void insertAccount(UUID companyId, String code, String name, String type) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
                "insert into accounting.accounts (id, active, code, company_id, description, name, type) values (?,?,?,?,?,?,?)")) {
            ps.setObject(1, UUID.randomUUID());
            ps.setBoolean(2, true);
            ps.setString(3, code);
            ps.setObject(4, companyId);
            ps.setString(5, name);
            ps.setString(6, name);
            ps.setString(7, type);
            ps.executeUpdate();
        }
    }

    private static void insertLine(UUID entryId, String code, String debit, String credit) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
                "insert into accounting.accounting_entry_lines (entry_id, account_code, credit, debit) values (?,?,?,?)")) {
            ps.setObject(1, entryId);
            ps.setString(2, code);
            ps.setBigDecimal(3, new BigDecimal(credit));
            ps.setBigDecimal(4, new BigDecimal(debit));
            ps.executeUpdate();
        }
    }

    private static BigDecimal[] balances(UUID companyId) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
                "select coalesce(sum(l.debit),0), coalesce(sum(l.credit),0) from accounting.accounting_entry_lines l join accounting.accounting_entries e on e.id=l.entry_id where e.company_id=?")) {
            ps.setObject(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new BigDecimal[]{rs.getBigDecimal(1), rs.getBigDecimal(2)};
            }
        }
    }

    private static byte[] serialize(YearMonth value) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(value);
        }
        return out.toByteArray();
    }
}
