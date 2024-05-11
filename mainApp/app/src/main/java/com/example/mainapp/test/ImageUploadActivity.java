package com.example.mainapp.test;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.RequestQueue;
import com.example.mainapp.R;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class ImageUploadActivity extends AppCompatActivity {
    ImageView imageView;
    EditText textview;
    String encodedImage;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addphoto_activity); // 화면 레이아웃을 설정하도록 해야 합니다.

        //imageView = findViewById(R.id.upload_image);
        Button send_btn = findViewById(R.id.upload_button);

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"눌림", Toast.LENGTH_SHORT).show();
            }
        });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = getBitmapFromUri(selectedImageUri);
                            imageView.setImageBitmap(bitmap);

                            encodedImage = encodeImage(bitmap); // 이미지를 문자열로 인코딩합니다.
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        openGallery();

    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent); // 변경된 방식으로 갤러리를 엽니다.
    }
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        InputStream imageStream = getContentResolver().openInputStream(uri);
        return BitmapFactory.decodeStream(imageStream);
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

}
