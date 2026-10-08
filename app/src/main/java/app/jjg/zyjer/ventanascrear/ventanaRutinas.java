/*
Clase ventanaRutinas.java
Fecha actualiza: 07/10/2026
Autor: Jorge Jimenez Garrido
Descripcion: Desde esta pantalla podemos crear nuestras ruitinas
*/
package app.jjg.zyjer.ventanascrear;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import app.jjg.zyjer.R;
import app.jjg.zyjer.database.Modelo;
import app.jjg.zyjer.database.RutinasTL;

public class ventanaRutinas extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) { //Metodod de inicio de la pantalla
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ventana_rutinas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //Boton de  crear rutinas
    public void crearRutClick(View view){

        Modelo obj = new Modelo(); //Nos conectamos a la bd
        RutinasTL rut = new RutinasTL();

        EditText tNom = findViewById(R.id.textNombre);
        EditText tdias = findViewById(R.id.textDias);

        //rut.setId("1");
        String sdias = tdias.getText().toString().trim();
        String nombre = tNom.getText().toString().trim();

        //Debemos de tener un nombre
        if (nombre.isEmpty()) {
            tNom.setError("Escribe un nombre para la rutina");
            tNom.requestFocus();
            return;
        }

        int dias;

        try {
            dias = Integer.parseInt(sdias);
        } catch (NumberFormatException e) { //Debemos de tener un numero de dias
            tdias.setError("Introduce un número de días");
            tdias.requestFocus();
            return;
        }
        //Minimo tene que tener 1 dia
        if (dias < 1) {
            tdias.setError("La rutina debe tener al menos un día");
            tdias.requestFocus();
            return;
        }
        rut.setDias(sdias);
        rut.setNombre(nombre);

        int resInsert = obj.InsertaRutina(ventanaRutinas.this,rut);

        if(resInsert == 1){ //Nos de vuelve 1 en caso de que se a insertado correctamente
            Toast.makeText(ventanaRutinas.this, "Ok", Toast.LENGTH_SHORT).show();
            int id = obj.SeleccionarIdRutina(ventanaRutinas.this,rut); //Obtenemos el id creado por la nueva rutina


            Intent intent = new Intent(this, ventanaEjercicios.class);
            intent.putExtra("idRutina", id); //Le enviamos el id de la rutina a la siguiente pantalla
            intent.putExtra("sdias", dias);
            startActivity(intent);//Llamamos a la siguiente pantalla

        } else if(resInsert == 2){ //Devuelve 2 si ya existe una rutina con ese nombre
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("¡Atención!");
            builder.setMessage("Ya tienes una rutina creada con ese nombre. No puedes tener dos rutinas con el mismo nombre!");
            builder.setPositiveButton("OK", null);
            builder.show();
           //Toast.makeText(ventanaRutinas.this, "Ya tienes una rutina creada cin ese nombre. No puedes tener dos rutinas con el mismo nombre", Toast.LENGTH_SHORT).show();

        } else{ //Ha lanzado un error no controlado
            Toast.makeText(ventanaRutinas.this, "¡Ups! Algo salió mal. Por favor, infórmaselo al desarrollador. Recuerda que esta es una versión Beta.", Toast.LENGTH_SHORT).show();
        }
    }
}
