package app.controller;

import app.exception.NotFoundException;
import app.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import app.repository.BookRepository;

/**
 * Created by jiankuan on 19/5/15.
 */
@RestController
public class BookController {

    @Autowired
    public BookRepository bookRepository;

    @RequestMapping("/book/{name}")
    public Book getBookByName(@PathVariable String name) {
        Book book = bookRepository.findBookByName(name);
        if (book == null) {
            throw new NotFoundException();
        }

        return book;
    }

    @RequestMapping(value = "/book/new", method = RequestMethod.POST)
    public HttpEntity<Book> insertBook(@RequestBody Book book) {
        bookRepository.save(book);
        return new ResponseEntity<>(book, HttpStatus.CREATED);
    }
}
