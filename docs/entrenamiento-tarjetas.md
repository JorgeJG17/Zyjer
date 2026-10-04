# Nueva pantalla de entrenamiento por tarjetas

## Estado e integración

Se ha creado `ventanaEntrenamientoTarjetas` con el layout `activity_entrenamiento_tarjetas.xml`. La actividad está declarada en el manifiest, pero **no se ha añadido ningún enlace de navegación**: la pantalla actual `ventanaRutEjerc` y `activity_ventana_rut_ejerc.xml` permanecen sin cambios.

Cuando se integre, debe iniciarse con los mismos extras existentes:

```java
intent.putExtra("idRutina", idRutina);
intent.putExtra("dia", dia);
```

## Comportamiento

- Cada ejercicio de `tlejercicios` se presenta en una tarjeta horizontal. Se cambia entre ejercicios mediante deslizamiento.
- La tarjeta muestra imagen, nombre, series, repeticiones, peso y RM. La RM usa la misma fórmula de la pantalla antigua: `peso / (1.0278 - 0.0278 * repeticiones)`.
- Series, repeticiones y peso se guardan automáticamente 750 ms después de la última edición, usando `Modelo.ActualizarDatosTabla`. El historial de peso conserva por tanto el comportamiento existente.
- El botón **Historial** abre la pantalla de historial ya existente, sin modificarla.
- **Añadir** utiliza el diálogo y la inserción de ejercicios existentes. **Eliminar** confirma la acción y borra también la imagen asociada.
- El botón **Reordenar ejercicio** abre opciones explícitas para moverlo al inicio, subir una posición, bajar una posición o moverlo al final. El nuevo orden se guarda para toda la rutina y día con `Modelo.ActualizarOrdenTabla`; deslizar tarjetas solo sirve para navegar.

## Imágenes

La versión 12 de base de datos crea `tlimagenes`:

```sql
CREATE TABLE tlimagenes (
  idejercicio INTEGER NOT NULL UNIQUE,
  ruta TEXT NOT NULL UNIQUE,
  PRIMARY KEY (idejercicio, ruta)
);
```

Así se mantiene una relación uno a uno: un ejercicio solo admite una imagen y una misma imagen no puede quedar asociada a dos ejercicios. La imagen se copia a `filesDir/imagenes_ejercicios/` (almacenamiento privado de la app) y SQLite conserva únicamente su ruta absoluta mediante `Modelo.GuardarRutaImagenEjercicio`. El selector limita cada archivo a 3 MB y los nombres se basan en SHA-256 para detectar el mismo archivo.

La migración 11 → 12 extrae los BLOB que se hubieran guardado durante las pruebas previas y los escribe en esa carpeta antes de sustituir la tabla. Por tanto, las imágenes ya seleccionadas no se pierden al actualizar. Al eliminar o sustituir una imagen se elimina también el archivo privado que deja de estar referenciado. Al eliminar una rutina, `Modelo.EliminarRutina` elimina las relaciones de imagen, los ejercicios y la rutina dentro de una transacción; solo después borra los archivos privados asociados.

## Archivos añadidos o modificados

- `ventanasentrenar/ventanaEntrenamientoTarjetas.java`: nueva actividad y adaptador de tarjetas.
- `layout/activity_entrenamiento_tarjetas.xml` y `layout/item_entrenamiento_tarjeta.xml`: pantalla y contenido de cada tarjeta.
- `drawable/training_exercise_card.xml`: fondo de tarjeta.
- `database/ConexionSQLite.java`, `database/Modelo.java`: migración, acceso y limpieza de imágenes.
- `app/build.gradle.kts`: dependencia de `ViewPager2` para el gesto horizontal.


## REVISADO OK JJG
