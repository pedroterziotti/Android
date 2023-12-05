package br.delt.acess;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.sql.Driver;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private ImageView imageView;
    private EditText nomeEditText, cargoEditText;
    private TextView lastAcessView, rfidView;
    private Button deleteButton, saveButton;
    private User selectedUser;
    private String imageURI ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        nomeEditText = findViewById(R.id.detailNome);
        cargoEditText = findViewById(R.id.detailCargo);

        lastAcessView = findViewById(R.id.detailLastAcess);
        rfidView = findViewById(R.id.detailRfidKey);

        imageView = findViewById(R.id.detailImage);

        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        checkEdit();

    }

    private void checkEdit()
    {
        Intent previousIntent = getIntent();
        int passedUserID = previousIntent.getIntExtra(User.USER_EDIT_EXTRA, -1);
        String passedRFID = previousIntent.getStringExtra(User.USER_RFID_EXTRA);
        if(passedRFID!=null)
        {
            rfidView.setText(passedRFID);
        }

        selectedUser = User.getUserForID(passedUserID);

        if(selectedUser !=null)
        {
            nomeEditText.setText(selectedUser.getNome());
            cargoEditText.setText(selectedUser.getCargo());
            lastAcessView.setText(SQLiteManager.getStringFromDate(selectedUser.getLastAcess()));
            rfidView.setText(selectedUser.getRfidKey());
            imageURI=selectedUser.getPictureURI();
            try {
                int flag = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                getApplicationContext().getContentResolver().takePersistableUriPermission(Uri.parse(imageURI), flag);
                imageView.setImageBitmap(MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), Uri.parse(imageURI)));
            } catch (Exception e) {
            }

        }
        else {
            lastAcessView .setText((new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date())));
        }

    }

    public void saveUser(View view)
    {
        SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this);
        if(selectedUser == null)
        {
            int id = User.userArrayList.size();
            User newUser = new User(id, String.valueOf(rfidView.getText()),String.valueOf(cargoEditText.getText()), imageURI,
                    SQLiteManager.getDateFromString(String.valueOf(lastAcessView.getText())),String.valueOf(nomeEditText.getText()));

            User.userArrayList.add(newUser);
            sqLiteManager.addUserToDatabase(newUser);

        }
        else {
            selectedUser.setNome(String.valueOf(nomeEditText.getText()));
            selectedUser.setCargo(String.valueOf(cargoEditText.getText()));
            selectedUser.setPictureURI(imageURI);
            sqLiteManager.updateUserInDB(selectedUser);
        }

        finish();

    }

    public void deleteUser(View view)
    {
        SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this);
        if(selectedUser != null)
        {
            sqLiteManager.deleteUserInDB(selectedUser);
            User.userArrayList.remove(selectedUser);
        }
        finish();
    }


    ActivityResultLauncher<PickVisualMediaRequest> launcher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), new ActivityResultCallback<Uri>() {
        public void onActivityResult(Uri o) {

            if (o == null) {
                Toast.makeText(DetailActivity.this, "No image Selected", Toast.LENGTH_SHORT).show();
            } else {
                //Glide.with(getApplicationContext()).load(o).into(imageView);
                try {
                    imageURI=o.toString();
                    int flag = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    getApplicationContext().getContentResolver().takePersistableUriPermission(Uri.parse(imageURI), flag);
                    imageView.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(),o));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    });
    public void findImage(View view)
    {
        launcher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

}