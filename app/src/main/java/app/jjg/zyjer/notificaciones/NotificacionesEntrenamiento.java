/*
Clase NotificacionesEntrenamiento.java
Fecha actualiza: 30/09/2026
Autor: Jorge Jimenez Garrido
Descripcion: Centraliza el canal, la programacion diaria y el contenido de los recordatorios de entrenamiento.
*/
package app.jjg.zyjer.notificaciones;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager; //permite pedir a android que ejecute una accion en un momento futuro
import android.app.NotificationChannel; //crean y muestran la notificacion.
import android.app.NotificationManager; //crean y muestran la notificacion.
import android.app.PendingIntent; // para que una accion pueda ser ejecutada por Android aunque la app no este abierta.
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences; //almacenamiento pequenno local para recordar que ya se aviso hoy
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat; //crean y muestran la notificacion.
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Random;

import app.jjg.zyjer.MainActivity;
import app.jjg.zyjer.R;
import app.jjg.zyjer.database.Modelo;

//Es una clase de utilidades: no representa una notificacion concreta, sino que reune metodos estaticos para gestionar todas.
//No queremos que hereden de ella
public final class NotificacionesEntrenamiento {

    //Ponemos privado el contructor para evitar que se pueda llamar
    //No necesitamos crear objectos porque todos los metodos son static
    private NotificacionesEntrenamiento() {

    }

    //Prepara los recordatorios y solicita el permiso que exige Android 13 o superior.
    public static void preparar(Activity activity) {
        crearCanal(activity); //creamos el canal de Android
        programarRecordatorioDiario(activity); //registra la alarma diaria
        programarRecordatorioFinalDia(activity); //registra el aviso de cierre del dia

        //Comprobamos el permiso de las notifiaciones en Android 13 superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU //Siempre compruebo la version por buena practica
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    CODIGO_PERMISO_NOTIFICACIONES);
            return;
        }
        comprobarYNotificarHoy(activity); //Si el permiso ya existe llamamos
    }

    //Crea o actualiza el canal visible en los ajustes de notificaciones del sistema. El canal aparece despues en los ajustes
    // del telefono, donde el usuario puede silenciarlo.
    private static void crearCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //Siempre compruebo la version por buena practica
            //identificador inetrno,nombre visible para el usuario, importancia normal
            NotificationChannel canal = new NotificationChannel(CANAL_ENTRENAMIENTO,
                    "Recordatorios de entrenamiento", NotificationManager.IMPORTANCE_DEFAULT);
            canal.setDescription("Avisos para los días que tienes un entrenamiento programado.");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(canal);
        }
    }

    //Programa una comprobacion diaria a las 09:00; el sistema puede ajustarla ligeramente para ahorrar bateria.
    public static void programarRecordatorioDiario(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, RecordatorioEntrenamientoReceiver.class); //se confiugura la alarma

        //ACCION_RECORDATORIO se annade al Intent de la alarma
        //Cuando el receiver recibe el evento, puede saber que no es un reinicio ni una actualizacion, sino la alarma diaria de entrenamiento.
        intent.setAction(ACCION_RECORDATORIO); //indica que, cuando llegue la hora, Android debe ejecutar la clase
        //Un PendingIntent es una “orden preparada” para que Android la ejecute mas tarde.
        PendingIntent pendiente = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE); //convierte la accion en una orden que Android opuede ejeuctar en un futuro '|'  “aplicar ambas opciones” une dos opciones en un unico numero que Android puede recibir
        //FLAG_UPDATE_CURRENT Si ya existia un PendingIntent igual, Android reutiliza el existente y actualiza sus datos.
        //FLAG_IMMUTABLE Indica que, una vez creado, nadie puede modificar el contenido del PendingIntent
        /*
            Ejemplo simple para entenderlo mejor:
                FLAG_UPDATE_CURRENT = 0010
                FLAG_IMMUTABLE      = 1000
                -------------------------
                resultado con |     = 1010
         */


        //Construimos la hora del aviso
        Calendar proximoAviso = Calendar.getInstance();
        proximoAviso.set(Calendar.HOUR_OF_DAY, 9);
        proximoAviso.set(Calendar.MINUTE, 0);
        proximoAviso.set(Calendar.SECOND, 0);
        proximoAviso.set(Calendar.MILLISECOND, 0);
        //Si son mas de las nueve se programa para mannana
        if (proximoAviso.getTimeInMillis() <= System.currentTimeMillis()) {
            proximoAviso.add(Calendar.DAY_OF_YEAR, 1);
        }

        //Creamos una alarma remetida cada 24 horas, android puede modificar esto si es necesario, por ejemplo por bateria
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, proximoAviso.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendiente);
    }

    //Programa un segundo aviso diario a las 21:00 para recordar registrar el entrenamiento realizado.
    public static void programarRecordatorioFinalDia(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, RecordatorioEntrenamientoReceiver.class);
        intent.setAction(ACCION_RECORDATORIO_FINAL_DIA);
        PendingIntent pendiente = PendingIntent.getBroadcast(context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar proximoAviso = Calendar.getInstance();
        proximoAviso.set(Calendar.HOUR_OF_DAY, 21);
        proximoAviso.set(Calendar.MINUTE, 0);
        proximoAviso.set(Calendar.SECOND, 0);
        proximoAviso.set(Calendar.MILLISECOND, 0);
        if (proximoAviso.getTimeInMillis() <= System.currentTimeMillis()) {
            proximoAviso.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, proximoAviso.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendiente);
    }

    //Consulta tlcalendar para hoy y evita avisar dos veces aunque coincidan el arranque y la alarma diaria.
    public static void comprobarYNotificarHoy(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU //Comprobamos version
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) //y comprobamso el permiso
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String hoy = LocalDate.now().toString();
        //consultamos preferncias
        SharedPreferences preferencias = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE); //Esto abre las preferencias llamadas recordatorios_entrenamiento
        //MODE_PRIVATE significa que solo ZyJer puede leer y escribir ese almacenamiento.
        //Luego preferencias.getString(CLAVE_ULTIMO_AVISO, null) Busca el valor con la clave guardada en la variable CLAVE_ULTIMO_AVISO en este caso ultimo_aviso
        if (hoy.equals(preferencias.getString(CLAVE_ULTIMO_AVISO, null))) { //se revisa si ya se aviso hoy
            return;
        }

        //Consulta a la base de datos para saber si hay entrenamiento hoy
        Modelo modelo = new Modelo();
        if (!modelo.HayEntrenamientoEnFecha(context, LocalDate.now())) {
            return;
        }

        //Si existe entrenamiento y no se ha avisado aún, se construye el aviso motivador.
        String mensaje = MENSAJES[new Random().nextInt(MENSAJES.length)];
        mostrarNotificacion(context, mensaje);

        //Guardamos que ya se aviso hoy en el archivo peque de PREFERENCIAS "recordatorios_entrenamiento" el SharedPreferences
        preferencias.edit().putString(CLAVE_ULTIMO_AVISO, hoy).apply();
    }

    //A las 21:00 recuerda registrar el resultado solo si el entrenamiento de hoy continua pendiente.
    public static void comprobarYNotificarFinalDia(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String hoy = LocalDate.now().toString();
        SharedPreferences preferencias = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
        if (hoy.equals(preferencias.getString(CLAVE_ULTIMO_AVISO_FINAL_DIA, null))) {
            return;
        }

        Modelo modelo = new Modelo();
        if (!modelo.HayEntrenamientoPendienteEnFecha(context, LocalDate.now())) {
            return;
        }

        mostrarNotificacion(context, MENSAJE_FINAL_DIA);
        preferencias.edit().putString(CLAVE_ULTIMO_AVISO_FINAL_DIA, hoy).apply();
    }

    //Construye el aviso común que se muestra al tocar cualquiera de los dos recordatorios diarios.
    private static void mostrarNotificacion(Context context, String mensaje) {
        Intent abrirApp = new Intent(context, MainActivity.class);
        abrirApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //FLAG_ACTIVITY_CLEAR_TOP Si MainActivity ya estaba abierta en el historial de pantallas, elimina las pantallas que hubiera encima de ella.
        //FLAG_ACTIVITY_SINGLE_TOP Si la actividad ya está arriba del top, no crea otra copia. Reutiliza la existente.

        PendingIntent accionAbrir = PendingIntent.getActivity(context, 0, abrirApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE); ///convierte la accion en una orden que Android opuede ejeuctar en un futuro '|'  “aplicar ambas opciones”.

        //Construimos la notificacion
        NotificationCompat.Builder notificacion = new NotificationCompat.Builder(context, CANAL_ENTRENAMIENTO)
                .setSmallIcon(R.mipmap.ic_launcher) //logo_app
                .setContentTitle("ZyJer") //nombre
                .setContentText(mensaje)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje)) //permite leer el texto completo al expandirla
                .setContentIntent(accionAbrir) //acción al pulsarla 'abrimos la app'
                .setAutoCancel(true) //la elimina al tocarla.
                .setPriority(NotificationCompat.PRIORITY_DEFAULT); //prioridad compatible con Android antiguos

        //Android muestra la notificacion
        NotificationManagerCompat.from(context).notify(ID_NOTIFICACION, notificacion.build());
    }

    //CAMPOS DE CLASE
    public static final int CODIGO_PERMISO_NOTIFICACIONES = 2001; //id de la peticio de permiso de android 13+
    private static final String CANAL_ENTRENAMIENTO = "recordatorios_entrenamiento"; //id interno del canal de notificaciones
    static final String ACCION_RECORDATORIO = "app.jjg.zyjer.RECORDATORIO_ENTRENAMIENTO"; //Es el nombre unico del evento propio de ZyJer.
    static final String ACCION_RECORDATORIO_FINAL_DIA = "app.jjg.zyjer.RECORDATORIO_FINAL_DIA"; //Evento propio del aviso nocturno.
    //El prefijo app.jjg.zyjer ayuda a que el nombre sea unico y no choque con otra app
    private static final String PREFERENCIAS = "recordatorios_entrenamiento"; //Es el nombre del pequenno archivo interno de preferencias de la app.
    private static final String CLAVE_ULTIMO_AVISO = "ultimo_aviso"; //CLAVE_ULTIMO_AVISO se guarda mediante SharedPreferences, que es almacenamiento interno persistente de la app
    private static final String CLAVE_ULTIMO_AVISO_FINAL_DIA = "ultimo_aviso_final_dia"; //Evita repetir el recordatorio nocturno el mismo día.
    //CLAVE_ULTIMO_AVISO Es el nombre del dato guardado del pequenno archivo de la variable PREFERENCIAS No es la fecha; es el nombre usado para encontrar la fecha.
    private static final int ID_NOTIFICACION = 1001; //id de la notificacion mostrada
    private static final String[] MENSAJES = { //frases motivadoras para los mensajes, en futuro las sacaremos de la bd.
            "Hoy toca entrenar. ¡Cada repetición cuenta!",
            "Tu entrenamiento te espera. ¡Vamos a por ello!"
    };
    //Mensaje nocturno
    private static final String MENSAJE_FINAL_DIA = "¿Has entrenado hoy? Entra en ZyJer y registra tu sesión.";
}
