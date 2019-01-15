package com.example.d_wen.freerun;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
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

    private static final FirebaseDatabase database=FirebaseDatabase.getInstance();
    private static final DatabaseReference profileGetRef=database.getReference("profiles");
    private static final String TAG="EmailPassword";

    private static final int PICK_IMAGE=1;

    private File imageFile;
    private Profile userProfile;
    private String userID;
    private boolean changePassword;
    private boolean newUser=true;
    private Uri savedImageUri;

    private FirebaseAuth mAuth;

    private String savedEmail;
    private String savedUsername;
    private String savedPassword;
    private String savedWeight;
    private String savedHeight;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth=FirebaseAuth.getInstance();

        Intent intent=getIntent();
        if (intent.getExtras() != null) {
            newUser=false;
            userID=intent.getExtras().getString(MyProfileFragment.USER_ID);
            changePassword=intent.getExtras().getBoolean(MyProfileFragment.CHANGE_PASSWORD);
            if (changePassword) {
                displayPasswordAndEmail();
            } else {
                fetchDataFromFirebase();
            }
        }

        if (savedInstanceState != null) {
            savedImageUri = savedInstanceState.getParcelable("ImageUri");
            if (savedImageUri != null) {
                try {
                    InputStream imageStream=getContentResolver().openInputStream( savedImageUri );
                    final Bitmap selectedImage=BitmapFactory.decodeStream( imageStream );
                    ImageView imageView=findViewById( R.id.userImage );
                    imageView.setImageBitmap( selectedImage );
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            TextView email = findViewById(R.id.emailEdit);
            TextView username = findViewById(R.id.usernameEdit);
            TextView password = findViewById(R.id.passwordEdit);
            TextView weight = findViewById(R.id.weightEdit);
            TextView height = findViewById(R.id.heightEdit);

            email.setText(savedEmail);
            username.setText(savedUsername);
            password.setText(savedPassword);
            weight.setText(savedWeight);
            height.setText(savedHeight);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                clearUser();
                break;
            case R.id.action_validate:
                if (!newUser) {
                    if (changePassword) {
                        reauthenticate();
                    } else {
                        editUser();
                        updateProfile();
                    }
                } else {
                    editUser();
                    createAccount();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createAccount() {
        String email=userProfile.email;
        String password=userProfile.password;

        Log.d(TAG, "createAccount:" + email);

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this, "Fields are empty",
                    Toast.LENGTH_SHORT).show();
        } else {
            // [START create_user_with_email]
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");

                                addProfileToFirebaseDB();
                                FirebaseUser user=mAuth.getCurrentUser();
                                sendEmailVerification(user);

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            // [END create_user_with_email]
        }
    }

    private void sendEmailVerification(final FirebaseUser user) {

        // Send verification email
        // [START send_email_verification]
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(RegisterActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

    public void updateProfile() {
        // [START update_email]
        addProfileToFirebaseDB();
    }

    public void reauthenticate() {
        // [START reauthenticate]
        FirebaseUser user=mAuth.getCurrentUser();

        TextView email=findViewById(R.id.emailEdit);
        String oldEmail=email.getText().toString();
        TextView password=findViewById(R.id.passwordEdit);
        String oldPassword=password.getText().toString();

        if (TextUtils.isEmpty(oldEmail) || TextUtils.isEmpty(oldPassword)) {
            Toast.makeText(RegisterActivity.this, "Fields are empty",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Get auth credentials from the user for re-authentication.
            AuthCredential credential=EmailAuthProvider
                    .getCredential(oldEmail, oldPassword);

            // Prompt the user to re-provide their sign-in credentials
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "User re-authenticated.");
                            updateEmailAndPassword();
                        }
                    });
            // [END reauthenticate]
        }
    }


    public void updateEmailAndPassword() {
        // [START update_email]
        FirebaseUser user = mAuth.getCurrentUser();

        userProfile=new Profile();

        updatePassword(user);
    }

    public void updatePassword(FirebaseUser user){

    TextView password=findViewById(R.id.heightEdit);
    String newPassword=password.getText().toString();

    TextView passwordOld=findViewById(R.id.passwordEdit);
    String oldPassword=passwordOld.getText().toString();

    int length = newPassword.length();
    if (TextUtils.isEmpty(newPassword)) {
        userProfile.password=oldPassword;
        updateEmail(user);

    }else {
        // [START update_password]
        if (length < 6){
            Toast.makeText(RegisterActivity.this,
                    "Password to short, minimum 6 character", Toast.LENGTH_LONG).show();
        } else {
            userProfile.password=newPassword;
            user.updatePassword(newPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User password updated.");
                                FirebaseUser user=mAuth.getCurrentUser();
                                updateEmail(user);
                                Toast.makeText(RegisterActivity.this,
                                        "Password updated",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegisterActivity.this,
                                        "Password update failed",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        // [END update_password]
    }
    }

    private void updateEmail(FirebaseUser user) {
        TextView email=findViewById(R.id.usernameEdit);
        String newEmail=email.getText().toString();

        TextView emailOld=findViewById(R.id.emailEdit);
        String oldEmail=emailOld.getText().toString();

        if (TextUtils.isEmpty(newEmail)) {
            userProfile.email=oldEmail;
            intentToLogin();
        } else {
            userProfile.email=newEmail;
            if (newEmail.equals(oldEmail)){
                intentToLogin();
            }else {
                user.updateEmail(newEmail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User email address updated.");
                                    FirebaseUser newUser=mAuth.getCurrentUser();
                                    sendEmailVerification(newUser);
                                    intentToLogin();
                                } else {
                                    Toast.makeText(RegisterActivity.this,
                                            "Email update failed",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
        // [END update_email]
        profileGetRef.child(userID).child("email").setValue(newEmail);
    }

    private void fetchDataFromFirebase() {
        final TextView emailTextView = findViewById(R.id.emailEdit);
        final TextView usernameTextView = findViewById(R.id.usernameEdit);
        final TextView passwordTextView = findViewById(R.id.passwordEdit);
        final TextView heightTextView = findViewById(R.id.heightEdit);
        final TextView weightTextView = findViewById(R.id.weightEdit);

        profileGetRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String user_db = dataSnapshot.child("username").getValue(String.class);
                int height_db = dataSnapshot.child("height").getValue(int.class);
                float weight_db = dataSnapshot.child("weight").getValue(float.class);
                String photo = dataSnapshot.child("photo").getValue(String.class);

                usernameTextView.setText(user_db);
                heightTextView.setText(String.valueOf(height_db));
                weightTextView.setText(String.valueOf(weight_db));

                emailTextView.setVisibility(View.GONE);
                passwordTextView.setVisibility(View.GONE);

                //  Reference to an image file in Firebase Storage
                StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl
                        (photo);
                storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        final Bitmap selectedImage = BitmapFactory.decodeByteArray(bytes, 0,
                                bytes.length);
                        ImageView imageView = findViewById(R.id.userImage);
                        imageView.setImageBitmap(selectedImage);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void displayPasswordAndEmail() {

        final TextView emailTextView = findViewById(R.id.emailEdit);
        final TextView usernameTextView = findViewById(R.id.usernameEdit);
        final TextView passwordTextView = findViewById(R.id.passwordEdit);
        final TextView heightTextView = findViewById(R.id.heightEdit);
        final TextView weightTextView = findViewById(R.id.weightEdit);

        profileGetRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String photo = dataSnapshot.child("photo").getValue(String.class);

                emailTextView.setHint("Email");
                usernameTextView.setHint("New email");
                usernameTextView.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                passwordTextView.setHint("Password");
                heightTextView.setHint("New password");
                heightTextView.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
                weightTextView.setVisibility(View.GONE);

                //  Reference to an image file in Firebase Storage
                StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl
                        (photo);
                storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        final Bitmap selectedImage = BitmapFactory.decodeByteArray(bytes, 0,
                                bytes.length);
                        ImageView imageView = findViewById(R.id.userImage);
                        imageView.setImageBitmap(selectedImage);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                savedImageUri = Uri.fromFile(imageFile);
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

        if (newUser) {
            int length=password.length();
            if (length < 6) {
                Toast.makeText(RegisterActivity.this,
                        "Password to short, minimum 6 character", Toast.LENGTH_LONG).show();
            }
        }

        userProfile = new Profile(username.getText().toString(), password.getText().toString());

        TextView height = findViewById(R.id.heightEdit);
        TextView weight = findViewById(R.id.weightEdit);
        TextView email = findViewById(R.id.emailEdit);

        userProfile.email = email.getText().toString();

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

    private void clearUser() {
        ImageView userImageView = findViewById(R.id.userImage);
        TextView emailTextView = findViewById(R.id.emailEdit);
        TextView usernameTextView = findViewById(R.id.usernameEdit);
        TextView passwordTextView = findViewById(R.id.passwordEdit);
        TextView heightTextView = findViewById(R.id.heightEdit);
        TextView weightTextView = findViewById(R.id.weightEdit);

        userImageView.setImageResource(R.drawable.avatar);
        emailTextView.setText("");
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
            StorageReference photoRef = storageRef.child("photos").child(profileGetRef.
                    child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getKey() + ".jpg");
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
                    profileGetRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                            runTransaction(new ProfileDataUploadHandler());
                }
            });
        }
    }

    private class ProfileDataUploadHandler implements Transaction.Handler {
        @NonNull
        @Override
        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
            mutableData.child("username").setValue(userProfile.username);
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

    private void intentToLogin() {
        Intent intent=new Intent(RegisterActivity.this, LoginActivity.class);
        intent.putExtra("userProfile", userProfile);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ImageUri", savedImageUri);

        TextView email = findViewById(R.id.emailEdit);
        TextView username = findViewById(R.id.usernameEdit);
        TextView password = findViewById(R.id.passwordEdit);
        TextView weight = findViewById(R.id.weightEdit);
        TextView height = findViewById(R.id.heightEdit);

        savedEmail = email.getText().toString();
        savedUsername = username.getText().toString();
        savedPassword = password.getText().toString();
        savedWeight = weight.getText().toString();
        savedHeight = height.getText().toString();
    }
}

