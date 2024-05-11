package com.example.mainapp.mainfragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.mainapp.MainActivity;
import com.example.mainapp.R;
import com.example.mainapp.user.Login;
import com.example.mainapp.util.NetworkStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AddPhotoActivite extends AppCompatActivity {

    ImageView imageView;
    EditText textview;
    String encodedImage;

    Button upload_btn;
    String text;

    private ActivityResultLauncher<Intent> imagePickerLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addphoto_activity);

        upload_btn = findViewById(R.id.upload_button);

        imageView = findViewById(R.id.upload_image);

        textview = findViewById(R.id.upload_text);

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = textview.getText().toString();

                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                if(status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {

                    if(text.length() > 0 && encodedImage.length() >0 ) {
                        String url = Login.mainurl+"/api/upload_post";
                        JSONObject jsonBody = new JSONObject();
                        try {
                            jsonBody.put("username", MainActivity.username.trim());
                            jsonBody.put("explain", text.trim());
                            jsonBody.put("likes", 0);
                            jsonBody.put("imageData", encodedImage.trim());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "json 오류", Toast.LENGTH_SHORT).show();
                        }

                        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Toast.makeText(getApplicationContext(), "공유 성공", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        // 네트워크 응답 자체가 없는 경우 (예: 네트워크 연결 불가)
                                        String errorMessage = "네트워크 오류가 발생했습니다. 연결을 확인하세요.";

                                        if (error.networkResponse != null) {
                                            int statusCode = error.networkResponse.statusCode;
                                            String responseBody = new String(error.networkResponse.data);
                                            errorMessage += "\n응답 코드: " + statusCode + "\n응답 내용: " + responseBody;
                                        }

                                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                }) {
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "Bearer " + MainActivity.token);
                                return headers;
                            }
                        };
                        queue.add(request);
                    }else {
                        Toast.makeText(getApplicationContext(), "입력 안된 칸이 있습니다.", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
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
                            encodedImage = encodeImage(bitmap);
                            //Bitmap decodedBitmap = decodeBase64(encodedImage);
                            //imageView.setImageBitmap(decodedBitmap);
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //Log.i("login_log", "이벤트 발생");
        View focusView = getCurrentFocus();
        if (focusView != null) {
            //Log.i("login_log", "포커스가 있다면");
            Rect rect = new Rect();
            focusView.getGlobalVisibleRect(rect);
            int x = (int) ev.getX(), y = (int) ev.getY();
            if (!rect.contains(x, y)) {
                // 터치위치가 태그위치에 속하지 않으면 키보드 내리기
                //Log.i("login_log", "터치위치가 태그위치에 속하지 않으면 키보드 내리기");
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                focusView.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }
//    private Bitmap decodeBase64(String base64ImageString) {
//        byte[] decodedBytes = Base64.decode(base64ImageString, Base64.DEFAULT);
//        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
//    }
}
