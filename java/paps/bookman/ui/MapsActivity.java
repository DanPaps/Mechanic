package paps.bookman.ui;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import paps.bookman.util.DirectionJSONParser;
import paps.bookman.R;
import paps.bookman.util.IGoogleAPI;
import paps.bookman.data.Mechanic;
import paps.bookman.data.User;
import paps.bookman.util.ConstantUtil;
import paps.bookman.util.MechanicUserPrefs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int PLACE_PICKER_REQUEST = 1;
    //Find mechanic button
    @BindView(R.id.findMechanic)
    Button findMechanic;
    //Change location floating button
    @BindView(R.id.fab)
    FloatingActionButton fab;
    //Toolbar
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    //Progress dialog
    private MaterialDialog loading;

    private String TAG = "MyActivity";

    private MechanicUserPrefs prefs;
    private GoogleMap mMap;

    //String to keep key of the minimum mechanic distant
    String minKeys = "";

    private Polyline direction;
    IGoogleAPI mService;

    //Hash map to store the distance calculated from the user to the mechanic location
    HashMap<String, Double> mechanicDistHash = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        setActionBar(toolbar);


        mService = ConstantUtil.getGoogleAPI();

        prefs = new MechanicUserPrefs(this);
        loading = ConstantUtil.getDialog(this);
        loading.show();

        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (prefs.isLoggedIn()) {
            mapFragment.getMapAsync(this);
        }


    }

    @OnClick(R.id.findMechanic)
    void dofindMechanic(){
        prefs.db.getReference().child(ConstantUtil.MECHANIC_REF).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    loading.show();
                    if (dataSnapshot != null && dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot.exists()) {
                                Mechanic mechanic = snapshot.getValue(Mechanic.class);
                                assert mechanic != null;
                                sendMechanicForDistCalc(mechanic);
                            }
                        }

                        // loop through the hash map
                        for (Map.Entry<String, Double> stringDoubleEntry : mechanicDistHash.entrySet()) {
                            double marks = ((double) ((Map.Entry) stringDoubleEntry).getValue());
                            Log.e(((Map.Entry) stringDoubleEntry).getKey().toString(), String.valueOf(marks));
                        }

                        Double min = Double.MAX_VALUE;

                        for(Map.Entry<String, Double> entry : mechanicDistHash.entrySet()) {
                            if(entry.getValue() < min) {
                                min = entry.getValue();
                                minKeys = "";
                            }
                            if(entry.getValue().equals(min)) {
                                minKeys =entry.getKey();
                            }
                        }
                        Log.e(TAG,min.toString());
                        Log.e(TAG, minKeys);

                        prefs.db.getReference().child(ConstantUtil.MECHANIC_REF).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                try {
                                    if (dataSnapshot != null && dataSnapshot.exists()) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            if (snapshot.exists()) {
                                                Mechanic mechanic = snapshot.getValue(Mechanic.class);
                                                assert mechanic != null;
                                                if (mechanic.getKey().equals(minKeys)){
                                                    LatLng closerlatLng = new LatLng(mechanic.getLatitude(), mechanic.getLongitude());
                                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(closerlatLng, 16.0f));

                                                    Double closerlat = mechanic.getLatitude();
                                                    Double closerlng = mechanic.getLongitude();

                                                    if (direction != null)
                                                        direction.remove();  // Remove old directions

                                                    getDirection(closerlat, closerlng);

                                                    loading.dismiss();
                                                }
                                            }
                                        }

                                    }
                                } catch (Exception e) {
                                    Toast.makeText(MapsActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(MapsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });



                    }
                } catch (Exception e) {
                    Toast.makeText(MapsActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDirection(Double ridlat , Double ridlng) {
        LatLng currentPosition = new LatLng(prefs.getLat(),prefs.getLng());
        double riderLat = ridlat;
        double riderLng = ridlng;


        String requestApi;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+riderLat+","+riderLng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("Location", requestApi);  //THIS WILL PRINT URL FOR DEBUG
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {

                            try {
                                new ParserTask().execute(response.body());


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(MapsActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @OnClick(R.id.fab)
    void doEditLocation(){
        Toast.makeText(this, "edit your location", Toast.LENGTH_SHORT).show();
            pickLocation();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng userLocation = new LatLng(prefs.getLat(), prefs.getLng());
        mMap.addMarker(new MarkerOptions()
                .position(userLocation)
                .snippet(prefs.getAddress())
                .title(prefs.getUsername())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15.0f));

        prefs.db.getReference().child(ConstantUtil.MECHANIC_REF).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot != null && dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot.exists()) {
                                loading.dismiss();
                                Mechanic mechanic = snapshot.getValue(Mechanic.class);
                                assert mechanic != null;
                                plotMechanicMarker(mechanic);
                                sendMechanicForDistCalc(mechanic);
                            }
                        }

                    }
                } catch (Exception e) {
                    Toast.makeText(MapsActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void pickLocation() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            setError(e.getMessage());
        }
    }

    private void setError(CharSequence message) {
        if (loading.isShowing()) loading.dismiss();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public  double sendMechanicForDistCalc(Mechanic mechanic) {
        LatLng endDist = new LatLng(mechanic.getLatitude(), mechanic.getLongitude());
        LatLng userStart = new LatLng(prefs.getLat(), prefs.getLng());

        String mechanickey = mechanic.getKey();
        double mechanic_dis = distance(userStart,endDist);

        mechanicDistHash.put(mechanickey, mechanic_dis);

        return distance(userStart,endDist);

    }


    private void plotMechanicMarker(Mechanic mechanic) {
        LatLng latLng = new LatLng(mechanic.getLatitude(), mechanic.getLongitude());
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions()
                    .title(mechanic.getMechanicName())
                    .position(latLng)
                    .snippet(mechanic.getMechanicLocation())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
    }


    public static double distance(LatLng start, LatLng end){
        try {
            Location location1 = new Location("locationA");
            location1.setLatitude(start.latitude);
            location1.setLongitude(start.longitude);
            Location location2 = new Location("locationB");
            location2.setLatitude(end.latitude);
            location2.setLongitude(end.longitude);
            return (double) location1.distanceTo(location2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PLACE_PICKER_REQUEST) {
                Place place = PlacePicker.getPlace(this, data);
                if (place != null) {
                    UpdateUser(place);
                }
            }
        } else {
            if (loading.isShowing()) loading.show();
        }
    }

    private void UpdateUser(final Place place) {
        prefs.db.getReference().child(ConstantUtil.USER_REF).child(prefs.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            User value = dataSnapshot.getValue(User.class);
                            assert value != null;
                            prefs.updateUser(value);
                            updateUI(place);
                            navHome();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        setError(databaseError.getMessage());
                    }
                });

    }

    private void navHome() {
        startActivity(new Intent(this, MapsActivity.class));
        finish();
    }

    private void updateUI( Place place) {

        Map<String, Object> map = new HashMap<>(0);
        map.put("lat", place.getLatLng().latitude);
        map.put("lng", place.getLatLng().longitude);

        //Save offline
        prefs.setLat(place.getLatLng().latitude);
        prefs.setLng(place.getLatLng().longitude);

        prefs.db.getReference().child(ConstantUtil.USER_REF).child(prefs.getUid())
                .updateChildren(map)
                .addOnFailureListener(this, e -> showMessage(e.getMessage()))
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            loading.dismiss();
                           // Toast.makeText(this, "Update was successful", Toast.LENGTH_SHORT).show();
                            //setResultAndFinish();
                        } else {
                            showMessage(task.getException().getMessage());
                        }
                    }
                });
    }

    private void showMessage(CharSequence message) {
        if (loading.isShowing()) loading.dismiss();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private  class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try{
                jObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            loading.dismiss();
            ArrayList points = null;
            PolylineOptions polylineOptions =null;

            for (int i =0; i<lists.size(); i++){
                points = new ArrayList();
                polylineOptions = new PolylineOptions();
                List<HashMap<String,String>> path = lists.get(i);

                for (int j =0; j<path.size(); j++){
                    HashMap<String,String> point= path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat,lng);

                    points.add(position);
                }

                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);

            }
            direction = mMap.addPolyline(polylineOptions);
        }
    }

}
