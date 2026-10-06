/*
Clase Modelo.java
Fecha actualiza: 30/09/2026
Autor: Jorge Jimenez Garrido
Descripcion: Clase con metodos para hacer las consultas a la base de datos, todas las consultas deben de estar en esta clase
Luego cada pagina llamara a esta clase opara utilizarlas si es necesario
*/

package app.jjg.zyjer.database;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Modelo {

    private static final int DB_VERSION = 13; //Si hacemos cambios en la base de datos, tablas nuevas, campos nuevos, sumamos uno

    //Metodo que genera la base de datos, la llamara dbgym
    public SQLiteDatabase getConn(Context context){
        ConexionSQLite conn = new ConexionSQLite(context,"dbgym", null, DB_VERSION); //conexion con la base de datos SQLite
        SQLiteDatabase db = conn.getWritableDatabase(); //Definir la base de datos como escritura
        return db;
    }

    //Metodo para insertar rutinas
    public int InsertaRutina(Context context, RutinasTL dto){
        int res = 0;
        Cursor resultados;
        SQLiteDatabase db = this.getConn(context);

        String sqlSelect="SELECT COUNT(1) FROM tlrutinas WHERE nombre = " + "'" +dto.getNombre() + "'" ; //Consulta select, recuerda que debe de ir entre ' consulta '
        resultados = db.rawQuery(sqlSelect, null); //Guardamos los resultados de la cosnulta select
        resultados.moveToFirst(); //Empezamos por el primero y en este caso el unico

        //Si tenemos resultados significa que ese nombre de rutina ya existe y no podemos tener dos rutinas con el mismo nombre
        if(resultados.getInt(0) == 0){ //Columna 0
            String sql = "INSERT INTO tlrutinas (nombre, dias) VALUES ('"+dto.getNombre()+"','"+dto.getDias()+"')";
            try{
                db.execSQL(sql);
                res = 1; //Se inserto correctamente
            }catch (Exception e){
                res = 3333; //Lanza un error no controlado

            }
        }else{
            res = 2; //Error controlado: la rutina con ese nombre ya existe
        }
        db.close(); //Cerramos la bases de datos
        return res;
    }

    //Metodo para insertar ejercicios
    public int InsertaEjercicios(Context context, EjerciciosTL dto){
        int res = 0;
        String sql = "INSERT INTO tlejercicios (nombre, dia, idrutinas, series, repes, peso) " +
                     "VALUES ('"+dto.getNombre()+"','"+dto.getDia()+"','"+dto.getIdRutina()+"','"+dto.getSeries()+"','"+dto.getRepes()+"','"+dto.getPeso()+"')";

        SQLiteDatabase db = this.getConn(context);
        try{
            db.execSQL(sql);
            db.execSQL(InsertarHistorial(dto)); //TASK 15 Hacemos que realice un primer registro en el historial
            res = 1; //Se inserto correctamente
        }catch (Exception e){
            res = 3333; //Lanza un error no controlado
        }

        db.close(); //Cerramos la bases de datos
        return res;
    }

    //Buscamos el id rutina por el nombre de la rutina ya que es unico tambien
    public int SeleccionarIdRutina(Context context, RutinasTL dto){
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;

        String sqlSelect = "SELECT id FROM tlrutinas WHERE nombre = " + "'" +dto.getNombre() + "'" ;
        resultados = db.rawQuery(sqlSelect, null);
        resultados.moveToFirst();

        int id = resultados.getInt(0);

        db.close();
        return id; //Devolvemos el id, se supone que siempre cogera uno, por lo menos en la version  1.0 Alpha
    }


    //Solucionar problema de cerrar db donde devolvemos un cursos // donde no devolvemos un cursor cierra perfectamente
    //Consulta para sacar las rutinas que tiene creadas en la DB
    public Cursor SeleccionarRutinas(Context context){
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;

        String sqlSelect = "SELECT id, nombre FROM tlrutinas";
        resultados = db.rawQuery(sqlSelect, null);

        //db.close(); Lanza errore despues al recorrer el cursor por culpa de cerrar la base de datos
        return resultados; //Devolvemos todas las rutinas encontradas en la db
    }

    //Consulta para sacar los dias de una rutina
    public Cursor SeleccionarDias(Context context, int idRutina){
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;

        String sqlSelect = "SELECT dias FROM tlrutinas WHERE id = '"+ idRutina +"'";
        resultados = db.rawQuery(sqlSelect, null);

        //db.close();
        return resultados; //Devolvemos nuemro los días  de la rutina encontradas en la db
    }

    //Consulta para sacar los ejercicios de una rutina y dia
    public Cursor SeleccionarEjercicos(Context context, int idRutina, int dia){
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;

        String sqlSelect = "SELECT id,nombre,series,repes,peso FROM tlejercicios WHERE idrutinas = '"+ idRutina +"' AND dia = '"+dia+"' ORDER BY orden";
        resultados = db.rawQuery(sqlSelect, null);

        //db.close();
        return resultados; //Devolvemos todas los ejercicios de la rutina que nos ha llegado por parametros
    }

    //Consulta para sacar el historial de un ejercicio
    public Cursor  SeleccionarHistorial(Context context, int idEjercicio){
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;

        String sqlSelect = "SELECT id,repes,peso,date FROM tlhistorial WHERE idejercicio = '"+ idEjercicio + "' ORDER BY id DESC";
        resultados = db.rawQuery(sqlSelect, null);

        //db.close();
        return resultados; //Devolvemos el historial del ejercicio
    }

    //Metodo para actualizar los datos de la tabla de ejercicios, peso, repes, series
    public int ActualizarDatosTabla(Context context, EjerciciosTL dto, boolean evento){
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;
        int res;
        //LocalDate fecha = LocalDate.now();
        //DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        //String fechaFormateada = fecha.format(formato);

        if(evento == true){
            String sql = "UPDATE tlejercicios SET series = '"+ dto.getSeries() +"',repes = '"+ dto.getRepes() +"',peso = '"+ dto.getPeso() +"' WHERE ideventos = '"+ dto.getIdRutina() +"' AND id = '"+ dto.getId() +"'";
            try{
                db.execSQL(sql);
                res = 1; //Se inserto correctamente
            }catch (Exception e){
                res = 3333; //Lanza un error no controlado
            }
        }
        else{
            String sql = "UPDATE tlejercicios SET series = '"+ dto.getSeries() +"',repes = '"+ dto.getRepes() +"',peso = '"+ dto.getPeso() +"' WHERE idrutinas = '"+ dto.getIdRutina() +"' AND id = '"+ dto.getId() +"'";
            String sqlH = "SELECT peso FROM tlejercicios WHERE id = '"+ dto.getId() +"'";
            //String sqlIH = "INSERT INTO tlhistorial (idejercicio, repes, peso, date) VALUES ('"+ dto.getId() +"','"+ dto.getRepes() +"','"+ dto.getPeso() +"','"+ fechaFormateada +"')";
            resultados = db.rawQuery(sqlH, null);
            resultados.moveToFirst();

            try{
                //Si el peso nuevo es diferente al que estaba antes entoces insertamos como historial del ejercicioo
                if(Double.parseDouble(dto.getPeso()) != resultados.getDouble(0)){

                    db.execSQL(InsertarHistorial(dto));
                }

                db.execSQL(sql);
                res = 1; //Se inserto correctamente
            }catch (Exception e){
                res = 3333; //Lanza un error no controlado
            }
        }

        db.close();
        return res; //Devolvemos el resultado
    }


    //Metodo para eliminar una rutina, sus ejercicios y las imagenes privadas asociadas rn lod ejercicios.
    public int EliminarRutina(Context context, int idRutina){
        SQLiteDatabase db = this.getConn(context);
        List<String> rutasImagenes = new ArrayList<>();
        // Array con los valores que sustituiran a los ? de la consulta SQL.
        // Aunque aqui solo hay un idRutina, el metodo que se usa exige un array porque una consulta puede tener varios ?.
        String[] argumentos = new String[]{String.valueOf(idRutina)};
        int res = 3333;

        // Guardamos las rutas antes de borrar los registros que permiten localizarlas.
        Cursor imagenes = db.rawQuery(
                "SELECT i.ruta FROM tlimagenes i " +
                "INNER JOIN tlejercicios e ON e.id = i.idejercicio " +
                "WHERE e.idrutinas = ?", argumentos);
        while (imagenes.moveToNext()) {
            rutasImagenes.add(imagenes.getString(0));
        }
        imagenes.close();

        db.beginTransaction();
        try {
            // Primero se elimina la relación imagen-ejercicio y después los ejercicios y la rutina.
            db.delete("tlimagenes", "idejercicio IN (SELECT id FROM tlejercicios WHERE idrutinas = ?)",
                    argumentos);
            db.delete("tlejercicios", "idrutinas = ?", argumentos);
            db.delete("tlrutinas", "id = ?", argumentos);
            db.setTransactionSuccessful();
            res = 1;
        } catch (Exception e) {
            res = 3333;
        } finally {
            db.endTransaction();
            db.close();
        }

        // Los archivos se borran solo si SQLite confirmo que la eliminación fue correcta.
        if (res == 1) {
            for (String rutaImagen : rutasImagenes) {
                new File(rutaImagen).delete();
            }
        }
        return res;
    }

    //Metodo para insetar en Historial //TASK 15
    private String InsertarHistorial(EjerciciosTL dto){

        LocalDate fecha = LocalDate.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String fechaFormateada = fecha.format(formato);
        String sqlIH = "INSERT INTO tlhistorial (idejercicio, repes, peso, date) VALUES ('"+ dto.getId() +"','"+ dto.getRepes() +"','"+ dto.getPeso() +"','"+ fechaFormateada +"')";

        return sqlIH;
    }


    //TASK 6
    //Metodo para seleccionar el nombre del dia y rutina que llega por parametros
    public String SeleccionarTipDia(Context context, int idRutina, int dia){
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;
        String nom_d;

        String sqlSelect = "SELECT nom_dia FROM tlejercicios WHERE idrutinas = '"+ idRutina +"' AND dia = '"+dia+"'";
        resultados = db.rawQuery(sqlSelect, null);

        resultados.moveToFirst();
        nom_d = resultados.getString(0);

        resultados.close();
        db.close();
        return nom_d;
    }

    //TASK 6
    //Metodo para actualizar el nombre del dia, rutina y el nuevo nombre que llega por parametros
    public  int ActualizarNomDia(Context context, int idRutina, int dia, String nd){
        SQLiteDatabase db = this.getConn(context);
        int res;

        String sql = "UPDATE tlejercicios SET nom_dia ='"+ nd +"' WHERE idrutinas = '"+ idRutina +"' AND dia = '"+dia+"'";

        try{
            db.execSQL(sql);
            res = 1; //Se inserto correctamente
        }catch (Exception e){
            res = 3333; //Lanza un error no controlado
        }

        db.close();
        return res;
    }

    //TASK 5
    public char[] tiposDisponibleEventos(Context context, int idEvento){

        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;
        char[] array = new char[5];

        String sql = "SELECT hiper,perder,fuerza,salud,toni FROM tleventos WHERE id ='"+ idEvento +"'";
        resultados = db.rawQuery(sql, null);
        resultados.moveToFirst();

        for(int i=0; i<5; i++){
            array[i] = resultados.getString(i).charAt(0);;
        }

        return array;
    }

    //Consulta para sacar los dias de un evento TASK 5
    public Cursor SeleccionarDiasEV(Context context, int idEvento){
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;

        String sqlSelect = "SELECT dias FROM tleventos WHERE id = '"+ idEvento +"'";
        resultados = db.rawQuery(sqlSelect, null);

        //db.close();
        return resultados; //Devolvemos nuemro los días  deL EVENTO encontradas en la db
    }

    public int SeleccionaridEvento(Context context, String cod){
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;
        int id;

        String sqlSelect = "SELECT idEvento FROM tleventos WHERE codigo = '"+ cod +"'";
        resultados = db.rawQuery(sqlSelect, null);

        resultados.moveToFirst();
        id = resultados.getInt(0);

        resultados.close();
        db.close();
        //db.close();
        return id; //Devolvemos nuemro los días  deL EVENTO encontradas en la db
    }


    //Consulta para sacar los ejercicios de una rutina y dia
    public Cursor SeleccionarEjerciciosEventos(Context context, int idEvento, int dia, int tipo){
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;

        String sqlSelect = "SELECT id,nombre,series,repes,peso FROM tlejercicios WHERE ideventos = '"+ idEvento +"' AND dia = '"+dia+"' AND tipo = '"+tipo+"'";
        resultados = db.rawQuery(sqlSelect, null);

        //db.close();
        return resultados; //Devolvemos todas los ejercicios de la rutina que nos ha llegado por parametros
    }


    //Metodo para actualizar el orden de los ejercicios
    public int ActualizarOrdenTabla(Context context, int ord, int idejer, int idrut){
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;
        int res;
            String sql = "UPDATE tlejercicios SET orden ='"+ ord +"' WHERE idrutinas = '"+ idrut +"' AND id = '"+ idejer +"'";
            try{
                db.execSQL(sql);
                res = 1; //Se inserto correctamente
            }catch (Exception e){
                res = 3333; //Lanza un error no controlado
            }

        db.close();
        return res; //Devolvemos el resultado
    }



    //Metodo para eliminar un ejerc, se borran el ejercicio y imagen en caso de existir
    public int EliminarEjercicio(Context context, int idejer){
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;
        int res;

        String sql = "DELETE FROM tlejercicios WHERE id = '"+ idejer +"'";
        String rutaImagen = null;
        Cursor imagen = db.query("tlimagenes", new String[]{"ruta"}, "idejercicio = ?",
                new String[]{String.valueOf(idejer)}, null, null, null);
        if (imagen.moveToFirst()) rutaImagen = imagen.getString(0);
        imagen.close();

            try{
                // La relación se limpia explicitamente porque el esquema historico no usa claves foraneas.
                db.delete("tlimagenes", "idejercicio = ?", new String[]{String.valueOf(idejer)});
                db.execSQL(sql);
                res = 1; //Se elimino correctamente
            }catch (Exception e){
                res = 3333; //Se lanza un error no controlado
            }

        db.close();
        if (res == 1 && rutaImagen != null) new File(rutaImagen).delete();
        return res; //Devolvemos el resultado
    }

    /** Devuelve la ruta privada de la imagen asociada al ejercicio, o null si todavía no se ha elegido una. */
    public String SeleccionarRutaImagenEjercicio(Context context, int idEjercicio) {
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados = db.query("tlimagenes", new String[]{"ruta"}, "idejercicio = ?",
                new String[]{String.valueOf(idEjercicio)}, null, null, null);
        String ruta = resultados.moveToFirst() ? resultados.getString(0) : null;
        resultados.close();
        db.close();
        return ruta;
    }

    /** Guarda la ruta privada de la imagen. Las restricciones UNIQUE protegen ambos lados de la relacion. */
    public int GuardarRutaImagenEjercicio(Context context, int idEjercicio, String ruta) {
        SQLiteDatabase db = this.getConn(context);
        ContentValues valores = new ContentValues();
        valores.put("idejercicio", idEjercicio);
        valores.put("ruta", ruta);
        String rutaAnterior = null;
        Cursor existente = db.query("tlimagenes", new String[]{"ruta"}, "idejercicio = ?",
                new String[]{String.valueOf(idEjercicio)}, null, null, null);
        if (existente.moveToFirst()) rutaAnterior = existente.getString(0);
        existente.close();
        try {
            int actualizadas = db.update("tlimagenes", valores, "idejercicio = ?",
                    new String[]{String.valueOf(idEjercicio)});
            if (actualizadas == 0 && db.insert("tlimagenes", null, valores) == -1) {
                return 3333;
            }
            if (rutaAnterior != null && !rutaAnterior.equals(ruta)) new File(rutaAnterior).delete();
            return 1;
        } catch (Exception e) {
            return 3333;
        } finally {
            db.close();
        }
    }

    ///Elimina la asociacion de imagen de un ejercicio y el archivo privado que guardaba su contenido.
    public int EliminarImagenEjercicio(Context context, int idEjercicio) {
        SQLiteDatabase db = this.getConn(context);
        String rutaImagen = null;

        try (Cursor imagen = db.query("tlimagenes", new String[]{"ruta"}, "idejercicio = ?",
                new String[]{String.valueOf(idEjercicio)}, null, null, null)) {
            if (!imagen.moveToFirst()) return 0;
            rutaImagen = imagen.getString(0);

            if (db.delete("tlimagenes", "idejercicio = ?",
                    new String[]{String.valueOf(idEjercicio)}) != 1) return 3333;
        } catch (Exception e) {
            return 3333;
        } finally {
            db.close();
        }

        if (rutaImagen != null) new File(rutaImagen).delete();
        return 1;
    }

    //Consulta para sacar el historial de pesaje
    public Cursor SeleccionarPesaje(Context context){
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;

        String sqlSelect = "SELECT id,peso,date FROM tlpesaje ORDER BY id DESC";
        resultados = db.rawQuery(sqlSelect, null);

        //db.close();
        return resultados; //Devolvemos el historial de pesaje
    }

    //Metodo para insetar en Pesaje
    public int InsertarPesaje(Context context, PesajeTL dto){
        int res;
        String sqlIP = "INSERT INTO tlpesaje (peso, date) VALUES ('"+ dto.getPeso() +"','"+ dto.getDate() +"')";
        SQLiteDatabase db = this.getConn(context);
        try{
            db.execSQL(sqlIP);
            res = 1; //Se inserto correctamente
        }catch (Exception e){
            res = 3333; //Lanza un error no controlado
        }

        db.close(); //Cerramos la bases de datos
        return res;
    }


    //Consulta para sacar el calendario de un ejercicio
    public Cursor SeleccionarCalendario(Context context){
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;

        String sqlSelect = "SELECT date, estado FROM tlcalendar ORDER BY id DESC";
        resultados = db.rawQuery(sqlSelect, null);

        //db.close();
        return resultados; //Devolvemos calendario de dias guardados
    }

    //Consulta insetar el calendario de un ejercicio
    public int InsertarCalendario(Context context,org.threeten.bp.LocalDate fecha){
        SQLiteDatabase db = this.getConn(context);
        int res;
        //LocalDate fecha = LocalDate.now();
        //DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String fechaFormateada = fecha.toString(); //fecha.format(formato);

        String sqlSelect = "INSERT INTO tlcalendar (date, estado) VALUES ('"+fechaFormateada+"',2)";

        try{
            db.execSQL(sqlSelect);
            res = 1; //Se inserto correctamente
        }catch (Exception e){
            res = 3333; //Lanza un error no controlado
        }

        db.close(); //Cerramos la bases de datos
        return res;
    }

    //Comprueba si existe un entrenamiento guardado para una fecha concreta del calendario.
    //Para saber si mostrar o no las notificacion, delvolvemos true o false
    public boolean HayEntrenamientoEnFecha(Context context, LocalDate fecha) {
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados = db.rawQuery(
                "SELECT 1 FROM tlcalendar WHERE date = ? LIMIT 1",
                new String[]{fecha.toString()});
        boolean existe = resultados.moveToFirst();
        resultados.close();
        db.close();
        return existe;
    }

    //Comprueba si el entrenamiento de una fecha sigue pendiente de registrar.
    public boolean HayEntrenamientoPendienteEnFecha(Context context, LocalDate fecha) {
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados = db.rawQuery(
                "SELECT 1 FROM tlcalendar WHERE date = ? AND estado = 2 LIMIT 1",
                new String[]{fecha.toString()});
        boolean existe = resultados.moveToFirst();
        resultados.close();
        db.close();
        return existe;
    }

    //Inserta, desde hoy hasta el 31 de diciembre, los dias semanales elegidos sin tocar fechas ya existentes.
    public int InsertarEntrenamientosSemanales(Context context, boolean[] diasSeleccionados, int estado) {
        SQLiteDatabase db = this.getConn(context);
        int insertados = 0;
        LocalDate fecha = LocalDate.now(); //fecha de hoy
        LocalDate ultimoDiaDelAnio = LocalDate.of(fecha.getYear(), 12, 31); //Con el anno de la fehca de hoy ya cogemos el ultimo dia del anno

        db.beginTransaction(); //agrupa todas las inserciones como una unica operacion, o se guardan correctamente todos los registros,
        // o si ocurre un error no se deja la base de datos a medias
        try {
            //Mientras la fecha no sea posterior al ultimo dia de anno, sigue recorriendo dias
            while (!fecha.isAfter(ultimoDiaDelAnio)) {
                //DayOfWeek usa valores de 1 (lunes) a 7 (domingo) por ello usamos el -1 porque los array empeiza en 0 es decir 0-6 en este caso
                //Es decir si getDayOfWeek().getValue() devuelve 1 significa que estamos en un lunes, -1, sera 0 en nuestro array que es el lunes
                //Y comprueba si llega a true, si llega a true insertamos como entrnamiento en la base de datos
                if (diasSeleccionados[fecha.getDayOfWeek().getValue() - 1]) {
                    ContentValues valores = new ContentValues(); //ContentValues es una forma segura y ordenada de preparar los datos antes de insertarlos
                    valores.put("date", fecha.toString()); //campo fecha de la tabla tlcalendar
                    valores.put("estado", estado); //campo estado del entrenamiento de la tabla tlcalendar
                    //Inserta los valores en la tabla
                    long resultado = db.insertWithOnConflict("tlcalendar", null, valores,
                            SQLiteDatabase.CONFLICT_IGNORE); //Si surge un error de que intentamos annadir una fecha ya existente, simplemente la ignora y pasamos a otra
                    //insertWithOnConflict devuelve: Un valor distinto de -1 si inserto una nueva fila. -1 si no inserto nada, por ejemplo porque esa fecha ya existia
                    if (resultado != -1) {
                        insertados++; //contador de cuantos llevamos insertados
                    }
                }
                fecha = fecha.plusDays(1); //pasamos al siguiente dia
            }
            db.setTransactionSuccessful(); //indica que termino correctamente
        } finally { //se ejecuta tanto si va bien como si hay un error
            db.endTransaction(); //confirma los cambios si  fue bien
            db.close();
        }
        //Podriamos annadir un catch tambien para controlar algun error insesperado
        return insertados;
    }

    //Consulta para borrarrun dia el calendario
    public int BorrarCalendario(Context context, org.threeten.bp.LocalDate date) {

        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;
        int res;

        //org.threeten.bp.format.DateTimeFormatter formato = org.threeten.bp.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaFormateada = date.toString(); //date.format(formato);

        String sql = "DELETE FROM tlcalendar WHERE date = '"+ date +"'";

        try{
            db.execSQL(sql);
            res = 1; //Se elimino correctamente
        }catch (Exception e){
            res = 3333; //Se lanza un error no controlado
        }

        db.close(); //Cerramos la bases de datos
        return res; //Devolvemos el resultado
    }

    //Metodo para marcar en el calendario el estado de una fecha ya guardada en la bd
    public int MarcarCalendario(Context context, int estado, org.threeten.bp.LocalDate date){
        SQLiteDatabase db = this.getConn(context);


        //org.threeten.bp.format.DateTimeFormatter formato = org.threeten.bp.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaFormateada = date.toString();//date.format(formato);
        String sql;
        int res;
        if (estado == 1) {
           sql = "UPDATE tlcalendar SET estado ='" + estado + "' WHERE date = '" + fechaFormateada + "'";
        }
        else{
            sql = "UPDATE tlcalendar SET estado ='" + estado + "' WHERE date < '" + fechaFormateada + "' and estado = 2";
        }
        try{
            db.execSQL(sql);
            res = 1; //Se actualizo correctamnet
        }catch (Exception e){
            res = 3333; //Lanza un error no controlado
        }

        db.close(); //Cerramos la bases de datos
        return res; //Devolvemos el resultado
    }

}
