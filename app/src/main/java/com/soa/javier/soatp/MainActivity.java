package com.soa.javier.soatp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.soa.javier.soatp.Objetos.Presencia;
import com.soa.javier.soatp.Objetos.Puerta;
import com.soa.javier.soatp.Objetos.Led;

import static com.soa.javier.soatp.Objetos.FireBaseReferencias.BBDDSOA_REFERENCE;
import static com.soa.javier.soatp.Objetos.FireBaseReferencias.SENPROXI_REFERENCE;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    SensorManager sm;
    Sensor sensorProx;
    Puerta puerta = new Puerta();
    TextView tView;
    TextView tView2;


    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference baseDatosSoaRef = database.getReference("baseDatosSoa").child("Puerta");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tView = (TextView)findViewById(R.id.tview);
        tView2 = (TextView)findViewById(R.id.tview2);


        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensorProx = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sm.registerListener(this,sensorProx,SensorManager.SENSOR_DELAY_NORMAL);
    }



    @Override
    protected void onStart() {
        super.onStart();

        baseDatosSoaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Puerta puerta = dataSnapshot.getValue(Puerta.class);
                String estadoLed = dataSnapshot.child("Led").child("estado").getValue().toString();
                String estadoPresencia = dataSnapshot.child("Presencia").child("estado").getValue().toString();
                Float anguloServo = Float.parseFloat(dataSnapshot.child("Servo").child("angulo").getValue().toString());
                Float esfuerzoServo = Float.parseFloat(dataSnapshot.child("Servo").child("esfuerzo").getValue().toString());

                puerta.setPuerta(estadoLed,estadoPresencia,anguloServo,esfuerzoServo);
                String txt1 = puerta.getLed().getEstado();
                tView.setText(txt1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        String valProximidad = String.valueOf(sensorEvent.values[0]);
        Float valor = Float.parseFloat(valProximidad);
        tView2.setText(Float.toString(valor));
        String estado;
        if(valor == 1){
            if(puerta.getLed().getEstado().equals("on")){
                estado = "off";
                baseDatosSoaRef.child("Led").child("estado").setValue(estado);
            }else {
                estado = "on";
                baseDatosSoaRef.child("Led").child("estado").setValue(estado);
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}
