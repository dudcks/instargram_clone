package com.example.mainapp.mainfragment;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.mainapp.MainActivity;
import com.example.mainapp.R;
import com.example.mainapp.user.Login;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import android.provider.MediaStore;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;

public class AccountFragment extends Fragment {

    private static Long account_post_id;
    private TextView puser;
    private TextView pnick;
    private TextView post_num;

    private TextView user_follower;

    private TextView user_following;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private ImageView profile_image;
    public String encodedImage;

    private JSONArray imageArray = new JSONArray();
    private Button load_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_fragment, container, false);
        account_post_id=-1L;

        puser = view.findViewById(R.id.username);
        pnick = view.findViewById(R.id.nickname);
        profile_image = view.findViewById(R.id.profile_image);
        post_num = view.findViewById(R.id.my_post);
        user_follower = view.findViewById(R.id.follower);
        user_following = view.findViewById(R.id.following);

        if (MainActivity.image == null) {
            profile_image.setImageResource(R.drawable.user_profile);
        } else {
            profile_image.setImageBitmap(MainActivity.m_profile_image);
        }

        load_btn = view.findViewById(R.id.account_loadMoreButton);
        load_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                get_post();
                load_btn.setVisibility(View.GONE);
            }
        });

        init();

        Button profile_edit_btn = view.findViewById(R.id.profile_edit);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = getBitmapFromUri(selectedImageUri);
                            profile_image.setImageBitmap(bitmap);
                            encodedImage = encodeImage(bitmap);
                            edit_profile(encodedImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        profile_edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        final GridView gv = view.findViewById(R.id.grid);

        gv.setOnScrollListener(new AbsListView.OnScrollListener() {
            private boolean loading = true;
            private int previousTotal = 0;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                load_btn.setVisibility(View.GONE);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (loading) {
                    if (totalItemCount > previousTotal) {
                        // 이전에 로드된 항목 수보다 더 많은 항목이 로드된 경우
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }

                // 스크롤이 마지막 요소에 도달하면 더보기 버튼을 활성화함
                int visibleThreshold = 6; // 더보기를 로드할 기준이 되는 임계값
                if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                    load_btn.setVisibility(View.VISIBLE);
                    // 더보기 버튼을 활성화하고 데이터를 추가로 로드하는 등의 작업을 수행
                    loading = true;
                }
            }
        });

        return view;
    }

    private void init() {
        post_num.setText(MainActivity.user_post_num+"\n게시물");
        puser.setText(MainActivity.username);
        pnick.setText(MainActivity.nickname);
        user_follower.setText(MainActivity.follower+"\n팔로워");
        user_following.setText(MainActivity.following+"\n팔로잉");
        get_profile();
        if (MainActivity.image == null) {
            profile_image.setImageResource(R.drawable.user_profile);
        } else {
            profile_image.setImageBitmap(MainActivity.m_profile_image);
        }
        get_post();
    }

    private static Bitmap decodeBase64(String base64ImageString) {
        byte[] decodedBytes = Base64.decode(base64ImageString, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private void get_post(){
        String url = Login.mainurl+"/api/my_post?last="+account_post_id+"&username="+ MainActivity.username;
        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject postObject = response.getJSONObject(i);
                                imageArray.put(postObject);
                                long postId = postObject.getLong("postId");
                                account_post_id=postId-1;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        GridView gridView = getActivity().findViewById(R.id.grid);
                        ImageAdapter imageAdapter = new ImageAdapter(getContext(), imageArray);
                        gridView.setAdapter(imageAdapter);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        //Log.e("YourClass", "Error while fetching JSON data: " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + MainActivity.token);
                return headers;
            }
        };

        queue.add(jsonArrayRequest);
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent); // 변경된 방식으로 갤러리를 엽니다.
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        InputStream imageStream = requireContext().getContentResolver().openInputStream(uri);
        return BitmapFactory.decodeStream(imageStream);
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 5, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void edit_profile(String encodedImage) {
        if (encodedImage != null) {
            String url = Login.mainurl + "/api/user/set_profile?username=" + MainActivity.username;
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("profile_image", encodedImage.trim());
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "json 오류", Toast.LENGTH_SHORT).show();
            }

            RequestQueue queue = Volley.newRequestQueue(getContext());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //Toast.makeText(getContext(), "편집 성공", Toast.LENGTH_SHORT).show();
                        }
                    },null) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer " + MainActivity.token);
                    return headers;
                }
            };

            queue.add(request);
        } else {
            // encodedImage가 null일 때의 처리 (예: 에러 메시지 출력)
            Toast.makeText(getContext(), "이미지를 선택하세요.", Toast.LENGTH_SHORT).show();
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private Context context;
        private JSONArray imageArray;

        public ImageAdapter(Context context, JSONArray imageArray) {
            this.context = context;
            this.imageArray = imageArray;
        }

        @Override
        public int getCount() {
            return imageArray.length();
        }

        @Override
        public Object getItem(int position) {
            try {
                return imageArray.getJSONObject(position);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;

            if (convertView == null) {
                imageView = new ImageView(context);
                //imageView.setLayoutParams(new GridView.LayoutParams(200, 200)); // Adjust size as needed
                imageView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT,400));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(10, 10, 10, 10);
            } else {
                imageView = (ImageView) convertView;
            }

            try {
                String imageData = ((JSONObject) getItem(position)).getString("imageData");
                Bitmap decodedBitmap = decodeBase64(imageData);
                imageView.setImageBitmap(decodedBitmap);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return imageView;
        }

        private Bitmap decodeBase64(String base64ImageString) {
            byte[] decodedBytes = Base64.decode(base64ImageString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        }
    }

    private void get_profile(){
        String url = Login.mainurl + "/api/user/get_profile?username=" + MainActivity.username;
        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();

                            // JSON 파싱
                            JsonNode jsonNode = objectMapper.readTree(String.valueOf(response));

                            // profile_image 값 얻기
                            JsonNode profileImageNode = jsonNode.get("profile_image");
                            if (profileImageNode != null && !profileImageNode.isNull()) {
                                MainActivity.image = profileImageNode.asText();
                                MainActivity.m_profile_image = decodeBase64(MainActivity.image);
                            } else {
                                MainActivity.image = null;
                            }
                        } catch (JsonProcessingException e) {
                            MainActivity.image = null;
                            e.printStackTrace();
                        }
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

                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
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
    }

}
