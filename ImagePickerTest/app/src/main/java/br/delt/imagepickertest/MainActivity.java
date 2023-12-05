package br.delt.imagepickertest;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    ActivityResultLauncher<PickVisualMediaRequest> launcher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), new ActivityResultCallback<Uri>() {
        public void onActivityResult(Uri o) {
            if (o == null) {
                Toast.makeText(MainActivity.this, "No image Selected", Toast.LENGTH_SHORT).show();
            } else {
                //Glide.with(getApplicationContext()).load(o).into(imageView);
                try {
                    imageView.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(),o));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        MaterialButton pickImage = findViewById(R.id.pickImage);

        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launcher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });
    }
}