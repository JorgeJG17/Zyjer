/*
Clase ventanaPesaje.java
Fecha actualiza: 28/03/2026
Autor: Jorge Jimenez Garrido
Descripcion: Clase de la ventana activity_ventana_pesaje.xml
*/

package app.jjg.zyjer.pesaje;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import app.jjg.zyjer.R;
import app.jjg.zyjer.database.Modelo;
import app.jjg.zyjer.database.PesajeTL;

public class ventanaPesaje extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ventana_pesaje);

        pintarPesaje(); //Pintamos la tabla del historial del ejercicio seleccionado

        //TASK 15
        if(contador != 1){ //Para que cuando llegue solo 1 registro pues que no pinte el grafico

            pintarGrafico(); //Pintamos el grafico del historial obtenido
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //Metodo para pintar la tabla del Pesaje  Fecha / Peso
    private void pintarPesaje(){

        List<PesajeTL> h = consultaBD(); //Llamamos a la consulta que nos devolvera una lista donde almacena objetos de tipo PesajeTL
        tableLayout = findViewById(R.id.tableLayout); //Creamos nuestra tabla
        int contadorArray = h.size()-1; //Un contador para añadir los datos al contrario a como nos llega, recordar que llegan en orden del mas nuevo al mas antiguo

        //Vamos a recorrer los datos que han llegado para pintarlos en la tabla
        for (PesajeTL datos : h) {

            TableRow tablaPesaje = new TableRow(this);
            //***************************************************************************************************************************************

            //Voy a rellenar la arraylist de los puntos del grafico del peso y la array de las fechas, esto servira para luego crear el garfico
            //como llega del peso mas nuevo registrado al mas antiguos, por eso se guarda alreves para que el mas antigu siempre este en la posiscion 0
            peso[contadorArray] = datos.getPeso();
            fechas[contadorArray] = datos.getDate();
            contadorArray--;

            //Luego seguimos pintando los datios en la tabla con normalidad
            // Crear las celdas para cada fila
            EditText textViewDate = new EditText(this);
            textViewDate.setText(datos.getDate());  // Mostrar el nombre de la rutina (Las demas explicaciones de los siguiente metodos llamado estan en Peso y RM de la clase ventanaRutEjerc.java)
            textViewDate.setBackgroundResource(R.drawable.border_tabla);
            textViewDate.setPadding(8, 8, 8, 8);
            textViewDate.setEnabled(false);       // No editable
            textViewDate.setFocusable(false);     // No se enfoca
            textViewDate.setClickable(false);     // No se puede clickar
            textViewDate.setSingleLine(true);

            //***************************************************************************************************************************************

            EditText textViewPeso = new EditText(this);
            textViewPeso.setText(datos.getPeso());
            textViewPeso.setBackgroundResource(R.drawable.border_tabla);
            textViewPeso.setPadding(8, 8, 8, 8);
            textViewPeso.setSingleLine(true);
            textViewPeso.setEnabled(false);       // No editable
            textViewPeso.setFocusable(false);     // No se enfoca
            textViewPeso.setClickable(false);     // No se puede clickar
            textViewPeso.setGravity(Gravity.CENTER);

            // Agregar las celdas a la fila
            tablaPesaje.addView(textViewDate);
            tablaPesaje.addView(textViewPeso);

            // Agregar la fila a la tabla
            tableLayout.addView(tablaPesaje);

            contador++;

            if(contador == 7){ //Para que solo muestre los 6 ultimos cambios
                break;
            }
        }

    }

    //Consulta a la DB para sacar el pesaje, devuelve una lista de objetos PesajeTL
    private List<PesajeTL> consultaBD(){

        Modelo obj = new Modelo();
        Cursor resultados = obj.SeleccionarPesaje(ventanaPesaje.this); //Llamamos al SeleccionarPesaje para hacer la consulta en la db

        List<PesajeTL> h = new ArrayList<>(); //Lista donde vamos almacenar cada objeto PesajeTl es decir cada fila de nuestra futura tabla

        if (resultados != null && resultados.moveToFirst()) {
            do {
                int peso = resultados.getInt(1);
                String date = resultados.getString(2);

                //Crea un objeto PesajeTL y le añadimos el pesaje de ese dia
                PesajeTL hisPesa = new PesajeTL();
                hisPesa.setPeso(Integer.toString(peso)); //Guardo peso
                hisPesa.setDate(date); //Guardo la fecha


                h.add(hisPesa);

            } while (resultados.moveToNext());  // Continúa hasta el siguiente resultado en el cursor
        }

        // Cierra el cursor después de usarlo
        if (resultados != null) {
            resultados.close();
        }

        return h; //devolvemos la lista
    }


    //Metodo que usamos para pintar el grafico, estas clases que usamos vienen de la libreria MPAndroidChart de github
    private void pintarGrafico(){

        //RESUMEN DE ESTE METODO (un poco mas chill):
        /*Basicamente nuestro grafico sabe el peso que llega ponte: 55.0 eso sabe que es lo que van en eje (y) pero como es primero que
        llega pues en el eje (x) pone 0, claro ese 0 lo formatea a la primera fecha guardada en la array de fecha, es decir que nuestro grafico no
        tiene ni idea de las fechas simplemnet pinta los puntos como se almacenaron en los puntos del peso (0,55.0),(1,55.0) y ese eje x para no pintar numero lo traduce
        a lo que tenemos guardados en la array de fechas(0) fechas(1) etc...
         */

        //Este for lo usamos para almacenar la array en los puntos correctamente desde 0 al X pero con el orden que pusimos en la array
        /*
        En el eje Y (donde van los pesos), no solemos usar un "traductor"
        (ValueFormatter) porque los pesos ya son números reales. El gráfico sabe perfectamente dónde dibujar el 75.5 o el 80.0.
        */

        //Para que toda esta logica tenga sentido, anteriormente hemos guarado en la arry peso y arraey fecha los datos a la vez en la misma posicion,
        //es decir, 55kg 01/01/2025 ambos se han guardao en sus arrays corresponidentes en la misma psoicion ej: pesos(0),fechas(0)
        for(int i = 0; i<contador; i++){

            puntos.add(new Entry(i, Float.parseFloat(peso[i]))); //aqui guardamos el orden los pesos y=peso1 en el x=0, y=peso2 en el x=1
        }

        //Este objeto con su metodo para formatear las fechas en un valor para los puntos 0,1,2,3,4,5 que hemos puesto antes en el eje x
        /*
        El gráfico no sabe qué es un objeto Fecha, él solo entiende de números (coordenadas X e Y).
        Para solucionar esto, En el for: En lugar de intentar pasarle la fecha al punto, le pasas la posición i (0, 1, 2...).
        Así, el primer punto está en X=0, el segundo en X=1, etc.
        El ValueFormatter: Es como un traductor. Cuando el gráfico va a dibujar la etiqueta del eje X para la posición 0, le pregunta al ValueFormatter: "Oye, ¿qué texto pongo aquí?".
        El formateador mira tu array de fechas en esa posición y le responde: Pon la fecha que está en fechas[0].
        */
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value; // El gráfico nos da un número (0.0, 1.0...)
                if (index >= 0 && index < fechas.length) {
                    return fechas[index]; // Devolvemos el texto de la fecha
                } else {
                    return "";
                }
            }
        };

        grafico = findViewById(R.id.lineChart); // Conectar nuestra variable con el grafico del xml

        XAxis xAxis = grafico.getXAxis();
        xAxis.setGranularity(1f);  // Para que no muestre valores intermedios
        xAxis.setValueFormatter(formatter); //antes de escribir cualquier número del gráfico, pásalo primero por este traductor, sin esta linea, el gráfico simplemente pondría los numeros brutos: 0.0, 1.0, 2.0
        xAxis.setTextColor(Color.WHITE); //Color eje x
        xAxis.setAvoidFirstLastClipping(true); //para que tenga espacio en eje x y se vean bien las fechas de los extremos


        YAxis leftAxis = grafico.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE); //Color eje y

        YAxis rightAxis = grafico.getAxisRight();
        rightAxis.setEnabled(false); //ocultar el de la derecha

        //Crear la linea de los puntos
        LineDataSet dataSet = new LineDataSet(puntos, "Peso (kg)");

        //Personalizar la linea del grafico
        dataSet.setLineWidth(2f); // Grosor de la línea
        dataSet.setCircleRadius(5f); // Tamaño de los puntos
        dataSet.setDrawValues(false); // No mostrar los números sobre los puntos

        // Color de línea y puntos
        dataSet.setColor(Color.parseColor("#ffffff")); //linea
        dataSet.setCircleColor(Color.parseColor("#e22929")); //puntos

        //Objeto que necesita nuestro grafico para mostrar lineas y puntos
        LineData lineData = new LineData(dataSet); //Añadimos los datos al objeto
        grafico.setData(lineData); //Añadimos el objeto a nuestro grafico

        //Descripcion del grafico
        Description desc = new Description();
        desc.setText(""); // Vacío = no mostrar nada
        grafico.setDescription(desc);

        //Dibujar el grafico
        grafico.invalidate();
    }

    //TODO POR AQUI PONIENDO COMENTARIOS
    //Metodo que usamos para insertar nuevos pesajes
    public void onPesaje(View view){

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_contacto, null);

        EditText etPeso = dialogView.findViewById(R.id.etPeso);
        EditText etFecha = dialogView.findViewById(R.id.etFecha);
        CheckBox cbNoFecha = dialogView.findViewById(R.id.cbNoFecha);

        cbNoFecha.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etFecha.setEnabled(false);
                etFecha.setText("");
            } else {
                etFecha.setEnabled(true);
            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Registro de Peso");
        builder.setView(dialogView);


        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String peso = etPeso.getText().toString();
            String fecha = cbNoFecha.isChecked() ? "Sin fecha" : etFecha.getText().toString();

            LocalDate fecha_act = LocalDate.now();
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String fechaFormateada = fecha_act.format(formato);


            //Toast.makeText(this, "Guardado: " + peso + "kg el " + fecha, Toast.LENGTH_SHORT).show();
            PesajeTL hisPesa = new PesajeTL();
            hisPesa.setPeso(peso); //Guardo peso
            if(fecha.equals("Sin fecha")){
                hisPesa.setDate(fechaFormateada); //Guardo la fecha
            } else {
                hisPesa.setDate(fecha); //Guardo la fecha
            }

            consultaBDInsertar(hisPesa);

            tiempoEspera.removeCallbacks(runnable); // Cancelamos la ejecución anterior (si hay alguna programada)
            runnable = () -> { //En este lambda no hace falta poner el metodo run porque esta interfaz solo tiene ese metodo entcoes entiende que ese el que esta poniendo
                recreate(); //recargar la pantalla
            };
            tiempoEspera.postDelayed(runnable, 1500);// Programamos la tarea para que se ejecute dentro de 1500 ms (1.5 segundo)
        });

        // Botón de Cancelar
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        // Mostrar el diálogo
        builder.create().show();

    }


    //Consulta a la DB para insertar el pesaje en PesajeTL
    private void consultaBDInsertar(PesajeTL dto){
        Modelo obj = new Modelo();
        int resultados = obj.InsertarPesaje(ventanaPesaje.this,dto); //Llamamos al InsertarPesaje para insertar el pesaje en la db
    }


    private TableLayout tableLayout;
    private LineChart grafico; //variable para nuestro grafico
    private ArrayList<Entry> puntos = new ArrayList<>(); //puntos de nuestra grafica
    private String[] fechas = new String[5]; //array para guardar todos las fechas
    private String[] peso = new String[5]; //array para guaradar todos los pesos
    private int contador = 0; //Contador de cuanto historial ha sacado de ese ejercicio, max 5
    private Handler tiempoEspera = new Handler(); //Lo usamos para hacer el tiempo de espera de 1500 segundos y llamara al run de Runnable
    private Runnable runnable; //para utilizar su metodo run así parar ese trozo hasta que yo diga
}