package com.soa.javier.soatp.Objetos;

/**
 * Created by javier on 14/10/2017.
 */

public class Puerta {
    Led led;
    Presencia presencia;
    Servo servo;


    public Puerta() {
        this.led = new Led();
        this.presencia = new Presencia();
        this.servo = new Servo();
    }

    public void setPuerta(String estLed,String estPresen,Float angServo,Float esfServo){
        this.led.setEstado(estLed);
        this.presencia.setEstado(estPresen);
        this.servo.setAngulo(angServo);
        this.servo.setEsfuerzo(esfServo);
    }

    public Led getLed() {
        return led;
    }

    public void setLed(Led led) {
        this.led = led;
    }

    public Presencia getPresencia() {
        return presencia;
    }

    public void setPresencia(Presencia presencia) {
        this.presencia = presencia;
    }

    public Servo getServo() {
        return servo;
    }

    public void setServo(Servo servo) {
        this.servo = servo;
    }
}
