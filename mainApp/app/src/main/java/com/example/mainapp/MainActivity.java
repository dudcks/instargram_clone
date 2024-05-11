package com.example.mainapp;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

import com.example.mainapp.mainfragment.AccountFragment;
import com.example.mainapp.mainfragment.AddPhotoActivite;
import com.example.mainapp.mainfragment.SearchFragment;
import com.example.mainapp.mainfragment.ShortsFragment;
import com.example.mainapp.mainfragment.homeFragment;
import com.example.mainapp.user.Login;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static String token;
    public static String nickname;
    public static String username;
    public static Bitmap m_profile_image;
    public static Long follower;

    public static Long following;
    public static Long user_post_num;
    public static String image=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#EFEFEF")); // #EFEFEF 색상으로 변경
        }

        Intent intent = getIntent();
        token = intent.getStringExtra("token");

        if(token.equals("test")){
            nickname="no net";
            username="no net";
            user_post_num = 0L;
            follower = 0L;
            following= 0L;

        } else if(token !=null){
            init();
        }
        else finish();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment, new homeFragment())
                //.addToBackStack(null)
                .commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                fragmentManager.beginTransaction()
                        .replace(R.id.main_fragment, new homeFragment())
                        //.addToBackStack(null)
                        .commit();
            } else if (item.getItemId() == R.id.search) {
                fragmentManager.beginTransaction()
                        .replace(R.id.main_fragment, new SearchFragment())
                        //.addToBackStack(null)
                        .commit();
            } else if (item.getItemId() == R.id.addphoto) {
                startActivityC(AddPhotoActivite.class);
                return true;
            }  else if (item.getItemId() == R.id.account) {
                init();
                fragmentManager.beginTransaction()
                        .replace(R.id.main_fragment, new AccountFragment())
                        //.addToBackStack(null)
                        .commit();
            }

            return true;
        });
    }

    public void init(){
        RequestQueue volleyQueue = Volley.newRequestQueue(this);

        String url = Login.mainurl+"/api/user";

        JsonObjectRequest jsonObjectRequest  = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Jackson ObjectMapper 생성
                            ObjectMapper objectMapper = new ObjectMapper();

                            // JSON 파싱
                            JsonNode jsonNode = objectMapper.readTree(String.valueOf(response));

                            // "nickname" 필드 추출
                            username = jsonNode.get("username").asText();
                            nickname = jsonNode.get("nickname").asText();
                            follower = jsonNode.get("follower").asLong();
                            following = jsonNode.get("following").asLong();
                            user_post_num = jsonNode.get("post").asLong();
                            image = jsonNode.get("profile_image").asText();
                            m_profile_image = decodeBase64(image);

                            Toast.makeText(getApplicationContext(), username +"  "+nickname, Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        volleyQueue.add(jsonObjectRequest);
        }
    public void startActivityC(Class c) {
        Intent intent = new Intent(getApplicationContext(), c);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private Bitmap decodeBase64(String base64ImageString) {
        byte[] decodedBytes = Base64.decode(base64ImageString, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

}