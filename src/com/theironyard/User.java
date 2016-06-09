package com.theironyard;

import java.util.ArrayList;

/**
 * Created by michaeldelli-gatti on 6/9/16.
 */
public class User {
    String name;
    String password;
    ArrayList<Beer> beers = new ArrayList<>();

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }
}
