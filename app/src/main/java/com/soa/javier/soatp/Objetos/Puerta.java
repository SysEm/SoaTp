package com.soa.javier.soatp.Objetos;

/**
 * Created by javier on 14/10/2017.
 */

public class Puerta {
    private String id;
    private Led led;
    private Presencia presencia;
    private Servo servo;


    public Puerta() {
        this.id = "";
        this.led = new Led();
        this.presencia = new Presencia();
        this.servo = new Servo();
    }

    public void setPuerta(String estLed, String estPresen, Integer angServo, Integer esfServo){
        this.led.setEstado(estLed);
        this.presencia.setEstado(estPresen);
        this.servo.setAngulo(angServo);
        this.servo.setEsfuerzo(esfServo);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Led getLed() {
        return led;
    }

    public void setLed(String estLed) {
        this.led.setEstado(estLed);
    }

    public Presencia getPresencia() {
        return presencia;
    }

    public void setPresencia(String estPresen) {
        this.presencia.setEstado(estPresen);
    }

    public Servo getServo() {
        return servo;
    }

    public void setServo(Integer angServo,Integer esfServo) {
        this.servo.setAngulo(angServo);
        this.servo.setEsfuerzo(esfServo);
    }


}
