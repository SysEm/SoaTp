package com.soa.javier.soatp.Objetos;
import android.text.format.DateUtils;

import android.util.TimeUtils;

import java.util.Date;

/**
 * Created by ferbroqua on 17/10/2017.
 */


public class Notificacion {
    public static final int URG_0EMERGENCIA = 0;
    public static final int URG_1ATENCION = 10;
    public static final int URG_2AVISO = 20;
    public static final int URG_3NOIMPORTANTE = 30;
    public static final int EST_0MARCADO = 0;
    public static final int EST_1NOLEIDO = 10;
    public static final int EST_2LEIDO = 20;
    public static final int EST_3OCULTO = 30;

    private Date fecha;
    private int estado;   // EST_*
    private String mensaje;
    private int urgencia; // URG_*

    public Notificacion (String mensaje, int urgencia) {
        //DateFormat date = new SimpleDateFormat("MMM dd yyyy, h:mm");
        //String dateFormatted = date.format(Calendar.getInstance().getTime());
        this.fecha = new Date();
        this.estado = EST_1NOLEIDO;
        this.mensaje = mensaje;
        this.urgencia = urgencia;
    }
}
