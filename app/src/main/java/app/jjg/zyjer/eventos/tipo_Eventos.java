package app.jjg.zyjer.eventos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Arrays;

import app.jjg.zyjer.R;
import app.jjg.zyjer.database.Modelo;
import app.jjg.zyjer.ventanasentrenar.ventanaRutEjerc;
import app.jjg.zyjer.ventanasentrenar.ventana_dias;

//TASK 5
public class tipo_Eventos extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tipo_eventos);

        Intent intent = getIntent();
        idEvento = intent.getIntExtra("idEvento", -1); //IdEvento que viene de la otra pantalla
        pintarTiposBT();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }

    private void pintarTiposBT(){

        consultaDB();
        layoutBotones = findViewById(R.id.layoutBotones);
        int contador = 0;

        for(char d : arrayDB){

            if(d == 'S'){
                Button boton = new Button(this);
                boton.setText(tiposarray[contador]);
                boton.setTag(contador); //0 --> Aumento de masa muscular, 1--> Perder Peso, 2-->Fuerza, 3-->Tonificar, 4-->Salud

                // Establecer listener para cuando se haga click en el botón LAMBDA
                boton.setOnClickListener(v -> { //v -> es lo mismo que: public void onClick(View v) { ... }. Sirve porque la interfaz View.OnClickListener solo tiene un metodo abstracto

                    Intent intent = new Intent(this, dias_Eventos.class);
                    intent.putExtra("tipo", (int)v.getTag()); //Le enviamos el dia de la rutina a la siguiente pantalla
                    intent.putExtra("idEvento", idEvento);  //Le enviamos el id de la rutina a la siguiente pantalla
                    //intent.putExtra("nomDia", nom_dia); //TASK 6

                    startActivity(intent);
                });

                layoutBotones.addView(boton);
            }

            contador++;
        }
    }


    private void consultaDB(){

        Modelo obj = new Modelo();
        arrayDB = obj.tiposDisponibleEventos(tipo_Eventos.this, idEvento);
    }

    //CAMPOS DE CLASE
    private int idEvento;
    private final String[] tiposarray = {"Aumento de masa muscular","Perder Peso","Fuerza","Salud","Tonificar"}; //final es constante pero en las array funciona para que nos se pueda agregar mas tamanoo
    //Tenemos que usar un Arrays.copyOF() del array para hacer ino inmutable y asi no se le pueda cambiar lo de dentro
    private char[] arrayDB = new char[5];
    private LinearLayout layoutBotones;  // El contenedor donde se agregarán los botones
}