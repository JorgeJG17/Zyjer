/*
Clase PesajeTL.java
Fecha actualiza: 26/03/2026
Autor: Jorge Jimenez Garrido
Descripcion: Clase para objetos tipos PesajeTL getters y setters
*/
package app.jjg.zyjer.database;

public class PesajeTL {

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private String id;
    private String peso;
    private String date;
}
