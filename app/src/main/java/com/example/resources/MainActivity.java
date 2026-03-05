package com.example.resources;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private Activity activity;
    private TextView versionAndroid;
    private int versionSDK;
    private ProgressBar pbLevelBattery;
    private TextView tvlevelBattery;
    private IntentFilter batteryFilter;
    private TextView tvConexion;
    private ConnectivityManager connectivityManager;
    private CameraManager cameraManager;
    private String cameraId;
    private Button onFlash;
    private Button offFlash;
    
    // Objetos para archivos
    private EditText namesFile;
    private Button btnSave;
    private FileAdmin fileAdmin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        initObjects();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(broadReceiver, batteryFilter);

        this.onFlash.setOnClickListener(this::startFlash);
        this.offFlash.setOnClickListener(this::endFlash);
        
        // Listener para guardar archivo
        this.btnSave.setOnClickListener(v -> saveFileAction());
    }

    private void saveFileAction() {
        String fileName = namesFile.getText().toString().trim();
        if (fileName.isEmpty()) {
            Toast.makeText(this, "Escribe un nombre para el archivo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fileAdmin.checkPermission()) {
            fileAdmin.crearArchivo("MisDocumentos", fileName + ".txt", "Hola, este es un archivo de prueba.");
        } else {
            fileAdmin.requestPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FileAdmin.REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveFileAction(); // Reintentar guardar si aceptó
            } else {
                Toast.makeText(this, "Permiso denegado para escribir archivos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initObjects(){
        this.context = this;
        this.activity = this;

        this.versionSDK = -1;
        this.cameraId= "";

        this.versionAndroid = findViewById(R.id.tvVersionAndroid);
        this.pbLevelBattery = findViewById(R.id.pbLevelBatery);
        this.tvlevelBattery = findViewById(R.id.tvLevelBatery);
        this.tvConexion = findViewById(R.id.tvConexion);
        
        this.onFlash = findViewById(R.id.btnOnFlash); // Asumiendo IDs
        this.offFlash = findViewById(R.id.btnOffFlash);
        
        // Inicializar FileAdmin y sus vistas
        this.fileAdmin = new FileAdmin(context, activity);
        this.namesFile = findViewById(R.id.etFileName); 
        this.btnSave = findViewById(R.id.btnSaveFile);
    }

    // ... (resto de tus métodos como startFlash, endFlash, etc. se mantienen igual)
    
    private void startFlash(View view) {
        try {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
            Toast.makeText(this, "Encendiendo flash", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.i("onFlash", "Error al encender el Flash"+e);
        }
    }

    private void endFlash(View view) {
        try {
            if (cameraManager != null && cameraId != null) {
                cameraManager.setTorchMode(cameraId, false);
            }
        } catch (Exception e) {
            Log.i("offFlash", "Error al apagar el Flash" + e);
        }
    }

    BroadcastReceiver broadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int levelBattery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            if (pbLevelBattery != null) pbLevelBattery.setProgress(levelBattery);
            if (tvlevelBattery != null) tvlevelBattery.setText("Nivel de batería: " + levelBattery + "%");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        getVersionAndroid();
        checkConnection();
    }

    private void getVersionAndroid(){
        String versionSO = Build.VERSION.RELEASE;
        versionSDK = Build.VERSION.SDK_INT;
        if (versionAndroid != null) versionAndroid.setText("Version SO:"+versionSO+"\nVersion SDK:"+versionSDK);
    }

    private void checkConnection(){
        try {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null){
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                boolean stateNet = networkInfo != null && networkInfo.isConnectedOrConnecting();
                if(tvConexion != null) tvConexion.setText(stateNet ? "State ON" : "State OFF");
            }
        } catch (Exception e) {
            Log.i("NETINFO", "Error de conexión" + e);
        }
    }
}
