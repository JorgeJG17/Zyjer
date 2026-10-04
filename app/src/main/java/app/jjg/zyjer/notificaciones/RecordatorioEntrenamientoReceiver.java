/*
Clase RecordatorioEntrenamientoReceiver.java
Fecha actualiza: 30/09/2026
Autor: Jorge Jimenez Garrido
Descripcion: Recibe la alarma diaria y vuelve a programarla despues de reiniciar el telefono. Tambien tiene alarma nocturna
*/

package app.jjg.zyjer.notificaciones;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//Esta clase es el puente con Android para que a la 9 sale la alrma en caso de que tenga un entrenamiento
//La ejecuta Android, hereda de BroadcastReceiver (Es una clase peque que Android puede activar puntualmente al ocurrir un evento: una alarma, un reinicio, una actualizacion, etc.)
public class RecordatorioEntrenamientoReceiver extends BroadcastReceiver {

    //Android Android llama automaticamente a este metodo cuando llega un evento que coincide con
    // los definidos en el AndroidManifest.xml
    @Override
    public void onReceive(Context context, Intent intent) {
        //Tras un reinicio se pierden las alarmas, por eso se reconstruye la programacion diaria
        // entcoes si el dispositivo se ha reiniciado o ZyJer se ha actualizado.
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) //getAction() vemos que evento llega
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            NotificacionesEntrenamiento.programarRecordatorioDiario(context); //volvemos a ejecutar programarRecordatorioDiario
            NotificacionesEntrenamiento.programarRecordatorioFinalDia(context); //restauramos también el aviso de las 21:00
            return;
        }

        //La accion propia de las 21:00 solo avisa cuando el entrenamiento de hoy continua pendiente.
        if (NotificacionesEntrenamiento.ACCION_RECORDATORIO_FINAL_DIA.equals(intent.getAction())) {
            NotificacionesEntrenamiento.comprobarYNotificarFinalDia(context);
            return;
        }

        //Pasamos a mostrar la alarma de la mañana; esto es responsabilidad de NotificacionesEntrenamiento,
        //que sabe comprobar permisos, base de datos, duplicados y mensaje aleatorio.
        NotificacionesEntrenamiento.comprobarYNotificarHoy(context);
    }
}
