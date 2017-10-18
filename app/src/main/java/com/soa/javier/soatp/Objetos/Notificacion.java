package com.soa.javier.soatp.Objetos;
import android.text.format.DateUtils;

import java.util.Date;

/**
 * Created by ferbroqua on 17/10/2017.
 */

public class Notificacion {
    private Date fecha;
    private String estado;
    private String mensaje;
    private int urgencia;   // 0= EMERGENCIA    1= ATENCION 2= AVISO    3= POCO IMPORTANTE

    public Notificacion (String mensaje, int urgencia) {
        this.fecha = new Date();
        this.estado = "NO LEIDO";
        this.mensaje = mensaje;
        this.urgencia = urgencia;
    }
}
