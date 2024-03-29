package pl.wat.nutpromobile.activity.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;


import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LifecycleObserver;
import androidx.navigation.NavController;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.wat.nutpromobile.NotificationCreator;
import pl.wat.nutpromobile.R;
import pl.wat.nutpromobile.activity.OnFragmentInteractionListener;
import pl.wat.nutpromobile.ble.Connection;
import pl.wat.nutpromobile.fragments.connection.FragmentConnection;
import pl.wat.nutpromobile.training.TrainingService;
import pl.wat.nutpromobile.training.TrainingServiceHelper;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener, FragmentConnection.OnFragmentInteractionListener {
    public final static String TAG = "Custom: " + MainActivity.class.getSimpleName();
    @BindView(R.id.navigation)
    BottomNavigationView navigation;

    private Connection connection;
    private Permission permission;
    private PreferencesManager preferencesManager;
    private TrainingServiceHelper trainingServiceHelper;

    private TrainingService trainingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, TAG + " created");
        setContentView(R.layout.activity_main);
        permission = new Permission(this);
        permission.startPermissionRequest();
        ButterKnife.bind(this);
        trainingServiceHelper = new TrainingServiceHelper(this);
        NavigationUI.setupWithNavController(navigation, Navigation.findNavController(this, R.id.nav_host_fragment));
        connection = new Connection(this, getLifecycle());
        getLifecycle().addObserver(connection);
        preferencesManager = new PreferencesManager(this);
        NotificationCreator.createNotificationChannel(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, TAG + " started");
        if (trainingService != null) {
            bindService(new Intent(this,
                    TrainingService.class), mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void startTraining() {
        System.out.println("Activity START Click");
        Intent intent = new Intent(this, TrainingService.class);
        intent.putExtra(TrainingService.NOTIFICATION, trainingServiceHelper.buildNotification());

        startService(intent);
        if (trainingService == null) {
            bindService(new Intent(this,
                    TrainingService.class), mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void stopTraining() {
        System.out.println("Activity STOP Click");
        Intent intent = new Intent(this, TrainingService.class);
        trainingService = null;
        stopService(intent);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TrainingService.LocalBinder binder = (TrainingService.LocalBinder) iBinder;
            trainingService = binder.getService();
            trainingService.handleTraining(connection);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            trainingService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, TAG +" destroyed");
        if (trainingService != null) {
            // Detach the service connection.
            unbindService(mConnection);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
