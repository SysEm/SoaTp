package com.soa.javier.soatp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
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
    //region VARIABLES
    //region SENSORES
    SensorManager sm;
    Sensor sensorProx;
    Sensor sensorAcel;
    Sensor sensorLumi;
    //endregion
    //region VIEWS
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
    //endregion
    //region AUXILIARES
    Puerta PuertaLecAppEscRas = new Puerta();
    Puerta PuertaEscAppLecRas = new Puerta();


    String estadoPeticionLed;
    TextView tviewProx;
    TextView tviewLumi;
    private static final float SHAKE_THRESHOLD = 2.1f;
    private static final int SHAKE_WAIT_TIME_MS = 500;
    private long mShakeTime = 0;
    Vibrator vibrator;
    MediaPlayer mp;
    //endregion
    //region BASE DE DATOS
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference baseDatosSoaRef = database.getReference("baseDatosSoa");
    //endregion
    //endregion

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
        activarSensores();

        //ACTIVO-DESACTIVO EL MENU DE FUNCIONALIDADES
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

        //BOTON PARA CANCELAR LA NOTIFICACION DE PUERTA FORZADA
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

        //VARIABLES AUXILIARES. EL VIBRATOR NECESITO DECLARAR EL PERMISO EN EL MANIFEST YA QUE CONTROLA HARDWARE
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
                        estadoPeticionLed = dataSnapshot.child("PuertaEscAppLecRas").child("Led").child("estado").getValue().toString();

                        String estadoLed = dataSnapshot.child("PuertaLecAppEscRas").child("Led").child("estado").getValue().toString();
                        String estadoPresencia = dataSnapshot.child("PuertaLecAppEscRas").child("Presencia").child("estado").getValue().toString();
                        Integer anguloServo = Integer.parseInt(dataSnapshot.child("PuertaLecAppEscRas").child("Servo").child("angulo").getValue().toString());
                        Integer esfuerzoServo = Integer.parseInt(dataSnapshot.child("PuertaLecAppEscRas").child("Servo").child("esfuerzo").getValue().toString());

                        PuertaLecAppEscRas.setPuerta(estadoLed, estadoPresencia, anguloServo, esfuerzoServo);
                        //SETEO LA INFORMACION DE LA PUERTA
                        if(PuertaLecAppEscRas.getLed().getEstado().equals("on")){
                            tviewLed.setText("Encendida");
                        }else{
                            tviewLed.setText("Apagada");
                        }

                        if(PuertaLecAppEscRas.getServo().getAngulo().toString().equals("0")){
                            tviewPuerta.setText("Cerrada");
                        }else{
                            tviewPuerta.setText("Abierta");
                        }

                        if(PuertaLecAppEscRas.getPresencia().getEstado().equals("on")){
                            tviewPresencia.setText("Si");
                        }else{
                            tviewPresencia.setText("No");
                        }

                        //SI SE ACTIVA QUE ESTA FORZANDOSE LA PUERTA, ACTIVO LA NOTIFICACION EN LA APP
                        if(PuertaLecAppEscRas.getServo().getEsfuerzo() == 1){
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

    //region FUNCIONES DE CAMBIO DE ESTADO DE SENSORES
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
    //endregion

    //region FUNCIONES PARA LOS SENSORES
    public  void accionProximidad(SensorEvent sensorEvent){
        String valProximidad = String.valueOf(sensorEvent.values[0]);
        Float valor = Float.parseFloat(valProximidad);
        tviewProx.setText(Float.toString(valor));

        //VALOR ES LO QUE ME DA EL SENSOR ES EL DE PROXIMIDAD
        if(valor == 1){
            sm.unregisterListener(this);
            if(PuertaLecAppEscRas.getLed().getEstado().equals("on")){
                Toast toast = Toast.makeText(getApplicationContext(), "TRATANDO DE APAGAR LA LUZ", Toast.LENGTH_LONG);
                toast.show();
                baseDatosSoaRef.child("PuertaEscAppLecRas").child("Led").child("estado").setValue("off");
                PuertaEscAppLecRas.setLed("off");
            }else {
                Toast toast = Toast.makeText(getApplicationContext(), "TRATANDO DE ENCENDER LA LUZ", Toast.LENGTH_LONG);
                toast.show();
                baseDatosSoaRef.child("PuertaEscAppLecRas").child("Led").child("estado").setValue("on");
                PuertaEscAppLecRas.setLed("on");
            }
            TareaLed tareaLed = new TareaLed();
            tareaLed.execute();
        }
    }

    public  void accionLuminosidad(SensorEvent sensorEvent){
        String valLuminosidad = String.valueOf(sensorEvent.values[0]);
        Float valor = Float.parseFloat(valLuminosidad);
        tviewLumi.setText(Float.toString(valor));

        if(valor > 150){
            if(PuertaLecAppEscRas.getLed().getEstado().equals("on")){
                sm.unregisterListener(this);
                Toast toast = Toast.makeText(getApplicationContext(), "TRATANDO DE APAGAR LA LUZ", Toast.LENGTH_LONG);
                toast.show();
                baseDatosSoaRef.child("PuertaEscAppLecRas").child("Led").child("estado").setValue("off");
                PuertaEscAppLecRas.setLed("off");
                TareaLed tareaLed = new TareaLed();
                tareaLed.execute();
            }

        }else{
            if(PuertaLecAppEscRas.getLed().getEstado().equals("off")){
                sm.unregisterListener(this);
                Toast toast = Toast.makeText(getApplicationContext(), "TRATANDO DE ENCENDER LA LUZ", Toast.LENGTH_LONG);
                toast.show();
                baseDatosSoaRef.child("PuertaEscAppLecRas").child("Led").child("estado").setValue("on");
                PuertaEscAppLecRas.setLed("on");
                TareaLed tareaLed = new TareaLed();
                tareaLed.execute();
            }

        }
    }

    public void accionShake(SensorEvent sensorEvent){
        long now = System.currentTimeMillis();

        if ((now - mShakeTime) > SHAKE_WAIT_TIME_MS) {
            mShakeTime = now;

            float gX = sensorEvent.values[0] / SensorManager.GRAVITY_EARTH;
            float gY = sensorEvent.values[1] / SensorManager.GRAVITY_EARTH;
            float gZ = sensorEvent.values[2] / SensorManager.GRAVITY_EARTH;

            double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > SHAKE_THRESHOLD) {
                sm.unregisterListener(this);
                Toast toast = Toast.makeText(getApplicationContext(), "SHAKE DETECTADO", Toast.LENGTH_SHORT);
                toast.show();

                if(PuertaLecAppEscRas.getServo().getAngulo() == 0){
                    toast = Toast.makeText(getApplicationContext(), "TRATANDO DE ABRIR", Toast.LENGTH_LONG);
                    toast.show();
                    baseDatosSoaRef.child("PuertaEscAppLecRas").child("Servo").child("angulo").setValue(1);
                    PuertaEscAppLecRas.setServo(1,0);
                }
                else {
                    toast = Toast.makeText(getApplicationContext(), "TRATANDO DE CERRAR", Toast.LENGTH_LONG);
                    toast.show();
                    baseDatosSoaRef.child("PuertaEscAppLecRas").child("Servo").child("angulo").setValue(0);
                    PuertaEscAppLecRas.setServo(0,0);
                }
                TareaServo tareaServo = new TareaServo();
                tareaServo.execute();
            }
        }

    }
    //endregion

    //region OVERRIDE DE LOS ESTADOS DE LA APP
    @Override
    protected void onResume() {
        super.onResume();
        Log.i("LOG:", "ESTOY EN RESUME");
        activarSensores();
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
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("LOG:", "ESTOY EN RESTART");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("LOG:", "ESTOY EN DESTROY");
        sm.unregisterListener(this);
    }
    //endregion

    public void activarSensores(){
        sm.registerListener(this, sensorProx, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, sensorAcel, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, sensorLumi, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private class TareaLed extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.i("ASYNC","Inicie");
            Long despues = System.currentTimeMillis()+5000;
            while(System.currentTimeMillis()<=despues){

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(PuertaEscAppLecRas.getLed().getEstado().equals(PuertaLecAppEscRas.getLed().getEstado())){
                Toast toast = Toast.makeText(MainActivity.this.getBaseContext(),"Accion ejecutada exitosamente",Toast.LENGTH_SHORT);
                toast.show();
            }else{
                Toast toast = Toast.makeText(MainActivity.this.getBaseContext(),"Accion no ejecutada",Toast.LENGTH_SHORT);
                toast.show();
                baseDatosSoaRef.child("PuertaEscAppLecRas").child("Led").child("estado").setValue(PuertaLecAppEscRas.getLed().getEstado());
            }
            activarSensores();
            Log.i("ASYNC","Finalice");

        }
    }

    private class TareaServo extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.i("ASYNC","Inicie");
            Long despues = System.currentTimeMillis()+5000;
            while(System.currentTimeMillis()<=despues){

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(PuertaEscAppLecRas.getServo().getAngulo()==PuertaLecAppEscRas.getServo().getAngulo()){
                Toast toast = Toast.makeText(MainActivity.this.getBaseContext(),"Accion ejecutada exitosamente",Toast.LENGTH_SHORT);
                toast.show();
            }else{
                Toast toast = Toast.makeText(MainActivity.this.getBaseContext(),"Accion no ejecutada",Toast.LENGTH_SHORT);
                toast.show();
                baseDatosSoaRef.child("PuertaEscAppLecRas").child("Servo").child("angulo").setValue(PuertaLecAppEscRas.getServo().getAngulo());
            }
            activarSensores();
            Log.i("ASYNC","Finalice");
            TareaServo.this.cancel(true);
            return;
        }
    }
}
