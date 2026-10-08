/*
Clase ventanaEntrenamientoTarjetas.java
Fecha actualiza: 07/10/2026
Autor: Jorge Jimenez Garrido
Descripcion: Nueva ventana de entrenamiento basada en tarjetas. Recibe los mismos extras que ventanaRutEjerc: idRutina y dia.
*/
package app.jjg.zyjer.ventanasentrenar;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
//ActivityResultLauncher y ActivityResultContracts.GetContent: abren el selector de imagenes moderno
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
//ViewPager2 usa internamente un adaptador basado en RecyclerView
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2; //permite deslizar horizontalmente entre tarjetas.

//File, FileOutputStream, InputStream y MessageDigest: copian la imagen desde el selector a la carpeta privada de la app y generan su nombre seguro.
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.jjg.zyjer.R;
import app.jjg.zyjer.database.EjerciciosTL;
import app.jjg.zyjer.database.Modelo;
import app.jjg.zyjer.ventanashistorial.ventanaBTejer;

public class ventanaEntrenamientoTarjetas extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entrenamiento_tarjetas);

        idRutina = getIntent().getIntExtra("idRutina", -1); //recuperadmos el idRutina
        dia = getIntent().getIntExtra("dia", -1);  //Recuperamos el dia de la pantalla anterior

        //Comprobamos que ambos datos sean validos, si no llegaron, muestra un mensaje y se cierra
        if (idRutina < 0 || dia < 0) {
            Toast.makeText(this, "Faltan los datos de la rutina", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //Tambien muestra el número de dia y guarda el nombre personalizado del dia
        ((TextView) findViewById(R.id.tv_dia_entrenamiento)).setText("Día: " + dia);
        EditText nombreDia = findViewById(R.id.tv_nombre_dia_entrenamiento);
        nombreDia.setText(ventana_dias.consultarTipDia(this, idRutina, dia)); //Usamos un metodo static de la clase ventana_dias

        nombreDia.addTextChangedListener(new TextWatcher() {  //Le asignamos como escuha un objeto de una clase que hereda de la interface TextWatche a campo Series
            //La clase es anonima por eso es así
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { } //Override obligatorio de la interfaz
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { } //Override obligatorio de la interfaz

            @Override
            public void afterTextChanged(Editable s) { //A este metodo se le llama cada vez que usuario realiza un cambio en nombre del dia
                modelo.ActualizarNomDia(ventanaEntrenamientoTarjetas.this, idRutina, dia,
                        s.toString().trim()); //Llamamos para actualizar en bd
            }
        });

        //
        pager = findViewById(R.id.pager_ejercicios); //id del ViewPager2,es el que permite deslizar entre tarjetas
        indicador = findViewById(R.id.tv_indicador_tarjetas); //ide del texto tipo: Ejercicio 2 de 5
        cargarEjercicios();//Llamamos para cargar los ejercicios

        //Una ves tengamos los ejercicos cargados, creamos el adapter
        //le pasamos la lista de ejercicios y
        //una implementacion de eventos que permite al adaptador avisar a la Activity de que el usuario ha hecho algo.
        adapter = new TarjetasAdapter(ejercicios, new TarjetasAdapter.Eventos() {
            //Cuando el usuario modifica series, repeticiones o peso de una tarjeta, el adaptador llama a alEditar
            @Override public void alEditar(EjercicioTarjeta ejercicio) { programarGuardado(ejercicio); }

            /*Cuando el usuario pulsa “Elegir imagen”:
                1. Se guarda el id del ejercicio actual en ejercicioPendienteImagen.
                2. Se abre el selector de imagenes del sistema.
                3. "image/*" significa: “muéstrame cualquier tipo de imagen”.
                Guardar el id antes de abrir el selector es necesario. El selector se abre y
                el usuario puede tardar varios segundos; cuando vuelve, el metodo que recibe el resultado
                necesita saber a qur ejercicio asignar esa imagen.
             */
            @Override public void alElegirImagen(EjercicioTarjeta ejercicio) {
                ejercicioPendienteImagen = ejercicio.id;
                selectorImagen.launch("image/*");
            }

            //El adaptador detecta la pulsacion, pero la Activity decide eliminar la imagen.
            @Override public void alBorrarImagen(EjercicioTarjeta ejercicio) {
                confirmarEliminarImagen(ejercicio);
            }

            //El adaptador detecta la pulsacion, pero la Activity decide las opciones de orden.
            @Override public void alSolicitarReordenar(EjercicioTarjeta ejercicio) {
                mostrarDialogoReordenar(ejercicio);
            }
        });

        //se conectan viewPager2 y el adaptador:
        pager.setAdapter(adapter);

        //un listener para saber cuándo cambia la tarjeta visible comienza en 0
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) { actualizarIndicador(position); }
        });
        actualizarIndicador(0);

        //estas lineaa es equivalente a usar android:onClick en XML, pero esta vez lo hacemos desde Java
        findViewById(R.id.bt_historial_tarjetas).setOnClickListener(v -> abrirHistorial());
        findViewById(R.id.bt_eliminar_tarjetas).setOnClickListener(v -> confirmarEliminarActual());
        findViewById(R.id.bt_anadir_tarjetas).setOnClickListener(v -> mostrarDialogoAnadir());
        findViewById(R.id.bt_copiar_tarjetas).setOnClickListener(v -> mostrarDialogoCopiar());
        findViewById(R.id.bt_copiar_tarjetas).setEnabled(dia > 1);
    }

    //En este metodo cargamos los datos necesarios
    private void cargarEjercicios() {
        Cursor resultados = modelo.SeleccionarEjercicos(this, idRutina, dia); //Recuperamos los ejercicios para la rutina x y el dia y
        if (resultados != null) {
            while (resultados.moveToNext()) { //Alamecanmos todos los datosd e cada ejercicio en un objecto de tipo EjercicioTarjeta
                EjercicioTarjeta ejercicio = new EjercicioTarjeta();
                ejercicio.id = resultados.getInt(0); //id
                ejercicio.nombre = resultados.getString(1); //nombre
                ejercicio.series = String.valueOf(resultados.getInt(2)); //series
                ejercicio.repes = String.valueOf(resultados.getInt(3)); //repes
                ejercicio.peso = String.valueOf(resultados.getDouble(4)); //peso
                ejercicio.rutaImagen = modelo.SeleccionarRutaImagenEjercicio(this, ejercicio.id); //ruta de la imagen sacada de la bd
                ejercicios.add(ejercicio); //Gurdamos cada objeto en una lista de tipo EjercicioTarjeta
            }
            resultados.close();
        }
    }


    private void programarGuardado(EjercicioTarjeta ejercicio) {
        //guardadoDiferido es un Handler: permite ejecutar algo en el hilo principal, ahora o más tarde.
        guardadoDiferido.removeCallbacksAndMessages(ejercicio); //cancela el guardado pendiente de ese ejercicio concreto, Si el usuario escribe 1, 10, 100, no escribe tres veces en base de datos: solo guarda el resultado final tras 750 ms sin cambios.
        //hora exacta de ejecucion, cuando llegue al tiempo indicado RETARDO_GUARDADO_MS, ejeuctara guardarEjercicio
        guardadoDiferido.postAtTime(() -> guardarEjercicio(ejercicio), ejercicio,
                android.os.SystemClock.uptimeMillis() + RETARDO_GUARDADO_MS);
    }

    private void guardarEjercicio(EjercicioTarjeta ejercicio) {
        //Evitamos guardar si alguno de los tres campos esta vacio
        if (ejercicio.series.isEmpty() || ejercicio.repes.isEmpty() || ejercicio.peso.isEmpty()) return;
        //guardamos los datos en un objeto EjerciciosTL
        EjerciciosTL datos = new EjerciciosTL();
        datos.setId(String.valueOf(ejercicio.id));
        datos.setIdRutina(String.valueOf(idRutina));
        datos.setSeries(ejercicio.series);
        datos.setRepes(ejercicio.repes);
        datos.setPeso(ejercicio.peso);

        //LLamamos a la base datos, si algo sale mal sacar ese mensaje
        if (modelo.ActualizarDatosTabla(this, datos, false) != 1) {
            Toast.makeText(this, "No se pudieron guardar los cambios", Toast.LENGTH_SHORT).show();
        }
    }

    //Metodo que se llama al pulsar el boton de ordenar los ejercicios
    private void mostrarDialogoReordenar(EjercicioTarjeta ejercicio) {
        int posicion = ejercicios.indexOf(ejercicio); //cogemos su posicion actual

        //Si no hay mas ejercios, no tiene sentido ordenar
        if (posicion < 0 || ejercicios.size() < 2) {
            Toast.makeText(this, "No hay otros ejercicios para reordenar", Toast.LENGTH_SHORT).show();
            return;
        }

        //Asignamops en una array las opcines y los destinos en su orden para que quede claro que pasara
        //Si elegimos la opcion [0] del array opcines, enviaremos el destino [0] del array destinos el metodo moverEjercicioAPosicion
        List<String> opciones = new ArrayList<>();
        List<Integer> destinos = new ArrayList<>();
        if (posicion > 0) {
            opciones.add("Mover a la primera posición");
            destinos.add(0);
            opciones.add("Subir una posición");
            destinos.add(posicion - 1);
        }
        if (posicion < ejercicios.size() - 1) {
            opciones.add("Bajar una posición");
            destinos.add(posicion + 1);
            opciones.add("Mover a la última posición");
            destinos.add(ejercicios.size() - 1);
        }

        //Lanzamos el alert con las opciones, si pulsa una llamara a moverEjercicioAPosicion
        new AlertDialog.Builder(this)
                .setTitle("Reordenar " + ejercicio.nombre)
                .setItems(opciones.toArray(new String[0]),
                        (dialog, which) -> moverEjercicioAPosicion(ejercicio, destinos.get(which)))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    //Metodo para raelizar la accion pulsada por el usuario en ordenar un ejercicio
    private void moverEjercicioAPosicion(EjercicioTarjeta ejercicio, int destino) {
        int posicion = ejercicios.indexOf(ejercicio);

        if (posicion < 0 || destino < 0 || destino >= ejercicios.size() || posicion == destino) return;

        ejercicios.remove(posicion); //borramos el ejercicio de la posion antigua
        ejercicios.add(destino, ejercicio); //lo asignamos en la nueva posiion

        //actualiza el orden de todos los ejercicios, al mover uno, todos cambian
        for (int i = 0; i < ejercicios.size(); i++) {
            //llamada a la base de datos
            if (modelo.ActualizarOrdenTabla(this, i + 1, ejercicios.get(i).id, idRutina, dia) != 1) {
                Toast.makeText(this, "No se pudo guardar el orden", Toast.LENGTH_SHORT).show();
                cargarDeNuevo(); //cargamos de nuevo
                return;
            }
        }
        //Le decimos al adaptador que las tarjetas han cambiado de oreden para que las vuelva a pintar
        adapter.notifyDataSetChanged();
        //Muestra la tarjeta movida; el deslizamiento del usuario sigue reservado para navegar.
        pager.setCurrentItem(destino, true);
    }

    //metodo para eliminar un ejercicio concreto del bt eliminar
    private void confirmarEliminarActual() {
        //las posisiones siempre empiezan en 0
        int posicion = pager.getCurrentItem(); //Obtiene la tarjeta que el usuario está viendo ahora mismo
        //si no hay ejercicios, no hay que eliminarnada
        if (ejercicios.isEmpty() || posicion >= ejercicios.size()) return;

        EjercicioTarjeta ejercicio = ejercicios.get(posicion); //obtenemos el objeto correspondiente de esa posicion

        //Sacamos un alert para conformar el borrado o cancelar
        new AlertDialog.Builder(this).setTitle("Eliminar ejercicio")
                .setMessage("Se quitará \"" + ejercicio.nombre + "\" del día " + dia + ". Si está en otro día, se conservarán sus datos e imagen.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    //llamamos a la bd
                    if (modelo.EliminarEjercicio(this, ejercicio.id, dia) == 1) {
                        ejercicios.remove(posicion);//borramos esa posicion de la lista
                        adapter.notifyDataSetChanged(); //Le decimos al adaptador que las tarjetas han cambiado para que las vuelva a pintar

                        //evita que el indicador quede en una posicion inexistente
                        //por si boramos la ultima carta y esra por eejmplo la numero 6, nos pasara
                        //a la numero 5, si borramos la 3 y habia 6 pues nos dejara en la 3
                        actualizarIndicador(Math.min(posicion, Math.max(0, ejercicios.size() - 1)));
                    } else {
                        Toast.makeText(this, "No se pudo eliminar el ejercicio", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Cancelar", null).show();
    }

    //Bt historial
    private void abrirHistorial() {
        Intent intent = new Intent(this, ventanaBTejer.class);
        intent.putExtra("idRutina", idRutina);
        intent.putExtra("dia", dia);
        startActivity(intent);
    }

    //Bt annadir
    private void mostrarDialogoAnadir() {
        //Mostramos una ventana emergente para rellenar los datos del nuevo ejercicio
        View vista = LayoutInflater.from(this).inflate(R.layout.activity_emergente, null);
        EditText nombre = vista.findViewById(R.id.etNombre);
        EditText series = vista.findViewById(R.id.etSeries);
        EditText repes = vista.findViewById(R.id.etRepeticiones);
        EditText peso = vista.findViewById(R.id.etPeso);
        new AlertDialog.Builder(this).setTitle("Añadir ejercicio").setView(vista)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    EjerciciosTL nuevo = new EjerciciosTL();
                    nuevo.setIdRutina(String.valueOf(idRutina));
                    nuevo.setDia(String.valueOf(dia));
                    nuevo.setNombre(nombre.getText().toString().trim());
                    nuevo.setSeries(series.getText().toString().trim());
                    nuevo.setRepes(repes.getText().toString().trim());
                    nuevo.setPeso(peso.getText().toString().trim());
                    //Comprobamos que se ha insertado correctamente en la bd, si no, mostramos un mensaje
                    if (modelo.InsertaEjercicios(this, nuevo) == 1) cargarDeNuevo();
                    else Toast.makeText(this, "Revisa los datos del ejercicio", Toast.LENGTH_SHORT).show();
                }).setNegativeButton("Cancelar", null).show();
    }

    private void mostrarDialogoCopiar() {
        List<Integer> ids = new ArrayList<>();
        List<String> opciones = new ArrayList<>();
        try (Cursor anteriores = modelo.SeleccionarEjerciciosAnteriores(this, idRutina, dia)) {
            while (anteriores.moveToNext()) {
                ids.add(anteriores.getInt(0));
                opciones.add("Día " + anteriores.getInt(2) + " · " + anteriores.getString(1));
            }
        }
        //Si ids esta vacio significa que SeleccionarEjerciciosAnteriores no ha traido nada, eso puede ser porque no existan,
        //o esten ya ocupados en otros dos dias y no se puede poner en un tercero
        if (ids.isEmpty()) {
            Toast.makeText(this, "No hay ejercicios anteriores disponibles", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this).setTitle("Añadir ejercicio anterior")
                .setItems(opciones.toArray(new String[0]), (dialogo, indice) -> {
                    int resultado = modelo.CompartirEjercicio(this, idRutina, ids.get(indice), dia);
                    if (resultado == 1) cargarDeNuevo();
                    else Toast.makeText(this, "No se pudo añadir el ejercicio", Toast.LENGTH_SHORT).show();
                }).setNegativeButton("Cancelar", null).show();
    }

    //este metodo se llama cuando el selector del sistema devuelve una imagen
    private void guardarImagenSeleccionada(Uri uri) {
        //Uri es una referencia al archivo elegido, No es el archivo ni una ruta normal
        //Si no hay ejercicio pendiente, no se sabe a que tarjeta debe asignarse la imagen.
        if (uri == null || ejercicioPendienteImagen < 0) return;
        try {
            //Copia la imagen desde la ubicacion temporal por el sistema a la carpeta privada de la app.

            ArchivoImportado archivo = copiarImagenAlAlmacenamientoPrivado(uri);
            /*Devuelve un ArchivoImportado, que contiene:
            archivo       → fichero final
            creadoAhora   → true si se creo en esta operacion
            */

            //Aqui se guarda en SQLite unicamente la ruta absoluta
            if (modelo.GuardarRutaImagenEjercicio(this, ejercicioPendienteImagen,
                    archivo.archivo.getAbsolutePath()) == 1) {

                /*
                La base de datos ya tiene la nueva ruta, pero la lista Java todavia
                tenia la ruta anterior o null. Este bucle actualiza
                el objeto que ya esta en memoria.
                 */
                for (EjercicioTarjeta ejercicio : ejercicios) {
                    if (ejercicio.id == ejercicioPendienteImagen) {
                        ejercicio.rutaImagen = archivo.archivo.getAbsolutePath();
                    }
                }

                adapter.notifyDataSetChanged();//volvemos a pintar la tarjeta para que muestre la imagen gaurdada
            } else { //Si algo falla debemos borrar el archvio creado
                if (archivo.creadoAhora) archivo.archivo.delete();
                Toast.makeText(this, "Esa imagen ya está asignada a otro ejercicio", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "La imagen no es válida o supera 3 MB", Toast.LENGTH_LONG).show();
        } finally {
            ejercicioPendienteImagen = -1; //Deja el estado limpio para la proxima seleccion de imagen
        }
    }

    //Pide confirmacion y elimina tanto la relacion en SQLite como el archivo privado de la imagen.
    private void confirmarEliminarImagen(EjercicioTarjeta ejercicio) {
        if (ejercicio.rutaImagen == null) {
            Toast.makeText(this, "Este ejercicio no tiene imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Borrar imagen")
                .setMessage("Se eliminará la imagen asociada a \"" + ejercicio.nombre + "\".")
                .setPositiveButton("Borrar", (dialog, which) -> {
                    if (modelo.EliminarImagenEjercicio(this, ejercicio.id) == 1) {
                        ejercicio.rutaImagen = null;
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Imagen eliminada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No se pudo eliminar la imagen", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /** Copia la imagen a filesDir; SQLite solo conservará la ruta resultante. */
    private ArchivoImportado copiarImagenAlAlmacenamientoPrivado(Uri uri) throws Exception {
        File carpeta = new File(getFilesDir(), "imagenes_ejercicios"); //Recuperamos la carpeta privada de la app
        if (!carpeta.exists() && !carpeta.mkdirs()) throw new IllegalStateException(); //Si la carpeta no existe, intenta crearla


        /*No se copia directamente al fichero definitivo. Primero se usa un archivo temporal.
          Con esto evitamos algun problema futuro si elproceso falla a mitad de copia
         */
        File temporal = File.createTempFile("ejercicio_", ".tmp", carpeta);

        //convierte el contenido completo de un archivo en una clave unica
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        int total = 0;

        //Leemos la imagen original seleccionada por el usuario, y escribi,os una copia en el archivo temporal de la app.
        try (InputStream entrada = getContentResolver().openInputStream(uri);//una uri es una direccion controlada por Android. Para leer los bytes de esa imagen, se usa ContentResolver
             FileOutputStream salida = new FileOutputStream(temporal)) { //InputStream es un flujo de lectura. No carga la foto completa en memoria; permite leerla poco a poco. new FileOutputStream(temporal) Abre el archivo temporal para escribir dentro de el
            if (entrada == null) throw new IllegalArgumentException(); //comprobamos que la imagen no este a null, es decir, su uri no haya llegado a null
            byte[] buffer = new byte[8192]; //creamos un bloque de memoria de 8KB
            int leidos;

            //La imagen no se carga entera en memoria. Se trabaja por pequennos trozos
            //Intentamos leer hasya 8KB de la imagen, si leidos es -1 entoces hemos llegado al final de la imagen
            while ((leidos = entrada.read(buffer)) != -1) {
                total += leidos; //en total guardamos lo que llevamos de la imagen

                //Tenemos un maximo de tamnno de imagen
                if (total > TAMANO_MAXIMO_IMAGEN) throw new IllegalArgumentException();

                //Annade el bloque actual a la calculadora SHA-256, Al finalizar todos los bloques, digest habra calculado una huella unica del contenido entero de la imagen
                digest.update(buffer, 0, leidos);

                //escribimos los bytes leidos en el archivo temporal, 0 es empezar desde la posisicion 0
                salida.write(buffer, 0, leidos);
            }
        } catch (Exception e) {
            temporal.delete(); //si algo falla borramos el archivo temporal
            throw e;
        }
        StringBuilder nombre = new StringBuilder();

        //El hash final lo transdormamos en texto hexadecimal, esto es lo que usamos como nombre unico del archivo
        //Locale.US aquí es importante porque asegura usar siempre dígitos y letras estándar en el hash.
        for (byte b : digest.digest()) nombre.append(String.format(Locale.US, "%02x", b));

        //creamos el archivo final
        File destino = new File(carpeta, nombre + ".img");


        /*Si ya existe un archivo con ese hash, significa que ya se copio antes exactamente la misma imagen.
        No se duplica.*/
        if (destino.exists()) {
            temporal.delete();
            return new ArchivoImportado(destino, false);
        }
        //Si no existia, se renombra el temporal al archivo final.
        if (!temporal.renameTo(destino)) {
            temporal.delete();
            throw new IllegalStateException();
        }
        return new ArchivoImportado(destino, true);
    }

    //recreate() destruye y vuelve a crear toda la Activity, no lo veo necesario, es mejor actualizar solo lo necesario
    //para eso usamos cargarDeNuevo()
    private void cargarDeNuevo() {
        ejercicios.clear();
        cargarEjercicios();
        adapter.notifyDataSetChanged();
        pager.setCurrentItem(0, false);
        actualizarIndicador(0);
    }

    private void actualizarIndicador(int posicion) {
        indicador.setText(ejercicios.isEmpty() ? "No hay ejercicios" : "Ejercicio " + (posicion + 1) + " de " + ejercicios.size());
    }

    //Metodo para calcular la repe max
    private static String calcularRM(String pesoTexto, String repesTexto) {
        try {
            double peso = Double.parseDouble(pesoTexto);
            int repes = Integer.parseInt(repesTexto);
            if (peso < 0 || repes <= 0) return "RM: —"; //muestra RM: — si no hay peso, las repeticiones son invalidas o el peso es negativo.
            //Locale representa la configuracion regional del movil: idioma, formato numerico, separador decimal
            return String.format(Locale.getDefault(), "RM: %.1f kg", peso / (1.0278 - (0.0278 * repes))); //Formula de la repr max siendo p peso y r repeticiones
        } catch (NumberFormatException e) { return "RM: —"; }
    }

    //Se define una clase interna simple que representa los datos de una tarjeta
    //No adaptamos EjerciciosTl para usarlo en vez de EjercioTarjeta porque rutaImagen no pertence a tlejercicios, pertenece a tlimagenes
    //EjerciciosTL realmente representa tlejercicios, EjercicioTrajeta representa el estado de una tarjeta visible
    private static class EjercicioTarjeta {
        int id;
        String nombre;
        String series;
        String repes;
        String peso;
        String rutaImagen;
    }

    //esta clase interna esta hecha para almacenar en un objeto ArchivoImportado  la ruta del archivo y si se ha creado ahora readoAhora
    private static class ArchivoImportado {
        final File archivo;
        final boolean creadoAhora;
        ArchivoImportado(File archivo, boolean creadoAhora) { this.archivo = archivo; this.creadoAhora = creadoAhora; }
    }

    //La clase para el adaptador permite a RecyclerView y ViewPager2 crear y reutilizar tarjeta
    //No se crea una vista distinta permanente por cada ejercicio. Android reutiliza las tarjetas visuales que ya no se ven
    //Usuario ve tarjeta 1, desliza a tarjeta 2, la vista que mostraba tarjeta 1 puede reutilizarse para tarjeta 3
    //RecyclerView decida segun sus necesidades cuntas tarjetas crear pero lo seguro es que si hay 50 ejercios, no hay 50 tarjetas,
    //imagina que crea 5 tarjetas y esas 5 las usa para ir mostrando los 50 ejercicos, es un ejemplo absurdo para explicarlo
    private static class TarjetasAdapter extends RecyclerView.Adapter<TarjetasAdapter.Holder> {
        //Detecta eventos pero no decide que hacer en ellos, por eso usmaos una interface
        interface Eventos { void alEditar(EjercicioTarjeta ejercicio); void alElegirImagen(EjercicioTarjeta ejercicio); void alBorrarImagen(EjercicioTarjeta ejercicio); void alSolicitarReordenar(EjercicioTarjeta ejercicio); }

        private final List<EjercicioTarjeta> datos;
        private final Eventos eventos;

        //Contructort
        TarjetasAdapter(List<EjercicioTarjeta> datos, Eventos eventos) { this.datos = datos; this.eventos = eventos; }

        //Crea el xml de una tarjeta y un hOLDER
        @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entrenamiento_tarjeta, parent, false));
            //Aqui false crea la vista, pero no la añadas todavia al padre; RecyclerView la añadira cuando corresponda
        }

        //recibe un Holder reutilizable y le asigna el ejercicio correspondiente.
        @Override public void onBindViewHolder(@NonNull Holder h, int position) {
            EjercicioTarjeta ejercicio = datos.get(position); //posisicion del ejercicio
            h.desconectarWatchers(); //quitamos los watcher anteriores, para no dejar los lsiyener del ejercicio anterior que usaba esta tarjeta

            //Escribimos los textos nuevos
            h.nombre.setText(ejercicio.nombre);
            h.series.setText(ejercicio.series);
            h.repes.setText(ejercicio.repes);
            h.peso.setText(ejercicio.peso);
            boolean usaPeso = !ejercicio.peso.equals("-1") && !ejercicio.peso.equals("-1.0");
            h.peso.setEnabled(usaPeso);
            h.rm.setText(calcularRM(ejercicio.peso, ejercicio.repes));

            //Si no hay ruta o el archivo ya no existe, pinta un icono generico de galeria
            //Si existe, lee el archivo y lo transforma en un Bitmap que ImageView puede mostrar
            if (ejercicio.rutaImagen == null || !new File(ejercicio.rutaImagen).exists()) {
                h.imagen.setImageResource(android.R.drawable.ic_menu_gallery);
            } else {
                h.imagen.setImageBitmap(BitmapFactory.decodeFile(ejercicio.rutaImagen));
            }

            //Vuelve a crear los listeners para este ejercicio concreto
            h.conectarWatchers(ejercicio);

            //botones de cada tarjeta, al pintar una tarjeta concreta asignamos el comportamiento de sus botones
            h.imagenBoton.setOnClickListener(v -> eventos.alElegirImagen(ejercicio));
            h.borrarImagen.setVisibility(ejercicio.rutaImagen == null ? View.GONE : View.VISIBLE);
            h.borrarImagen.setOnClickListener(v -> eventos.alBorrarImagen(ejercicio));
            h.reordenar.setOnClickListener(v -> eventos.alSolicitarReordenar(ejercicio));
        }

        //Nos dice cuantas tarjetas hay, lo llama internamente RecyclerView/ViewPager2, asi
        //sabe que la primera posición válida es 0, que la última es X;
        //que debe mostrar Ejercicio Y de X, que no puede deslizar más allá de la posición x, cuantas posiciones puede pedir al adaptador.
        @Override public int getItemCount() { return datos.size(); }

        //Un Holder guarda referencias a las vistas de una tarjeta, asi no se llama repetidamente a findViewById cada vez que se reutiliza la tarjeta.
        class Holder extends RecyclerView.ViewHolder {
            final TextView nombre, rm; final EditText series, repes, peso; final ImageView imagen; final Button imagenBoton, borrarImagen, reordenar;
            TextWatcher watcherSeries, watcherRepes, watcherPeso;
            Holder(View v) { super(v);
                nombre=v.findViewById(R.id.tv_nombre_ejercicio);
                rm=v.findViewById(R.id.tv_rm_tarjeta);
                series=v.findViewById(R.id.et_series_tarjeta);
                repes=v.findViewById(R.id.et_repes_tarjeta);
                peso=v.findViewById(R.id.et_peso_tarjeta);
                imagen=v.findViewById(R.id.iv_ejercicio);
                imagenBoton=v.findViewById(R.id.bt_imagen_ejercicio);
                borrarImagen=v.findViewById(R.id.bt_borrar_imagen_ejercicio);
                reordenar=v.findViewById(R.id.bt_reordenar_ejercicio); }

            //Este metodo fabrica un listener para cada campo editable. Actualiza primero el objeto que vive en memoria
            //despues actualiza el rm visual y avisa a la activity para guardar
            TextWatcher crearWatcher(EjercicioTarjeta ejercicio, EditText campo) {
                return new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
                    @Override public void onTextChanged(CharSequence s, int st, int b, int c) { }
                    @Override public void afterTextChanged(Editable s) {
                        if (campo == series) ejercicio.series = s.toString().trim();
                        else if (campo == repes) ejercicio.repes = s.toString().trim();
                        else ejercicio.peso = s.toString().trim();
                        rm.setText(calcularRM(ejercicio.peso, ejercicio.repes));
                        eventos.alEditar(ejercicio);
                    }
                };
            }

            //Elimina listeners antiguos antes de reutilizar una tarjeta
            void desconectarWatchers() {
                if (watcherSeries != null) series.removeTextChangedListener(watcherSeries);
                if (watcherRepes != null) repes.removeTextChangedListener(watcherRepes);
                if (watcherPeso != null) peso.removeTextChangedListener(watcherPeso);
            }

            //Crea y annade los listeners nuevos
            void conectarWatchers(EjercicioTarjeta ejercicio) {
                watcherSeries = crearWatcher(ejercicio, series);
                watcherRepes = crearWatcher(ejercicio, repes);
                watcherPeso = crearWatcher(ejercicio, peso);
                series.addTextChangedListener(watcherSeries);
                repes.addTextChangedListener(watcherRepes);
                peso.addTextChangedListener(watcherPeso);
            }
        }
    }

    //CAMPOS DE CLASE
    private static final long RETARDO_GUARDADO_MS = 750L;
    private static final int TAMANO_MAXIMO_IMAGEN = 3 * 1024 * 1024;

    private final Modelo modelo = new Modelo(); //acceso a la bsd
    private final Handler guardadoDiferido = new Handler(Looper.getMainLooper()); //para realizar la espera antes de guardar
    private final List<EjercicioTarjeta> ejercicios = new ArrayList<>(); //lista en memoria de tarjetas completas
    private int idRutina;
    private int dia;
    private int ejercicioPendienteImagen = -1; //sabe a que ejercicio asignar el resultado del selector de imagen
    //vistas/controladores principales.
    private TarjetasAdapter adapter;
    private ViewPager2 pager;
    private TextView indicador;

    //lanzador del selector de fotos, cuando seleccione la foto, llamara a guardarImagenSeleccionada
    private final ActivityResultLauncher<String> selectorImagen = registerForActivityResult(
            new ActivityResultContracts.GetContent(), this::guardarImagenSeleccionada);
}


//Flujo de clase resumen:
//SQLite > Activity: ventanaEntrenamientoTarjetas > TarjetasAdapter > ViewPager2 + tarjetas XML > Usuario

/*
Al abrir una pantalla
1. Android crea la Activity.
2. Se ejecuta onCreate().
3. Se carga activity_entrenamiento_tarjetas.xml.
4. Se reciben idRutina y dia.
5. Se consulta SQLite.
6. Se llena List<EjercicioTarjeta>.
7. Se crea TarjetasAdapter.
8. Se conecta al ViewPager2.
9. ViewPager2 pide al adaptador las tarjetas que debe mostrar.
10. Se pinta la primera tarjeta
*/

/*
Al deslizar una tarjeta
Usuario desliza a la derecha > ViewPager2 cambia de posicion > Puede reutilizar una tarjeta visual anterior >
> TarjetasAdapter.onBindViewHolder(...) > Pinta nombre, series, repes, peso, RM e imagen > onPageSelected(position)
> Se actualiza “Ejercicio X de Y”
*/

/*
Al modificar peso, series o repeticiones
Usuario escribe en EditText > TextWatcher detecta el cambio > Actualiza EjercicioTarjeta en memoria >
> Recalcula RM inmediatamente > Avisa a la Activity: alEditar(...) > Activity programa guardado en 750 ms
> Si el usuario sigue escribiendo, se cancela el anterior > Cuando para 750 ms, se guarda en SQLite
*/

/*
Al elegir una imagen
Usuario pulsa “Elegir imagen” > Adaptador avisa a Activity > Activity guarda el id del ejercicio >
> Se abre selector del sistema > Usuario selecciona imagen > guardarImagenSeleccionada(uri) >
> Se copia la imagen a filesDir/imagenes_ejercicios > SQLite guarda solo la ruta > Se refresca la tarjeta
*/

/*
Al mover un ejercicio
Usuario pulsa “Mover ←” o “Mover →” > Adaptador avisa a Activity > La lista ejercicios cambia de posicion >
> Se guarda el orden completo en SQLite > Se repintan las tarjetas > Se muestra la tarjeta movida
*/

/*
Al eliminar un ejercicio
Usuario pulsa “Eliminar” > Activity obtiene pager.getCurrentItem() > Muestra confirmacion > Usuario confirma >
> Modelo elimina registro, relación de imagen y archivo > Se elimina de la lista Java > Adaptador repinta tarjetas
*/

