package app.jjg.zyjer.ventanashistorial;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import app.jjg.zyjer.R;
import app.jjg.zyjer.database.EjerciciosTL;
import app.jjg.zyjer.database.Modelo;
import app.jjg.zyjer.database.RutinasTL;
import app.jjg.zyjer.ventanasentrenar.ventana_dias;
import app.jjg.zyjer.ventanasentrenar.ventana_entrenar;

public class ventanaBTejer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ventana_btejer);

        Intent intent = getIntent();
        idRutina = intent.getIntExtra("idRutina", -1); //recuperadmos el idRutina
        dia = intent.getIntExtra("dia", -1); //recuperamos el dia

        PintarBT();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void PintarBT(){
        layoutBotones = findViewById(R.id.layoutBotones);

        // Realiza la consulta a la base de datos y recupera los nombres
        List<EjerciciosTL> ejercicios = consultaDB();

        // Crea un botón para cada nombre recuperado
        for (EjerciciosTL datos : ejercicios) {
            Button boton = new Button(this);
            boton.setText(datos.getNombre());
            boton.setTag(Integer.parseInt(datos.getId()));

            // Establecer listener para cuando se haga clic en el botón
            boton.setOnClickListener(v -> {

                    Intent intent = new Intent(this, ventanaHistorial.class);
                    intent.putExtra("idEjercicio", (int) v.getTag()); //Le enviamos el id de la rutina a la siguiente pantalla
                    startActivity(intent);

                    // Aquí puedes utilizar el ID de la rutina
                    //Toast.makeText(MiActividad.this, "ID de la rutina: " + rutina.getId(), Toast.LENGTH_SHORT).show();
            });

            layoutBotones.addView(boton);
        }
    }


    private List<EjerciciosTL> consultaDB() {

        List<EjerciciosTL> ejercicios = new ArrayList<>();

        Modelo obj = new Modelo(); //Nos conectamos a la bd

        Cursor resultados = obj.SeleccionarEjercicos(ventanaBTejer.this, idRutina, dia);

        if (resultados != null && resultados.moveToFirst()) {
            do {
                // Obtén el nombre y el ID de la rutina desde el cursor
                int idejercicio = resultados.getInt(0);
                String nombreEjercicio = resultados.getString(1);


                EjerciciosTL ejerciciosct = new EjerciciosTL();
                ejerciciosct.setId(Integer.toString(idejercicio));
                ejerciciosct.setNombre(nombreEjercicio);

                // Crea un objeto Ejercicio y lo agrega a la lista
                ejercicios.add(ejerciciosct);

            } while (resultados.moveToNext());  // Continúa hasta el siguiente resultado en el cursor
        }

        // Cierra el cursor después de usarlo
        if (resultados != null) {
            resultados.close();
        }

        return ejercicios;
    }

    private int idRutina; //idRutina
    private int dia; //dia de entrenamiento
    private LinearLayout layoutBotones;
}