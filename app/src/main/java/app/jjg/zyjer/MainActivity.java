package app.jjg.zyjer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import app.jjg.zyjer.ventanascrear.ventanaRutinas;
import app.jjg.zyjer.ventanasentrenar.ventana_entrenar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  /* Esta línea siempre debe ir al principio de onCreate() y es esencial.
            Hace lo siguiente:
            Llama al método onCreate() de la clase base (Activity).
            Inicializa aspectos fundamentales del ciclo de vida de la actividad.
            Permite restaurar el estado anterior de la actividad si fue recreada (por ejemplo, tras una rotación de pantalla).
            Nunca debes omitirla, salvo casos muy específicos y controlados (que rara vez se dan en apps normales).
            
            */
        //EdgeToEdge.enable(this); //Esto es para que use la pantalla completa TASK 7 no es necesario por el cambio de pantalla principal
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //Que no deje el modo oscuro, asi manetenmos los colores de nuestra app

        //TASK 7 Ponemos como pantalla principal la ventana_entrenar
        //setContentView(R.layout.activity_main); // No es necesario inflar el layout si solo vas a redirigir
        Intent intent = new Intent(this, ventana_entrenar.class);
        startActivity(intent);

        finish(); // Finalizar MainActivity para que no quede en el historial

        //TASK 7 esto no es neceario
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
    }

    //Boton de  crear rutinas
    /*public void onButtonClick3(View view){

       Intent intent = new Intent(this, ventanaRutinas.class);
        startActivity(intent);
    }*/

    //Boton de entrenar
    /*public void onButtonClick4(View view){

        Intent intent = new Intent(this, ventana_entrenar.class);
        startActivity(intent);
    }*/

    /*public void onButtonClick2(View view){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¡Bienvenido/a a ZyJer!");
        builder.setMessage("Estás usando una versión Alpha, lo que significa que todavía estamos en fase temprana de desarrollo. Algunas funciones podrían no estar completas o presentar errores.\n\nTu ayuda es muy valiosa: si notas algún problema o tienes sugerencias, por favor compártelas con el desarrollador. ¡Tu feedback nos ayuda a mejorar!\n" +
                        "\n" + "¡Gracias por ser parte del inicio de este proyecto!");
        builder.setPositiveButton("OK", null);
        builder.show();
    }*/
}