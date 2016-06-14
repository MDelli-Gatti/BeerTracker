package com.theironyard;

/**
 * Created by michaeldelli-gatti on 6/9/16.
 */
public class Beer {
    int id;
    String name;
    String brewery;
    int rating;
    String comment;

    

    public Beer(int id, String name, String brewery, int rating, String comment) {
        this.id = id;
        this.name = name;
        this.brewery = brewery;
        this.rating = rating;
        this.comment = comment;

    }
}
