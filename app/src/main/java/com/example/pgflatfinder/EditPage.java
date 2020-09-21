package com.example.pgflatfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import id.zelory.compressor.Compressor;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

public class EditPage extends AppCompatActivity {

    ImageView display_image;
    FloatingActionButton edit_image;
    Button save_data;
    TextInputLayout name,email,mobile;
    ProgressDialog progressDialog;
    private StorageReference mStorageRef;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_page);
        // hooks
        display_image = findViewById(R.id.display_image);
        edit_image = findViewById(R.id.editimage);
        save_data = findViewById(R.id.save_data);
        name = findViewById(R.id.editName);
        email = findViewById(R.id.editEmail);
        mobile = findViewById(R.id.editNumber);
        progressDialog = new ProgressDialog(this);

        // storage reference
        mStorageRef = FirebaseStorage.getInstance().getReference();


        mRootRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Objects.requireNonNull(name.getEditText()).setText(Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                Objects.requireNonNull(mobile.getEditText()).setText(Objects.requireNonNull(snapshot.child("mobile").getValue()).toString());
                Objects.requireNonNull(email.getEditText()).setText(Objects.requireNonNull(snapshot.child("email").getValue()).toString());
                String profile_url = Objects.requireNonNull(snapshot.child("profile").getValue()).toString();

                Picasso.get().load(profile_url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile_image).into(display_image);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mRootRef.keepSynced(true);

        save_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!validateName()  || !validateEmail() || !validateMobile())
                {
                    return;
                }

                String new_name = Objects.requireNonNull(name.getEditText()).getText().toString();
                String new_mobile = Objects.requireNonNull(mobile.getEditText()).getText().toString();
                String new_email = Objects.requireNonNull(email.getEditText()).getText().toString();

                progressDialog.setTitle("Updating Data");
                progressDialog.setMessage("Please wait while we update your account!");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                updateData(new_name,new_mobile,new_email);
            }
        });

        edit_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1)
                        .setMinCropWindowSize(500,500)
                        .start(EditPage.this);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                progressDialog = new ProgressDialog(EditPage.this);
                progressDialog.setTitle("Loading");
                progressDialog.setMessage("Please wait while Uploading your profile picture");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();

                File thumb_file = new File(resultUri.getPath());

                // // bitmap upload ////////////////////////////////////////////////////////////////
                try {
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxHeight(300)
                            .setMaxWidth(300)
                            .setQuality(85)
                            .compressToBitmap(thumb_file);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] thumb_byte = baos.toByteArray();
                    final StorageReference thumbpath = mStorageRef.child("profile").child(uid + ".jpg");
                    UploadTask uploadTask = thumbpath.putBytes(thumb_byte);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(EditPage.this, "Failed to upload Thumb image", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                            // ...
                            thumbpath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String profile_url = uri.toString();
                                    mRootRef.child("users").child(uid).child("profile").setValue(profile_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(EditPage.this, "Profile Picture Successfully Updated", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to convert to bitmap", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private Boolean validateMobile() {

        String val = Objects.requireNonNull(mobile.getEditText()).getText().toString();
        val = "+91" + val;
        String noWhite = "^((\\+){1}91){1}[1-9]{1}[0-9]{9}$";
        if (val.isEmpty()) {
            mobile.setError("Field can't be Empty");
            return false;
        }
        if(!val.matches(noWhite))
        {
            mobile.setError("Invalid mobile number");
            return false;
        }
        else {
            mobile.setError(NULL);
            mobile.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateName() {
        String val = Objects.requireNonNull(name.getEditText()).getText().toString();

        if (val.isEmpty()) {
            name.setError("Field can't be Empty");
            return false;
        }
        else {
            name.setError(NULL);
            name.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateEmail() {
        String val = Objects.requireNonNull(email.getEditText()).getText().toString();
        String noWhite = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        if (val.isEmpty()) {
            email.setError("Field can't be Empty");
            return false;
        }
        else if(!val.matches(noWhite))
        {
            email.setError("Invalid E-Mail Address");
            return false;
        }
        else {
            email.setError(NULL);
            email.setErrorEnabled(false);
            return true;
        }
    }

    private void updateData(String new_name , String new_mobile , String new_email) {

        mRootRef.child("users").child(uid).child("name").setValue(new_name);
        mRootRef.child("users").child(uid).child("email").setValue(new_email);
        mRootRef.child("users").child(uid).child("mobile").setValue(new_mobile);

        progressDialog.dismiss();
        Intent intent = new Intent(EditPage.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

}