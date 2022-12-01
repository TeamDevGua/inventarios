package com.guajardo.inventarios;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.StrictMode;
//import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.widget.RadioButton;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;
// -- SQL
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity3 extends AppCompatActivity {

    //-- conexiones SQL
    String ip, db, un, passwords;
    Connection connect;
    PreparedStatement stmt;
    ResultSet rs;

    private TextView txtusrname, txtcdinv, txtalm, txtsuc;
    private RadioButton rbcajas, rbpzas;

    //-- String
    String rbcajastemp, rbpzastemp;

    String dasbprogalm = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        // -- Titulo de la aplicacion
        setTitle("OPCION PIEZAS O CAJAS");

        txtusrname = (TextView) findViewById(R.id.txtusrname);
        txtcdinv = (TextView) findViewById(R.id.txtcdinv);
        txtalm = (TextView) findViewById(R.id.txtalm);
        txtsuc = (TextView) findViewById(R.id.txtsuc);

        rbcajas = (RadioButton) findViewById(R.id.rbcontar);
        rbpzas = (RadioButton) findViewById(R.id.rbauditar);

        // -- Trae la informacion grabada de las preferencias
        // -- Codigo de inventario
        SharedPreferences prefe = getSharedPreferences("conf_codinv", Context.MODE_PRIVATE);
        txtusrname.setText(prefe.getString("nameusr", ""));//= prefe.getString("server","");
        txtcdinv.setText(prefe.getString("codinvt", ""));//= prefe.getString("server","");
        txtalm.setText(prefe.getString("alm", ""));//= prefe.getString("server","");
        txtsuc.setText(prefe.getString("zona", ""));//= prefe.getString("server","");

        //-- Trae la informacion grabada de las preferencias
        SharedPreferences pref = getSharedPreferences("conf_red", Context.MODE_PRIVATE);
        ip = pref.getString("server", "");
        db = pref.getString("database", "");
        un = pref.getString("user", "");
        passwords = pref.getString("password", "");
        connect = CONN(un, passwords, db, ip);

        // -- Busca el tipo de almacen
        bscalmacen();
        if (dasbprogalm.equalsIgnoreCase("0")) {
            // -- No existe
            rbpzas.setChecked(true);
            rbcajas.setChecked(true);
        }

        if (dasbprogalm.equalsIgnoreCase("1")) {
            // -- Piezas
            rbpzas.setChecked(true);
            rbpzas.setEnabled(false);
            rbcajas.setEnabled(false);
        }

        if (dasbprogalm.equalsIgnoreCase("2")) {
            // -- Cajas
            rbcajas.setChecked(true);
            rbcajas.setEnabled(false);
            rbpzas.setEnabled(false);
        }


        // End onCreate
    }

    public void Siguientepag(View view) {
        try {
            if (rbcajas.isChecked() == false && rbpzas.isChecked() == false) {
                Toast.makeText(MainActivity3.this, "Seleccione una opcion", Toast.LENGTH_SHORT).show();
                return;
            }
            rbcajastemp = "false";
            rbpzastemp = "false";
            if (rbcajas.isChecked() == true) {
                rbcajastemp = "true";
            } else if (rbpzas.isChecked() == true) {
                rbpzastemp = "true";
            }
            // -- Guarda preferencias para la siguiente pagina
            SharedPreferences preferencias = getSharedPreferences("conf_cjspzs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferencias.edit();
            editor.putString("rbcajtm", rbcajastemp);
            editor.putString("rbpzstm", rbpzastemp);
            editor.commit();
            Intent i = new Intent(this, MainActivity4.class);
            startActivity(i);
            rbcajas.setChecked(false);
            rbpzas.setChecked(false);
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void cajsfalse(View view) {
        rbpzas.setChecked(true);
        rbcajas.setChecked(false);

    }

    public void pzasfalse(View view) {
        rbcajas.setChecked(true);
        rbpzas.setChecked(false);
    }

    //-- Cancelar boton retroceso
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //Acción
        }
        return true;
    }


    //-- Busca la clave corta del usuario
    public void bscalmacen() {
        dasbprogalm = "0";
        String txtalmtmp = txtalm.getText().toString();
        String query = "SELECT PC_CJ From tblinventarioalm_tic Where ALMACEN='" + txtalmtmp + "'";
        try {
            connect = CONN(un, passwords, db, ip);
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            //-- Muestra la informacion
            while (rs.next()) {
                dasbprogalm = rs.getString("PC_CJ");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // ------------------
    // -- CONEXION SQL --
    // ------------------
    @SuppressLint("NewApi")
    private Connection CONN(String _user, String _pass, String _DB,
                            String _server) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String ConnURL = null;
        try {

            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnURL = "jdbc:jtds:sqlserver://" + _server + ";"
                    + "databaseName=" + _DB + ";user=" + _user + ";password="
                    + _pass + ";";
            conn = DriverManager.getConnection(ConnURL);
        } catch (SQLException se) {
            savetoLog(se.toString());
        } catch (ClassNotFoundException e) {
            savetoLog(e.toString());
        } catch (Exception e) {
            savetoLog(e.toString());
        }
        return conn;
    }

    //SE AGREGA METODO PARA CREAR EL LOG DE LA APLICACION
    public void savetoLog(String v) {
        Date fechahoy = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");
        String fechahoyre = formatter.format(fechahoy);
        String text = fechahoyre + " | " + v + "|\n";

        File output = new File(getApplicationContext().getExternalFilesDir(null), "logAPP.txt");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(output.getAbsolutePath(), true);
            fos.write(text.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // End Activity
}