package com.example.mainapp.mainfragment;

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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.mainapp.MainActivity;
import com.example.mainapp.R;
import com.example.mainapp.user.Login;
import com.example.mainapp.util.SearchActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SearchFragment extends Fragment {
    private JSONArray imageArray;
    private GridView sgv;
    private static Long post_id;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 프래그먼트의 UI 초기화
        View view = inflater.inflate(R.layout.search_fragment, container, false);
        // 여기에 UI 초기화 코드 추가
        post_id=-1L;

        Button search = view.findViewById(R.id.search_btn);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText s= getActivity().findViewById(R.id.search_exp);
                String search_string = s.getText().toString();
                startActivityString(SearchActivity.class,"search",search_string);
            }
        });

        Button loadMoreButton = view.findViewById(R.id.searchloadMoreButton);
        //loadMoreButton.setVisibility(View.VISIBLE);
        loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button loadMoreButton = v.findViewById(R.id.searchloadMoreButton);
                loadMoreButton.setVisibility(View.GONE);
                get_post();
            }
        });

        sgv = view.findViewById(R.id.search_feed_grid);

        sgv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 여기서 필요한 경우 스크롤 상태의 변화를 처리할 수 있습니다.
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // 마지막 아이템이 보이는지 여부를 확인합니다.
                boolean lastItemVisible = firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0;

                // 조건에 따라 버튼의 가시성을 설정합니다.
                if (lastItemVisible) {
                    // 마지막 항목이 보이면 버튼을 보이게 합니다.
                    loadMoreButton.setVisibility(View.VISIBLE);
                } else {
                    // 그렇지 않으면 버튼을 숨깁니다.
                    loadMoreButton.setVisibility(View.INVISIBLE);
                }
            }
        });


        get_post();
        return view;
    }

    private void get_post(){
        String url = Login.mainurl+"/api/load_post?last="+post_id+"&username="+ MainActivity.username;
        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        imageArray = response;
                        int size=response.length();
                        try {
                            JSONObject postObject = response.getJSONObject(size-1);
                            long postId = postObject.getLong("postId");
                            post_id=postId-1;
                            GridView gridView = getActivity().findViewById(R.id.search_feed_grid);
                            ImageAdapter imageAdapter = new ImageAdapter(getContext(), imageArray);
                            gridView.setAdapter(imageAdapter);
                        } catch (JSONException e) {
                            Toast.makeText(getContext(),"불러오기 오류",Toast.LENGTH_SHORT).show();
                        }
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

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        InputStream imageStream = requireContext().getContentResolver().openInputStream(uri);
        return BitmapFactory.decodeStream(imageStream);
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
                imageView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT,400));// Adjust size as needed
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

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public void startActivityString(Class c, String name , String sendString) {
        Intent intent = new Intent(getContext(), c);
        intent.putExtra(name, sendString);
        startActivity(intent);
    }
}
