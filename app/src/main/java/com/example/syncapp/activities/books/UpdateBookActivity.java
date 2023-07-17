package com.example.syncapp.activities.books;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.syncapp.auth.Auth;
import com.example.syncapp.database.DatabaseMethods;
import com.example.syncapp.databinding.ActivityAddBookBinding;
import com.example.syncapp.entity.Book;
import com.example.syncapp.util.LogHelper;

import java.util.Objects;

public class UpdateBookActivity extends AppCompatActivity {

    private static final String TAG = LogHelper.tag(AddBookActivity.class);
    private DatabaseMethods databaseMethods;

    private ActivityAddBookBinding binding;

    private Auth auth;

    private Book book;

    private int position;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseMethods = new DatabaseMethods(this);
        auth = new Auth(this);

        binding.addBookToolbar.setTitle("Update Book");

        String bookId = getIntent().getStringExtra("id");
        position = getIntent().getIntExtra("position", -1);
        book = databaseMethods.getBookRepo().find(bookId);
        if(book == null) {
            Toast.makeText(this, "Book not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.addBookEditTextTitle.setText(book.getTitle());
        binding.addBookEditTextContent.setText(book.getContent());
        binding.addBookEditTextAuthor.setText(book.getAuthor());

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
            updateBook();
        });
    }

    private void updateBook() {
        String title = Objects.requireNonNull(binding.addBookEditTextTitle.getText()).toString().trim();
        String author = Objects.requireNonNull(binding.addBookEditTextAuthor.getText()).toString().trim();
        String content = Objects.requireNonNull(binding.addBookEditTextContent.getText()).toString().trim();

        book.setTitle(title);
        book.setAuthor(author);
        book.setContent(content);

        try {
            databaseMethods.getBookRepo().update(book);
        }
        catch (Exception e) {
            Log.e(TAG, "updateBook: ", e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Book Updated Successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("id", book.getId());
        intent.putExtra("position", position);
        setResult(RESULT_OK, intent);
        finish();
    }

    @SuppressLint("DefaultLocale")
    private void calculateTextSize(CharSequence s) {
        binding.addBookLabelContentSize.setText(String.format("%.6f KB", (s.toString().getBytes().length / 1024.0)));
    }
}

