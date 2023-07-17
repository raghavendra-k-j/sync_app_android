package com.example.syncapp.activities.books;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.syncapp.auth.Auth;
import com.example.syncapp.database.DatabaseMethods;
import com.example.syncapp.databinding.ActivityAddBookBinding;
import com.example.syncapp.entity.Book;
import com.example.syncapp.util.IdGenerator;
import com.example.syncapp.util.LogHelper;
import com.github.javafaker.Faker;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

public class AddBookActivity extends AppCompatActivity {

    private static final String TAG = LogHelper.tag(AddBookActivity.class);
    private DatabaseMethods databaseMethods;

    private ActivityAddBookBinding binding;

    private Auth auth;

    private Faker faker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseMethods = new DatabaseMethods(this);
        auth = new Auth(this);
        faker = new Faker();

        com.github.javafaker.Book fakeBook = faker.book();
        binding.addBookEditTextTitle.setText(fakeBook.title());
        binding.addBookEditTextAuthor.setText(fakeBook.author());
        binding.addBookEditTextContent.setText(faker.lorem().paragraph(100));
        calculateTextSize(Objects.requireNonNull(binding.addBookEditTextContent.getText()).toString());
        binding.addBookEditTextContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @SuppressLint("DefaultLocale")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTextSize(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.addBookToolbar.setNavigationOnClickListener(v-> onBackPressed());

        binding.addBookBtnSave.setOnClickListener(v -> {
            addBook();
        });
        
        binding.addBookBtnSave.setOnLongClickListener(v -> {
            refillFields();
            return true;
        });
    }

    private void addBook() {
        String title = Objects.requireNonNull(binding.addBookEditTextTitle.getText()).toString().trim();
        String author = Objects.requireNonNull(binding.addBookEditTextAuthor.getText()).toString().trim();
        String content = Objects.requireNonNull(binding.addBookEditTextContent.getText()).toString().trim();

        Book book = new Book();
        book.setId(IdGenerator.generate(auth));
        book.setUserId(auth.getUserId());
        book.setTitle(title);
        book.setContent(content);
        book.setAuthor(author);

        try {
            databaseMethods.getBookRepo().add(book);
            Snackbar.make(binding.getRoot(), "Book Added Successfully", Snackbar.LENGTH_INDEFINITE).setAction("Refill", v -> {
                refillFields();
            }).setAnchorView(binding.addBookBtnSave).show();
        }
        catch (Exception e) {
            Log.e(TAG, "addBook: ", e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void refillFields() {
        com.github.javafaker.Book fakeBook = faker.book();
        binding.addBookEditTextTitle.setText(fakeBook.title());
        binding.addBookEditTextAuthor.setText(fakeBook.author());
        binding.addBookEditTextContent.setText(faker.lorem().paragraph(100));
        calculateTextSize(binding.addBookEditTextContent.getText().toString());
        Toast.makeText(this, "Refilled", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("DefaultLocale")
    private void calculateTextSize(CharSequence s) {
        binding.addBookLabelContentSize.setText(String.format("%.6f KB", (s.toString().getBytes().length / 1024.0)));
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
