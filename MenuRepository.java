package com.collegemess;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByMealTypeIgnoreCase(String mealType);
    List<MenuItem> findByAvailableTrue();
}
