package com.reucle.user.domain;

public class Customer extends User {
    private String collectionAddress;
    private String phone;

    public Customer(String userId, String name, String email, String passwordHash,
                    UserStatus status, String collectionAddress, String phone) {
        super(userId, name, email, passwordHash, status);
        this.collectionAddress = collectionAddress;
        this.phone = phone;
    }

    public void updateProfile(String collectionAddress, String phone) {
        this.collectionAddress = collectionAddress;
        this.phone = phone;
    }

    public String getCollectionAddress() { return collectionAddress; }
    public String getPhone() { return phone; }
}
