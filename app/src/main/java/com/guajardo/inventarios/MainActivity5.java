package com.guajardo.inventarios;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity5 extends AppCompatActivity {

    // DECLARA VARIABLES
    private EditText evservidor;
    private EditText evusuario;
    private EditText evclave;
    private EditText evbasedatos;

    private EditText limPie, limCaj;


    // -- EC Line --
    private Context thisCon=null;
    private PublicAction PAct=null;

    private RadioButton ciclicos,anuales;
    String clianutmp="0";

    String tipodetomainv="0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);

        //-- Titulo de la aplicacion
        setTitle("Configuración");




        // -- Enlazar variables con objetos definidos en el archivo XML
        evservidor = findViewById(R.id.evservidor);
        evusuario = findViewById(R.id.evusuario);
        evclave = findViewById(R.id.evclave);
        evbasedatos = findViewById(R.id.evbasedatos);

        limPie = findViewById(R.id.txtLimPiezas);
        limCaj = findViewById(R.id.txtLimCajas);

        //-- Trae la informacion grabada de las preferencias SQL
        SharedPreferences prefeX1 = getSharedPreferences("conf_red", Context.MODE_PRIVATE);
        evservidor.setText(prefeX1.getString("server",""));
        evusuario.setText(prefeX1.getString("user",""));
        evclave.setText(prefeX1.getString("password",""));
        evbasedatos.setText(prefeX1.getString("database",""));

        SharedPreferences limites = getSharedPreferences("limites", Context.MODE_PRIVATE);
        limPie.setText(limites.getString("limPiezas",""));
        limCaj.setText(limites.getString("limCajas",""));


        ciclicos= findViewById(R.id.ciclicos);
        anuales= findViewById(R.id.anuales);
        clianutmp="";


        // ***************************************
        SharedPreferences prefeX11 = getSharedPreferences("conf_tipoinv", Context.MODE_PRIVATE);
        tipodetomainv=prefeX11.getString("conftipoinv","");

        if (tipodetomainv.equals("1")){
            ciclicos.setChecked(true);
            anuales.setChecked(false);
            clianutmp = "1";
        }else if (tipodetomainv.equals("2")){
            ciclicos.setChecked(false);
            anuales.setChecked(true);
            clianutmp = "2";
        }else if(tipodetomainv.equals(""))
        {
            ciclicos.setChecked(false);
            anuales.setChecked(false);
        }
        evservidor.requestFocus();
        // end onCreate
    }


    //-- Guardar configuraciones
    public void ejecutar(View v){
        if(!ciclicos.isChecked() && !anuales.isChecked())
        {
            Toast.makeText(thisCon, "Favor de elegir un tipo de inventario", Toast.LENGTH_SHORT).show();
        }
        else
        {
            //-- Guarda la configuracion a RED
            SharedPreferences preferencias=getSharedPreferences("conf_red",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=preferencias.edit();
            editor.putString("server",evservidor.getText().toString());
            editor.putString("user",evusuario.getText().toString());
            editor.putString("password",evclave.getText().toString());
            editor.putString("database",evbasedatos.getText().toString());
            editor.apply();

            tipoinventario();
            guardarlimites();
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            //finish();;
        }
    }


    // -- tipo de captura
    private void tipoinventario(){
        SharedPreferences preferenciasx=getSharedPreferences("conf_tipoinv",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferenciasx.edit();
        editor.putString("conftipoinv",clianutmp);
        editor.apply();
    }

    private void guardarlimites(){
        SharedPreferences pref = getSharedPreferences("limites",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= pref.edit();
        editor.putString("limPiezas", limPie.getText().toString());
        editor.putString("limCajas", limCaj.getText().toString());
        editor.apply();
    }



    //-- Cancelar boton retroceso
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //Acción
        }
        return true;
    }

    public void  ciclicoss(View view){
        ciclicos.setChecked(true);
        clianutmp="1";
        anuales.setChecked(false);
    }
    public void  anualess(View view){
        anuales.setChecked(true);
        clianutmp="2";
        ciclicos.setChecked(false);
    }

    //SE AGREGA METODO PARA CREAR EL LOG DE LA APLICACION
    public void savetoLog(String v) {
        Date fechahoy = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");
        String fechahoyre = formatter.format(fechahoy);
        String text = fechahoyre + " | " + v + "|\n";

        File output = new File(getApplicationContext().getExternalFilesDir(null),"logAPP.txt");
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(output.getAbsolutePath(), true );
            fos.write(text.getBytes());
        }catch(FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fos != null){
                try{
                    fos.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    // End Activity
}