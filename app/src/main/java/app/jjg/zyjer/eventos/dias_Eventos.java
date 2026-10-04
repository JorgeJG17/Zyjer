package app.jjg.zyjer.eventos;

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
import app.jjg.zyjer.ventanasentrenar.ventana_dias;

public class dias_Eventos extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dias_eventos);

        Intent intent = getIntent();
        idEvento = intent.getIntExtra("idEvento", -1); //IdEvento que viene de la otra pantalla
        tipo = intent.getIntExtra("tipo", -1);

        PintarDiasBT();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void PintarDiasBT(){

        consultaDB();
        layoutBotones = findViewById(R.id.layoutBotones);
        int contador = 1;
        int dias = consultaDB();

        while(contador <= dias){

                Button boton = new Button(this);
                boton.setText("Día " + contador);
                boton.setTag(contador); //0 --> Aumento de masa muscular, 1--> Perder Peso, 2-->Fuerza, 3-->Tonificar, 4-->Salud

                // Establecer listener para cuando se haga click en el botón LAMBDA
                boton.setOnClickListener(v -> { //v -> es lo mismo que: public void onClick(View v) { ... }. Sirve porque la interfaz View.OnClickListener solo tiene un metodo abstracto

                    Intent intent = new Intent(this, ejer_eventos.class);
                    intent.putExtra("tipo", tipo); //Le enviamos el dia de la rutina a la siguiente pantalla
                    intent.putExtra("idEvento", idEvento);
                    intent.putExtra("dia", (int)v.getTag());

                    startActivity(intent);
                });

                layoutBotones.addView(boton);
                contador ++;
        }
    }

    private int consultaDB(){

        int dias;

        Modelo obj = new Modelo(); //Nos conectamos a la bd

        Cursor resultados = obj.SeleccionarDiasEV(dias_Eventos.this,idEvento); //Recuperamos los dias para la rutina x de la db
        resultados.moveToFirst(); //primera fila recupera y unica en esa consulta
        dias = resultados.getInt(0); //Nos devuelve el numero de dias totales de esta rutina
        resultados.close();

        return dias;
    }

    //CAMPOS DE CLASE
    private int idEvento;
    private int tipo;
    private LinearLayout layoutBotones;  // El contenedor donde se agregarán los botones
}