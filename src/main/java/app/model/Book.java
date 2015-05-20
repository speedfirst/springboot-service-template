package app.model;

import org.springframework.data.annotation.Id;

/**
 * Created by jiankuan on 19/5/15.
 */
public class Book {
    @Id
    public String _id;

    public int id;

    public String name;

    public double price;
}
