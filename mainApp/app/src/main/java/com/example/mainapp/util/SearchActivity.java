package com.example.mainapp.util;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.mainapp.MainActivity;
import com.example.mainapp.R;
import com.example.mainapp.mainfragment.homeFragment;
import com.example.mainapp.user.Login;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {
    public static String search_text;
    private List<feedItem> feedItems;
    private static Long post_id;
    private RecyclerView recyclerView;
    private FeedAdapter feedAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        post_id = -1L;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#EFEFEF")); // #EFEFEF 색상으로 변경
        }

        recyclerView = findViewById(R.id.search_recyclerView);
        feedItems = new ArrayList<>();
        feedAdapter = new FeedAdapter(feedItems, new FeedAdapter.OnItemClickListener(){
            @Override
            public void onAlarmClick(int position,long LikePosiId){
                setpostlike(LikePosiId);
            }
            @Override
            public void onMessageClick(int position) {
                // "메시지" 버튼 클릭 시 실행할 동작
                Toast.makeText(getApplicationContext(), "메시지 버튼이 클릭되었습니다. Position: " + position, Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = getIntent();
        search_text = intent.getStringExtra("search");

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(feedAdapter);

        Button loadMoreButton = findViewById(R.id.Searcj_loadMoreButton);
        loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button loadMoreButton = findViewById(R.id.Searcj_loadMoreButton);
                loadMoreButton.setVisibility(View.GONE);
                getPost(search_text);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                    Button loadMoreButton = findViewById(R.id.Searcj_loadMoreButton);
                    loadMoreButton.setVisibility(View.VISIBLE);
                }
                else {
                    Button loadMoreButton = findViewById(R.id.Searcj_loadMoreButton);
                    loadMoreButton.setVisibility(View.GONE);
                }
            }
        });



        getPost(search_text);
    }

    public static class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
        public interface OnItemClickListener{
            void onAlarmClick(int position,long LikePostId);
            void onMessageClick(int position);
        }
        private List<feedItem> feedItems;

        private OnItemClickListener onItemClickListener;
        public FeedAdapter(List<feedItem> feedItems, OnItemClickListener onItemClickListener) {
            this.feedItems = feedItems;
            this.onItemClickListener = onItemClickListener;
        }

        @NonNull
        @Override
        public FeedAdapter.FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_feed, parent, false);
            return new FeedAdapter.FeedViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FeedAdapter.FeedViewHolder holder, int position) {
            int currentPosition = holder.getAdapterPosition();

            feedItem item = feedItems.get(position);

            long LikePostId = item.getPostID();


            Glide.with(holder.itemView).load(item.getProfileImageUrl()).into(holder.profileImage);

            Bitmap decodedBitmap = decodeBase64(item.getImage());
            holder.homeFeed_Image.setImageBitmap(decodedBitmap);

            holder.profileText.setText(item.getProfileUser());

            holder.homeFeed_userName.setText(item.getProfileUser());

            holder.homeFeed_like.setText("  " + item.getLikes());

            holder.homeFeed_explainText.setText(item.getExplainText());

            holder.homeFeed_bestComment.setText(item.getBestComment());

            holder.alarmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // FeedAdapter 내부에 정의된 리스너를 호출하여 알람 버튼 클릭 이벤트 전달
                    if (onItemClickListener != null) {
                        onItemClickListener.onAlarmClick(currentPosition,LikePostId);
                    }

                    holder.messageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // FeedAdapter 내부에 정의된 리스너를 호출하여 메시지 버튼 클릭 이벤트 전달
                            if (onItemClickListener != null) {
                                onItemClickListener.onMessageClick(currentPosition);
                            }
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return feedItems.size();
        }

        public class FeedViewHolder extends RecyclerView.ViewHolder {
            ImageView profileImage;
            TextView profileText;
            ImageView homeFeed_Image;
            TextView favoriteCounterText;
            TextView homeFeed_userName;
            TextView homeFeed_explainText;
            TextView homeFeed_bestComment;
            TextView homeFeed_like;

            Button alarmButton;
            Button messageButton;


            public FeedViewHolder(@NonNull View itemView) {
                super(itemView);
                profileImage = itemView.findViewById(R.id.homeFeed_profileImage);
                profileText = itemView.findViewById(R.id.homeFeed_profileText);
                homeFeed_Image = itemView.findViewById(R.id.homeFeed_Image);
                favoriteCounterText = itemView.findViewById(R.id.homeFeed_favoriteCounterText);
                homeFeed_userName = itemView.findViewById(R.id.homeFeed_userName);
                homeFeed_explainText = itemView.findViewById(R.id.homeFeed_explainText);
                homeFeed_bestComment = itemView.findViewById(R.id.homeFeed_bestComment);
                alarmButton = itemView.findViewById(R.id.homeFeed_favoriteIcon);
                messageButton = itemView.findViewById(R.id.homeFeed_commentIcon);
                homeFeed_like = itemView.findViewById(R.id.homefeed_like);

                alarmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // FeedAdapter 내부에 정의된 리스너를 호출하여 알람 버튼 클릭 이벤트 전달
                        int position = getAdapterPosition();
                        feedItem item = feedItems.get(position);
                        long POST_ID = item.getPostID();
                        if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                            onItemClickListener.onAlarmClick(position,POST_ID);
                        }
                    }
                });

                // "메시지" 버튼 클릭 리스너 설정
                messageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // FeedAdapter 내부에 정의된 리스너를 호출하여 메시지 버튼 클릭 이벤트 전달
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                            onItemClickListener.onMessageClick(position);
                        }
                    }
                });
            }
        }
    }

    private static Bitmap decodeBase64(String base64ImageString) {
        byte[] decodedBytes = Base64.decode(base64ImageString, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
    public void getPost(String search_text){
        String url = Login.mainurl+"/api/search_post?search="+search_text+"&last="+post_id+"&username="+MainActivity.username;
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Process each item in the array
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject postObject = response.getJSONObject(i);

                                long postId = postObject.getLong("postId");
                                String username = postObject.getString("username");
                                String explain = postObject.getString("explain");
                                int likes = postObject.getInt("likes");
                                String imageData = postObject.getString("imageData");

                                feedItem newItem = new feedItem();

                                newItem.setPostID(postId);
                                newItem.setProfileUser(username);
                                newItem.setExplainText(explain);
                                newItem.setLikes(likes);
                                newItem.setImage(imageData);
                                newItem.setProfileImageUrl("https://search.pstatic.net/common/?src=http%3A%2F%2Fcafefiles.naver.net%2FMjAxNjEwMDdfMjQx%2FMDAxNDc1ODA5NzI3MTU0.hyt8VNKUQJUEfzYZgSvgW0PfvdV34QMh2NF1G73WE_kg.gTFZYfJpkMoq1x92WrdMCno1c7gWB02z7PAFPCdRqXsg.JPEG.minjea3636%2F2.jpg&type=a340");

                                post_id=postId-1;

                                feedItems.add(newItem);
                                feedAdapter.notifyDataSetChanged();

                                Log.d("YourClass", "Received post data: " + postId + ", " + username + ", " + explain + ", " + likes + ", " + imageData);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        Log.e("YourClass", "Error while fetching JSON data: " + error.getMessage());
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

    public void setpostlike(long LikePostId){
        String url = Login.mainurl+"/api/post_like?id="+LikePostId;
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("YourClass", "Error" + error.getMessage());
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
}