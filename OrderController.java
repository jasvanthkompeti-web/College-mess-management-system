package com.collegemess;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
interface OrderRepository extends org.springframework.data.jpa.repository.JpaRepository<Order, Long> {
    List<Order> findByStudentEmail(String studentEmail);
}

public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody Order order) {
        if (order.getQuantity() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(orderRepository.save(order));
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/student/{email}")
    public List<Order> getStudentOrders(@PathVariable String email) {
        return orderRepository.findByStudentEmail(email);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return orderRepository.findById(id)
                .map(order -> {
                    order.setStatus(status.toUpperCase());
                    return ResponseEntity.ok(orderRepository.save(order));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
