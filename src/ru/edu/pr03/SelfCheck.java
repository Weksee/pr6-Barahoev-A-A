package ru.edu.pr03;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SelfCheck {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws IOException {
        System.out.println("=== Unit-проверки ===");
        runUnitTests();
        System.out.println();
        System.out.println("=== Data-driven (sample.csv) ===");
        runDataDriven();
        System.out.println();
        System.out.println("ИТОГО: passed=" + passed + ", failed=" + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void pass(String name) {
        passed++;
        System.out.println("PASS " + name);
    }

    private static void fail(String name, String reason) {
        failed++;
        System.out.println("FAIL " + name + " : " + reason);
    }

    private static void expectRoute(String name, long amount, boolean urgent,
                                    RiskLevel risk, boolean personal, ApprovalRoute expected) {
        try {
            ApprovalRoute actual = new ApprovalRouter().route(amount, urgent, risk, personal);
            if (actual == expected) pass(name);
            else fail(name, "expected=" + expected + ", actual=" + actual);
        } catch (IllegalArgumentException e) {
            fail(name, "unexpected exception: " + e.getMessage());
        }
    }

    private static void expectException(String name, long amount, boolean urgent,
                                        RiskLevel risk, boolean personal, String expectedMsgPart) {
        try {
            new ApprovalRouter().route(amount, urgent, risk, personal);
            fail(name, "no exception thrown");
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains(expectedMsgPart)) pass(name);
            else fail(name, "wrong message: " + e.getMessage());
        }
    }

    private static void expectBool(String name, boolean actual, boolean expected) {
        if (actual == expected) pass(name);
        else fail(name, "expected=" + expected + ", actual=" + actual);
    }

    private static void runUnitTests() {
        // Пересечения (1-4)
        expectRoute("пересечение: personal > urgent",
                10_000, true, RiskLevel.LOW, true, ApprovalRoute.SECURITY_REVIEW);
        expectRoute("пересечение: HIGH > urgent",
                10_000, true, RiskLevel.HIGH, false, ApprovalRoute.DIRECTOR_REVIEW);
        expectRoute("пересечение: personal > HIGH",
                10_000, false, RiskLevel.HIGH, true, ApprovalRoute.SECURITY_REVIEW);
        expectRoute("пересечение: сумма > urgent",
                600_000, true, RiskLevel.LOW, false, ApprovalRoute.FINANCE_REVIEW);

        // Базовые (5-9)
        expectRoute("FAST_TRACK (вариант 3, сумма < 100000)",
                50_000, true, RiskLevel.LOW, false, ApprovalRoute.FAST_TRACK);
        expectRoute("FINANCE_REVIEW (граница 500000)",
                500_000, false, RiskLevel.LOW, false, ApprovalRoute.FINANCE_REVIEW);
        expectRoute("STANDARD",
                10_000, false, RiskLevel.LOW, false, ApprovalRoute.STANDARD);
        expectRoute("MEDIUM -> STANDARD",
                10_000, false, RiskLevel.MEDIUM, false, ApprovalRoute.STANDARD);
        expectRoute("SECURITY_REVIEW",
                10_000, false, RiskLevel.LOW, true, ApprovalRoute.SECURITY_REVIEW);

        // Границы (10-11)
        expectRoute("499999 urgent < 100000? НЕТ -> STANDARD (вариант 3)",
                499_999, true, RiskLevel.LOW, false, ApprovalRoute.STANDARD);
        expectRoute("500001 -> FINANCE_REVIEW",
                500_001, false, RiskLevel.LOW, false, ApprovalRoute.FINANCE_REVIEW);

        // Защитные (12-13)
        expectException("amount < 0", -1, true, RiskLevel.LOW, false, "amount must be >= 0");
        expectException("risk == null", 10_000, true, null, false, "risk is null");

        // --- Часть А варианта 3 (2 проверки) ---
        expectRoute("вариант 3: 150000 urgent -> STANDARD",
                150_000, true, RiskLevel.LOW, false, ApprovalRoute.STANDARD);
        expectRoute("вариант 3: 99999 urgent -> FAST_TRACK",
                99_999, true, RiskLevel.LOW, false, ApprovalRoute.FAST_TRACK);

        // --- Часть Б варианта 3 (2 проверки) ---
        expectBool("addUrgencyFlag(50000, true)",
                new ApprovalRouter().addUrgencyFlag(50_000, true), true);
        expectBool("addUrgencyFlag(100000, true)",
                new ApprovalRouter().addUrgencyFlag(100_000, true), false);
    }

    private static void runDataDriven() throws IOException {
        Path path = Path.of("data", "sample.csv");
        if (!Files.exists(path)) {
            System.out.println("sample.csv не найден, пропускаем data-driven");
            return;
        }
        List<String> lines = Files.readAllLines(path);
        int n = 0;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            n++;
            String[] p = line.split(";", -1);
            if (p.length != 5) {
                fail("data/" + n, "bad format of csv line");
                continue;
            }
            long amount;
            boolean urgent;
            RiskLevel risk;
            boolean personal;
            String expected;
            try {
                amount = Long.parseLong(p[0]);
                urgent = Boolean.parseBoolean(p[1]);
                risk = p[2].equals("null") ? null : RiskLevel.valueOf(p[2]);
                personal = Boolean.parseBoolean(p[3]);
                expected = p[4];
            } catch (Exception e) {
                fail("data/" + n, "parse error: " + e.getMessage());
                continue;
            }

            try {
                ApprovalRoute actual = new ApprovalRouter().route(amount, urgent, risk, personal);
                if (expected.equals("ERROR")) {
                    fail("data/" + n, "expected ERROR, got " + actual);
                } else if (actual.name().equals(expected)) {
                    pass("data/" + n);
                } else {
                    fail("data/" + n, "expected=" + expected + ", actual=" + actual);
                }
            } catch (IllegalArgumentException e) {
                if (expected.equals("ERROR")) pass("data/" + n);
                else fail("data/" + n, "unexpected exception: " + e.getMessage());
            }
        }
        System.out.println("Data-driven: " + passed + "/" + (passed + failed) + " passed");
    }
}