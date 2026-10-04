/*
Clase EjerciciosTl.java
Fecha actualiza: 26/03/2026
Autor: Jorge Jimenez Garrido
Descripcion: Clase para objetos tipos EjerciciosTL getters y setters
*/
package app.jjg.zyjer.database;

public class EjerciciosTL {

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getRepes() {
        return repes;
    }

    public void setRepes(String repes) {
        this.repes = repes;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    public String getIdRutina() {
        return idRutina;
    }

    public void setIdRutina(String idRutina) {
        this.idRutina = idRutina;
    }

    private String id;
    private String nombre;
    private String dia;
    private String series;
    private String repes;
    private String peso;
    private String idRutina;
}
