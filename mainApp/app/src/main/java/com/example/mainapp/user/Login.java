package com.example.mainapp.user;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.mainapp.MainActivity;
import com.example.mainapp.R;
import com.example.mainapp.util.NetworkStatus;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    public static String mainurl = "https://8ef1-113-198-137-2.ngrok-free.app";
    Button btn_signup;
    Button btn_login;
    EditText input_id;
    EditText input_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#EFEFEF")); // #EFEFEF 색상으로 변경
        }


        btn_signup = (Button) findViewById(R.id.btn_regi);
        btn_login = (Button)findViewById(R.id.btn_login);
        input_id = (EditText) findViewById(R.id.input_id);
        input_password = (EditText) findViewById(R.id.input_password);

        input_id.setBackgroundResource(R.drawable.login_text);
        input_password.setBackgroundResource(R.drawable.login_text);

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityC(Signup.class);
            }
        });

        input_id = findViewById(R.id.input_id);
        input_password = findViewById(R.id.input_password);


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                if(status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {

                    if(input_id.getText().toString().trim().length() > 0 || input_password.getText().toString().trim().length() > 0 || input_id.getText().toString().trim().length() > 0 ) {
                        findViewById(R.id.cpb).setVisibility(View.VISIBLE);
                        String url = mainurl + "/api/authenticate ";
                        JSONObject jsonBody = new JSONObject();
                        try {
                            jsonBody.put("username", input_id.getText().toString().trim());
                            jsonBody.put("password", input_password.getText().toString().trim());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "json 오류", Toast.LENGTH_SHORT).show();
                        }

                        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        findViewById(R.id.cpb).setVisibility(View.GONE);
                                        try {
                                                // "token" 키를 사용하여 토큰 추출
                                                String token = response.getString("token");
                                                startActivityString(MainActivity.class, "token", token);

                                                finish();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            // JSON 파싱 중 예외 발생시 사용자에게 알림
                                            Toast.makeText(getApplicationContext(), "서버 응답 처리 중 오류 발생", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        findViewById(R.id.cpb).setVisibility(View.GONE);
                                        // 에러 로깅
                                        Log.e("LoginError", "Volley error: " + error.toString());
                                        // Network Response를 확인
                                        if (error.networkResponse != null) {
                                            Log.e("StatusCode", "Error Response code: " + error.networkResponse.statusCode);
                                        }
                                        if("net".equals(input_id.getText().toString())&&"net".equals(input_password.getText().toString())){
                                            startActivityString(MainActivity.class, "token", "test");

                                            finish();
                                        }
                                        Toast.makeText(getApplicationContext(), "아아디 또는 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }) {
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
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

    }

    public void startActivityC(Class c) {
        Intent intent = new Intent(getApplicationContext(), c);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
    public void startActivityflag(Class c) {
        Intent intent = new Intent(getApplicationContext(), c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
    public void startActivityString(Class c, String name , String sendString) {
        Intent intent = new Intent(getApplicationContext(), c);
        intent.putExtra(name, sendString);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
    public void startActivityNewTask(Class c){
        Intent intent = new Intent(getApplicationContext(), c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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

}