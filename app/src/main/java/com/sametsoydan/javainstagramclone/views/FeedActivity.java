package com.sametsoydan.javainstagramclone.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.sametsoydan.javainstagramclone.R;
import com.sametsoydan.javainstagramclone.adapters.PostAdapter;
import com.sametsoydan.javainstagramclone.databinding.ActivityFeedBinding;
import com.sametsoydan.javainstagramclone.models.Post;

import java.util.ArrayList;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {

    private ActivityFeedBinding binding;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    ArrayList<Post> postsArrayList;

    PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityFeedBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        postsArrayList = new ArrayList<>();

        setSupportActionBar(binding.toolbar);

        getData();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(postsArrayList);
        binding.recyclerView.setAdapter(postAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.option_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.add_post) {
            // Upload Activity
            Intent intent = new Intent(FeedActivity.this, UploadActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.signOut) {
            // SignOut
            auth.signOut();
            Intent intent = new Intent(FeedActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void getData() {
        db.collection("Posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null) {
                    if (value == null) {

                    } else {
                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                            Map<String, Object> dataMap = snapshot.getData();

                            String userEmail =(String) dataMap.get("userEmail");
                            String comment =(String) dataMap.get("comment");
                            String downloadURL =(String) dataMap.get("downloadURL");

                            Post post = new Post(userEmail,comment,downloadURL);
                            postsArrayList.add(post);
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(FeedActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}