package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;

public class Main {

    static HashMap<String, User> users = new HashMap<>();

    public static void main(String[] args) {

        Spark.init();
        Spark.get(
                "/",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");

                    HashMap m = new HashMap();
                    if (username == null) {
                        return new ModelAndView(m, "login.html");
                    }
                    else {
                        User user = users.get(username);
                        m.put("beers", user.beers);
                        m.put("username", username);
                        return new ModelAndView(m, "home.html");
                    }
                },
                new MustacheTemplateEngine()
        );
        Spark.get(
                "/edit",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    HashMap m = new HashMap();
                    User user = users.get(username);
                    int idNum = Integer.valueOf(request.queryParams("id"));
                    Beer br = user.beers.get(idNum);
                    m.put("beer", br);
                    return new ModelAndView(m,"edit.html");
                },
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                (request, response) -> {
                    String name = request.queryParams("username");
                    String pass = request.queryParams("password");
                    if (name == null || pass == null){
                        throw new Exception("name or pass not set");
                    }

                    User user = users.get(name);
                    if (user == null){
                        user = new User(name, pass);
                        users.put(name, user);
                    }
                    else if (!pass.equals(user.password)){
                        throw new Exception("wrong password");
                    }

                    Session session = request.session();
                    session.attribute("username", name);

                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/logout",
                (request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/create-beer",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    if (username == null){
                        throw new Exception("not logged in");
                    }

                    String name = request.queryParams("name");
                    String brewery = request.queryParams("brewery");
                    int rating = Integer.valueOf(request.queryParams("rating"));
                    String comment = request.queryParams("comment");
                    if (name == null || brewery == null || comment == null){
                        throw new Exception("invalid form fields");
                    }

                    User user = users.get(username);
                    if (user == null){
                        throw new Exception("User does not exist");
                    }

                    Beer b = new Beer(name, brewery, rating, comment, user.beers.size());
                    user.beers.add(b);

                    response.redirect("/");
                    return "";

                }
        );
        Spark.post(
                "/delete-beer",
                (request, response) -> {
                    int id = Integer.valueOf(request.queryParams("id"));

                    Session session = request.session();
                    String username = session.attribute("username");
                    User user = users.get(username);


                    user.beers.remove(id);
                    int index = 0; //reset ids
                    for (Beer b : user.beers){
                        b.id = index;
                        index++;
                    }
                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/edit-beer",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    if (username == null) {
                        throw new Exception("not logged in");
                    }
                    User user = users.get(username);
                    int id = Integer.valueOf(request.queryParams("id"));

                    Beer b = user.beers.get(id);
                    b.name = request.queryParams("name");
                    b.brewery = request.queryParams("brewery");
                    b.rating = Integer.valueOf(request.queryParams("rating"));
                    b.comment = request.queryParams("comment");
                    user.beers.add(b);

                    response.redirect("/");
                    return "";
                }
        );

    }
}
