package com.guajardo.inventarios;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Spinner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;

public class MainActivity2 extends AppCompatActivity {

    // -- usuario --
    private TextView etusrname;
    private TextView etusrcode;
    // -- codigo de inventario --
    private Spinner evcodinv;
    private TextView evalm, evalmacen;
    //-- conexiones SQL --
    String ip, db, un, passwords;
    Connection connect;
    PreparedStatement stmt;
    ResultSet rs;
    Integer msges;

    String tipodetomainv = "0";

    //-- Determina si el contador fue registrado desde el dashborad --
    String dasbprogcontador = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        // -- Titulo de la aplicacion
        setTitle("COD. DE INVENTARIO");

        etusrname = (TextView) findViewById(R.id.txtusrname);
        etusrcode = (TextView) findViewById(R.id.etusrcodess);

        evcodinv = (Spinner) findViewById(R.id.evcodinv);
        evalm = (TextView) findViewById(R.id.evalm);
        evalmacen = (TextView) findViewById(R.id.evalmacen);

        // -- Codigo de inventario --
        SharedPreferences prefe = getSharedPreferences("conf_usr", Context.MODE_PRIVATE);
        etusrname.setText(prefe.getString("namess", ""));//= prefe.getString("server","");
        etusrcode.setText(prefe.getString("namecut", ""));//= prefe.getString("server","");

        //-- Trae la informacion grabada de las preferencias
        SharedPreferences pref = getSharedPreferences("conf_red", Context.MODE_PRIVATE);
        ip = pref.getString("server", "");
        un = pref.getString("user", "");
        passwords = pref.getString("password", "");
        db = pref.getString("database", "");
        connect = CONN(un, passwords, db, ip);

        // -- Trae la prefencia del tipo de inventario --
        SharedPreferences prefx1 = getSharedPreferences("conf_tipoinv", Context.MODE_PRIVATE);
        tipodetomainv = prefx1.getString("conftipoinv", "");

        // -- Toma de CICLICOS -- Revisado
        // -- Llena el Spinner sin bloquear
        if (tipodetomainv.equalsIgnoreCase("1")) {

            //String query = "select DISTINCT codigo from invmar";
            String query = "select DISTINCT codigo from invmar where status = '1'";

            try {
                connect = CONN(un, passwords, db, ip);
                stmt = connect.prepareStatement(query);
                rs = stmt.executeQuery();
                ArrayList<String> data = new ArrayList<String>();


                while (rs.next()) {
                    String id = rs.getString("codigo");
                    data.add(id);
                }

                //-- Creamos el ArrayAdapter que necesitará el spinner, dándole como parámetros
                //-- (Contexto, referencia al layout de elemento, valores (opciones)
                String[] array = data.toArray(new String[0]);
                ArrayAdapter NoCoreAdapter = new ArrayAdapter(this,
                        android.R.layout.simple_list_item_1, data);
                evcodinv.setAdapter(NoCoreAdapter);
            } catch (SQLException e) {
                savetoLog(e.toString());
                e.printStackTrace();
            }
        }

        try {
            // -- Toma ANUAL -- Revisado
            if (tipodetomainv.equalsIgnoreCase("2")) {
                Buscarcodinv();
                if (dasbprogcontador == "1") {
                    String capturotemp = etusrcode.getText().toString();
                    //String query = "select ninventario from tblinventario_tic where capturo='" + capturotemp + "' and estado='1'";
                    String query = "select ninventario from tblinventario_tic where capturo='" + capturotemp + "' and estado='1' and ninventario in (select codigo from invmar)";
                    try {
                        connect = CONN(un, passwords, db, ip);
                        stmt = connect.prepareStatement(query);
                        rs = stmt.executeQuery();
                        ArrayList<String> data = new ArrayList<String>();
                        //-- Llena el Spinner
                        while (rs.next()) {
                            String id = rs.getString("ninventario");
                            data.add(id);
                        }
                        // -- Creamos el ArrayAdapter que necesitará el spinner, dándole como parámetros,
                        // -- (Contexto, referencia al layout de elemento, valores (opciones)
                        String[] array = data.toArray(new String[0]);
                        ArrayAdapter NoCoreAdapter = new ArrayAdapter(this,
                                android.R.layout.simple_list_item_1, data);
                        evcodinv.setAdapter(NoCoreAdapter);

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }

        //-- indicamos al Spinner que utiliza el Adapter recien creado
        //-- Detecta la posicion del Items
        evcodinv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    msges = position;
                    if (msges == 0) {
                        if (dasbprogcontador == "0") { // -- No encuentra que sea programacion del dashboard
                            //mesajecodigoinv();
                            Buscaralmacen();
                            BuscaralmacenZ();
                        } else if (dasbprogcontador == "1") { // -- Encuentra que es programacion del dashboard
                            Buscaralmacen();
                            BuscaralmacenZ();
                            evcodinv.setEnabled(false);
                        }
                    } else ;

                    if (msges > 0) {
                        if (dasbprogcontador == "0") { // -- No encuentra que sea programacion del dashboard
                            //mesajecodigoinv();
                            Buscaralmacen();
                            BuscaralmacenZ();
                        } else if (dasbprogcontador == "1") { // -- Encuentra que es programacion del dashboard
                            Buscaralmacen();
                            BuscaralmacenZ();
                            evcodinv.setEnabled(false);
                        }
                    }
                } catch (Exception ex) {
                    savetoLog(ex.toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        // End onCreate
    }

    // -- Conectar a SQL --
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

    //-- Mensajes --
    //-- Alerta, al seleccionar el CODIDIGO DE INVENTARIO --
    private void mesajecodigoinv() {
        try {
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle(evcodinv.getSelectedItem().toString());
            dialogo1.setMessage("¿ El codigo de inventario es correcto ? " + "");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("Confirmar", (dialogo112, id) -> aceptar());
            dialogo1.setNegativeButton("Cancelar", (dialogo11, id) -> cancelar());
            dialogo1.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void aceptar() {
        try {
            Buscaralmacen();
            BuscaralmacenZ();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void cancelar() {
        limpiar1();
    }

    //-- Buscar el almacen (JUA1, GUA1, etc) --
    public void Buscaralmacen() {
        String evcodinv1 = evcodinv.getSelectedItem().toString();
        String query = "SELECT DISTINCT salm FROM  invmar WHERE codigo='" + evcodinv1 + "'";
        try {
            connect = CONN(un, passwords, db, ip);
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String salm1 = rs.getString("salm");
                evalm.setText(salm1);
            }
        } catch (SQLException e) {
            savetoLog(e.toString());
            e.printStackTrace();
        }
    }

    //-- Buscar el almacen (Z01, Z02, etc) --
    public void BuscaralmacenZ() {
        String evcodinv1 = evcodinv.getSelectedItem().toString();
        String query = "SELECT DISTINCT alm FROM  invmar WHERE codigo='" + evcodinv1 + "'";

        try {
            connect = CONN(un, passwords, db, ip);
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String alm1 = rs.getString("alm");
                evalmacen.setText(alm1);
            }
        } catch (SQLException e) {
            savetoLog(e.toString());
            e.printStackTrace();
        }
    }

    //-- Buscar el usuario si es registrado desde el dashboard
    //-- Si es si muestra el codigo de inventario
    public void Buscarcodinv() {
        dasbprogcontador = "0";
        String etusrcodesstem = etusrcode.getText().toString();

        //String query = "select ninventario from tblinventario_tic where capturo='" + etusrcodesstem + "' and estado='1' and depto<>'noa'";
        String query = "select ninventario from tblinventario_tic where capturo='" + etusrcodesstem + "' and estado='1' and depto<>'noa' and ninventario in (select codigo from invmar)";

        try {
            connect = CONN(un, passwords, db, ip);
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                dasbprogcontador = "1"; // -- Registro encontrado
            }
        } catch (SQLException e) {
            savetoLog(e.toString());
            e.printStackTrace();
        }
    }

    public void Siguientepag(View view) {

        try {
            if (evalm.getText().toString().trim().equalsIgnoreCase("---")) {
                Toast.makeText(MainActivity2.this, "Seleccione un codigo de inventario", Toast.LENGTH_SHORT).show();
                return;
            }
            if (evalmacen.getText().toString().trim().equalsIgnoreCase("---")) {
                Toast.makeText(MainActivity2.this, "Seleccione un codigo de inventario", Toast.LENGTH_SHORT).show();
                return;
            }
            //ciclico
            if(tipodetomainv.equalsIgnoreCase("1")){
                try {
                    AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
                    dialogo1.setTitle(evcodinv.getSelectedItem().toString());
                    dialogo1.setMessage("¿ El codigo de inventario es correcto ? " + "");
                    dialogo1.setCancelable(false);
                    dialogo1.setPositiveButton("Confirmar", (dialogo112, id) -> {
                        SharedPreferences preferencias = getSharedPreferences("conf_codinv", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferencias.edit();
                        editor.putString("codinvt", evcodinv.getSelectedItem().toString());
                        editor.putString("nameusr", etusrname.getText().toString());
                        editor.putString("codeusr", etusrcode.getText().toString());
                        editor.putString("alm", evalm.getText().toString());
                        editor.putString("zona", evalmacen.getText().toString());
                        editor.commit();
                        limpiar1();
                        // -- Va al formulario PIEZAS o CAJAS --
                        Intent i = new Intent(this, MainActivity3.class);
                        startActivity(i);
                    });
                    dialogo1.setNegativeButton("Cancelar", (dialogo11, id) -> {});
                    dialogo1.show();
                } catch (Exception ex) {
                    savetoLog(ex.toString());
                }
            }else if(tipodetomainv.equalsIgnoreCase("2")){ //anual
                // -- Guarda preferencias para la siguiente pagina
                SharedPreferences preferencias = getSharedPreferences("conf_codinv", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferencias.edit();
                editor.putString("codinvt", evcodinv.getSelectedItem().toString());
                editor.putString("nameusr", etusrname.getText().toString());
                editor.putString("codeusr", etusrcode.getText().toString());
                editor.putString("alm", evalm.getText().toString());
                editor.putString("zona", evalmacen.getText().toString());
                editor.commit();
                limpiar1();
                // -- Va al formulario PIEZAS o CAJAS --
                Intent i = new Intent(this, MainActivity3.class);
                startActivity(i);
            }





        } catch (Exception ex) {
            savetoLog(ex.toString());
        }

    }


    public void limpiar1() {
        try {
            evalm.setText("---");
            evalmacen.setText("---");
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    // -- Va al formulario LOGIN --
    public void Regresarpag(View view) {
        try {
            Intent ii = new Intent(this, MainActivity.class);
            startActivity(ii);
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    //-- Cancelar boton retroceso
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //Acción
        }
        return true;
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