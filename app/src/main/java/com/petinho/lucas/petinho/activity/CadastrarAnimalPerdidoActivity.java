package com.petinho.lucas.petinho.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.petinho.lucas.petinho.R;
import com.petinho.lucas.petinho.helper.ConfiguracaoFirebase;
import com.petinho.lucas.petinho.model.UserInformation;
import com.petinho.lucas.petinho.model.UserLocation;
import com.santalu.widget.MaskEditText;

public class CadastrarAnimalPerdidoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AddToDatabase";

    private Button btnSubmit;
    private EditText mName, mDescricao;
    private MaskEditText mPhone;
    private String userID;
    private String urlFoto, urlRecuperada;
    private ImageView imagem1;
    private UserLocation mUserLocation;
    FusedLocationProviderClient mFusedLocationClient;
    private StorageReference storage;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseFirestore mDb;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_animal_perdido);
        mDb = FirebaseFirestore.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        inicializarComponentes();


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in" + user.getUid());
                    toastMessage("Logado com sucesso com:" + user.getEmail());
                } else {
                    //User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    toastMessage("Deslogado com sucesso");
                }
            }
        };
        //Read from the Database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //This method is called once with the  initial value and again
                //whenever data at this location is updated
                Log.d(TAG, "onDataChange: Added information to database: \n" + dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Failed to read value
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toastMessage("Iniciando Processo...");
                Log.d(TAG, "onClick: Submit pressed.");
                final String name = mName.getText().toString();
                final String descricao = mDescricao.getText().toString();
                final String phoneNum = mPhone.getText().toString();
                //Criando no Storage
                final StorageReference imagemAnucio = storage.child("imagens")
                        .child("animaisPerdidos")
                        .child(userID)
                        .child("imagem");
                //Fazendo upaload do arquvio
                UploadTask uploadTask = imagemAnucio.putFile(Uri.parse(urlRecuperada));
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful()) ;
                        Uri downloadUrl = urlTask.getResult();
                        urlFoto = downloadUrl.toString();
                        UserInformation userInformation = new UserInformation(name, descricao, phoneNum, urlFoto, userID);
                        myRef.child("Users").child(userID).setValue(userInformation);
                        toastMessage("Animal perdido cadastrado");
                        mName.setText("");
                        mDescricao.setText("");
                        mPhone.setText("");
                        getUserDetails();
                        toastMessage("Processo finalizado");
                        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                                .setTimestampsInSnapshotsEnabled(true)
                                .build();
                        //mDb.setFirestoreSettings(settings);

                        DocumentReference newUserRef = mDb
                                .collection("Users")
                                .document(FirebaseAuth.getInstance().getUid());
                        newUserRef.set(userInformation);
                        //finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        toastMessage("Falha ao fazer upload");
                    }
                });
            }
        });
    }

    private void getUserDetails(){
        if(mUserLocation == null){
            mUserLocation = new UserLocation();

            DocumentReference userRef = mDb.collection("Users")
                    .document(FirebaseAuth.getInstance().getUid());
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: Detalhes recuperados");
                        UserInformation user = task.getResult().toObject(UserInformation.class);
                        mUserLocation.setUser(user);
                        getLastKnowLocation();
                    }
                }
            });
        }
    }

    private void saveUserLocation() {
        if (mUserLocation != null) {
            DocumentReference locationRef = mDb.
                    collection("User Locations")
                    .document(FirebaseAuth.getInstance().getUid());
            locationRef.set(mUserLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: " + mUserLocation.getGeo_point().getLongitude());
                    }
                }
            });
        }
    }

    private void getLastKnowLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                Log.d(TAG, "onComplete: latitude" + geoPoint.getLatitude());
                Log.d(TAG, "onComplete: longitude" + geoPoint.getLongitude());
                mUserLocation.setGeo_point(geoPoint);
                mUserLocation.setTimestamp(null);
                saveUserLocation();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            //Recuperar Imagem
            Uri imagemSelecionada = data.getData();
            String caminhoImagem = imagemSelecionada.toString();

            //Configura imagem no ImageView
            if( requestCode == 1){
                imagem1.setImageURI(imagemSelecionada);
            }
            urlRecuperada = caminhoImagem;
        }
    }

    private void inicializarComponentes(){
        storage = ConfiguracaoFirebase.getFirebaseStorage();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        imagem1 = findViewById(R.id.imageCadastro1);
        imagem1.setOnClickListener(this);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        final FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mName = (EditText) findViewById(R.id.etName);
        mDescricao = (EditText) findViewById(R.id.etDescricao);
        mPhone =  findViewById(R.id.etPhone);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageCadastro1 :
                escolherImagem(1);
                break;
        }
    }

    public void escolherImagem(int requestCode){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, requestCode);
    }

    @Override
    public void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop(){
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }


}
