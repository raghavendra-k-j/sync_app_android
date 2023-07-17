package com.example.syncapp.database.repo;

import com.example.syncapp.database.DatabaseMethods;
import com.example.syncapp.entity.Book;
import com.example.syncapp.util.LogHelper;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

public class BookRepo {

    public static final String TAG = LogHelper.tag(BookRepo.class);

    public static final String ENTITY = "Book";

    private final DatabaseMethods databaseMethods;

    public BookRepo(DatabaseMethods databaseMethods) {
        this.databaseMethods = databaseMethods;
    }

    public Book add(Book book) throws Exception {
        if (book.getId() == null || book.getId().isEmpty()) {
            throw new Exception("Book ID is required");
        }

        if (book.getUserId() <= 0) {
            throw new Exception("User ID must be a positive number");
        }

        Book previousBook = find(book.getId());
        if (previousBook != null) {
            throw new Exception("A book with the same ID already exists");
        }


        String title = book.getTitle();
        if (title == null || title.isEmpty()) {
            throw new Exception("Book title is required");
        }

        if (title.length() > 255) {
            throw new Exception("Book title cannot exceed 255 characters");
        }

        String author = book.getAuthor();
        if (author == null || author.isEmpty()) {
            throw new Exception("Book author is required");
        }

        if (author.length() > 40) {
            throw new Exception("Book author cannot exceed 40 characters");
        }

        // Check if the title is unique
        if (existsByTitle(title)) {
            throw new Exception("Book with the same title already exists");
        }

        String content = book.getContent();
        if (content == null || content.isEmpty()) {
            throw new Exception("Book content is required");
        }

        int rowsAffected = databaseMethods.getDatabaseHelper().getBookDao().create(book);
        if (rowsAffected == 1) {
            databaseMethods.getTransactionRepo().addTransaction(ENTITY, "add", book.getId(), new Date());
            return find(book.getId());
        } else {
            return null;
        }
    }

    public Book update(Book book) throws Exception {
        if (book.getId() == null || book.getId().isEmpty()) {
            throw new Exception("Book ID is required");
        }

        if (book.getUserId() <= 0) {
            throw new Exception("User ID must be a positive number");
        }

        Book previousBook = find(book.getId());
        if (previousBook == null) {
            throw new Exception("Cannot update a non-existing book");
        }

        String title = book.getTitle();
        if (title == null || title.isEmpty()) {
            throw new Exception("Book title is required");
        }

        if (title.length() > 255) {
            throw new Exception("Book title cannot exceed 255 characters");
        }

        if (existsByTitleAndId(title, book.getId())) {
            throw new Exception("Book with the same title already exists");
        }

        String author = book.getAuthor();
        if (author == null || author.isEmpty()) {
            throw new Exception("Book author is required");
        }

        if (author.length() > 40) {
            throw new Exception("Book author cannot exceed 40 characters");
        }

        String content = book.getContent();
        if (content == null || content.isEmpty()) {
            throw new Exception("Book content is required");
        }

        int rowsAffected = databaseMethods.getDatabaseHelper().getBookDao().update(book);
        if (rowsAffected == 1) {
            databaseMethods.getTransactionRepo().mergeTransaction(ENTITY, "update", book.getId(), new Date());
            return find(book.getId());
        } else {
            return null;
        }
    }

    public boolean delete(String id) throws Exception {
        if (id == null || id.isEmpty()) {
            throw new Exception("Book ID is required");
        }

        Book book = find(id);
        if (book == null) {
            throw new Exception("Cannot delete a non-existing book");
        }

        int rowsAffected = databaseMethods.getDatabaseHelper().getBookDao().delete(book);
        if (rowsAffected == 1) {
            databaseMethods.getTransactionRepo().addTransaction(ENTITY, "delete", book.getId(), new Date());
            return true;
        } else {
            return false;
        }
    }

    public Book find(String id) {
        return databaseMethods.getDatabaseHelper().getBookDao().queryForId(id);
    }

    public boolean existsByTitle(String title) {
        QueryBuilder<Book, String> queryBuilder = databaseMethods.getDatabaseHelper().getBookDao().queryBuilder();
        try {
            queryBuilder.where().eq("title", new SelectArg(title));
            return queryBuilder.queryForFirst() != null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean existsByTitleAndId(String title, String id) {
        QueryBuilder<Book, String> queryBuilder = databaseMethods.getDatabaseHelper().getBookDao().queryBuilder();
        try {
            queryBuilder.where().eq("title", new SelectArg(title)).and().ne("id", id);
            return queryBuilder.queryForFirst() != null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Book> findAll() {
        return (ArrayList<Book>) databaseMethods.getDatabaseHelper().getBookDao().queryForAll();
    }
}
