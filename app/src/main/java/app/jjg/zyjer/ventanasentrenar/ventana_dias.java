package app.jjg.zyjer.ventanasentrenar;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import app.jjg.zyjer.R;
import app.jjg.zyjer.database.Modelo;

public class ventana_dias extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ventana_dias);

        Intent intent = getIntent();
        idRutina = intent.getIntExtra("idRutina", -1); //Idrutina que viene de la otra pantalla
        PintarBT(); //Metodo para pintar los botones de los dias que tiene la rutina

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void PintarBT(){
        layoutBotones = findViewById(R.id.layoutBotones);

        // Realiza la consulta a la base de datos y recupera los dias de la rutina previamente seleccionada
        int dias = consultaDB();

        // Crea un botón para cada dia recuperado
        for(int i = 1; i<=dias; i++){
            Button boton = new Button(this);
            String nom_dia = consultarTipDia(ventana_dias.this,idRutina,i); //TASK 6
            boton.setText("Día "+ i + " " + nom_dia); //Mpodificacion en la TASK 6
            boton.setTag(i);

            // Establecer listener para cuando se haga click en el botón
            boton.setOnClickListener(v -> {

                //Intent intent = new Intent(this, ventanaRutEjerc.class); //venatana Tabla
                Intent intent = new Intent(this, ventanaEntrenamientoTarjetas.class); //Ventana Tarjetas
                intent.putExtra("dia", (int)v.getTag()); //Le enviamos el dia de la rutina a la siguiente pantalla
                intent.putExtra("idRutina", idRutina);  //Le enviamos el id de la rutina a la siguiente pantalla
                //intent.putExtra("nomDia", nom_dia); //TASK 6

                startActivity(intent);
            });

            layoutBotones.addView(boton);
        }
    }

    private int consultaDB(){

        int dias;

        Modelo obj = new Modelo(); //Nos conectamos a la bd

        Cursor resultados = obj.SeleccionarDias(ventana_dias.this,idRutina); //Recuperamos los dias para la rutina x de la db
        resultados.moveToFirst(); //primera fila recupera y unica en esat consulta
        dias = resultados.getInt(0); //Nos devuelve el numero de dias totales de esta rutina
        resultados.close();

        return dias;
    }

    //TASK 6
    //Este metodop es publico y statico para usarlo fuera de esta clase
    //Lo usamos para consultar el nombre del dia selecionado
    public static String consultarTipDia(Context cont,int idRutina_n, int d){

        Modelo obj = new Modelo(); //Nos conectamos a la bd
        String nom_dia = obj.SeleccionarTipDia(cont,idRutina_n,d);

        return nom_dia;
    }


    //Campos de clase
    private LinearLayout layoutBotones;  // El contenedor donde se agregarán los botones
    private int idRutina; //idRutina
}