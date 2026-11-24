package com.reucle.user.dto;

public record RegistrationData(
    String name,
    String email,
    String password,
    String collectionAddress,
    String phone
) {}
