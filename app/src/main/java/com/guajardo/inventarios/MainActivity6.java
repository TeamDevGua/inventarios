package com.guajardo.inventarios;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class      MainActivity6 extends AppCompatActivity {

    String ip, db, un, passwords;
    Connection connect;
    PreparedStatement stmt;
    ResultSet rs;

    TextView txtusrname, txtusrnamecorto;
    private RadioButton rbcontar, rbauditar;
    TextView textView2;

    String rbcontartemp, rbauditartemp;
    CheckBox impr;
    String imprestemp;

    //-- Determina si el contador fue registrado desde el dashborad
    String tipodetomainv = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);

        // -- Titulo de la aplicacion --
        setTitle("OPCION CAPTURA O AUDITORIA");

        txtusrname = findViewById(R.id.txtusrname);
        txtusrnamecorto = findViewById(R.id.txtusrnamecorto);
        textView2 = findViewById(R.id.textView2);

        rbcontar = findViewById(R.id.rbcontar);
        rbauditar = findViewById(R.id.rbauditar);

        // -- Trae el usuario --
        SharedPreferences prefe = getSharedPreferences("conf_usr", Context.MODE_PRIVATE);
        txtusrname.setText(prefe.getString("namess", ""));//= prefe.getString("server","");
        txtusrnamecorto.setText(prefe.getString("namecut", ""));//= prefe.getString("server","");
        //evusrcrt.setText(prefe.getString("namecut",""));//= prefe.getString("server","");

        impr = findViewById(R.id.checkBox);

        //-- Trae la informacion grabada de las preferencias
        SharedPreferences pref = getSharedPreferences("conf_red", Context.MODE_PRIVATE);
        ip = pref.getString("server", "");
        db = pref.getString("database", "");
        un = pref.getString("user", "");
        passwords = pref.getString("password", "");
        connect = CONN(un, passwords, db, ip);


        SharedPreferences prefx1 = getSharedPreferences("conf_tipoinv", Context.MODE_PRIVATE);
        tipodetomainv = prefx1.getString("conftipoinv", "");
        // -- Toma de ciclicos -- Revisado
        if (tipodetomainv.equalsIgnoreCase("1")) {
            rbcontar.setChecked(true);
            rbcontar.setEnabled(false);
            rbauditar.setEnabled(false);
            textView2.setText(R.string.ModoCiclico);
        }
        // -- Toma anual -- Revisado
        if (tipodetomainv.equalsIgnoreCase("2")) {
            //-- Busca si el estado esta abierto como CONTADOR o AUDITOR
            String user = txtusrnamecorto.getText().toString();
            TipoUser us = obtenerTipoUsuario(user);
            if (us.capturista.equals("1") && us.auditor.equals("0")) {
                //Solo tiene areas de capturista
                rbcontar.setChecked(true);
                rbcontar.setEnabled(false);
                rbauditar.setEnabled(false);
                textView2.setText(R.string.ModoAnual);
            } else if (us.capturista.equals("0") && us.auditor.equals("1")) {
                //Solo tiene areas de auditor
                rbauditar.setChecked(true);
                rbauditar.setEnabled(false);
                rbcontar.setChecked(false);
                rbcontar.setEnabled(false);
                textView2.setText(R.string.ModoAuditor);
            } else if (us.capturista.equals("1") && us.auditor.equals("1")) {
                //Tiene areas de capturista y areas de auditor
                rbcontar.setEnabled(true);
                rbcontar.setChecked(false);
                rbauditar.setEnabled(true);
                rbauditar.setChecked(false);
                Alerta();
            } else if (us.capturista.equals("0") && us.auditor.equals("0")) {
                //No tiene areas disponibles
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle("No cuentas con areas disponibles");
                builder.setMessage("Te regresaremos al inicio de sesion");
                builder.setPositiveButton("OK",
                        (dialog, which) -> {
                            Intent login = new Intent(this, MainActivity.class);
                            startActivity(login);
                            finish();
                        });
                builder.show();
            }
        }
    }


    private static class TipoUser {
        public String capturista, auditor;
    }

    public TipoUser obtenerTipoUsuario(String user) {
        TipoUser tu = new TipoUser();
//        String query = "DECLARE @Capturista varchar(1) = '0', @Auditor varchar(1) = '0' " +
//                "IF EXISTS (Select estado From tblinventario_tic Where capturo = '" + user + "' and estado = '1' and depto <> 'noa') " +
//                "BEGIN SET @Capturista = '1' END " +
//                "IF EXISTS (select audasig from tblinventario_tic where audito = '" + user + "' and audasig = '2') " +
//                "BEGIN SET @Auditor = '1' END " +
//                "SELECT @Capturista AS [Capturista], @Auditor AS [Auditor] ";
        String query = "DECLARE @Capturista varchar(1) = '0', @Auditor varchar(1) = '0' " +
                "IF EXISTS (Select estado From tblinventario_tic Where capturo = '" + user + "' and estado = '1' and depto <> 'noa' and ninventario in (select codigo from invmar)) " +
                "BEGIN SET @Capturista = '1' END " +
                "IF EXISTS (select audasig from tblinventario_tic where audito = '" + user + "' and audasig = '2' and ninventario in (select codigo from invmar)) " +
                "BEGIN SET @Auditor = '1' END " +
                "SELECT @Capturista AS [Capturista], @Auditor AS [Auditor] ";


        try {
            connect = CONN(un, passwords, db, ip);
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                tu.capturista = rs.getString("Capturista");
                tu.auditor = rs.getString("Auditor");
            }
            return tu;
        } catch (Exception ex) {
            savetoLog("obtenerTipoUsuario | query: " + query + " | " + ex.toString());
            return null;
        }
    }


    public void Siguientepag(View view) {
        if (!rbcontar.isChecked() && !rbauditar.isChecked()) {
            Toast.makeText(MainActivity6.this, "Seleccione una opcion", Toast.LENGTH_SHORT).show();
            return;
        }
        rbcontartemp = "false";
        rbauditartemp = "false";
        imprestemp = "false";
        if (impr.isChecked()) {
            imprestemp = "true";
            // -- grabar configuracion de impresion
        }
        if (!impr.isChecked()) {
            imprestemp = "false";
            // -- grabar configuracion de impresion
        }
        if (rbcontar.isChecked()) {
            // -- Guarda preferencias para la siguiente pagina
            SharedPreferences preferencias = getSharedPreferences("conf_impres", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferencias.edit();
            editor.putString("sino", imprestemp);
            editor.apply();
            rbcontartemp = "true";
            // -- va a la pagina seleccionar codigo de inventario
            Intent i = new Intent(this, MainActivity2.class);
            startActivity(i);
            rbcontar.setChecked(false);
            rbauditar.setChecked(false);
        } else if (rbauditar.isChecked()) {
            // -- Guarda preferencias para la siguiente pagina
            SharedPreferences preferencias = getSharedPreferences("conf_impres", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferencias.edit();
            editor.putString("sino", imprestemp);
            editor.apply();
            rbauditartemp = "true";
            // -- va a la pagina seleccionar codigo de inventario
            Intent i = new Intent(this, MainActivity7.class);
            startActivity(i);
            rbcontar.setChecked(false);
            rbauditar.setChecked(false);
        }
    }

    // -- selecion de botones
    public void contarfalse(View view) {
        rbauditar.setChecked(false);
        rbcontar.setChecked(true);

    }

    public void auditarfalse(View view) {
        rbcontar.setChecked(false);
        rbauditar.setChecked(true);
    }

    //-- Cancelar boton retroceso
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            return true;
        }
        return true;
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
        String ConnURL;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnURL = "jdbc:jtds:sqlserver://" + _server + ";"
                    + "databaseName=" + _DB + ";user=" + _user + ";password="
                    + _pass + ";";
            conn = DriverManager.getConnection(ConnURL);
        } catch (Exception se) {
            savetoLog("CONN " + se.toString());
        }
        return conn;
    }

    public void Alerta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Usuario es capturista y auditor:");
        builder.setMessage("seleccione la acción a realizar");
        builder.setPositiveButton("OK",
                (dialog, which) -> {
                });
        builder.setCancelable(false);
        builder.show();
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