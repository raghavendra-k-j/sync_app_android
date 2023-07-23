package com.example.syncapp.activities.mypictures;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.syncapp.database.DatabaseMethods;
import com.example.syncapp.database.repo.MyPictureRepo;
import com.example.syncapp.databinding.ActivityMyPictureBinding;
import com.example.syncapp.entity.MyPicture;
import com.example.syncapp.util.LogHelper;
import com.github.javafaker.Faker;

import java.util.ArrayList;

public class MyPicturesActivity extends AppCompatActivity {

    private ActivityMyPictureBinding binding;

    private ArrayList<MyPicture> myPictureArrayList;
    private MyPicturesAdapter myPicturesAdapter;
    private DatabaseMethods databaseMethods;

    private MyPictureRepo myPictureRepo;

    private Faker faker;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPictureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        faker = new Faker();
        binding.myPictureToolbar.setNavigationOnClickListener(v -> onBackPressed());

        myPictureArrayList = new ArrayList<>();
        myPicturesAdapter = new MyPicturesAdapter(this, myPictureArrayList);
        databaseMethods = new DatabaseMethods(this);
        myPictureRepo = new MyPictureRepo(databaseMethods);
        binding.myPicturesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.myPicturesRecyclerView.setAdapter(myPicturesAdapter);

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.getData() != null) {
                    Uri imageUri = data.getData();

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("Enter Citizenship ID");

                    // Create the edit text programmatically
                    final EditText inputEditText = new EditText(this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    );
                    inputEditText.setLayoutParams(lp);
                    inputEditText.setText(faker.animal().name().toLowerCase());
                    inputEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                    alertDialogBuilder.setView(inputEditText);

                    alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) -> {
                        String citizenShipId = inputEditText.getText().toString().trim();
                        if (citizenShipId.isEmpty()) {
                            Toast.makeText(this, "Please Enter Citizenship Id", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                MyPicture addedPicture = myPictureRepo.add(MyPicturesActivity.this, citizenShipId, imageUri);
                                if (addedPicture != null) {
                                    myPictureArrayList.add(addedPicture);
                                    myPicturesAdapter.addItem(addedPicture);
                                    Toast.makeText(MyPicturesActivity.this, "Picture added successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MyPicturesActivity.this, "Failed to add picture", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Log.e(LogHelper.tag(this), "onCreate: " + e.getMessage());
                                Toast.makeText(MyPicturesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    alertDialogBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });

        binding.myPicturesAddButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        loadMyPictures();
    }

    private void loadMyPictures() {
        myPictureArrayList = myPictureRepo.findAll();
        myPicturesAdapter.setItems(myPictureArrayList);
    }
}
