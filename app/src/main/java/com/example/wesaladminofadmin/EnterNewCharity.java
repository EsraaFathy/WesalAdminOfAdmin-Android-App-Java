package com.example.wesaladminofadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.wesaladminofadmin.databinding.ActivityEnterNewCharityBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class EnterNewCharity extends AppCompatActivity {
    private ActivityEnterNewCharityBinding activityEnterNewCharityBinding;
    private ArrayList<String> texts;
    private final static int PICK_UP_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private DatabaseReference ob;
    private StorageTask uploadTask;
    private String imageNameURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityEnterNewCharityBinding = DataBindingUtil.setContentView(this, R.layout.activity_enter_new_charity);

        FirebaseApp.initializeApp(Objects.requireNonNull(this));


        activityEnterNewCharityBinding.uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        activityEnterNewCharityBinding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uploadTask != null && uploadTask.isInProgress()) {
                    openDialog(getString(R.string.this_process_has_done_before));
                } else {
                    parseData();
                }
            }
        });

    }

    private void parseData() {
        turnTextToString();
        //check Text correction

        if (texts.get(0).isEmpty() || texts.get(2).isEmpty() || texts.get(3).isEmpty() || texts.get(4).isEmpty() || texts.get(5).isEmpty()) {
            // create dialog to say enter correct data
            openDialog(getString(R.string.some_misssed_data));

        } else {
            uploadImage();
            uploadToFireBase();
            emptyEditTexts();
        }
    }

    // this method will return the extension of file we pick
    private String getFileExtension(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

    private void uploadImage() {
        StorageReference storageReferenceCharityProfile = FirebaseStorage.getInstance().getReference("CharityImageProfile");
        if (imageUri != null) {
            // image name in storage
            final StorageReference fileReference = storageReferenceCharityProfile.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(EnterNewCharity.this, "Success", Toast.LENGTH_SHORT).show();

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    activityEnterNewCharityBinding.prograssPar.setProgress(0);
                                }
                            }, 500);
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            passUrl(fileReference);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EnterNewCharity.this, "Fail" , Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            activityEnterNewCharityBinding.prograssPar.setProgress((int) progress);
                        }
                    });
        } else {
            openDialog("No image Selected");
        }

    }

    private void passUrl(StorageReference fileReference){
        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageNameURL = uri.toString();
                ob.child("imageNameURL").setValue(imageNameURL);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EnterNewCharity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // open file to choose image from mobile storage
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_UP_IMAGE_REQUEST);
    }

    //to receive the choosed image and view it in to image view
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_UP_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.with(this).load(imageUri).into(activityEnterNewCharityBinding.imageViewEnterCharity);
        }
    }

    //firebase code
    private void uploadToFireBase() {
        DatabaseReference charityDataBaseReference = FirebaseDatabase.getInstance().getReference("CharityProfiles");
        String uniqueKey = charityDataBaseReference.push().getKey();
        assert uniqueKey != null;
        ob= charityDataBaseReference.child(uniqueKey);
        ob.child("charityName").setValue(texts.get(0));
        ob.child("latitude").setValue(texts.get(1));
        ob.child("longitude").setValue(texts.get(2));
        ob.child("Phone").setValue(texts.get(3));
        ob.child("address").setValue(texts.get(4));
        ob.child("about").setValue(texts.get(5));
        ob.child("uniqueKey").setValue(uniqueKey);
    }


    private void openDialog(String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.missed_data);
        alert.setMessage(message);
        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.create().show();
    }

    private void emptyEditTexts() {
        activityEnterNewCharityBinding.charityName.setText("");
        activityEnterNewCharityBinding.longitude.setText("");
        activityEnterNewCharityBinding.latitude.setText("");
        activityEnterNewCharityBinding.Phone.setText("");
        activityEnterNewCharityBinding.address.setText("");
        activityEnterNewCharityBinding.about.setText("");
    }

    private void turnTextToString() {
        texts = new ArrayList<>();
        texts.add(activityEnterNewCharityBinding.charityName.getText().toString());
        texts.add(activityEnterNewCharityBinding.latitude.getText().toString());
        texts.add(activityEnterNewCharityBinding.longitude.getText().toString());
        texts.add(activityEnterNewCharityBinding.Phone.getText().toString());
        texts.add(activityEnterNewCharityBinding.address.getText().toString());
        texts.add(activityEnterNewCharityBinding.about.getText().toString());
    }
}
