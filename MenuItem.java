package com.collegemess;

import jakarta.persistence.*;

@Entity
@Table(name = "menu_items")
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String mealType;
    private double price;
    private boolean available = true;

    public MenuItem() {}

    public MenuItem(String name, String mealType, double price) {
        this.name = name;
        this.mealType = mealType;
        this.price = price;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getMealType() { return mealType; }
    public double getPrice() { return price; }
    public boolean isAvailable() { return available; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setMealType(String mealType) { this.mealType = mealType; }
    public void setPrice(double price) { this.price = price; }
    public void setAvailable(boolean available) { this.available = available; }
}
