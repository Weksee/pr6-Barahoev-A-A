package ru.edu.pr03;

public class ApprovalRouter {

    public static final long FINANCE_LIMIT = 500_000L;

    // Часть Б (вариант 3): срочность с учётом нового порога
    public boolean addUrgencyFlag(long amount, boolean urgent) {
        return urgent && amount < 100_000L;
    }

    public ApprovalRoute route(long amount, boolean urgent, RiskLevel risk, boolean containsPersonalData) {
        // Защитные условия
        if (amount < 0) {
            throw new IllegalArgumentException("Ошибка: amount must be >= 0, got " + amount);
        }
        if (risk == null) {
            throw new IllegalArgumentException("Ошибка: risk is null");
        }

        // R1
        if (containsPersonalData) {
            return ApprovalRoute.SECURITY_REVIEW;
        }
        // R2
        if (risk == RiskLevel.HIGH) {
            return ApprovalRoute.DIRECTOR_REVIEW;
        }
        // R3
        if (amount >= FINANCE_LIMIT) {
            return ApprovalRoute.FINANCE_REVIEW;
        }
        // R4 (ВАРИАНТ 3: только если amount < 100_000)
        if (addUrgencyFlag(amount, urgent)) {
            return ApprovalRoute.FAST_TRACK;
        }
        // R5
        return ApprovalRoute.STANDARD;
    }
}