package com.soa.javier.soatp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.soa.javier.soatp.Objetos.Puerta;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    SensorManager sm;
    Sensor sensorProx;
    Sensor sensorAcel;
    Sensor sensorLumi;

    Puerta puerta = new Puerta();

    TextView tviewLed;
    TextView tviewPuerta;
    TextView tviewPresencia;
    TextView tViewNotificacion;
    Switch switchActivacion;
    RadioButton rBtnLed;
    RadioButton rBtnPuerta;
    RadioButton rBtnLuminosidad;

    RadioGroup radioGroup;
    Button btnOk;


    //AUXILIARES
    TextView tviewProx;
    TextView tviewLumi;
    private static final float SHAKE_THRESHOLD = 2.1f;
    private static final int SHAKE_WAIT_TIME_MS = 500;
    private long mShakeTime = 0;

    Vibrator vibrator;
    MediaPlayer mp;


    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference baseDatosSoaRef = database.getReference("baseDatosSoa").child("Puerta");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("LOG:", "ESTOY EN CREATE");

        //INSTANCIO Y RELACIONO LOS ELEMENTOS QUE VOY A USAR EN LA PANTALLA.
        tviewLed = (TextView)findViewById(R.id.tviewLed);
        tviewPuerta = (TextView)findViewById(R.id.tviewPuerta);
        tviewPresencia = (TextView)findViewById(R.id.tviewPresencia);
        tViewNotificacion = (TextView)findViewById(R.id.tViewNotificacion);

        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        for(int i = 0; i < radioGroup.getChildCount(); i++){
            ((RadioButton)radioGroup.getChildAt(i)).setEnabled(false);
        }

        rBtnLed = (RadioButton) findViewById(R.id.rBtnLed);
        rBtnPuerta = (RadioButton)findViewById(R.id.rBtnPuerta);
        rBtnLuminosidad = (RadioButton)findViewById(R.id.rBtnLuminosidad);


        //INSTANCIO LOS SENSORES
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensorProx = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorAcel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorLumi = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
        //INSTANCIO SENSORES PARA QUE EMPIECEN A ESCUCHAR.
        sm.registerListener(this, sensorAcel,SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, sensorProx,SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, sensorLumi,SensorManager.SENSOR_DELAY_NORMAL);

        //ACTIVO O DESACTIVO LAS FUNCIONALIDADES
        switchActivacion = (Switch)findViewById(R.id.switchActivacion);
        switchActivacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(switchActivacion.isChecked()){
                    for(int i = 0; i < radioGroup.getChildCount(); i++){
                        ((RadioButton)radioGroup.getChildAt(i)).setEnabled(true);
                    }
                }else{
                    for(int i = 0; i < radioGroup.getChildCount(); i++){
                        ((RadioButton)radioGroup.getChildAt(i)).setEnabled(false);
                    }
                    radioGroup.clearCheck();
                }
            }
        });

        btnOk = (Button)findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.cancel();
                mp.stop();
                tViewNotificacion.setVisibility(View.INVISIBLE);
                btnOk.setVisibility(View.INVISIBLE);
            }
        });

        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        mp = MediaPlayer.create(MainActivity.this,R.raw.alarma);
        tviewProx = (TextView)findViewById(R.id.tviewProx);
        tviewLumi = (TextView)findViewById(R.id.tviewLumi);
    }



    @Override
    protected void onStart() {
        super.onStart();
        Log.i("LOG:", "ESTOY EN START");

        if (baseDatosSoaRef != null) {
            //APENAS INICIO LA APP HAGO QUE LA BBDD ESTE EN ESCUCHA ACTIVA
            baseDatosSoaRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //SI SE PRODUCE ALGUN CAMBIO EN LA RUTA ESPECIFICADA CUANDO INSTANCIE EN LA BBDD
                    try {
                        String estadoLed = dataSnapshot.child("Led").child("estado").getValue().toString();
                        String estadoPresencia = dataSnapshot.child("Presencia").child("estado").getValue().toString();
                        Integer anguloServo = Integer.parseInt(dataSnapshot.child("Servo").child("angulo").getValue().toString());
                        Integer esfuerzoServo = Integer.parseInt(dataSnapshot.child("Servo").child("esfuerzo").getValue().toString());

                        puerta.setPuerta(estadoLed, estadoPresencia, anguloServo, esfuerzoServo);
                        //SETEO LA INFORMACION DE LA PUERTA
                        tviewLed.setText(puerta.getLed().getEstado());

                        if(puerta.getServo().getAngulo().toString().equals("0")){
                            tviewPuerta.setText("Cerrada");
                        }else{
                            tviewPuerta.setText("Abierta");
                        }

                        if(puerta.getPresencia().getEstado().equals("on")){
                            tviewPresencia.setText("Si");
                        }else{
                            tviewPresencia.setText("No");
                        }

                        //SI SE ACTIVA QUE ESTA FORZANDOSE LA PUERTA, ACTIVO LA NOTIFICACION EN LA APP
                        if(puerta.getServo().getEsfuerzo() == 1){
                            tViewNotificacion.setText("PRECAUCION, PUERTA FORZADA!!!");
                            tViewNotificacion.setVisibility(View.VISIBLE);
                            btnOk.setVisibility(View.VISIBLE);
                            mp.start();
                            vibrator.vibrate(8000L);
                        }




                    }catch (NullPointerException e){
                        // error, seguramente nombre de campos incorrectos devuelven objeto NULO
                        tviewLed.setText("Error obteniendo datos BBDD");
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    //SI UNO DE LOS SENSORES TIENE UN CAMBIO SE EJECUTA ESTO Y DISCRIMINO PORQUE TIPO DE SENSOR FUE
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
            if(sensorEvent.sensor.getType()== Sensor.TYPE_ACCELEROMETER){
                if(rBtnPuerta.isChecked()){
                    accionShake(sensorEvent);
                }
            }else{
                if(sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY){
                    if(rBtnLed.isChecked()){
                        accionProximidad(sensorEvent);
                    }
                }else{
                    if(sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT){
                        if(rBtnLuminosidad.isChecked()){
                            accionLuminosidad(sensorEvent);
                        }
                    }
                }
            }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public  void accionProximidad(SensorEvent sensorEvent){
        String valProximidad = String.valueOf(sensorEvent.values[0]);
        Float valor = Float.parseFloat(valProximidad);
        tviewProx.setText(Float.toString(valor));

        if(valor == 1){
            if(puerta.getLed().getEstado().equals("on")){
                baseDatosSoaRef.child("Led").child("estado").setValue("off");
            }else {
                baseDatosSoaRef.child("Led").child("estado").setValue("on");
            }
        }
    }

    public  void accionLuminosidad(SensorEvent sensorEvent){
        String valLuminosidad = String.valueOf(sensorEvent.values[0]);
        Float valor = Float.parseFloat(valLuminosidad);
        tviewLumi.setText(Float.toString(valor));

        if(valor > 155){
            baseDatosSoaRef.child("Led").child("estado").setValue("off");
        }else{
            baseDatosSoaRef.child("Led").child("estado").setValue("on");
        }
    }

    public void accionShake(SensorEvent sensorEvent){
        long now = System.currentTimeMillis();

        if ((now - mShakeTime) > SHAKE_WAIT_TIME_MS) {
            mShakeTime = now;

            float gX = sensorEvent.values[0] / SensorManager.GRAVITY_EARTH;
            float gY = sensorEvent.values[1] / SensorManager.GRAVITY_EARTH;
            float gZ = sensorEvent.values[2] / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when there is no movement
            double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            // Change background color if gForce exceeds threshold;
            // otherwise, reset the color
            if (gForce > SHAKE_THRESHOLD) {
                Toast toastAux = Toast.makeText(getApplicationContext(), "ANTES: "+puerta.getServo().getAngulo().toString(), Toast.LENGTH_LONG);
                toastAux.show();
                Toast toast = Toast.makeText(getApplicationContext(), "DO NOT SHAKE ME", Toast.LENGTH_LONG);
                toast.show();

                if(puerta.getServo().getAngulo() == 0){
                    toast = Toast.makeText(getApplicationContext(), "PASE A 180", Toast.LENGTH_LONG);
                    toast.show();
                    baseDatosSoaRef.child("Servo").child("angulo").setValue(180);
                }
                else {
                    toast = Toast.makeText(getApplicationContext(), "PASE A 0", Toast.LENGTH_LONG);
                    toast.show();
                    baseDatosSoaRef.child("Servo").child("angulo").setValue(0);
                }
            }
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i("LOG:", "ESTOY EN RESUME");
        sm.registerListener(this, sensorProx, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, sensorAcel, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, sensorLumi, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("LOG:", "ESTOY EN PAUSE");
        sm.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("LOG:", "ESTOY EN STOP");
        sm.unregisterListener(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("LOG:", "ESTOY EN RESTART");
        sm.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("LOG:", "ESTOY EN DESTROY");
        sm.unregisterListener(this);
    }

}
