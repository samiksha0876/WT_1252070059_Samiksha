package com.booknest.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="books")
public class Book {
 @Id private String id; private String title; private String author; private double price; private String category; private String coverUrl; private double rating;
 public Book() {}
 public Book(String id,String title,String author,double price,String category,String coverUrl,double rating){this.id=id;this.title=title;this.author=author;this.price=price;this.category=category;this.coverUrl=coverUrl;this.rating=rating;}
 public String getId(){return id;} public void setId(String id){this.id=id;} public String getTitle(){return title;} public void setTitle(String v){title=v;} public String getAuthor(){return author;} public void setAuthor(String v){author=v;} public double getPrice(){return price;} public void setPrice(double v){price=v;} public String getCategory(){return category;} public void setCategory(String v){category=v;} public String getCoverUrl(){return coverUrl;} public void setCoverUrl(String v){coverUrl=v;} public double getRating(){return rating;} public void setRating(double v){rating=v;}
}
