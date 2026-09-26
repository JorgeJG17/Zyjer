/*
Clase Modelo.java
Fecha actualiza: 26/03/2026
Autor: Jorge Jimenez Garrido
Descripcion: Clase con metodos para hacer las consultas a la base de datos, todas las consultas deben de estar en esta clase
Luego cada pagina llamara a esta clase opara utilizarlas si es necesario
*/

package app.jjg.nanogym.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Modelo {

    private static final int DB_VERSION = 10; //Si hacemos cambios en la base de datos, tablas nuevas, campos nuevos, sumamos uno

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


    //Metodo para eliminar una rutina, se borran sus ejercicios en caso de existir
    public int EliminarRutina(Context context, int idRutina){
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;
        int res;

        String sqldatos = "SELECT count(1) FROM tlejercicios WHERE idrutinas = '"+ idRutina +"'";

        String sql = "DELETE FROM tlejercicios WHERE idrutinas = '"+ idRutina +"'";
        String sql2 = "DELETE FROM tlrutinas WHERE id = '"+ idRutina +"'";

        resultados = db.rawQuery(sqldatos, null);
        resultados.moveToFirst(); //Empezamos por el primero y en este caso el unico

        if(resultados.getInt(0) != 0){

            try{
                db.execSQL(sql);
                db.execSQL(sql2);
                res = 1; //Se elimino correctamente
            }catch (Exception e){
                res = 3333; //Se lanza un error no controlado
            }

        }else{

            try{
                db.execSQL(sql2);
                res = 1; //Se elimino correctamente
            }catch (Exception e){
                res = 3333; //Se lanza un error no controlado
            }

        }

        db.close();
        return res; //Devolvemos el resultado
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


    //Metodo para actualizar los datos de la tabla de ejercicios, peso, repes, series
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



    //Metodo para eliminar un ejerc, se borran sus ejercicios en caso de existir
    public int EliminarEjercicio(Context context, int idejer){
        SQLiteDatabase db = this.getConn(context);
        Cursor resultados;
        int res;

        String sql = "DELETE FROM tlejercicios WHERE id = '"+ idejer +"'";

            try{
                db.execSQL(sql);
                res = 1; //Se elimino correctamente
            }catch (Exception e){
                res = 3333; //Se lanza un error no controlado
            }

        db.close();
        return res; //Devolvemos el resultado
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
