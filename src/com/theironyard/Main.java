package com.theironyard;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void editBeer (Connection conn, int id, String name, String brewery, int rating, String comment) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE beers SET name = ?, brewery = ?, rating = ?, comment = ? WHERE id = ?");
        stmt.setString(1, name);
        stmt.setString(2, brewery);
        stmt.setInt(3, rating);
        stmt.setString(4, comment);
        stmt.setInt(5, id);
        stmt.execute();
    }
    public static void insertBeer (Connection conn, String name, String brewery, int rating, String comment, int userId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO beers VALUES (NULL, ?, ?, ?, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, brewery);
        stmt.setInt(3, rating);
        stmt.setString(4, comment);
        stmt.setInt(5, userId);
        stmt.execute();
    }

    public static void deleteBeer(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM beers WHERE id = ?");
        stmt.setInt(1, id);
        stmt.execute();
    }

    public static ArrayList<Beer> selectBeers(Connection conn, int userId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM beers INNER JOIN users ON beers.user_id = users.id WHERE users.id = ?");
        stmt.setInt(1, userId);
        ResultSet results = stmt.executeQuery();
        ArrayList<Beer> beers = new ArrayList<>();
        while (results.next()){
            int id = results.getInt("id");
            String name = results.getString("name");
            String brewery = results.getString("brewery");
            int rating = results.getInt("rating");
            String comment = results.getString("comment");
            Beer b = new Beer(id, name, brewery, rating, comment);
            beers.add(b);
        }
        return beers;
    }

    static void insertUser(Connection conn, String name, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, password);
        stmt.execute();
    }

    static User selectUser(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
        stmt.setString(1, name);
        ResultSet results = stmt.executeQuery();
        if (results.next()){
            int id = results.getInt("id");
            String password = results.getString("password");
            return new User(id, name, password);
        }
        return null;
    }

    public static Beer selectBeer(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM beers INNER JOIN users ON beers.user_id = users.id WHERE users.id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        while (results.next()){
            int idB = results.getInt("id");
            String name = results.getString("name");
            String brewery = results.getString("brewery");
            int rating = results.getInt("rating");
            String comment = results.getString("comment");
            return new Beer(idB, name, brewery, rating, comment);
        }
        return null;
    }

    //static HashMap<String, User> users = new HashMap<>();

    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");

        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS beers (id IDENTITY, name VARCHAR, brewery VARCHAR, rating INT, comment VARCHAR, user_id INT)");
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, password VARCHAR)");


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
                        User user = selectUser(conn, username);
                        m.put("beers", selectBeers(conn, user.id));

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
                    User user = selectUser(conn, username);
                    int idNum = Integer.valueOf(request.queryParams("id"));
                    Beer b = selectBeer(conn, idNum);
                    m.put("beer", b);
                    m.put("user", user);
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

                    User user = selectUser(conn, name);
                    if (user == null){
                        //user = new User(name, pass);
                        //users.put(name, user);
                        insertUser(conn, name, pass);
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

                    User user = selectUser(conn, username);
                    if (user == null){
                        throw new Exception("User does not exist");
                    }

                    insertBeer(conn, name, brewery, rating, comment, user.id);

                    //Beer b = new Beer(user.beers.size(), name, brewery, rating, comment);
                    //user.beers.add(b);

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

                    deleteBeer(conn, id);

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
                    int id = Integer.valueOf(request.queryParams("id"));

                    Beer b = selectBeer(conn, id);

                    b.name = request.queryParams("name");
                    b.brewery = request.queryParams("brewery");
                    b.rating = Integer.valueOf(request.queryParams("rating"));
                    b.comment = request.queryParams("comment");

                    editBeer(conn, b.id, b.name, b.brewery, b.rating, b.comment);

                    response.redirect("/");
                    return "";
                }
        );
    }
}
