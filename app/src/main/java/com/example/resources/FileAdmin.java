package com.example.resources;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileAdmin {

    private Context context;
    private Activity activity;
    
    // El permiso que queremos solicitar
    private static final String PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final int REQUEST_CODE = 100;

    public FileAdmin(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public FileAdmin(){
    }

    // Metodo para verificar si el permiso ya fue concedido
    public boolean checkPermission(){
        // En Android 10 (API 29) o superior, para usar almacenamiento interno de la app 
        // ya no es estrictamente necesario solicitar permiso de escritura externo.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }
        int result = ContextCompat.checkSelfPermission(context, PERMISSION);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    // Metodo para solicitar el permiso al usuario
    public void requestPermission(){
        ActivityCompat.requestPermissions(activity, new String[]{PERMISSION}, REQUEST_CODE);
    }

    // Metodo para gestionar la creacion del directorio
    public File crearDirectorio(String nombreCarpeta) {
        // Usamos getExternalFilesDir para que la carpeta sea privada de la app 
        // y funcione correctamente en versiones nuevas de Android (Scoped Storage)
        File folder = new File(context.getExternalFilesDir(null), nombreCarpeta);
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                Toast.makeText(context, "Carpeta creada: " + nombreCarpeta, Toast.LENGTH_SHORT).show();
            }
        }
        return folder;
    }

    // Metodo para gestionar la creacion del archivo
    public void crearArchivo(String nombreCarpeta, String nombreArchivo, String contenido) {
        File folder = crearDirectorio(nombreCarpeta);
        File file = new File(folder, nombreArchivo);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(contenido.getBytes());
            Toast.makeText(context, "Archivo guardado: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al crear archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Validacion de versiones
    public boolean requierePermisosEspeciales() {
        // Las versiones inferiores a Android 10 (Q) requieren permisos explícitos
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q;
    }
}
