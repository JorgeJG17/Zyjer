/*
Clase Calendario.java
Fecha actualiza: 30/09/2026
Autor: Jorge Jimenez Garrido
Descripcion: Clase de la ventana activity_calendario.xml
*/

package app.jjg.zyjer.calendario;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import androidx.annotation.NonNull;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import app.jjg.zyjer.R;
import app.jjg.zyjer.database.CaledarTL;
import app.jjg.zyjer.database.HistorialTL;
import app.jjg.zyjer.database.Modelo;
import app.jjg.zyjer.ventanasentrenar.ventana_entrenar;
import app.jjg.zyjer.ventanashistorial.ventanaHistorial;

public class Calendario extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendario);
        revisarEntrenamientosCaducados();
        calendar();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    /*TODO:
            -PONER UN MENSAJE SEMANAL CON LO DÍAS ENTRENADO Y PROGRAMADOS DE LA SEMANA
            -Mirar si podemos hacer un diseño de los botone mas chulos estilos pixel
     */

    //Metodo para pintar el calendario
    private void calendar(){
        //Conectar con el XML
        MaterialCalendarView calendarView = findViewById(R.id.calendarView);

        calendarView.setSelectedDate(CalendarDay.today()); // Selecciona el día de hoy al abrir

        //Creamos listas para recoger los diferente tipos de dias ya almacenadoe en la bd
        ArrayList<CalendarDay> diasEntrenados = new ArrayList<>();
        ArrayList<CalendarDay> diasFaltados = new ArrayList<>();
        ArrayList<CalendarDay> diasPendientes = new ArrayList<>();
        List<CaledarTL> c =  consultaBD();//Recuperamos los datos de la bd que vienen en una lista

        //Esta son fechas de PRUEBA
        //diasEntrenados.add(CalendarDay.from(2026, 1, 20)); // 20 de Enero
        //diasEntrenados.add(CalendarDay.from(2026, 1, 22)); // 22 de Enero

        if(c!=null && !c.isEmpty()) { //Comprobamos que esa lista no este vacia

            for (CaledarTL datos : c) { //Recorremos la lista para comprobar cada fecha recuperada

                String date = datos.getDate();
                String color;
                //DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate fecha = LocalDate.parse(date);

                //diasEntrenados.add(CalendarDay.from(fecha));

                //comprobamos el estado de cada fecha recuperada, segun el estado de las fechas se guardar en la lista correspondiente
                switch (Integer.parseInt(datos.getestado())) {

                    case FALTADO:
                        //calendarView.addDecorator(new CalendarioDecorator(Color.parseColor("#F44336"), diasEntrenados));
                        diasFaltados.add(CalendarDay.from(fecha));
                        break;
                    case ENTRENADO:
                        //calendarView.addDecorator(new CalendarioDecorator(Color.parseColor("#4CAF50"), diasEntrenados));
                        diasEntrenados.add(CalendarDay.from(fecha));
                        break;
                    case PENDIENTE:
                        diasPendientes.add(CalendarDay.from(fecha));
                        //calendarView.addDecorator(new CalendarioDecorator(Color.parseColor("#E0E0E0"), diasEntrenados));
                        break;
                }

            }
        }

        //Comprobamos que las listas no se encuentren vacias, si hay datos se añaden al calendario con un color
        if(diasFaltados!=null && !diasFaltados.isEmpty()){
            //se crea un obejto de la clase nuestra CalendarioDEcorator que implementa una interfaz y ese objeto se lo enviamos al metodo addDEcorator
            //para que se encarge de pintar los dias almacenados en ese objeto con el fondo/color/imagen alamecnado en ese objeto
            calendarView.addDecorator(new CalendarioDecorator(Calendario.this,R.drawable.circulo_rojo,diasFaltados));
        }

        if(diasEntrenados!=null && !diasEntrenados.isEmpty()){
            calendarView.addDecorator(new CalendarioDecorator(Calendario.this,R.drawable.circulo_verde,diasEntrenados));
        }

        if(diasPendientes!=null && !diasPendientes.isEmpty()){
            calendarView.addDecorator(new CalendarioDecorator(Calendario.this,R.drawable.circulo_gris,diasPendientes));
        }

        //calendarView.addDecorator(new CalendarioDecorator(Color.parseColor("#4CAF50"), diasEntrenados));

        //Si el usuario pulsa algun día del calendario, llamara a este evento que estara esperando.
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                //String.format("%02d/%02d/%04d", date.getDay(), date.getMonth() + 1,date.getYear());
                f_seleccionada = date.getDate(); //Guardamos la fehca seleccionada por el usuario
                //Toast.makeText(MainActivity.this, "Día seleccionado: " + fechaSeleccionada, Toast.LENGTH_SHORT).show();

                //Vemos si esa fecha se encuntra entre algunas de las lista
                //Posible bug con la variable contiene, tenmos que aclarar esto //TODO
               if(diasEntrenados.contains(date) || diasFaltados.contains(date) || diasPendientes.contains(date)){
                   contiene = true;
               }
               else contiene = false;
            }
        });
    }

    //Consulta a la DB para sacar las fechas guardadas
    private List<CaledarTL> consultaBD(){

        Modelo obj = new Modelo();
        Cursor resultados = obj.SeleccionarCalendario(Calendario.this); //Llamamos al SeleccionarCalendario para hacer la consulta en la db

        List<CaledarTL> c = new ArrayList<>(); //Lista para almacenar objetos de tipo CalendarTl

        if (resultados != null && resultados.moveToFirst()) { //Comprobamos que el resultado de la consulta que venga con datos
            do {

                String date = resultados.getString(0); //fecha
                int estado = resultados.getInt(1); //su estado


                //un objeto calendartl para almacenar la fecha y su estado, guardarlo en la lista, cada objeto se usara mas tarde
                CaledarTL calendar_res = new CaledarTL();
                calendar_res.setDate(date);
                calendar_res.setestado(Integer.toString(estado));

                c.add(calendar_res);

            } while (resultados.moveToNext());  //Paamos al siguiente resultado, si ya no hay mas pues se sale del bucle
        }

        // Aseguramos que se cierre el curso una vez usado, si viene vacio, entoces no es necesario
        if (resultados != null) {
            resultados.close();
        }

        return c; //Devolvemos la lista con objetos CaledarTL almacenados (puede devolverla vacia segun como es el metodo actualmente)
    }

    //BT annadir entrenamiento
    public void onEntrenamiento(View view){
        LocalDate fecha = LocalDate.now();

        /*Se comprueba si la fecha seleccionada cuando el usario a pulsado este bt no sea del pasado
          y que tampoco se encuentra ya en la bd
          Si es del presente y no esta en la bd insertara la fecha en el calendario con el estado PENDIENTE = 2
         */
        if(f_seleccionada.isBefore(fecha)){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Fecha no válida");
            builder.setMessage("No puedes añadir entrenamientos en el pasado.\n\nPor favor, selecciona el día de hoy o una fecha futura.");

            builder.setPositiveButton("Entendido", null);
            builder.show();
        } else if (contiene){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Día ocupado");
            builder.setMessage("Esta fecha ya contiene un entrenamiento.");
            builder.setPositiveButton("Entendido", null);
            builder.show();
        }else{

            Modelo obj = new Modelo();
            int resultados = obj.InsertarCalendario(Calendario.this,f_seleccionada);
            recreate(); //recargar la pantalla
        }

    }

    //Muestra un selector semanal para crear automaticamente los entrenamientos restantes del anno actual.
    public void onProgramarDiasFijos(View view) {
        //Almacenamos los dias de la semana
        final String[] nombresDias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        final boolean[] diasSeleccionados = new boolean[7]; //como maximo solo puede seleccionar 7

        new AlertDialog.Builder(this)
                //Primer mensaje
                .setTitle("Programar días fijos")
                .setMessage("Selecciona los días en los que entrenas. " +
                        "Se añadirán desde hoy hasta el final del año " +
                        "sin duplicar fechas.")
                .setPositiveButton("OK", (dialog, which) -> {

                    // Al pulsar OK, mostramos el segundo mensaje

                    //Segundo mensaje,es unicamente para elegir los dias
                    new AlertDialog.Builder(this)
                            .setTitle("Programar días fijos")

                            .setMultiChoiceItems(nombresDias, diasSeleccionados, (dialogo, whichSecundario, checked) -> //se ejecuta cada vez que el usuario marca o desmarca un día.
                                   //whichSecundario: posicion del día pulsado, de 0 a 6. checked: true si se marca; false si se desmarca. OnMultiChoiceClickListener
                                    diasSeleccionados[whichSecundario] = checked) //guardamos en la array los dias, guardando true o false segun el checked
                            .setNegativeButton("Cancelar", null) //Si cancela no hacemos nada
                            //Si pulsa en Programar, los dias seleccionados seran true, el resto false
                            .setPositiveButton("Programar", (dialogo, whichSecundario) -> {
                                boolean hayDiaSeleccionado = false;
                                //Recorremos diasSeleccionados para comprobar que minimo hay un dia marcado
                                for (boolean seleccionado : diasSeleccionados) {
                                    if (seleccionado) {
                                        hayDiaSeleccionado = true; //si hay un dia marcado colocamos true
                                        break;
                                    }
                                }

                                //Si no teniamos un dia seleccionado, la variable estaia el false por lo tanto entraria en el if y no llamamso a la base de datos
                                if (!hayDiaSeleccionado) {
                                    Toast.makeText(this, "Selecciona al menos un día de entrenamiento.", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                //Si se salta el if signifca que uminimo un dia esta seleccioando, así que podemos insertar en la base de datos
                                Modelo obj = new Modelo();
                                int insertados = obj.InsertarEntrenamientosSemanales(this, diasSeleccionados, PENDIENTE);
                                //Por el plantemaineto pensado, solo permitimos programar hasta el final del año, una vez finalizado el año se
                                //puede volver a programar, para no realizar un infinito de insert de golpe
                                Toast.makeText(this, insertados + " entrenamientos programados hasta el 31 de diciembre.", Toast.LENGTH_LONG).show();
                                recreate(); //reacaargar pagina
                            })
                            .show();

                })
                .show();

    }

    //bt entrenar
    public void onEntrenar(View view){
        LocalDate fecha = LocalDate.now();
        /* Se compruba si existe un dia en el entrenamineto seleccionado
           No se permite marcar como entrenado días del pasado o futuro.
           Si es hoy pues se actualiza el estado de la fecha seleccionada a Entrenado = 1
         */
        if(!contiene){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Día sin entrenamiento");
            builder.setMessage("Esta fecha no contiene un entrenamiento.");
            builder.setPositiveButton("Entendido", null);
            builder.show();
        } else if (!f_seleccionada.isEqual(fecha)){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Solo hoy es posible");
            builder.setMessage("No puedes registrar sesiones en fechas pasadas ni futuras.\n\n" +
                    "El pasado ya está escrito y el futuro está por llegar. ¡Concéntrate en el entrenamiento de hoy!");
            builder.setPositiveButton("Entendido", null);
            builder.show();
        }else{

            Modelo obj = new Modelo();
            int resultados = obj.MarcarCalendario(Calendario.this,ENTRENADO,fecha);
            recreate(); //recargar la pantalla
        }

    }

    //bt borrar entrenamiento
    public void onBorrarEnt(View view){
        //Para poder borrar un entrenamineto primero debe de haber un entrenamineto en el dia pulsado, no se borrar
        //entremainetos en el dia de hoy, si no se entrena pues el sistema te lo maracra automaticamente como fallido al terminar el dia
        LocalDate fecha = LocalDate.now();
        if(!contiene){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No contiene un entrenamiento");
            builder.setMessage("No puedes borrar entrenamientos que no existen.\n\n");
            builder.setPositiveButton("Aceptar el reto", null);
            builder.show();
        } else if(f_seleccionada.isBefore(fecha) || f_seleccionada.isEqual(fecha)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Compromiso ZyJer");
            builder.setMessage("No puedes borrar entrenamientos pasados ni el de hoy.\n\n" +
                    "La planificación es una obligación. Si no entrenas, el sistema lo marcará como fallido. ¡No busques excusas!");
            builder.setPositiveButton("Aceptar el reto", null);
            builder.show();
        }else{
            //Si se va borrar se realizar un control de confirmacion
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirmación");
            builder.setMessage("¿Estas seguro de borrar este día de entrenamiento: " + f_seleccionada + "?");
            builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Modelo obj = new Modelo();
                    int resultados = obj.BorrarCalendario(Calendario.this, f_seleccionada);

                    //Si se ha eliminado correctamente devolvera 1
                    if (resultados == 1) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Calendario.this);
                        builder.setTitle("Entrenamiento Borrado");
                        builder.setMessage("El día de entrenamiento se ha borrado correctamente");
                        builder.setPositiveButton("Entendido", null);
                        builder.show();

                        tiempoEspera.removeCallbacks(runnable); // Cancelamos la ejecución anterior (si hay alguna programada)
                        runnable = () -> { //En este lambda no hace falta poner el metodo run porque esta interfaz solo tiene ese metodo entcoes entiende que es ese el que esta poniendo
                            recreate(); //recargar la pantalla
                        };
                        tiempoEspera.postDelayed(runnable, 1500);// Programamos la tarea para que se ejecute dentro de 1500 ms (1.5 segundo)

                    } else {
                        new AlertDialog.Builder(Calendario.this)
                                .setTitle("Error")
                                .setMessage("¡Ups! Algo salió mal. Por favor, infórmaselo al desarrollador. Recuerda que esta es una versión Beta.")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() { //Si dice que no hacemos nada
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        }
    }

    //A este merodo se le llama cada vez que entramos en la ventana del calendario
    //Lo usamos para marcar los entrenaminetos pasados que siguen en pendiente, se ponen directamnet como fallo
    public void revisarEntrenamientosCaducados() {
        Modelo obj = new Modelo();
        LocalDate fecha = LocalDate.now();
        int resultados = obj.MarcarCalendario(Calendario.this,FALTADO,fecha); //Llamamos al SeleccionarCalendario para hacer la consulta en la db

        if (resultados == 1) {


        } else {
            new AlertDialog.Builder(Calendario.this)
                    .setTitle("Error")
                    .setMessage("¡Ups! Algo salió mal. Por favor, infórmaselo al desarrollador. Recuerda que esta es una versión Beta.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
    //CAMPOS DE CLASE
    private static final int FALTADO = 0;
    private static final int ENTRENADO = 1;
    private static final int PENDIENTE = 2;
    private Handler tiempoEspera = new Handler(); //Lo usamos para hacer el tiempo de espera de 1500 segundos y llamara al run de Runnable
    private Runnable runnable; //para utilizar su metodo run así parar ese trozo hasta que yo diga

    private LocalDate f_seleccionada = LocalDate.now(); //por defecto la fecha es actial la de hoy
    private boolean contiene;

}
