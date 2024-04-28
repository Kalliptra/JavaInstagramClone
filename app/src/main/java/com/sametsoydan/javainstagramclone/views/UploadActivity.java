package com.sametsoydan.javainstagramclone.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sametsoydan.javainstagramclone.R;
import com.sametsoydan.javainstagramclone.databinding.ActivityUploadBinding;

import java.util.HashMap;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {

    private FirebaseStorage firebaseStorage;
    private FirebaseAuth auth;

    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    Uri imageDataUri;
    private ActivityUploadBinding binding;

    ActivityResultLauncher<Intent> activityResultLauncher; // for launch gallery intent
    ActivityResultLauncher<String> permissionLauncher;  // for launch permission

    //Bitmap selectedImageBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUploadBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        registerLauncher();

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        // selectImage onClick Method
        binding.selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(view);
            }
        });

        //Upload Button onClick Method
        binding.uploadButton.setOnClickListener(v -> {
            if (imageDataUri != null) {
                // universal unique id
                UUID uuid = UUID.randomUUID();
                String imageName = "images/" + uuid;

                storageReference.child(imageName).putFile(imageDataUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Download url
                        firebaseStorage.getReference(imageName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadURl = uri.toString();
                                String comment = binding.commentTextView.getText().toString();

                                FirebaseUser user = auth.getCurrentUser();
                                String email = user.getEmail();

                                HashMap<String, Object> postData = new HashMap<>();

                                postData.put("userEmail", email);
                                postData.put("downloadURL", downloadURl);
                                postData.put("comment", comment);
                                postData.put("date", FieldValue.serverTimestamp());

                                firebaseFirestore.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Intent intent = new Intent(UploadActivity.this, FeedActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(UploadActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UploadActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void selectImage(View view) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(view, "Permission Needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // ask permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            } else {
                // ask permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else {
            // Permission Guaranteed
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        }
    }

    private void registerLauncher() {
        // Initialize
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == RESULT_OK) {
                    Intent intentFromResult = o.getData();
                    if (intentFromResult != null) {
                        imageDataUri = intentFromResult.getData();
                        binding.selectImage.setImageURI(imageDataUri);


                     /*
                        // Bitmap'a çevirme fonksiyonu
                     try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                ImageDecoder.Source source = ImageDecoder.createSource(UploadActivity.this.getContentResolver(), imageDataUri);
                                selectedImageBitmap = ImageDecoder.decodeBitmap(source);
                                binding.selectImage.setImageBitmap(selectedImageBitmap);
                            } else {
                                selectedImageBitmap = MediaStore.Images.Media.getBitmap(UploadActivity.this.getContentResolver(), imageDataUri);
                                binding.selectImage.setImageBitmap(selectedImageBitmap);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }*/
                    } else {
                        // image was not picked
                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    // Permission guaranteed
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                } else {
                    // Permisson not guaranteed
                    Toast.makeText(UploadActivity.this, "Permission Needed !", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}
//ftrglkıjftghkljfghlıkgnbnıtıfhrhvogyoıtuıjvıfıvgıtvbtbııhg:gflkjfglıkjtrhloıjgbıjlkhtıjkgfıjtgıjgbıjıjyıl,fgkljfhgıuohgıyıughıtyıgf sikito,samo and müco by 15m dildo;
//rdrdhfdhfdjytfdjtyfyjtfjytfyjt müco was be fucked by samet and özgü;
//uhguhoırftgesgggggggggegsegsegsegsegsegsesgegsegsegshjesurhgesoıhgkjfvnfkjbn sameti sikito özgüş