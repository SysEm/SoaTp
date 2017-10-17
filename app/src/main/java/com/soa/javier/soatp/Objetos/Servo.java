package com.soa.javier.soatp.Objetos;

/**
 * Created by javier on 14/10/2017.
 */

public class Servo {
    Float angulo, esfuerzo;

    public Servo(Float angulo, Float esfuerzo) {
        this.angulo = angulo;
        this.esfuerzo = esfuerzo;
    }

    public Servo() {
    }

    public Float getAngulo() {
        return angulo;
    }

    public void setAngulo(Float angulo) {
        this.angulo = angulo;
    }

    public Float getEsfuerzo() {
        return esfuerzo;
    }

    public void setEsfuerzo(Float esfuerzo) {
        this.esfuerzo = esfuerzo;
    }
}
