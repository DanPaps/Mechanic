package paps.bookman.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import paps.bookman.R;
import paps.bookman.data.User;
import paps.bookman.util.ConstantUtil;
import paps.bookman.util.MechanicUserPrefs;

public class AuthActivity extends AppCompatActivity {
    private static final int PLACE_PICKER_REQUEST = 1;

    @BindView(R.id.email)
    TextInputEditText email;
    @BindView(R.id.password)
    TextInputEditText password;
    @BindView(R.id.username)
    TextInputEditText username;
    @BindView(R.id.phone)
    TextInputEditText phone;
    @BindView(R.id.login)
    Button login;
    @BindView(R.id.signup)
    Button signUp;
    @BindView(R.id.container)
    ViewGroup container;
    @BindView(R.id.signup_container)
    ViewGroup signUpContainer;

    private boolean isShowingSignUp = false;
    private MechanicUserPrefs prefs;
    private MaterialDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        // Init prefs
        prefs = new MechanicUserPrefs(this);
        dialog = ConstantUtil.getDialog(this);
    }

    @OnClick(R.id.login)
    void doLogin() {
        if (isShowingSignUp) {
            TransitionManager.beginDelayedTransition(container);
            signUpContainer.setVisibility(View.GONE);
            isShowingSignUp = false;
        } else {
            if (ConstantUtil.hasValidFields(password, email)) {
                dialog.show();
                prefs.auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    getUser(task.getResult().getUser());
                                } else {
                                    if (task.getException() != null)
                                        setError(task.getException().getMessage());
                                }
                            }
                        }).addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        setError(e.getMessage());
                    }
                });
            } else {
                setError("Please check the fields properly");
                email.requestFocus();
            }
        }
    }

    private void getUser(FirebaseUser user) {
        prefs.db.getReference().child(ConstantUtil.USER_REF).child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            User value = dataSnapshot.getValue(User.class);
                            prefs.updateUser(value);
                            navHome();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        setError(databaseError.getMessage());
                    }
                });
    }

    @OnClick(R.id.signup)
    void doSignUp() {
        if (isShowingSignUp) {
            pickLocation();
            dialog.show();
        } else {
            TransitionManager.beginDelayedTransition(container);
            signUpContainer.setVisibility(View.VISIBLE);
            isShowingSignUp = true;
        }
    }

    private void updateUI(FirebaseUser user, Place place) {
        final User newUser = new User(
                user.getUid(),
                username.getText().toString(),
                user.getEmail(),
                phone.getText().toString(),
                null,
                place.getAddress() == null ? null : place.getAddress().toString(),
                place.getLatLng().latitude,
                place.getLatLng().longitude
        );
        prefs.db.getReference().child(ConstantUtil.USER_REF).child(user.getUid())
                .setValue(newUser).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    prefs.updateUser(newUser);
                    navHome();
                } else {
                    if (task.getException() != null)
                        setError(task.getException().getMessage());
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                setError(e.getMessage());
            }
        });
    }

    private void navHome() {
        startActivity(new Intent(this, MapsActivity.class));
        finish();
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
        if (dialog.isShowing()) dialog.dismiss();
        Snackbar.make(container, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PLACE_PICKER_REQUEST) {
                Place place = PlacePicker.getPlace(this, data);
                if (place != null) {
                    createNewUser(place);
                }
            }
        } else {
            if (dialog.isShowing()) dialog.show();
        }
    }

    private void createNewUser(final Place place) {
        if (ConstantUtil.hasValidFields(email, password, username, phone)) {
            prefs.auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                updateUI(task.getResult().getUser(), place);
                            } else {
                                if (task.getException() != null)
                                    setError(task.getException().getMessage());
                            }
                        }
                    }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    setError(e.getMessage());
                }
            });
        } else {
            setError("Please check the fields properly");
            email.requestFocus();
        }
    }
}
