package com.guajardo.inventarios;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // -- Muestra la conexion de la red
    private TextView etconec;
    // -- usuario y clave
    private EditText usuario, clave;
    private Button iniSesion;

    // -- Conexiones a SQL
    String servidor;
    String DB;
    String user;
    String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // -- Titulo de la aplicacion --
        setTitle("INVENTARIOS");

        // -- Enlace de variables --
        etconec = (TextView) findViewById(R.id.etconec);
        usuario = (EditText) findViewById(R.id.usuario);
        clave = (EditText) findViewById(R.id.clave);
        iniSesion = (Button) findViewById(R.id.iniSesion);

        iniSesion.setOnClickListener(v -> {
            iniSesion.setEnabled(false);
            Thread thread = new Thread(() -> {
                try {
                    ValidaUsuario();
                } catch (Exception e) {
                    iniSesion.setEnabled(true);
                    savetoLog("Iniciar sesion | " + e.toString());
                }
            });
            thread.start();
        });

        // -- Trae la informacion grabada de las preferencias --
        // -- para las conexiones a SQL                       --
        SharedPreferences prefeX1 = getSharedPreferences("conf_red", Context.MODE_PRIVATE);
        servidor = prefeX1.getString("server", "");
        user = prefeX1.getString("user", "");
        pass = prefeX1.getString("password", "");
        DB = prefeX1.getString("database", "");

        usuario.requestFocus();
        iniSesion.setBackgroundColor(Color.WHITE);
        // -- La accion despues del Enter
        clave.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                ocultarkey();
                //-- Termina
                return true;
            }
            return false;
        });

        // End OnCreate
    }

    // -- Valida la conexion a WIFI --
    private final BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = manager.getActiveNetworkInfo();
            onNetworkChange(ni);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        unregisterReceiver(networkStateReceiver);
        super.onPause();
    }

    // -- Encuentra conexion a Wifi --
    private void onNetworkChange(NetworkInfo networkInfo) {
        if (networkInfo != null) {
            if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                String network = networkInfo.getExtraInfo();
                etconec.setText(getString(R.string.RedConectada, network));
                etconec.setTextColor(Color.parseColor("#186A3B"));
            }
        } else {
            etconec.setText(R.string.RedDesconectada);
            savetoLog("Red Desconectada");
            etconec.setTextColor(Color.parseColor("#E74C3C"));
        }
    }

    // -- Cancelar boton retroceso --
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //Acción
            return true;
        }
        return true;
    }

    // -- Valida que el usuario y la clave exista
    public void ValidaUsuario() {
        // -- Valida que campos esten llenos
        if (usuario.getText().toString().trim().equalsIgnoreCase("")) {
            runOnUiThread(() -> {
                iniSesion.setEnabled(true);
                Toast.makeText(MainActivity.this, "Coloque el usuario", Toast.LENGTH_SHORT).show();
            });
            return;
        } else if (clave.getText().toString().trim().equalsIgnoreCase("")) {
            runOnUiThread(() -> {
                iniSesion.setEnabled(true);
                Toast.makeText(MainActivity.this, "Coloque la clave", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        // -- Verifica que sea administrador
        if (usuario.getText().toString().trim().equalsIgnoreCase("admin") && clave.getText().toString().trim().equalsIgnoreCase("SIS01gojv")) {
            runOnUiThread(() -> {
                usuario.setText("");
                clave.setText("");
                iniSesion.setEnabled(true);
            });
            // -- Formulario configuracion
            //Intent i = new Intent(this, Main5Activity.class);
            Intent i = new Intent(this, MainActivity5.class);
            startActivity(i);
            return;
        }


        // -- Sin conexion a la red
        String etconecstr = etconec.getText().toString();
        if (etconecstr.trim().equalsIgnoreCase("Red desconectada") || etconecstr.trim().equalsIgnoreCase("RED")) {

            runOnUiThread(() -> {
                Toast.makeText(this, "Verifica que el equipo este conectado a la red", Toast.LENGTH_SHORT).show();
                iniSesion.setEnabled(true);
            });
            return;
        }

        // -- Consulta, Busca en SQL
        String usr = usuario.getText().toString();
        String cve = clave.getText().toString();
        String SQL = "select nombre_lar, nom_cto from tcausr where nombre = '" + usr + "' And pwd = '" + cve + "'";

        try {
            ResultSet rs = queryDT(SQL);
            if (rs != null) {
                if (rs.next()) {
                    // -- Formulario contador o auditor
                    Intent i = new Intent(this, MainActivity6.class);
                    // -- Resultado de la consulta
                    String nombres = rs.getString("nombre_lar");
                    String nomcorto = rs.getString("nom_cto");
                    // -- Guarda preferencias para la siguiente pagina
                    SharedPreferences preferencias = getSharedPreferences("conf_usr", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferencias.edit();
                    editor.putString("namess", nombres);
                    editor.putString("namecut", nomcorto);
                    editor.apply();
                    startActivity(i);
                    usuario.setText("");
                    clave.setText("");
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show());
                    savetoLog("Usuario no encontrado | " + usr);
                }
            } else {
                runOnUiThread(() -> {
                    iniSesion.setEnabled(true);
                });
            }

        } catch (SQLException e) {
            runOnUiThread(() -> {
                iniSesion.setEnabled(true);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            });

            savetoLog("ValidaUsuario | SQLException |" + e.toString());
        }
    }

    private void ocultarkey() {
        //Lineas para ocultar el teclado virtual (Hide keyboard)
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(clave.getWindowToken(), 0);
    }

    // End Activity


    //SE AGREGA METODO PARA CREAR EL LOG DE LA APLICACION
    public void savetoLog(String v) {
        Date fechahoy = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
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


    public ResultSet queryDT(String query) {
        Connection con;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            String ConnURL = "jdbc:jtds:sqlserver://" + servidor + ";databaseName=" + DB + ";user=" + user + ";password=" + pass + "";
            con = DriverManager.getConnection(ConnURL);
            Statement st = con.createStatement();
            st.setQueryTimeout(10);
            ResultSet rs = st.executeQuery(query);
            runOnUiThread(() -> iniSesion.setEnabled(true));
            return rs;
        } catch (SQLException s) {
            savetoLog("queryDT | " + s.toString());
            runOnUiThread(() -> iniSesion.setEnabled(true));
            runOnUiThread(() -> msgError(s.toString()));
            return null;
        } catch (Exception e) {
            savetoLog("queryDT | " + e.toString());
            runOnUiThread(() -> msgError(e.toString()));
            runOnUiThread(() -> iniSesion.setEnabled(true));
            return null;
        }
    }

    private void msgError(String query) {
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
        dialogo1.setTitle("Error de red");
        dialogo1.setMessage(query);
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Ok", (dialogo11, id) -> {
        });
        dialogo1.show();
        iniSesion.setEnabled(true);
    }


}