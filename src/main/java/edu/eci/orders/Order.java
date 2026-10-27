package edu.eci.orders;
public record Order(String id, String customerId, String product, int quantity) {}