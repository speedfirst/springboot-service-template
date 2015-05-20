package app.repository;

import app.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by jiankuan on 19/5/15.
 */
public interface BookRepository extends MongoRepository<Book, Integer> {

   // most CURD methods have been implemented somewhere, define dummy one
   Book findBookByName(String name);
}
