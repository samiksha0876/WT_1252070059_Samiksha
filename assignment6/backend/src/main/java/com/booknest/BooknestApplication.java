package com.booknest;

import com.booknest.model.Book;
import com.booknest.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.List;

@SpringBootApplication
public class BooknestApplication {
  public static void main(String[] args) { SpringApplication.run(BooknestApplication.class, args); }

  @Bean CommandLineRunner seedBooks(BookRepository repo) {
    return args -> {
      if (repo.count() == 0) repo.saveAll(List.of(
        new Book(null,"Atomic Habits","James Clear",399,"Self Help","https://covers.openlibrary.org/b/isbn/9780735211292-L.jpg",4.8),
        new Book(null,"The Psychology of Money","Morgan Housel",349,"Business","https://covers.openlibrary.org/b/isbn/9780857197689-L.jpg",4.7),
        new Book(null,"Sapiens","Yuval Noah Harari",450,"History","https://covers.openlibrary.org/b/isbn/9780062316097-L.jpg",4.8),
        new Book(null,"Think Like a Monk","Jay Shetty",399,"Self Help","https://covers.openlibrary.org/b/isbn/9781982134488-L.jpg",4.6),
        new Book(null,"The Midnight Library","Matt Haig",399,"Fiction","https://covers.openlibrary.org/b/isbn/9780525559474-L.jpg",4.6),
        new Book(null,"Ikigai","Héctor García",350,"Self Help","https://covers.openlibrary.org/b/isbn/9780143130727-L.jpg",4.5),
        new Book(null,"Dune","Frank Herbert",499,"Science Fiction","https://covers.openlibrary.org/b/isbn/9780441172719-L.jpg",4.9),
        new Book(null,"The Alchemist","Paulo Coelho",350,"Fiction","https://covers.openlibrary.org/b/isbn/9780062315007-L.jpg",4.7)
      ));
    };
  }
}
