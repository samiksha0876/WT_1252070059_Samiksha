package com.booknest.controller;
import com.booknest.model.Book; import com.booknest.repository.BookRepository; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/books") @CrossOrigin(origins="http://localhost:5173")
public class BookController { private final BookRepository repo; public BookController(BookRepository repo){this.repo=repo;}
 @GetMapping public List<Book> all(@RequestParam(required=false) String category){return category==null||category.isBlank()?repo.findAll():repo.findByCategoryIgnoreCase(category);}
 @GetMapping("/{id}") public Book one(@PathVariable String id){return repo.findById(id).orElseThrow();}
}
