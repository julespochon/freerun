package com.example.d_wen.freerun;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RegisterActivity extends AppCompatActivity {

    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference profileGetRef = database.getReference("profiles");
    private static final String COLOR= "COLOR";
    private static DatabaseReference profileRef = profileGetRef.push();

    private static final int PICK_IMAGE = 1;

    private File imageFile;
    private Profile userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void chooseImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            imageFile = new File(getExternalFilesDir(null), "profileImage");
            try {
                copyImage(imageUri, imageFile);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            final InputStream imageStream;
            try {
                imageStream = getContentResolver().openInputStream(Uri.fromFile(imageFile));
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                ImageView imageView = findViewById(R.id.userImage);
                imageView.setImageBitmap(selectedImage);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void copyImage(Uri imageUri, File imageFile) throws IOException{
        InputStream in = null;
        OutputStream out = null;

        try {
            in = getContentResolver().openInputStream(imageUri);
            out = new FileOutputStream(imageFile);
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
            out.close();
        }
    }

    private void editUser() {
        TextView username = findViewById(R.id.usernameEdit);
        TextView password = findViewById(R.id.passwordEdit);
        userProfile = new Profile(username.getText().toString(), password.getText().toString());

        TextView height = findViewById(R.id.heightEdit);
        TextView weight = findViewById(R.id.weightEdit);
        try {
            userProfile.height = Integer.valueOf(height.getText().toString());
        } catch (NumberFormatException e) {
            userProfile.height = 0;
        }
        try {
            userProfile.weight = Float.valueOf(weight.getText().toString());
        } catch (NumberFormatException e) {
            userProfile.weight = 0;
        }
        if (imageFile == null) {
            userProfile.photoPath = "";
        } else {
            userProfile.photoPath = imageFile.getPath();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_register_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                clearUser();
                break;
            case R.id.action_validate:
                editUser();
                addProfileToFirebaseDB();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearUser() {
        ImageView userImageView = findViewById(R.id.userImage);
        TextView usernameTextView = findViewById(R.id.usernameEdit);
        TextView passwordTextView = findViewById(R.id.passwordEdit);
        TextView heightTextView = findViewById(R.id.heightEdit);
        TextView weightTextView = findViewById(R.id.weightEdit);

        userImageView.setImageResource(R.drawable.avatar);
        usernameTextView.setText("");
        passwordTextView.setText("");
        heightTextView.setText("");
        weightTextView.setText("");
    }

    private void addProfileToFirebaseDB() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) ((ImageView) findViewById(R.id
                .userImage)).getDrawable();

        Bitmap bitmap = bitmapDrawable.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] data = baos.toByteArray();

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference photoRef = storageRef.child("photos").child(profileRef.getKey() + ".jpg");
            UploadTask uploadTask = photoRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(RegisterActivity.this, R.string.photo_upload_failed, Toast
                            .LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new PhotoUploadSuccessListener());


    }

    private class PhotoUploadSuccessListener implements OnSuccessListener<UploadTask.TaskSnapshot> {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(final Uri uri) {
                    userProfile.photoPath = uri.toString();
                    profileRef.runTransaction(new ProfileDataUploadHandler());
                }
            });
        }
    }

    private class ProfileDataUploadHandler implements Transaction.Handler {
        @NonNull
        @Override
        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
            mutableData.child("username").setValue(userProfile.username);
            mutableData.child("password").setValue(userProfile.password);
            mutableData.child("height").setValue(userProfile.height);
            mutableData.child("weight").setValue(userProfile.weight);
            mutableData.child("photo").setValue(userProfile.photoPath);
            return Transaction.success(mutableData);
        }

        @Override
        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable
                DataSnapshot dataSnapshot) {
            if (b) {
                Toast.makeText(RegisterActivity.this, R.string.registration_success, Toast
                        .LENGTH_SHORT).show();

                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.putExtra("userProfile", userProfile);
                setResult(AppCompatActivity.RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, R.string.registration_failed, Toast
                        .LENGTH_SHORT).show();
            }
        }
    }
}

