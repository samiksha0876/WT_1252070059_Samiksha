package com.booknest.controller;
import com.booknest.model.User; import com.booknest.repository.UserRepository; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController @RequestMapping("/api/users") @CrossOrigin(origins="http://localhost:5173")
public class UserController { private final UserRepository repo; public UserController(UserRepository repo){this.repo=repo;}
 @PostMapping("/register") public ResponseEntity<?> register(@RequestBody User user){ if(repo.existsByEmail(user.getEmail())) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","Email already registered")); return ResponseEntity.ok(repo.save(user)); }
 @PostMapping("/login") public ResponseEntity<?> login(@RequestBody User input){ return repo.findByEmail(input.getEmail()).filter(u->u.getPassword().equals(input.getPassword())).map(u->ResponseEntity.ok(Map.of("message","Login successful","name",u.getFullName(),"email",u.getEmail()))).orElseGet(()->ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","Invalid email or password"))); }
}
