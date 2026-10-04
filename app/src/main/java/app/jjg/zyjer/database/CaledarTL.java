/*
Clase CalendarTL.java
Fecha actualiza: 26/03/2026
Autor: Jorge Jimenez Garrido
Descripcion: Clase para objetos tipos CalendarTL getters y setters
*/
package app.jjg.zyjer.database;

public class CaledarTL {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getdia_semana() {
        return dia_semana;
    }

    public void setdia_semana(String dia_semana) {
        this.dia_semana = dia_semana;
    }

    public String gethora() {
        return hora;
    }

    public void sethora(String hora) {
        this.hora = hora;
    }

    public String getestado() {
        return estado;
    }

    public void setestado(String estado) {
        this.estado = estado;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private String id;
    private String dia_semana;
    private String hora;
    private String estado;
    private String date;
}
