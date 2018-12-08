package com.petinho.lucas.petinho.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterManager;
import com.petinho.lucas.petinho.R;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.petinho.lucas.petinho.helper.MyClusterManagerRenderer;
import com.petinho.lucas.petinho.model.ClusterMarker;
import com.petinho.lucas.petinho.model.UserInformation;
import com.petinho.lucas.petinho.model.UserLocation;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{

    private ListenerRegistration mUserListEventListener;
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();

    GoogleMap mGoogleMap;
    GoogleApiClient mGoogleApiClient;
    FusedLocationProviderClient mFusedLocationClient;
    private FirebaseFirestore mDb;
    private static final String TAG = "CurrentLocationApp";
    private Location mLastLocation;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mLocationDatabaseReference;

    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();
    private ArrayList<UserInformation> mUserList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mDb = FirebaseFirestore.getInstance();
        getUsersFireStore();
        if (googleServicesAvailable()) {
            //setContentView(R.layout.activity_maps);
            initMap();
            FirebaseApp.initializeApp(this);
            mFirebaseDatabase =  FirebaseDatabase.getInstance();
            mLocationDatabaseReference= mFirebaseDatabase.getReference().child("my current location");
            buildGoogleApiClient();
        } else {
            // No Google Maps Layout
        }
    }

    public void cadastrarPerdido(View view){
        startActivity(new Intent(getApplicationContext(), CadastrarAnimalPerdidoActivity.class));
    }


    private Bitmap createUserBitmap(String urlFoto) {
        Bitmap result = null;
        try {
            result = Bitmap.createBitmap((62), (76), Bitmap.Config.ARGB_8888);
            result.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(result);
            Drawable drawable = getResources().getDrawable(R.drawable.icone);
            drawable.setBounds(0, 0, (62), (76));
            drawable.draw(canvas);

            Paint roundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            RectF bitmapRect = new RectF();
            canvas.save();

            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.amu_bubble_mask);
            Bitmap bitmap = BitmapFactory.decodeFile(urlFoto); /*generate bitmap here if your image comes from any url*/
            if (bitmap != null) {
                BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Matrix matrix = new Matrix();
                float scale = (52) / (float) bitmap.getWidth();
                matrix.postTranslate((5), (5));
                matrix.postScale(scale, scale);
                roundPaint.setShader(shader);
                shader.setLocalMatrix(matrix);
                bitmapRect.set((5), (5), (52 + 5), (52 + 5));
                canvas.drawRoundRect(bitmapRect, (26), (26), roundPaint);
            }
            canvas.restore();
            try {
                canvas.setBitmap(null);
            } catch (Exception e) {}
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    public static Drawable LoadImageFromWebURL(String url) {
        try {
            InputStream iStream = (InputStream) new URL(url).getContent();
            Drawable drawable = Drawable.createFromStream(iStream, "src name");
            return drawable;
        } catch (Exception e) {
            return null;
        }}

    private void addMapMarkers(){
        if(mGoogleMap != null){
            View v = getLayoutInflater().inflate(R.layout.info_window, null);
            final ImageView imageView = v.findViewById(R.id.imagemAnimalPerdido);

            if(mClusterManager == null){
                mClusterManager = new ClusterManager<ClusterMarker>(this, mGoogleMap);
            }
            if(mClusterManagerRenderer == null){
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        this,
                        mGoogleMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }
            for(final UserLocation userLocation: mUserLocations){

                /*if(mGoogleMap != null){
                    mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                        @Override
                        public View getInfoWindow(Marker marker) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {

                            View vi = getLayoutInflater().inflate(R.layout.info_window, null);
                            final ImageView imageTeste = findViewById(R.id.imagemAnimalPerdidoTeste);
                            final ImageView imageProvisoria = findViewById(R.id.imagemAnimalPerdido);

                            Picasso.get().load(userLocation.getUser().getFotoAnimal()).into(imageTeste);
                            Picasso.get().load(userLocation.getUser().getFotoAnimal()).into(imageView);
                            Picasso.get().load(userLocation.getUser().getFotoAnimal()).into(imageProvisoria);

                            Log.d(TAG, "getInfoContents: "+userLocation.getUser().getFotoAnimal());
                            TextView tvNome = vi.findViewById(R.id.tv_nome);
                            TextView tvPhone = vi.findViewById(R.id.tv_phone);
                            TextView tvDescricao = vi.findViewById(R.id.tv_descricao);
                            TextView tvNom = findViewById(R.id.tv_nome);
                            TextView tvPhon = findViewById(R.id.tv_phone);
                            TextView tvDesc = findViewById(R.id.tv_descricao);
                            tvNome.setText(userLocation.getUser().getNome());
                            tvNom.setText(userLocation.getUser().getNome());
                            tvPhone.setText(userLocation.getUser().getTelefone());
                            tvPhon.setText(userLocation.getUser().getTelefone());
                            tvDescricao.setText(userLocation.getUser().getDescricao());
                            tvDesc.setText(userLocation.getUser().getDescricao());
                            return vi;
                        }
                    });
                }*/

                Log.d(TAG, "addMapMarkers: location: " + userLocation.getGeo_point().toString());
                try{
                    String snippet = "";
                    if(userLocation.getUser().getUser_id().equals(FirebaseAuth.getInstance().getUid())){
                        //snippet = "Este é você!";
                    }
                    else{
                        snippet = "Determinar rota para " + userLocation.getUser().getNome() + "?";
                    }
                    snippet = userLocation.getUser().getTelefone();
                    Bitmap bitmap = createUserBitmap(userLocation.getUser().getFotoAnimal());
                    //String avatar = userLocation.getUser().getFotoAnimal();
                    int avatar = R.drawable.ic_arrow_back_black_24dp; // set the default avatar
                    /*try{
                        avatar = Integer.parseInt(userLocation.getUser().getAvatar());
                    }catch (NumberFormatException e){
                        Log.d(TAG, "addMapMarkers: no avatar for " + userLocation.getUser().getUsername() + ", setting default.");
                    }*/
                    Handler handler = new Handler();
                    ClusterMarker newClusterMarker = new ClusterMarker(
                            new LatLng(userLocation.getGeo_point().getLatitude(), userLocation.getGeo_point().getLongitude()),
                            userLocation.getUser().getNome(),
                            snippet,
                            avatar,
                            userLocation.getUser()
                    );
                    mClusterManager.addItem(newClusterMarker);
                    mClusterMarkers.add(newClusterMarker);

                }catch (NullPointerException e){
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage() );
                }

            }/*
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // Actions to do after 10 seconds
                }
            }, 10000);*/
            mClusterManager.cluster();

            goToLocationZoom(-11.0126396, -37.0734345, 15);
        }
    }

    private void getUsersFireStore(){
        CollectionReference usersRef = mDb
                .collection("Users");

        mUserListEventListener = usersRef
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(e != null){
                            Log.d(TAG, "onEvent: Listen failed", e);
                            return;
                        }
                        if (queryDocumentSnapshots != null){
                            //Limpa todos usuarios e adiciona dnv
                            mUserList.clear();
                            mUserList = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots){
                                UserInformation user = doc.toObject(UserInformation.class);
                                mUserList.add(user);
                                getUserLocations(user);
                            }
                            Log.d(TAG, "onEvent: user list size"+mUserList.size());
                        }
                    }


                });
    }

    private void getUserLocations(UserInformation user) {
        DocumentReference locationRef = mDb.collection("User Locations")
                .document(user.getUser_id());

        locationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().toObject(UserLocation.class) != null){
                        mUserLocations.add(task.getResult().toObject(UserLocation.class));
                        Log.d(TAG, "onComplete: Localização dos usuarios recuperada" + mUserLocations.size());
                        Toast.makeText(MapsActivity.this, "Localizaçao recuperada" + mUserLocations.get(0).getGeo_point().getLongitude(), Toast.LENGTH_SHORT).show();
                        addMapMarkers();
                    }
                }
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.collapseActionView) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Não pôde se comunicar com os serviços google", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;
        //addMapMarkers();
        //goToLocationZoom(-11.0126514,-37.0742083, 15);

        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();*/


    }

    private void goToLocationZoom(double lat, double lng, float zoom){
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMap.moveCamera(update);
    }



    public void geoLocate(View view) throws IOException {
        EditText et = (EditText) findViewById(R.id.editText);
        String location = et.getText().toString();

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(location, 1);
        Address address = list.get(0);
        String locality = address.getLocality();

        Toast.makeText(this,locality,Toast.LENGTH_LONG).show();

        double lat = address.getLatitude();
        double lng = address.getLongitude();
        goToLocationZoom(lat, lng, 15);

    }

    LocationRequest mLocationRequest;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
/*
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {

            value_lat= String.valueOf(mLastLocation.getLatitude());
            value_lng =String.valueOf(mLastLocation.getLongitude());
            mLatitudeText.setText(value_lat);
            mLongitudeText.setText(value_lng);

            saveLocationToFirebase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mLocationDatabaseReference.push().setValue("Latitude : "+value_lat +"  & Longitude : "+value_lng);
                    Toast.makeText(MapsActivity.this ,"Location saved to the Firebasedatabase",Toast.LENGTH_LONG).show();
                }
            });

        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);

        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                Log.d(TAG, "onComplete: latitude" +geoPoint.getLatitude());
                Log.d(TAG, "onComplete: longitude"+ geoPoint.getLongitude());

            }
        });
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        */
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location == null){
            Toast.makeText(this, "Não foi possível verificar a localização", Toast.LENGTH_SHORT).show();
        }else{
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll,15);
            mGoogleMap.animateCamera(update);
        }
    }
}
