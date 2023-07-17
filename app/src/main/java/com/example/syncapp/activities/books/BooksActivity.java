package com.example.syncapp.activities.books;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.syncapp.auth.Auth;
import com.example.syncapp.database.DatabaseMethods;
import com.example.syncapp.databinding.ActivityBooksBinding;
import com.example.syncapp.entity.Book;
import com.example.syncapp.util.IdGenerator;
import com.example.syncapp.util.LogHelper;
import com.github.javafaker.Faker;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BooksActivity extends AppCompatActivity implements BooksAdapter.BookItemActionListener {

    private static final String TAG = LogHelper.tag(BooksActivity.class);
    private Auth auth;
    private DatabaseMethods databaseMethods;
    private ActivityBooksBinding binding;

    private ExecutorService executorService;

    private ArrayList<Book> bookArrayList;

    private BooksAdapter booksAdapter;

    private ActivityResultLauncher<Intent> addLauncher, updateLauncher;

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBooksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseMethods = new DatabaseMethods(this);
        auth = new Auth(this);
        bookArrayList = new ArrayList<>();
        booksAdapter = new BooksAdapter(this, bookArrayList);

        addLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            loadBooks();
        });

        updateLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    String bookId = data.getStringExtra("id");
                    int position = data.getIntExtra("position", -1);
                    Book editedBook = databaseMethods.getBookRepo().find(bookId);
                    booksAdapter.updateItem(editedBook, position);
                }
            }
        });


        binding.booksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.booksRecyclerView.setAdapter(booksAdapter);

        binding.booksToolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.booksBtnAdd.setOnClickListener(v -> {
            addLauncher.launch(new Intent(this, AddBookActivity.class));
        });

        Faker faker = new Faker();
        binding.booksBtnAdd.setOnLongClickListener(v -> {
            Toast.makeText(this, "Started Adding 100 Books", Toast.LENGTH_SHORT).show();
            ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
            threadExecutor.execute(() -> {
                runOnUiThread(() -> {
                    binding.booksCount.setText("Adding Books...");
                });
                int successCount = 0;
                int failedCount = 0;
                for (int i = 0; i < 100; i++) {
                    try {
                        com.github.javafaker.Book fakeBook = faker.book();
                        databaseMethods.getBookRepo().add(new Book(IdGenerator.generate(auth), auth.getUserId(), fakeBook.title(), fakeBook.author(), faker.lorem().paragraph(100)));
                        successCount++;
                    }
                    catch (Exception e) {
                        failedCount++;
                        runOnUiThread(() -> {
                            Log.d(TAG, "onCreate: " + e.getMessage());
                        });
                    }
                }
                int finalSuccessCount = successCount;
                int finalFailedCount = failedCount;
                runOnUiThread(() -> {
                    Toast.makeText(this, (String.format("%02d Books added successfully, %02d Failed to add", finalSuccessCount, finalFailedCount)), Toast.LENGTH_SHORT).show();
                    loadBooks();
                });
            });
            threadExecutor.shutdown();
            return true;
        });


        executorService = Executors.newSingleThreadExecutor();

        loadBooks();
    }

    @Override
    protected void onDestroy() {
        executorService.shutdown();
        super.onDestroy();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void loadBooks() {
        executorService.execute(() -> {
            runOnUiThread(() -> {
                binding.booksCount.setOnClickListener(null);
                binding.booksCount.setText("Loading...");
            });
            bookArrayList = databaseMethods.getBookRepo().findAll();
            runOnUiThread(() -> {
                booksAdapter.setItems(bookArrayList);
                binding.booksCount.setText(String.format("Total: %02d", bookArrayList.size()));
                binding.booksCount.setOnClickListener(v -> loadBooks());
            });
        });
    }

    @Override
    public void onEditBookClicked(Book book, int position) {
        Intent intent = new Intent(this, UpdateBookActivity.class);
        intent.putExtra("id", book.getId());
        intent.putExtra("position", position);
        updateLauncher.launch(intent);
    }
}
