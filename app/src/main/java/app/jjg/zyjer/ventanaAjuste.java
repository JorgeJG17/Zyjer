package app.jjg.zyjer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ventanaAjuste extends AppCompatActivity {
    //Ventana creada por la TASK 7
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ventana_ajuste);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //Bt Antes de Empezar
    public void onButtonClick2(View view){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¡Bienvenido/a a ZyJer!");
        builder.setMessage("Estás usando una versión Beta, lo que significa que todavía estamos en fase temprana de desarrollo. Algunas funciones podrían no estar completas o presentar errores.\n\nTu ayuda es muy valiosa: si notas algún problema o tienes sugerencias, por favor compártelas con el desarrollador. ¡Tu feedback nos ayuda a mejorar!\n" +
                        "\n" + "¡Gracias por ser parte del inicio de este proyecto!");
        builder.setPositiveButton("OK", null);
        builder.show();
    }


    //Bt Version
    public void onVersion(View view){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¡Bienvenido/a a la Versión 2.0Beta!");
        // Texto resumido para el AlertDialog
        String mensaje = "\n\n" +
                "• \n\n" +
                "• \n\n" +
                "• \n\n" +
                "• \n\n" +
                "• ";

        builder.setMessage(mensaje);
        builder.setPositiveButton("OK", null);
        builder.show();

    }

    public void onbd(View view){
        BD(ventanaAjuste.this);
    }
    public void BD(Context context) {
        try {
            // Origen: la base de datos interna
            File dbFile = context.getDatabasePath("dbgym");

            // Destino: carpeta externa privada de la app (no requiere permisos)
            File exportDir = new File(context.getExternalFilesDir(null), "dbgym.db");

            FileInputStream fis = new FileInputStream(dbFile);
            FileOutputStream fos = new FileOutputStream(exportDir);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            fis.close();
            fos.close();

            // Compartir el archivo (WhatsApp u otros)
            Uri fileUri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".provider", exportDir);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/octet-stream");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, "Compartir base de datos"));

            Toast.makeText(context, "Backup creado: " + exportDir.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error exportando la base de datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}