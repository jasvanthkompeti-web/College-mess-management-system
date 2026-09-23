package com.collegemess;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentEmail;
    private String itemName;
    private int quantity;
    private double totalPrice;
    private String status = "PLACED";
    private LocalDateTime orderedAt = LocalDateTime.now();

    public Order() {}

    public Order(String studentEmail, String itemName, int quantity, double totalPrice) {
        this.studentEmail = studentEmail;
        this.itemName = itemName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public Long getId() { return id; }
    public String getStudentEmail() { return studentEmail; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public LocalDateTime getOrderedAt() { return orderedAt; }

    public void setId(Long id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }
}
