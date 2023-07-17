package com.example.syncapp.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONException;
import org.json.JSONObject;

@DatabaseTable(tableName = "books")
public class Book {

    @DatabaseField(id = true, columnName = "id")
    private String id;

    @DatabaseField(columnName = "userId", canBeNull = false)
    private int userId;

    @DatabaseField(columnName = "title", unique = true, canBeNull = false)
    private String title;

    @DatabaseField(columnName = "author", canBeNull = false)
    private String author;

    @DatabaseField(columnName = "content", canBeNull = false)
    private String content;

    public Book() {

    }

    public Book(String id, int userId, String title, String author, String content) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.author = author;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("userId", userId);
            jsonObject.put("title", title);
            jsonObject.put("author", author);
            jsonObject.put("content", content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static Book fromJSONObject(JSONObject jsonObject) {
        Book book = new Book();
        try {
            book.setId(jsonObject.getString("id"));
            book.setUserId(jsonObject.getInt("userId"));
            book.setTitle(jsonObject.getString("title"));
            book.setAuthor(jsonObject.getString("author"));
            book.setContent(jsonObject.getString("content"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return book;
    }

}