package com.reucle.user.repository;

public interface IAuditRepository {
    void logAction(String userId, String action);
}
