package com.booknest.repository;
import com.booknest.model.Book; import org.springframework.data.mongodb.repository.MongoRepository; import java.util.List;
public interface BookRepository extends MongoRepository<Book,String>{ List<Book> findByCategoryIgnoreCase(String category); }
