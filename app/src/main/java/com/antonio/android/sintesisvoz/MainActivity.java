package com.antonio.android.sintesisvoz;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    ChatterBotFactory factory;
    ChatterBot bot1;
    ChatterBotSession bot1Session;
    private boolean reproductor = false;
    static final int CTE_LEE = 1;
    static final int CTE_RECONOCE = 2;
    private TextToSpeech tts;
    private EditText et;
    private String conversacion;
    private String respuesta;
    private SeekBar seekBarV;
    private SeekBar seekBarT;
    private int velocidad;
    private int tono;
    private RadioButton esp;
    private RadioButton eng;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, CTE_LEE);
        factory = new ChatterBotFactory();
        velocidad=1;
        tono=1;
        conversacion="";
        try {
            bot1 = factory.create(ChatterBotType.CLEVERBOT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bot1Session = bot1.createSession();
        et = (EditText)findViewById(R.id.editText);
        et.setHint(R.string.etHint);
        esp=(RadioButton)findViewById(R.id.rbEsp);
        eng=(RadioButton)findViewById(R.id.rbEng);
        tv=(TextView)findViewById(R.id.textView);

        seekBarV = (SeekBar) findViewById(R.id.seekBarV);
        seekBarT = (SeekBar) findViewById(R.id.seekBarT);
        seekBarV.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 1;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(progress>=1){
                    velocidad=progress ;

                }else{
                    velocidad=1;
                }

            }
        });
        seekBarT.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 1;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(progress>=1){
                    tono=progress ;

                }else{
                    tono=1;
                }
            }
        });
    }
    class HiloFacil extends AsyncTask<Object, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(et.getText().toString().isEmpty()){
                tostada("escriba o dicte ");
                cancel(true);
            }else {
                String s= et.getText().toString();
                Calendar cal = new GregorianCalendar();
                Date date = cal.getTime();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String fecha = df.format(date);
                conversacion=s+System.getProperty("line.separator")+fecha+System.getProperty("line.separator");
                tv.append(conversacion);
                Button bt = (Button) findViewById(R.id.btConversar);
                bt.setVisibility(View.INVISIBLE);
                Button bt1 = (Button) findViewById(R.id.btDictar);
                bt1.setVisibility(View.INVISIBLE);
                tts.setSpeechRate(velocidad);//Velocidad
                tts.setPitch(tono);//Tono
                if (esp.isChecked()) {
                    tts.setLanguage(Locale.getDefault());//Idioma
                } else {
                    tts.setLanguage(Locale.ENGLISH);//Idioma
                }
            }
        }
        @Override
        protected String doInBackground(Object[] params) {
            respuesta="";
            try {
                String s= et.getText().toString();
                respuesta=bot1Session.think(s);
            } catch (Exception ex) {
                System.out.println("error al contestar "+ex);
            }

            if(respuesta.isEmpty()){
                respuesta= String.valueOf(R.string.vacio);
            }
            if(reproductor==true){
                if(!tts.isSpeaking()) {
                    tts.speak(respuesta, TextToSpeech.QUEUE_FLUSH, null);

                }
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            et.setText("");
            et.setHint(R.string.etHint);
            Button bt=(Button)findViewById(R.id.btConversar);
            bt.setVisibility(View.VISIBLE);
            Button bt1=(Button)findViewById(R.id.btDictar);
            bt1.setVisibility(View.VISIBLE);
            Calendar cal = new GregorianCalendar();
            Date date = cal.getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String fecha = df.format(date);
            conversacion="Respuesta "+System.getProperty("line.separator")+respuesta+System.getProperty("line.separator")+fecha+System.getProperty("line.separator");
            tv.append(conversacion);

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
        }
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            reproductor = true;
            //tts.setPitch(tono);//Tono
           // tts.setSpeechRate(velocidad);//Velocidad
            tts.isSpeaking();// Para saber si esta hablando, mientras esta hablando no no hacer nada.
        } else {
            //no se puede reproducir
        }
        Log.v("¿Funciona?", reproductor + "");

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CTE_LEE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);//contexto y listener
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        }
        if (requestCode == CTE_RECONOCE && resultCode == RESULT_OK) {
            ArrayList<String> textos = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            et.setText(textos.get(0));
            for (int i = 0; i <textos.size(); i++) {
                Log.v("textos:",textos.get(i));
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    public void hablar(View v) {
        HiloFacil hf=new HiloFacil();
        hf.execute();
    }
    public void escribir(View v){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES");
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,R.string.habla);
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,3000);
        startActivityForResult(i, CTE_RECONOCE);
    }
    public Toast tostada(String s) {
        Toast toast =
                Toast.makeText(getApplicationContext(),
                        s + "", Toast.LENGTH_SHORT);
        toast.show();
        return toast;
    }

}
