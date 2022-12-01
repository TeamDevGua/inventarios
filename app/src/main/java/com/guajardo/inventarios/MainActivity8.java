package com.guajardo.inventarios;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity8 extends AppCompatActivity {
    private TextView txtusrname, txtalm, txtcdinv, txtsuc, txtarea, usrcvecorta;
    private EditText evcodbar;
    private TextView evdesc;
    private TextView evart, evpzass, evpzascant, evprecio;

    private TextView etcajas, etpiezas;
    private EditText evcajas, evpza;

    //-- conexiones SQL
    String ip, db, un, passwords;
    Connection connect;
    PreparedStatement stmt;
    ResultSet rs;

    String captpzcajexiste;
    String limiteCantidadpzs;
    String limiteCantidadcajs;
    Integer folioBuscdinvactmax;
    String mbtsfolio;
    double resultcantidadpzscjs;

    String tipodecaptura;

    //-- convierte el valor a pesos
    NumberFormat formatoImporte = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    //-- numero cerrado
    //-- valor con decimales
    DecimalFormat df = new DecimalFormat("####0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main8);

        // -- Titulo de la aplicacion
        setTitle("CAPTURA ADICIONAL");

        // ---------------
        // -- VARIABLES --
        // ---------------
        txtusrname = (TextView) findViewById(R.id.txtusrname);
        txtalm = (TextView) findViewById(R.id.txtalm);
        txtcdinv = (TextView) findViewById(R.id.txtcdinv);
        txtsuc = (TextView) findViewById(R.id.txtsuc);
        txtarea = (TextView) findViewById(R.id.txtarea);
        usrcvecorta = (TextView) findViewById(R.id.usrcvecorta);
        evcodbar = (EditText) findViewById(R.id.evcodbar);
        evdesc = (TextView) findViewById(R.id.evdesc);
        evart = (TextView) findViewById(R.id.evart);
        evpzass = (TextView) findViewById(R.id.evpzass);
        evpzascant = (TextView) findViewById(R.id.evpzascant);
        evprecio = (TextView) findViewById(R.id.evprecio);
        etcajas = (TextView) findViewById(R.id.etcajas);
        etpiezas = (TextView) findViewById(R.id.etpiezas);
        evcajas = (EditText) findViewById(R.id.evcajas);
        evpza = (EditText) findViewById(R.id.evpza);
        // -- Mascara de 2 decimales
        evcajas.setFilters(new InputFilter[]{new MainActivity8.DecimalDigitsInputFilter(6, 3)});
        evpza.setFilters(new InputFilter[]{new MainActivity8.DecimalDigitsInputFilter(6, 3)});

        // ------------------
        // -- PREFERENCIAS --
        // ------------------
        // -- Trae el auditor logeado
        SharedPreferences prefe = getSharedPreferences("conf_usr", Context.MODE_PRIVATE);
        txtusrname.setText(prefe.getString("namess", ""));
        usrcvecorta.setText(prefe.getString("namecut", ""));
        ;

        // -- Trae la informacion grabada de las preferencias para la conexion a SQL
        SharedPreferences pref = getSharedPreferences("conf_red", Context.MODE_PRIVATE);
        ip = pref.getString("server", "");
        db = pref.getString("database", "");
        un = pref.getString("user", "");
        passwords = pref.getString("password", "");
        connect = CONN(un, passwords, db, ip);

        //Se agrega para obtener los limites de cantidades en piezas y cajas
        SharedPreferences limites = getSharedPreferences("limites", Context.MODE_PRIVATE);
        limiteCantidadpzs = limites.getString("limPiezas", "10000");
        limiteCantidadcajs = limites.getString("limCajas", "10000");


        // -- Recibe valor de otra actividad
        Bundle datos = this.getIntent().getExtras();
        String recuperamos_variable_string = datos.getString("variable_evarea");
        txtarea.setText(recuperamos_variable_string);

        String recuperamos_variable_string1 = datos.getString("variable_txtcdinv");
        txtcdinv.setText(recuperamos_variable_string1);

        String recuperamos_variable_string2 = datos.getString("variable_txtalm");
        txtalm.setText(recuperamos_variable_string2);

        String recuperamos_variable_string4 = datos.getString("variable_txtsuc");
        txtsuc.setText(recuperamos_variable_string4);


        // ------------
        // -- BUSCAR --
        // ------------
        // -- Busca el tipo de captura piezas o cajas
        bsaudasig();

        // -- Poseciona el cursor segun sea la seleccion de piezas o cajas --
        if (tipodecaptura.equals("2")) {
            evcajas.setEnabled(false);

            evcodbar.requestFocus();
            evcodbar.selectAll();

            captpzcajexiste = "2";

            evpza.setText("0.00");
            evpza.setEnabled(false);
        }

        if (tipodecaptura.equals("1")) {
            evpza.setEnabled(false);

            evcodbar.requestFocus();
            evcodbar.selectAll();

            captpzcajexiste = "1";

            evcajas.setText("0.00");
            evcajas.setEnabled(false);

        }


        // ----------------
        // -- AL TECLEAR --
        // ----------------
        //-- Al muestra la informacion automaticamente al buscar el codigo de barras
        evcodbar.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    try {
                        evdesc.setTextColor(Color.parseColor("#186A3B"));
                        //-- Detecta que el campo de buscar no este en blanco
                        if (evcodbar.getText().toString().trim().equalsIgnoreCase("")) {
                            evdesc.setText(R.string.NoEncontrado);
                            evdesc.setTextColor(Color.parseColor("#E74C3C"));
                            evcodbar.setText(null);
                            evcodbar.requestFocus();
                            evcodbar.selectAll();
                            evart.setText("---");
                            evpzass.setText("---");
                            evpzascant.setText("---");
                            evprecio.setText("--");
                            evpza.setEnabled(false);
                            evcajas.setEnabled(false);
                        } else {
                            //Obtiene el codigo de barras
                            String codigo = evcodbar.getText().toString();
                            //Otiene almacen
                            String almacen = txtsuc.getText().toString();
                            //Obtiene salm
                            String salm = txtalm.getText().toString();
                            //Ejecuta consulta
                            MainActivity4.Articulo art = ValidarArticulo(codigo, salm, almacen);
                            //Trae el porcentaje de avance y la cantidad de errores

                            if (art != null) {
                                colorestext();
                                //Set articulo
                                evart.setText(String.valueOf(art.cve));
                                //Muestra nombre del articulo
                                evdesc.setText(art.descripcion);
                                //Muestra precio
                                evprecio.setText(formatoImporte.format(Double.parseDouble(art.precio)));
                                //Guarda clave art
                                evcodbar.setText(codigo);
                                //Unidades
                                evpzass.setText(art.unidad);
                                //Empaque
                                evpzascant.setText(df.format(Double.parseDouble(art.empaque)));
                                // -- Posiciona el cursor segun sea la seleccion de piezas o cajas --
                                if (tipodecaptura.equals("2")) {
                                    evcajas.setText("");
                                    evcajas.setEnabled(true);
                                    evcajas.selectAll();
                                    evpza.setEnabled(false);
                                    evcajas.requestFocus();
                                }
                                if (tipodecaptura.equals("1")) {
                                    evpza.setText("");
                                    evpza.setEnabled(true);
                                    evpza.selectAll();
                                    evcajas.setEnabled(false);
                                    evpza.setFocusable(true);
                                    evpza.requestFocus();
                                }
                                return true;
                            } else {
                                evdesc.setText(R.string.NoEncontrado);
                                evdesc.setTextColor(Color.parseColor("#E74C3C"));
                                evcodbar.setText(null);
                                evcodbar.requestFocus();
                                evcodbar.selectAll();
                                evart.setText("---");
                                evpzass.setText("---");
                                evpzascant.setText("---");
                                evprecio.setText("--");
                                evpza.setEnabled(false);
                                evcajas.setEnabled(false);
                            }
                        }
                    } catch (Exception ex) {
                        savetoLog("evcodmbt.setOnKeyListener " + ex.toString());
                    }
                }
                return true;
            } else {
                return false;
            }
        });

        // -- Validacion de cantidades en piezas
        // -- enter automatico
        evpza.setOnKeyListener((v, keyCode, event) ->
        {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                try {
                    ocultarkeypza();
                    //-- Detecta si es Punto
                    String evpzapunto = evpza.getText().toString();
                    if (evpzapunto.equals(".")) {
                        Snackbar.make(v, "Este articulo no permite el simbolo de punto", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        evpza.setText(null);
                        evpza.requestFocus();
                        evpza.selectAll();
                        return true;
                    }

                    //-- Detecta que el campo de buscar no este en blanco
                    String evareabco = evpza.getText().toString();
                    if (evareabco.equals("")) {
                        Snackbar.make(v, "Este articulo no permite espacios en blanco", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        evpza.setText(null);
                        evpza.requestFocus();
                        evpza.selectAll();
                        return true;
                    }

                    if (evpzass.getText().toString().trim().equalsIgnoreCase("---")) {
                        Snackbar.make(v, "Falta algun campo", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        evpza.setText(null);
                        evpza.requestFocus();
                        evpza.selectAll();
                        return true;
                    }

                    //-- Detecta que el campo aplique decimal
                    String decimal = verificaempaque(evart.getText().toString());
                    String evpzadec = evpza.getText().toString();
                    int punt = evpzadec.indexOf(".");

                    if (decimal.equals("N") && punt != -1) {
                        evpza.setText(null);
                        evpza.requestFocus();
                        evpza.selectAll();
                        Snackbar.make(v, "Este articulo no permite decimales", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        return true;
                    }
                    String number3 = evpza.getText().toString();
                    String numbre5 = limiteCantidadpzs;
                    double result3 = Double.parseDouble(number3);
                    double result5 = Double.parseDouble(numbre5);
                    //-- Valida que el valor sea mayor a ceros
                    if (result3 == 0) {
                        Snackbar.make(v, "Este articulo no permite el cero", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        evpza.setText(null);
                        evpza.requestFocus();
                        evpza.selectAll();
                        return true;
                    }
                    //-- Valida que el valor no sea mayor a lo permitido
                    if (result3 > result5) {
                        Snackbar.make(v, "Este articulo permite cantidase menor a" + result5, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        evpza.setText(null);
                        evpza.requestFocus();
                        evpza.selectAll();
                        return true;
                    }
                    //-- Valida que el valor no se mayor a lo permitido
                    if (result3 <= result5) {
                        evpza.setEnabled(false);
                        evpza.clearFocus();
                        evcajas.setEnabled(false);
                        evcajas.clearFocus();
                        mesajeCantPza();
                    }
                } catch (Exception ex) {
                    savetoLog(ex.toString());
                }
                return true;
            }
            return false;
        });

        // -- Validacion de cantidades en cajas
        // -- enter automatico
        evcajas.setOnKeyListener((v, keyCode, event) ->
        {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                try {
                    ocultarkeycaja();
                    //-- Detecta si es Punto
                    String evcajaspunto = evcajas.getText().toString();
                    if (evcajaspunto.equals(".")) {
                        Snackbar.make(v, "Este articulo no permite el simbolo de punto", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        evcajas.setText(null);
                        evcajas.requestFocus();
                        evcajas.selectAll();
                        return true;
                    }

                    //-- Detecta que el campo de buscar no este en blanco
                    String evcajasbco = evcajas.getText().toString();
                    if (evcajasbco.equals("")) {
                        Snackbar.make(v, "Este articulo no permite espacios en blanco", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        evcajas.setText(null);
                        evcajas.requestFocus();
                        evcajas.selectAll();
                        return true;
                    }

                    if (evpzass.getText().toString().trim().equalsIgnoreCase("---")) {
                        Snackbar.make(v, "Falta algun campo", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        evcajas.setText(null);
                        evcajas.requestFocus();
                        evcajas.selectAll();
                        return true;
                    }

                    //-- Detecta que el campo aplique decimal
                    String decimal = verificaempaque(evart.getText().toString());
                    String evcajasdec = evcajas.getText().toString();
                    int punt = evcajasdec.indexOf(".");

                    if (decimal.equals("N") && punt != -1) {
                        Snackbar.make(v, "Este articulo no permite decimales", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        evcajas.setText(null);
                        evcajas.requestFocus();
                        evcajas.selectAll();
                        return true;
                    }

                    String number6 = evcajas.getText().toString();
                    String numbre7 = limiteCantidadcajs;
                    double result6 = Double.parseDouble(number6);
                    double result7 = Double.parseDouble(numbre7);
                    //-- Valida que el valor sea mayor a ceros
                    if (result6 == 0) {
                        Snackbar.make(v, "Este articulo no permite el cero", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        evcajas.setText(null);
                        evcajas.requestFocus();
                        evcajas.selectAll();
                        return true;
                    }
                    //-- Valida que el valor no se mayor a lo permitido
                    if (result6 > result7) {
                        Snackbar.make(v, "Este articulo permite cantidase menor a" + result7, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        evcajas.setText(null);
                        evcajas.requestFocus();
                        evcajas.selectAll();
                        return true;
                    }
                    //-- Valida que el valor no se mayor a lo permitido
                    if (result6 <= result7) {
                        evpza.setEnabled(false);
                        evpza.clearFocus();
                        evcajas.setEnabled(false);
                        evcajas.clearFocus();
                        mesajeCantCaj();
                    }
                } catch (Exception ex) {
                    savetoLog(ex.toString());
                }
                return true;
            }
            return false;
        });
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
        } catch (Exception se) {
            savetoLog("conexionBD: " + se.getMessage() + " | " + se.toString());
        }
        return conn;
    }

    private MainActivity4.Articulo ValidarArticulo(String codigo, String almacen, String salm) {
        //Verifica que codigo y almacen sean diferentes a null ""
        if (codigo == null || codigo.equals("") || almacen == null || almacen.equals("")) {
            //Codigo en blanco
        } else {
            //Obtiene la informacion
            String query = "[proc_Inv_SerachArticle] '" + codigo.trim() + "','" + almacen.trim() + "', '" + salm + "'";
            try {
                ResultSet n = queryDT(query);
                if (n == null) {
                    return null;
                } else {
                    if (n.next()) {
                        MainActivity4.Articulo art = new MainActivity4.Articulo();
                        art.descripcion = n.getString("des1");
                        art.cve = Integer.parseInt(n.getString("art").trim());
                        art.unidad = n.getString("UnidadMinima");
                        art.estatus = n.getString("Estatus");
                        art.decimales = n.getString("Decimales");
                        art.precio = n.getString("Precio");
                        art.empaque = n.getString("fac_ent_sal");
                        return art;
                    }
                }
                return null;
            } catch (SQLException ex) {
                savetoLog("ValidarArticulo | query: " + query + " | " + ex.toString());
                return null;
            }
        }
        return null;
    }

    // -- Consulta con Metodo con Metodo Global --
    public ResultSet queryDT(String query) {
        Connection con;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            String ConnURL = "jdbc:jtds:sqlserver://" + ip + ";databaseName=" + db + ";user=" + un + ";password=" + passwords + "";
            con = DriverManager.getConnection(ConnURL);
            Statement st = con.createStatement();
            st.setQueryTimeout(10);
            return st.executeQuery(query);
        } catch (Exception s) {
            savetoLog("queryDT " + query + " | " + s.toString());
            return null;
        }
    }


    // -----------------------------
    // -- BUSQUEDA DE INFORMACION --
    // -----------------------------
    // -- Busca el tipo de captura piezas o cajas
    public void bsaudasig() {

        tipodecaptura = "0";
        String almacen_tmp = txtsuc.getText().toString();
        String query = "select PC_CJ from tblinventarioalm_tic where almacen='" + almacen_tmp + "'";
        try {
            connect = CONN(un, passwords, db, ip);
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {

                String tipodecapturas = rs.getString("PC_CJ");
                tipodecaptura = tipodecapturas;
                txtarea.setBackgroundColor(Color.YELLOW);
                txtarea.setTextColor(Color.RED);
                usrcvecorta.setTextColor(Color.LTGRAY);
            }
        } catch (SQLException e) {
            savetoLog("bsaudasig: " + query + " | " + e.toString());
            e.printStackTrace();

        }
    }


    //-- Busca el ultimo numero consecutivo --
    public void Buscdinvactmax() {
        folioBuscdinvactmax = 0;
        String Buscdinvactmax1 = txtcdinv.getText().toString();
        String Busareaactmax1 = txtarea.getText().toString();
        String query = "Select valor_int from tblinventariocdimbt_tic Where ninventario ='" + Buscdinvactmax1 + "' and area='" + Busareaactmax1 + "'";
        try {
            connect = CONN(un, passwords, db, ip);
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            //-- Muestra la informacion
            while (rs.next()) {
                String Buscdinvactmax2 = rs.getString("valor_int");
                int resultBuscdinvactmax2 = Integer.parseInt(Buscdinvactmax2);
                folioBuscdinvactmax = resultBuscdinvactmax2 + 1;
                actualizarcdincose();
            }
        } catch (SQLException e) {
            savetoLog("Buscdinvactmax: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }


    // ----------------------------
    // -- GUARDAR LA INFORMACION --
    // ----------------------------
    //-- Grabar a tabla invmar piezas--
    public String grabarinvmarpzas(String cantidad) {
        String codinvGinvmar = txtcdinv.getText().toString();
        String zonaGinvmar = txtalm.getText().toString();
        String almGinvmar = txtsuc.getText().toString();
        String artGinvmar = quitaEspacios(evart.getText().toString());
        //-- convierte el string a double ejem: 25.00 OK
        DecimalFormat df3 = new DecimalFormat("#.###");
        df3.setRoundingMode(RoundingMode.DOWN);
        double resultpzas = Double.parseDouble(cantidad);
        //-- Obtiene el numero de marbete maximo que coincida con el codigo de inv
        String usrGinvmar = usrcvecorta.getText().toString();
        String areaGinvmar = txtarea.getText().toString();
        String empaqGinvmar = evpzascant.getText().toString();

        String query = "proc_Inv_GrabaInvmar @pzacaja = 'pieza', @codinvGinvmar = '" + codinvGinvmar + "', @zonaGinvmar = '" + zonaGinvmar + "', " +
                "@almGinvmar = '" + almGinvmar + "', @artGinvmar = '" + artGinvmar + "', @cantidad = '" + df3.format(resultpzas) + "', " +
                "@usrGinvmar = '" + usrGinvmar + "', @areaGinvmar = '" + areaGinvmar + "', @empaqGinvmar = '" + empaqGinvmar + "', @tipoUsuario ='Auditor'";

        try {
            connect = CONN(un, passwords, db, ip);
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            String marbsys = "";
            if(rs!=null){
                while (rs.next()) {
                    marbsys = rs.getString("Marbete");
                }
                return marbsys;
            }else{
                return "ERROR";
            }
        } catch (SQLException e) {
            savetoLog("grabarinvmarpzas: " + query + " | " + e.toString());
            return "ERROR";
        }
    }


    //-- Grabar codigo del marbete --
    public void grabarmbt(String marbsys, String cantidad) {
        String mbtsevcodbar = evart.getText().toString();// -- Codigo de inventario
        String mbtscodinv = txtcdinv.getText().toString();// -- Codigo de inventario
        String mbtsarea = txtarea.getText().toString(); // -- Codigo de area
        String mbtscapturo = usrcvecorta.getText().toString(); // -- Clave corta del usuario
        String mbtsart = quitaEspacios(evart.getText().toString()); //-- Clave corta del articulo
        // -- 6 digitos para el numero consecutivo
        Formatter fmt1 = new Formatter();
        fmt1.format("%06d", folioBuscdinvactmax);
        // -- 5 digitos para el area
        String evarea0izq = txtarea.getText().toString();
        int result1_evarea0izq = Integer.parseInt(evarea0izq);
        Formatter fmt2 = new Formatter();
        fmt2.format("%05d", result1_evarea0izq);
        // -- Codigo de Marbete largo
        mbtsfolio = mbtscodinv + fmt2 + fmt1;
        // -- 6 digitos para el numero consecutivo
        int marbetesis = Integer.parseInt(marbsys);
        Formatter fmtsis = new Formatter();
        fmtsis.format("%06d", marbetesis);
        String mbtszona = txtalm.getText().toString(); // -- almacen Z02-Z01
        String mbtssucursal = txtsuc.getText().toString(); // -- JUA1
        //-- convierte el string a double ejem: 25.00
        String numbercantpzscaj = "0";
        resultcantidadpzscjs = Double.parseDouble(numbercantpzscaj);
        //-- convierte el string a double ejem: 25.00 OK
        //Declara variables
        double resultpzas = 0;
        double resultcjs = 0;
        double pzasOcajs = 0;
        DecimalFormat df3 = new DecimalFormat("#.###");
        df3.setRoundingMode(RoundingMode.DOWN);
        if (tipodecaptura.equals("1")) {
            //Si regresa valor, intenta convertir a double
            try {
                resultpzas = Double.parseDouble(cantidad);
            } catch (Exception e) {
                Toast.makeText(this, "Error de captura. Escribe una cantidad de forma correcta", Toast.LENGTH_LONG).show();
                savetoLog("grabarmbt: Error al convertir campo piezas en DOUBLE, valor recibido: " + cantidad + " | " + e.toString());
            }
        } else if (tipodecaptura.equals("2")) {
            try {
                resultcjs = Double.parseDouble(cantidad);
            } catch (Exception e) {
                Toast.makeText(this, "Error de captura. Escribe una cantidad de forma correcta", Toast.LENGTH_LONG).show();
                savetoLog("grabarmbt: Error al convertir campo cajas en DOUBLE, valor recibido: " + cantidad + " | " + e.toString());
            }
        } else {
            Toast.makeText(this, "Favor de capturar una cantidad", Toast.LENGTH_LONG).show();
        }
        //Si variable de cantidad piezas es igual a 0, asigna valor de cantidad cajas a variable de insercion
        if (resultpzas == 0) {
            pzasOcajs = resultcjs;
            captpzcajexiste = "2";
        }//Si variable de cantidad piezas es diferente a 0, asigna valor de cantidad piezas a variable de insercion
        else {
            pzasOcajs = resultpzas;
            captpzcajexiste = "1";
        }
        String query = ("INSERT INTO tblinventariodet_tic (ninventario,area,capturo,codmarbet,art,marbet,marbetsys,fechahr,zona,sucursal,tipocap,audexistencia,audito,fechahraud,auddif,capexistencia,codbar) " +
                "VALUES ('" + mbtscodinv + "','" + mbtsarea + "','" + mbtscapturo + "','" + mbtsfolio + "','" + mbtsart + "','" + fmt1 + "','" + fmtsis + "',Concat(Convert(char(10),GETDATE(),105),' ',CONVERT(varchar,GETDATE(),8)),'" + mbtszona + "','" + mbtssucursal + "','" + captpzcajexiste + "','" + df3.format(pzasOcajs) + "','" + mbtscapturo + "',Concat(Convert(char(10),GETDATE(),105),' ',CONVERT(varchar,GETDATE(),8)),'1','" + df3.format(pzasOcajs) + "','" + mbtsevcodbar + "')");
        try {
            Boolean res = queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("grabarmbt: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }

    //-- Grabar a tabla invmar piezas--
    public String grabarinvmarcajas(String cantidad) {
        String codinvGinvmar = txtcdinv.getText().toString();
        String zonaGinvmar = txtalm.getText().toString();
        String almGinvmar = txtsuc.getText().toString();
        String artGinvmar = quitaEspacios(evart.getText().toString());
        //-- convierte el string a double ejem: 25.00 OK
        DecimalFormat df3 = new DecimalFormat("#.###");
        df3.setRoundingMode(RoundingMode.DOWN);
        double resultcajass = Double.parseDouble(cantidad);

        //-- Obtiene el numero de marbete maximo que coincida con el codigo de inv
        String usrGinvmar = usrcvecorta.getText().toString();
        String areaGinvmar = txtarea.getText().toString();
        String empaqGinvmar = evpzascant.getText().toString();

        String query = "proc_Inv_GrabaInvmar @pzacaja = 'caja', @codinvGinvmar = '" + codinvGinvmar + "', @zonaGinvmar = '" + zonaGinvmar + "', " +
                "@almGinvmar = '" + almGinvmar + "', @artGinvmar = '" + artGinvmar + "', @cantidad = '" + df3.format(resultcajass) + "', " +
                "@usrGinvmar = '" + usrGinvmar + "', @areaGinvmar = '" + areaGinvmar + "', @empaqGinvmar = '" + empaqGinvmar + "', @tipoUsuario ='Auditor'";
        try {
            connect = CONN(un, passwords, db, ip);
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            String marbsys = "";
            if(rs!=null){
                while (rs.next()) {
                    marbsys = rs.getString("Marbete");
                }
                return marbsys;
            }else{
                return "ERROR";
            }

        } catch (SQLException e) {
            savetoLog("grabarinvmarcajas: " + query + " | " + e.toString());
            return "ERROR";
        }
    }


    // -------------------------------
    // -- ACTUALIZAR LA INFORMACION --
    // -------------------------------
//-- Actualiza es estado codigo de marbete --
    public void actualizarcdincose() {
        String codinvcons = txtcdinv.getText().toString();
        String areassscons = txtarea.getText().toString();
        String query = ("update tblinventariocdimbt_tic set valor_int='" + folioBuscdinvactmax + "' where ninventario='" + codinvcons + "' and area='" + areassscons + "'");

        try {
            Boolean res = queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("actualizarcdincose: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }


    // -------------
    // -- COLORES --
    // -------------
    // -- Color al resultado de la busqueda
    public void colorestext() {
        try {
            evdesc.setTextColor(Color.parseColor("#186A3B"));
            evart.setTextColor(Color.parseColor("#186A3B"));
            evpzass.setTextColor(Color.parseColor("#186A3B"));
            evpzascant.setTextColor(Color.parseColor("#34495E"));
            evprecio.setTextColor(Color.parseColor("#186A3B"));
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    // -------------
    // -- Limpiar --
    // -------------
    //-- Limpia etiquetas al capturar el codigo de barras
    private void limpiardespuesdecodigobar() {
        try {
            evart.setText("");
            evdesc.setText("");
            evpzass.setText("");
            evpzascant.setText("");
            evprecio.setText("");
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    // --------------
// -- Mascaras --
// --------------
// -- Mascara de 2 decimales --
    class DecimalDigitsInputFilter implements InputFilter {
        private Pattern mPattern;

        DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero) {
            mPattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Matcher matcher = mPattern.matcher(dest);
            if (!matcher.matches())
                return "";
            return null;
        }

    }

    //------------------
    //---Desactiva boton de retroceso del escaner
    //------------------
    @Override
    public void onBackPressed() {

    }

    // --------------
    // -- DECIMALES --
    // --------------
    public String verificaempaque(String art) {
        //getPreferences();

        String articulos = art.trim();
        articulos = articulos.replace("'", "");
        String SQL = "select art_ser from inviar where art = '" + articulos + "'";
        try {
            connect = CONN(un, passwords, db, ip);
            stmt = connect.prepareStatement(SQL);
            rs = stmt.executeQuery();
            if (rs.next()) {
                String ex = rs.getString("art_ser");
                return ex;
            } else {
                savetoLog("verificaempaque: " + SQL + " | No se encontraron resultados");
                return "";
            }
        } catch (Exception e) {
            savetoLog("verificaempaque: " + SQL + " | " + e.toString());
            return "";
        }
    }

    private void ocultarkeypza() {
        try {
            //Lineas para ocultar el teclado virtual (Hide keyboard)
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(evpza.getWindowToken(), 0);
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    private void ocultarkeycaja() {
        //Lineas para ocultar el teclado virtual (Hide keyboard)
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(evcajas.getWindowToken(), 0);
    }

    // -----------------------
    // -- ELIMINA ESPACIOS --
    // -----------------------
    public String quitaEspacios(String texto) {
        java.util.StringTokenizer tokens = new java.util.StringTokenizer(texto);
        texto = "";
        while (tokens.hasMoreTokens()) {
            texto += " " + tokens.nextToken();
        }
        texto = texto.trim();
        return texto;
    }

    //-- Regresar al menu principal
    public void onBackPressed(View view) {
        try {
            Intent intent = new Intent(this, MainActivity7.class);
            startActivity(intent);
            Log.d("MainActivity7", "onBackPressed()");
            finish();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }


    //-- Alerta al seleccionar el PIEZAS --
    private void mesajeCantPza() {
        try {
            String cantidad = evpza.getText().toString();
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle(evpzass.getText().toString() + " DE: " + cantidad );
            dialogo1.setMessage("¿ Esta cantidad es correcta ? " + "");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("Si", (dialogo11, id) -> ConfirmarPza(cantidad));
            dialogo1.setNegativeButton("No", (dialogo112, id) -> NoConfirmarPza());
            dialogo1.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void NoConfirmarPza() {
        try {
            evpza.setText(null);
            evpza.requestFocus();
            evpza.setEnabled(true);
            evpza.selectAll();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void ConfirmarPza(String cantidad) {
        try {
            String marbsys = grabarinvmarpzas(cantidad);
            if(marbsys.equals("ERROR")){
                savetoLog("Error de captura");
                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
                dialogo1.setTitle(R.string.Alerta);
                dialogo1.setMessage(R.string.ArtNoGuardado);
                dialogo1.setCancelable(false);
                dialogo1.setNegativeButton("OK", (dialogo11, id) -> {
                    evpza.setText(null);
                    evpza.setEnabled(true);
                });
                dialogo1.show();
            }else{
                Buscdinvactmax();
                grabarmbt(marbsys, cantidad);
                // --Salir
                Intent intent = new Intent(this, MainActivity7.class);
                startActivity(intent);
                Log.d("MainActivity7", "onBackPressed()");
                finish();
            }

        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    //-- Alerta al seleccionar el CAJAS --
    private void mesajeCantCaj() {
        try {
            String cantidad = evcajas.getText().toString();
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle(evpzass.getText().toString() + " DE: " + cantidad);
            dialogo1.setMessage("¿ Esta cantidad es correcta ? " + "");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("Si", (dialogo112, id) -> ConfirmarCaj(cantidad));
            dialogo1.setNegativeButton("No", (dialogo11, id) -> NoConfirmarCaj());
            dialogo1.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void NoConfirmarCaj() {
        try {
            evcajas.setText(null);
            evcajas.requestFocus();
            evcajas.setEnabled(true);
            evcajas.selectAll();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void ConfirmarCaj(String cantidad) {
        try {
            String marbsys = grabarinvmarcajas(cantidad);
            if(marbsys.equals("ERROR")){
                savetoLog("Error de captura");
                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
                dialogo1.setTitle(R.string.Alerta);
                dialogo1.setMessage(R.string.ArtNoGuardado);
                dialogo1.setCancelable(false);
                dialogo1.setNegativeButton("OK", (dialogo11, id) -> {
                    evcajas.setText(null);
                    evcajas.setEnabled(true);
                });
                dialogo1.show();
            }else{
                Buscdinvactmax();
                grabarmbt(marbsys, cantidad);
                // --Salir
                Intent intent = new Intent(this, MainActivity7.class);
                startActivity(intent);
                Log.d("MainActivity7", "onBackPressed()");
                finish();
            }
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    // -- Update o Insert con Metodo Global --
    public Boolean queryUpdIns(String query) {
        Connection con = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            String ConnURL = "jdbc:jtds:sqlserver://" + ip + ";databaseName=" + db + ";user=" + un + ";password=" + passwords + "";
            con = DriverManager.getConnection(ConnURL);
            try {
                Statement st = con.createStatement();
                String resbool = "false";
                int res = st.executeUpdate(query);
                if (res > 0) {
                    resbool = "true";
                }
                return Boolean.parseBoolean(resbool);
            } catch (SQLException s) {
                dialogerror(s.toString());
                savetoLog("queryDT | " + s.toString());
                return false;
            }
        } catch (Exception e) {
            dialogerror(e.toString());
            savetoLog("queryDT | " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    public void dialogerror(String texto) {
        try {
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle("Error encontrado");
            dialogo1.setMessage(texto);
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("OK", (dialogo11, id) -> {

            });
            dialogo1.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }


    // End Activity

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
}