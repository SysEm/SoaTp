package com.soa.javier.soatp.Objetos;

/**
 * Created by javier on 14/10/2017.
 */

public class Servo {
    Integer angulo, esfuerzo;

    public Servo(Integer angulo, Integer esfuerzo) {
        this.angulo = angulo;
        this.esfuerzo = esfuerzo;
    }

    public Servo() {
    }

    public Integer getAngulo() {
        return angulo;
    }

    public void setAngulo(Integer angulo) {
        this.angulo = angulo;
    }

    public Integer getEsfuerzo() {
        return esfuerzo;
    }

    public void setEsfuerzo(Integer esfuerzo) {
        this.esfuerzo = esfuerzo;
    }
}
