package com.theironyard;

/**
 * Created by michaeldelli-gatti on 6/9/16.
 */
public class Beer {
    String name;
    String brewery;
    int rating;
    String comment;
    int id;
    

    public Beer(String name, String brewery, int rating, String comment, int id) {
        this.name = name;
        this.brewery = brewery;
        this.rating = rating;
        this.comment = comment;
        this.id = id;
    }
}
