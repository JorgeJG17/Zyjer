# Notificaciones y programación semanal del calendario

## Notificaciones de entrenamiento

La clase `app.jjg.zyjer.notificaciones.NotificacionesEntrenamiento` concentra el sistema de avisos. Al abrir la pantalla principal de entrenamiento se crea el canal de Android, se programan alarmas diarias aproximadas para las 09:00 y las 21:00, y se pide el permiso de notificaciones en Android 13 o superior.

Cuando se dispara la alarma, `RecordatorioEntrenamientoReceiver` consulta `tlcalendar` usando `Modelo.HayEntrenamientoEnFecha`. Solo se crea una notificación si existe una fila cuya fecha (`date`) coincide con la fecha actual. El texto se elige aleatoriamente entre dos mensajes motivadores definidos en el código. Las preferencias de la app guardan la fecha del último aviso, evitando duplicados si la app se abre el mismo día.

El aviso de las 21:00 se muestra únicamente si existe un entrenamiento de hoy con estado pendiente (`estado = 2`), para recordar registrar la sesión sin molestar a quien ya la marcó como realizada. El receptor vuelve a programar ambas alarmas después de reiniciar el dispositivo o actualizar la aplicación. Los avisos son inexactos para respetar las restricciones de batería de Android, por lo que pueden llegar ligeramente después de la hora objetivo. Si el usuario deniega el permiso, la aplicación sigue funcionando pero Android no mostrará los avisos.

## Programación de días fijos

En Calendario se añadió el botón **Programar días fijos**. Abre un selector de lunes a domingo y, tras confirmar, llama a `Modelo.InsertarEntrenamientosSemanales`.

El método recorre desde la fecha actual hasta el 31 de diciembre del año actual. Para cada día semanal marcado inserta un registro pendiente (`estado = 2`) en `tlcalendar`. Se utiliza una transacción y `CONFLICT_IGNORE`: como `date` es único, una fecha que el usuario ya hubiera añadido manualmente o programado antes no se duplica ni se modifica. No se crean fechas de años posteriores ni fechas anteriores al día en que se ejecuta la acción.


## REVISADO OK JJG
