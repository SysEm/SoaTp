package com.soa.javier.soatp.Objetos;

/**
 * Created by javier on 14/10/2017.
 */

public class Led {
    String estado;

    public Led() {
    }

    public Led(String estado) {
        this.estado = estado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
