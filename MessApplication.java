package com.jaswanth.mess;

import jakarta.persistence.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class MessApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessApplication.class, args);
    }

    @Bean PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean CommandLineRunner demo(UserRepo users, PasswordEncoder encoder, MenuRepo menus) {
        return args -> {
            if (users.findByEmail("admin@mess.com").isEmpty()) {
                User admin = new User();
                admin.name = "Mess Admin";
                admin.email = "admin@mess.com";
                admin.password = encoder.encode("Admin@123");
                admin.role = "ADMIN";
                users.save(admin);
            }
            if (menus.count() == 0) {
                menus.save(menu("Idli + Sambar", "BREAKFAST", 35, "Four idlis, sambar and chutney"));
                menus.save(menu("Rice + Dal + Curry", "LUNCH", 60, "Healthy college lunch"));
                menus.save(menu("Chapati + Curry", "DINNER", 55, "Fresh chapati and vegetable curry"));
            }
        };
    }

    static MenuItem menu(String name, String type, double price, String description) {
        MenuItem m = new MenuItem();
        m.name=name; m.mealType=type; m.price=price;
        m.description=description; m.menuDate=LocalDate.now();
        return m;
    }

    @Entity @Table(name="users")
    public static class User {
        @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
        public String name;
        @Column(unique=true, nullable=false) public String email;
        public String password;
        public String role;
    }

    @Entity @Table(name="menu_items")
    public static class MenuItem {
        @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
        public String name, mealType, description;
        public double price;
        public LocalDate menuDate;
    }

    @Entity @Table(name="orders")
    public static class Order {
        @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
        @ManyToOne(optional=false) public User user;
        @ManyToOne(optional=false) public MenuItem menuItem;
        public int quantity;
        public LocalDateTime orderedAt = LocalDateTime.now();
    }

    interface UserRepo extends org.springframework.data.jpa.repository.JpaRepository<User,Long> {
        Optional<User> findByEmail(String email);
    }
    interface MenuRepo extends org.springframework.data.jpa.repository.JpaRepository<MenuItem,Long> {
        List<MenuItem> findByMenuDate(LocalDate date);
    }
    interface OrderRepo extends org.springframework.data.jpa.repository.JpaRepository<Order,Long> {
        List<Order> findByUserOrderByOrderedAtDesc(User user);
        List<Order> findAllByOrderByOrderedAtDesc();
    }

    @Service
    static class Auth {
        final UserRepo users; final PasswordEncoder encoder;
        final Map<String,User> tokens = new ConcurrentHashMap<>();
        Auth(UserRepo u, PasswordEncoder e) { users=u; encoder=e; }

        String register(String name,String email,String password) {
            email=email.toLowerCase().trim();
            if(users.findByEmail(email).isPresent()) throw new RuntimeException("Email already registered");
            User u=new User(); u.name=name; u.email=email;
            u.password=encoder.encode(password); u.role="USER"; users.save(u);
            return token(u);
        }

        String login(String email,String password) {
            User u=users.findByEmail(email.toLowerCase().trim())
                    .orElseThrow(()->new RuntimeException("Invalid email or password"));
            if(!encoder.matches(password,u.password)) throw new RuntimeException("Invalid email or password");
            return token(u);
        }

        String token(User u) {
            String t=UUID.randomUUID().toString();
            tokens.put(t,u);
            return t;
        }
        User user(String token) { return tokens.get(token); }
    }

    @Component
    static class TokenFilter extends OncePerRequestFilter {
        final Auth auth;
        TokenFilter(Auth a){auth=a;}
        protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)
                throws ServletException,IOException {
            String h=req.getHeader("Authorization");
            if(h!=null && h.startsWith("Bearer ")) {
                User u=auth.user(h.substring(7));
                if(u!=null) req.setAttribute("user",u);
            }
            chain.doFilter(req,res);
        }
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http, TokenFilter filter) throws Exception {
        http.csrf(c->c.disable())
            .cors(c->{})
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a->a
                .requestMatchers("/", "/index.html", "/style.css", "/app.js",
                                 "/api/auth/**", "/api/menu").permitAll()
                .anyRequest().permitAll())
            .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @RestController @RequestMapping("/api/auth")
    static class AuthController {
        final Auth auth;
        AuthController(Auth a){auth=a;}

        @PostMapping("/register")
        Map<String,Object> register(@RequestBody Map<String,String> b) {
            return Map.of("token",auth.register(b.get("name"),b.get("email"),b.get("password")));
        }

        @PostMapping("/login")
        Map<String,Object> login(@RequestBody Map<String,String> b) {
            User u=auth.users.findByEmail(b.get("email").toLowerCase().trim())
                    .orElseThrow(()->new RuntimeException("Invalid email or password"));
            String token=auth.login(b.get("email"),b.get("password"));
            return Map.of("token",token,"name",u.name,"email",u.email,"role",u.role);
        }
    }

    @RestController @RequestMapping("/api/menu")
    static class MenuController {
        final MenuRepo menus;
        MenuController(MenuRepo m){menus=m;}

        @GetMapping List<MenuItem> today(){return menus.findByMenuDate(LocalDate.now());}

        @PostMapping ResponseEntity<?> add(@RequestBody MenuItem m,HttpServletRequest req){
            User u=(User)req.getAttribute("user");
            if(u==null || !"ADMIN".equals(u.role)) return ResponseEntity.status(403).body(Map.of("message","Admin only"));
            m.id=null; return ResponseEntity.ok(menus.save(m));
        }

        @PutMapping("/{id}") ResponseEntity<?> edit(@PathVariable Long id,@RequestBody MenuItem data,HttpServletRequest req){
            User u=(User)req.getAttribute("user");
            if(u==null || !"ADMIN".equals(u.role)) return ResponseEntity.status(403).body(Map.of("message","Admin only"));
            MenuItem m=menus.findById(id).orElseThrow(()->new RuntimeException("Menu item not found"));
            m.name=data.name; m.mealType=data.mealType; m.price=data.price; m.description=data.description; m.menuDate=data.menuDate;
            return ResponseEntity.ok(menus.save(m));
        }

        @DeleteMapping("/{id}") ResponseEntity<?> delete(@PathVariable Long id,HttpServletRequest req){
            User u=(User)req.getAttribute("user");
            if(u==null || !"ADMIN".equals(u.role)) return ResponseEntity.status(403).body(Map.of("message","Admin only"));
            menus.deleteById(id); return ResponseEntity.ok(Map.of("message","Deleted"));
        }
    }

    @RestController @RequestMapping("/api/orders")
    static class OrderController {
        final OrderRepo orders; final MenuRepo menus;
        OrderController(OrderRepo o,MenuRepo m){orders=o;menus=m;}

        @PostMapping ResponseEntity<?> order(@RequestBody Map<String,Object> b,HttpServletRequest req){
            User u=(User)req.getAttribute("user");
            if(u==null) return ResponseEntity.status(401).body(Map.of("message","Please login"));
            Long menuId=Long.valueOf(b.get("menuItemId").toString());
            int quantity=Integer.parseInt(b.getOrDefault("quantity",1).toString());
            MenuItem m=menus.findById(menuId).orElseThrow(()->new RuntimeException("Menu item not found"));
            Order o=new Order(); o.user=u; o.menuItem=m; o.quantity=Math.max(1,quantity);
            return ResponseEntity.ok(orders.save(o));
        }

        @GetMapping("/my") ResponseEntity<?> mine(HttpServletRequest req){
            User u=(User)req.getAttribute("user");
            if(u==null) return ResponseEntity.status(401).body(Map.of("message","Please login"));
            return ResponseEntity.ok(orders.findByUserOrderByOrderedAtDesc(u));
        }

        @GetMapping ResponseEntity<?> all(HttpServletRequest req){
            User u=(User)req.getAttribute("user");
            if(u==null || !"ADMIN".equals(u.role)) return ResponseEntity.status(403).body(Map.of("message","Admin only"));
            return ResponseEntity.ok(orders.findAllByOrderByOrderedAtDesc());
        }
    }

    @RestControllerAdvice
    static class Errors {
        @ExceptionHandler(RuntimeException.class)
        ResponseEntity<?> error(RuntimeException e){
            return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
        }
    }
}
