/*
Clase CaonexionSQLite.java
Fecha actualiza: 07/10/2026
Autor: Jorge Jimenez Garrido
Descripcion: Clase para relizar la conexion de a la bd local desde la Modelo.java
aqui tenemos las tablas y un metodo para ejecutar script al inicar la app, y un control de version de la app que tiene la bd del movil
con la que llega en la actualizacion asi saber si se crea por primera vez o solo ejecutar si trae algo de bd en la actualizacion
*/

package app.jjg.zyjer.database;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConexionSQLite extends SQLiteOpenHelper {
    //TABLAS
    final String TBL_USR = "CREATE TABLE tlrutinas (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT UNIQUE, dias INTEGER)"; //tabla de la rutinas
    final String TBL_EJE = "CREATE TABLE tlejercicios (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, dia INTEGER NOT NULL, idrutinas INTEGER, series INTEGER, repes INTEGER, peso INTEGER)"; //tablas de los ejercicios
    final String TBL_HIS = "CREATE TABLE tlhistorial (id INTEGER PRIMARY KEY AUTOINCREMENT, idejercicio INTEGER, repes INTEGER, peso INTEGER, date TEXT)";
    final String TBL_PESAJE = "CREATE TABLE tlpesaje (id INTEGER PRIMARY KEY AUTOINCREMENT, peso INTEGER, date TEXT)";
    final String TBL_CALENDAR = "CREATE TABLE tlcalendar (id INTEGER PRIMARY KEY AUTOINCREMENT,date TEXT UNIQUE,estado INTEGER)";
    final String TBL_NOMBRES_DIAS = "CREATE TABLE tlnombresdias (idrutinas INTEGER NOT NULL, dia INTEGER NOT NULL, nombre TEXT, PRIMARY KEY (idrutinas, dia))";
    // Solo se persiste la ruta privada del archivo, nunca los bytes de la imagen dentro de SQLite.
    final String TBL_IMAGENES = "CREATE TABLE tlimagenes (idejercicio INTEGER NOT NULL UNIQUE, ruta TEXT NOT NULL UNIQUE, PRIMARY KEY (idejercicio, ruta))";
    //final String TBL_MET = "CREATE TABLE tleventos (id INTEGER PRIMARY KEY AUTOINCREMENT, codigo TEXT UNIQUE,nombre TEXT, fechaini TEXT, fechafin TEXT, dias INTEGER, hiper TEXT, perder TEXT, fuerza TEXT, salud TEXT, toni TEXT)";

    public ConexionSQLite(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override //LLamara al onCreate cuando sea la primera vez que instala la app
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(TBL_USR); //Tabla rutinas
        db.execSQL(TBL_EJE); //Tabla ejercicios
        db.execSQL(TBL_HIS); //Tabla Historial
        db.execSQL(TBL_PESAJE); //Tabla Pesaje
        db.execSQL("ALTER TABLE tlejercicios ADD COLUMN nom_dia TEXT"); //Un nuevo campo para la tabla ejercicios
        db.execSQL("ALTER TABLE tlejercicios ADD COLUMN orden INTEGER"); //Un nuevo campo para la tabla ejecicios
        db.execSQL("ALTER TABLE tlejercicios ADD COLUMN dia_secundario INTEGER"); //Un nuevo campo para la tabla ejecicios
        db.execSQL("ALTER TABLE tlejercicios ADD COLUMN orden_secundario INTEGER"); //Orden propio del ejercicio en su dia secundario
        db.execSQL(TBL_CALENDAR); //Tabla Calendario
        db.execSQL(TBL_NOMBRES_DIAS); //Tabla Nombres_Dias
        db.execSQL(TBL_IMAGENES); //Tabla de imagenes de ejercicios
    }

    @Override //Llamara a onUpgrade si detesta que la version nueva es mayor que la version de la bases de datos del dipositivo
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if(oldVersion < 2 && newVersion >= 2){ //Actualizar si la bbdd esta en una version inferior a 2

            db.execSQL(TBL_HIS); //Tabla Historial
        }
        /*if (oldVersion < 8 && newVersion >= 8){ //Actualizar la db con la version 3
            //TASK 6
            EjecutarScript(db, context, "02_s.sql");
            db.execSQL("ALTER TABLE tlejercicios ADD COLUMN nom_dia TEXT"); //Un nuevo campo para la tabla
            db.execSQL("ALTER TABLE tlejercicios ADD COLUMN orden INTEGER"); //Un nuevo campo para la tabla
        }*/
        /*if(oldVersion < 4 && newVersion >= 4){
            EjecutarScript(db, context, "02_s.sql");
        }
        if(oldVersion < 5 && newVersion >= 5){
            db.execSQL("ALTER TABLE tlejercicios ADD COLUMN nom_dia TEXT");
        }*/
        /*if(oldVersion < 6 && newVersion >= 6){
            db.execSQL("ALTER TABLE tlejercicios ADD COLUMN orden INTEGER");
        }*/

        /*if(oldVersion < 7 && newVersion >= 7){ //solo arreglo para martin
            EjecutarScript(db, context, "03_arreglomartin.sql");
        }*/

        /*if (oldVersion < 8 && newVersion >= 8){
            db.execSQL(TBL_MET);
            db.execSQL("ALTER TABLE tlejercicios ADD COLUMN ideventos INTEGER"); //Un nuevo campo para la tabla
            db.execSQL("ALTER TABLE tlejercicios ADD COLUMN levento TEXT"); //Un nuevo campo para la tabla
            db.execSQL("ALTER TABLE tlejercicios ADD COLUMN tipo INTEGER"); //Un nuevo campo para la tabla

            db.execSQL("INSERT into tleventos (codigo,nombre,fechaini,fechafin,dias,hiper,perder,fuerza,salud,toni) VALUES ('ELORG1','El Origen','21/07/2025','04/08/2025','2','S','N','S','N','N')"); //TODO tenemos que crear los ejercicios de la rutina el origen Investigar si podemos hacer un script modo un tlf
            //db.execSQL("INSERT into tlejercicios (nombre,dia,idrutina,series,repes,peso,ideventos,levento) VALUES ('El Origen','21/07/2025','04/08/2025','3')");
        }*/

        if (oldVersion < 9 && newVersion >= 9){ //Actualizar si la bbdd esta en una version inferior a 9
            db.execSQL(TBL_PESAJE); //Tabla Pesaje
        }

        if(oldVersion < 10 && newVersion >= 10){ //Actualizar si la bbdd esta en una version inferior a 10
            db.execSQL(TBL_CALENDAR); //Tabla Calendar
        }

        if(oldVersion < 11 && newVersion >= 11){
            db.execSQL(TBL_IMAGENES);
        }

        // Solo la versión 11 creó tlimagenes con BLOB. Versiones anteriores ya reciben la tabla nueva.
        /*if (oldVersion == 12 && newVersion >= 13) {
            migrarImagenesBlobARutas(db);
        }*/
    }

    //Metodo para ejecutar un script sql que esta guardado en assets
    private void EjecutarScript(SQLiteDatabase db, Context context, String assetFileName){
        try {
            //El context lo necesitamos para accedera al empaquetado de la app, es como un carnet de identidad
            //Sin el context no podemos acceder a la carpeta Assets

            //En el context usamos el getAssets para devolver el contendio de esa carpeta y .open para delvolver
            //justo ese archivo que se encunetra en esa carpeta, eso devuelve un InputStream

            /*Un InputStream es el flujo de bytes (ceros y unos).
             *No le importa si el archivo es un texto, una imagen o un sonido; su trabajo es simplemente
             * mover esos bits de un lugar a otro. */

            InputStream is = context.getAssets().open(assetFileName);

            //InputStreamReader traduce  esos bytes(0 y 1) a caracteres (letras)
            //BufferedReader Agrupa las letras en lineas completas
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            StringBuilder statement = new StringBuilder();
            String line;

            //Esto es un seguro de vida, si algo en el script falla no se guaradara ningun camvio ya ejecutado
            //Aseguramos que la bbdd no se quede a medias
            db.beginTransaction();
            try {
                //Con readLine pasamos a la siguiente linea y se guarda en line
                while ((line = reader.readLine()) != null) {

                    line = line.trim(); //Quitamos espacios innecesarios al principio y final de la linea
                    if (line.isEmpty() || line.startsWith("--")) continue; // ignoramos las lineas vacias o comentarios
                    statement.append(line); //Esta acumulando los trozos del codigo

                    //Si llegamos al final de una instruccion entramos y ejecutamos la sentencia
                    if (line.endsWith(";")) {

                        String sql = statement.toString(); //Intruccion guardada
                        db.execSQL(sql); //ejecutar como siempre hacemos
                        statement.setLength(0); //Limpiamos el acumulador para la siguiente instruccion
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                reader.close();
            }
        } catch (IOException | SQLiteException e) {
            e.printStackTrace(); //error
        }
    }

    /** Conserva las imágenes creadas por la versión 11, sacándolas de SQLite a filesDir.*/
    /*private void migrarImagenesBlobARutas(SQLiteDatabase db) {
        db.beginTransaction();
        Cursor imagenesAntiguas = null;
        try {
            db.execSQL("ALTER TABLE tlimagenes RENAME TO tlimagenes_blob");
            db.execSQL(TBL_IMAGENES);
            File carpeta = new File(context.getFilesDir(), "imagenes_ejercicios");
            if (!carpeta.exists() && !carpeta.mkdirs()) throw new IOException("No se pudo crear la carpeta de imágenes");

            imagenesAntiguas = db.query("tlimagenes_blob", new String[]{"idejercicio", "imagen"},
                    null, null, null, null, null);
            while (imagenesAntiguas.moveToNext()) {
                int idEjercicio = imagenesAntiguas.getInt(0);
                File archivo = new File(carpeta, "ejercicio_" + idEjercicio + ".img");
                try (FileOutputStream salida = new FileOutputStream(archivo)) {
                    salida.write(imagenesAntiguas.getBlob(1));
                }
                android.content.ContentValues valores = new android.content.ContentValues();
                valores.put("idejercicio", idEjercicio);
                valores.put("ruta", archivo.getAbsolutePath());
                if (db.insert("tlimagenes", null, valores) == -1) throw new SQLException("No se pudo migrar una imagen");
            }
            db.execSQL("DROP TABLE tlimagenes_blob");
            db.setTransactionSuccessful();
        } catch (IOException e) {
            throw new SQLiteException("No se pudieron migrar las imágenes existentes", e);
        } finally {
            //if (imagenesAntiguas != null) imagenesAntiguas.close();
            db.endTransaction();
        }
    }*/


    //Campos de clase
    Context context;
}
