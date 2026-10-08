/*
Clase ventanaEjercicios.java
Fecha actualiza: 07/10/2026
Autor: Jorge Jimenez Garrido
Descripcion: Desde esta pantalla podemos crear nuestros ejercicios de la rutina creada
*/
package app.jjg.zyjer.ventanascrear;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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
import app.jjg.zyjer.ventanasentrenar.ventana_entrenar;

public class ventanaEjercicios extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ventana_ejercicios);
        idRutina = getIntent().getIntExtra("idRutina", -1);
        totalDias = getIntent().getIntExtra("sdias", -1);
        if (idRutina < 1 || totalDias < 1) {
            finish();
            return;
        }

        //tabla y bt
        tabla = findViewById(R.id.tableLayout);
        etiquetaDia = findViewById(R.id.ndia);
        copiarDia = findViewById(R.id.bt_copiar_dia);
        copiarEjercicio = findViewById(R.id.bt_copiar_ejercicio);

        mostrarDia();
        nuevaFila();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });
    }

    //Mostramos el dia en el que estamos annadiendo ejercicios y el total de dias de la rutin creada
    private void mostrarDia() {
        etiquetaDia.setText(diaActual + " de " + totalDias);
        copiarDia.setEnabled(diaActual > 1);
        copiarEjercicio.setEnabled(diaActual > 1);
    }

    //metodo del bt para añadir un ejercicio
    public void new_fila_click(View view) {
        nuevaFila();
    }

    //metodo del bt para borrar un ejercicio
    public void clear_fila_click(View view) {
        for (int i = tabla.getChildCount() - 1; i > 0; i--) {
            TableRow fila = (TableRow) tabla.getChildAt(i);
            if (fila.getTag() == null) {
                tabla.removeViewAt(i);
                return;
            }
        }
        Toast.makeText(this, "No hay filas nuevas para quitar", Toast.LENGTH_SHORT).show();
    }

    //Metodo para copiar un dia entero anteror ya rellenado, por ejemplo copiar el dia 1 en el 3
    public void copiarDiaClick(View view) {
        //para usar esto obviamente debemos de tener minimo un dia completo ya creado
        if (diaActual <= 1) return;

        //Las opciones van a depender de cuantos dias llevamos creados en el momento
        String[] opciones = new String[diaActual - 1];

        //rellenamos las opciones
        for (int i = 0; i < opciones.length; i++) opciones[i] = "Día " + (i + 1);

        //sacamos el alert
        new AlertDialog.Builder(this).setTitle("Copiar un día anterior")
                .setItems(opciones, (dialogo, indice) -> {
                    //llamamos a CompartirDia
                    int resultado = modelo.CompartirDia(this, idRutina, indice + 1, diaActual);

                    //Segun el resultado tenemos varios errores controlados
                    if (resultado == 1) {
                        cargarCompartidos(); //llamamos para cargar la tabla nueva
                        Toast.makeText(this, "Día compartido", Toast.LENGTH_SHORT).show();
                    } else if (resultado == 2) {
                        aviso("Este día ya contiene ejercicios guardados.");
                    } else if (resultado == 3) {
                        aviso("Algún ejercicio ya pertenece a dos días. Copia los demás por separado.");
                    } else if (resultado == 4) {
                        aviso("El día elegido no contiene ejercicios.");
                    } else {
                        aviso("No se pudo copiar el día.");
                    }
                }).setNegativeButton("Cancelar", null).show();
    }

    //Metodo para copiar un ejercicio anterior de un dia ya rellenado, por ejemplo copiar el ejercicio x del dia 1 en el dia 3
    public void copiarEjercicioClick(View view) {
        //para usar esto obviamente debemos de tener minimo un dia completo ya creado
        if (diaActual <= 1) return;

        List<Integer> ids = new ArrayList<>();
        List<String> opciones = new ArrayList<>();

        //Sacamos ejercicicios anteriores de los dias ya creados, demomento solo podemos tener un mismo ejercicio en un maximo de dos dias
        try (Cursor anteriores = modelo.SeleccionarEjerciciosAnteriores(this, idRutina, diaActual)) {
            while (anteriores.moveToNext()) {
                ids.add(anteriores.getInt(0));
                opciones.add("Día " + anteriores.getInt(2) + " · " + anteriores.getString(1));
            }
        }
        if (ids.isEmpty()) {
            aviso("No hay ejercicios anteriores disponibles para compartir.");
            return;
        }
        //Mostramos el alert con las opciones
        new AlertDialog.Builder(this).setTitle("Copiar ejercicio")
                .setItems(opciones.toArray(new String[0]), (dialogo, indice) -> {
                    int resultado = modelo.CompartirEjercicio(this, idRutina, ids.get(indice), diaActual);
                    if (resultado == 1) cargarCompartidos(); //llamamos para cargar la tabla nueva
                    else if (resultado == 2) aviso("Ese ejercicio ya está en este día.");
                    else aviso("No se pudo compartir el ejercicio.");
                }).setNegativeButton("Cancelar", null).show();
    }

    //Si se han compartod correcatmente los nuevos ejercicos o un ejercicio concreto, lo mostramos en pantalla en la tabla
    private void cargarCompartidos() {
        //Antes de cargar nada, eliminamos las filas compartidas que ya estuvieran visibles
        for (int i = tabla.getChildCount() - 1; i > 0; i--) {
            if (FILA_COMPARTIDA.equals(tabla.getChildAt(i).getTag())) tabla.removeViewAt(i);
        }

        //Consultamos los ejercicios de la rutina, porque en algunos que ya estan insertados, se ha puesto dia secundario al compartir
        //dia_secundario = actual
        try (Cursor ejercicios = modelo.SeleccionarEjercicos(this, idRutina, diaActual)) {
            while (ejercicios.moveToNext()) {
                TableRow fila = new TableRow(this);
                fila.setTag(FILA_COMPARTIDA); //Rellenamos el tag para saber despues que ejercico es compartido y cual nuevo, porque los compartidos no se insertan
                fila.addView(celda(ejercicios.getString(1), 160));
                fila.addView(celda(ejercicios.getString(2), 64));
                fila.addView(celda(ejercicios.getString(3), 64));
                fila.addView(celda(ejercicios.getString(4), 64));

                //Una marca, que indica que es un ejercicio ya guardado y compartido.
                fila.addView(celda("✓", 80));

                //Insertamos la fila en la posicion 1, justo debajo de la cabecera. Por eso los ejercicios compartidos aparecen arriba y las filas nuevas editables quedan debajo.
                tabla.addView(fila, 1);
            }
        }
    }

    private TextView celda(String texto, int anchoDp) {
        TextView vista = new TextView(this);
        vista.setText(texto);
        vista.setTextColor(0xFFFFFFFF);
        vista.setBackgroundResource(R.drawable.border_tabla);
        vista.setGravity(Gravity.CENTER_VERTICAL);
        vista.setPadding(8, 8, 8, 8);
        vista.setLayoutParams(new TableRow.LayoutParams((int) (anchoDp * getResources().getDisplayMetrics().density),
                TableRow.LayoutParams.WRAP_CONTENT));
        return vista;
    }

    //Metodo del bt finalizar dia
    public void finDiaClick(View view) {
        //Comprobamos que este correctamente cad ejercicio
        for (int i = 1; i < tabla.getChildCount(); i++) {
            TableRow fila = (TableRow) tabla.getChildAt(i);
            if (fila.getTag() != null) continue; //Si esta relleno, se salta el codigo y pasamos a la siguiente fila, significa que es compartida
            EditText nombre = (EditText) fila.getChildAt(0);
            EditText series = (EditText) fila.getChildAt(1);
            EditText repes = (EditText) fila.getChildAt(2);
            EditText peso = (EditText) fila.getChildAt(3);
            String n = nombre.getText().toString().trim();
            String s = series.getText().toString().trim();
            String r = repes.getText().toString().trim();
            String p = peso.getText().toString().trim();
            if (n.isEmpty() && s.isEmpty() && r.isEmpty() && p.isEmpty()) continue;
            if (n.isEmpty() || !positivo(s) || !positivo(r) || !numeroPeso(p)) {
                aviso("Completa nombre, series, repeticiones y peso de cada ejercicio.");
                nombre.requestFocus();
                return;
            }
        }

        //Guardamos los ejercicios en un objeto EjerciciosTL
        for (int i = 1; i < tabla.getChildCount(); i++) {
            TableRow fila = (TableRow) tabla.getChildAt(i);
            if (fila.getTag() != null) continue; //Si esta relleno, se salta el codigo y pasamos a la siguiente fila, significa que es compartida
            EditText nombre = (EditText) fila.getChildAt(0);
            if (nombre.getText().toString().trim().isEmpty()) continue;
            EjerciciosTL ejercicio = new EjerciciosTL();
            ejercicio.setIdRutina(String.valueOf(idRutina));
            ejercicio.setDia(String.valueOf(diaActual));
            ejercicio.setNombre(nombre.getText().toString().trim());
            ejercicio.setSeries(((EditText) fila.getChildAt(1)).getText().toString().trim());
            ejercicio.setRepes(((EditText) fila.getChildAt(2)).getText().toString().trim());
            ejercicio.setPeso(((EditText) fila.getChildAt(3)).getText().toString().trim());
            //Insertamos los ejercicios en la bd
            if (modelo.InsertaEjercicios(this, ejercicio) != 1) {
                aviso("No se pudo guardar un ejercicio. Revisa la tabla e inténtalo de nuevo.");
                return;
            }
            fila.setTag(FILA_GUARDADA);
        }
        if (diaActual == totalDias) {
            startActivity(new Intent(this, ventana_entrenar.class));
            finish();
        } else {
            diaActual++;
            tabla.removeViews(1, tabla.getChildCount() - 1);
            mostrarDia();
            nuevaFila();
        }
    }

    //metodo para comprobar que un valor sea positivo
    private boolean positivo(String texto) {
        try { return Integer.parseInt(texto) > 0; }
        catch (NumberFormatException e) { return false; }
    }

    //metodo para comprobar que un valor no sea negativo, salvo el -1
    private boolean numeroPeso(String texto) {
        try { return Double.parseDouble(texto) >= 0 || "-1".equals(texto); }
        catch (NumberFormatException e) { return false; }
    }

    //Metodo para enviar un aviso rapido
    private void aviso(String mensaje) {
        new AlertDialog.Builder(this).setMessage(mensaje).setPositiveButton("Entendido", null).show();
    }

    //metodo para annadir una nueva fila
    private void nuevaFila() {
        TableRow fila = new TableRow(this);
        EditText nombre = entrada(160, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        EditText series = entrada(64, InputType.TYPE_CLASS_NUMBER);
        EditText repes = entrada(64, InputType.TYPE_CLASS_NUMBER);
        EditText peso = entrada(64, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        CheckBox libre = new CheckBox(this);
        libre.setBackgroundResource(R.drawable.border_tabla);
        libre.setLayoutParams(new TableRow.LayoutParams((int) (80 * getResources().getDisplayMetrics().density),
                TableRow.LayoutParams.WRAP_CONTENT));
        libre.setOnCheckedChangeListener((boton, marcado) -> {
            peso.setEnabled(!marcado);
            peso.setText(marcado ? "-1" : "");
        });
        fila.addView(nombre);
        fila.addView(series);
        fila.addView(repes);
        fila.addView(peso);
        fila.addView(libre);
        tabla.addView(fila);
    }

    //Es para crear celdas editables para una nueva fila,ancho de la celda y el tipo de teclado y contenido permitido, los tipos estan en InputType
    private EditText entrada(int anchoDp, int tipo) {
        EditText campo = new EditText(this);
        campo.setLayoutParams(new TableRow.LayoutParams((int) (anchoDp * getResources().getDisplayMetrics().density),
                TableRow.LayoutParams.WRAP_CONTENT));
        campo.setBackgroundResource(R.drawable.border_tabla); //fondo
        campo.setPadding(8, 10, 8, 10); //espacio interno
        campo.setSingleLine(true);// una unica linea
        campo.setTextColor(0xFF000000); //texto negro
        campo.setInputType(tipo); //el tipo del texto, numerico,decimal,texto, etc...
        return campo; //devuelve el campo ya preparado para annadirlo a una TableRow
    }

    //CAMPOS DE CLASE
    private static final String FILA_COMPARTIDA = "compartida"; //etiquetas para una fila visual de un ejercicio copiado desde otro dia
    private static final String FILA_GUARDADA = "guardada";//etiqueta para una fila nueva que se inserto correctamente en la base de datos al pulsar “Finalizar dia”.
    private final Modelo modelo = new Modelo();
    private int idRutina;
    private int totalDias;
    private int diaActual = 1;
    private TableLayout tabla; //Referencia a la tabla visual del XML
    private TextView etiquetaDia; //texto de los dias
    private Button copiarDia;
    private Button copiarEjercicio;

}
