package com.miguelangelleiva.login;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText etemail, etpass, etDatos;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        etemail = (EditText) findViewById(R.id.etEmail);
        etpass = (EditText) findViewById(R.id.etPass);
        etDatos = (EditText) findViewById(R.id.etDatos);
    }

    public void add(View view) {
        String email = etemail.getText().toString().trim();
        String password = etpass.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Introduce Email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Introduce Password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Después de que la cuenta se crea con éxito
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Cuenta creada.", Toast.LENGTH_SHORT).show();

                            // Obtén la instancia del usuario actual
                            FirebaseUser currentUser = mAuth.getCurrentUser();

                            // Agrega información adicional a Cloud Firestore
                            if (currentUser != null) {
                                String userId = currentUser.getUid();

                                // Puedes cambiar "users" por el nombre de tu colección en Firestore
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                DocumentReference userRef = db.collection("users").document(userId);

                                // Crea un objeto con la información que deseas almacenar
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", email); // Puedes agregar más campos según tus necesidades
                                userData.put("password", password);

                                // Añade los datos a Cloud Firestore
                                userRef.set(userData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("ID", "DocumentSnapshot added with ID: " + userId);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error adding document", e);
                                            }
                                        });

                                db.collection("users")
                                        .document(userId)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        String email = document.getString("email");
                                                        etDatos.setText("Id: " + userId + "\nContraseña: " +email
                                                                + "\nPassword: " + password);
                                                    } else {
                                                        Log.d(TAG, "No such document");
                                                    }
                                                } else {
                                                    Log.d(TAG, "get failed with ", task.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });


    }
}