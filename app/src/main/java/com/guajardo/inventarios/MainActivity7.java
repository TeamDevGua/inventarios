package com.guajardo.inventarios;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.StrictMode;
import android.os.Vibrator;

import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
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
import java.util.List;
import java.util.Locale;

// -- Mascara de 2 decimales
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// ---------------
// -- IMPRESORA --
// ---------------
import com.citizen.sdk.ESCPOSConst;
import com.citizen.sdk.ESCPOSPrinter;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;


public class MainActivity7 extends AppCompatActivity {
    private int count = 0;
    // al inicio
    private TextView txtusrname;
    // al escanear
    private EditText evcodmbt;
    private TextView txtcdinv, evarea, evart, evusrcrt, evfechor, mrbtsyste;
    private TextView evconto, txtalm, txtsuc;
    private TextView evemp, evemp2;
    private TextView evcodbar, evcodbardes, evprec;
    private TextView etpiez, etcaj;
    private EditText evcaj, evpiez;
    // -- Muestra la conexion de la red
    private TextView etconec;
    private TextView evusrcrt1;
    private TextView exiscap;
    private TextView etinc;
    private TextView avancess;

    // -- Llena el spinner
    private Spinner evcodinv;

    private ProgressDialog progressDialog;

    Button btnacc2;
    String limiteCantidadpzs;
    String limiteCantidadcajs;
    //-- determina si el marbete fue impreso al salir
    String mbtprintsino = "0";
    //-- Determina si el AUDITOR fue registrado desde el dashborad
    String fecinicaptura = "0";
    String newareaasignada = "0";
    //-- conexiones SQL
    String ip, db, un, passwords;
    Connection connect;
    PreparedStatement stmt;
    ResultSet rs;
    Integer msges;
    //-- valor con decimales
    DecimalFormat df = new DecimalFormat("####0.000");
    //-- convierte el valor a pesos
    NumberFormat formatoImporte = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    // Obtiene instancia a Vibrator
    private Vibrator vibrator;
    ImageButton btnrefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main7);
        //Declara spinner de areas
        evcodinv = findViewById(R.id.evcodinv);
        // Obtiene instancia a Vibrator
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        btnrefresh = findViewById(R.id.btnrefresh);
        // -- Titulo de la aplicacion
        setTitle("AUDITOR");
        // ---------------
        // -- IMPRESORA --
        // ---------------

        txtusrname = findViewById(R.id.txtusrname);
        txtcdinv = findViewById(R.id.txtcdinv);

        evarea = findViewById(R.id.evarea);
        evart = findViewById(R.id.evart);
        evusrcrt = findViewById(R.id.evusrcrt);

        evusrcrt1 = findViewById(R.id.evusrcrt1);

        exiscap = findViewById(R.id.exiscap);
        etinc = findViewById(R.id.etinc);
        avancess = findViewById(R.id.avancess);

        evfechor = findViewById(R.id.evfechor);
        mrbtsyste = findViewById(R.id.mrbtsyste);
        txtalm = findViewById(R.id.txtalm);
        txtsuc = findViewById(R.id.txtsuc);
        evemp = findViewById(R.id.evemp);
        evemp2 = findViewById(R.id.evemp2);

        evcodbar = findViewById(R.id.evcodbar);
        evcodbardes = findViewById(R.id.evcodbardes);
        evprec = findViewById(R.id.evprec);

        evconto = findViewById(R.id.evconto);

        etcaj = findViewById(R.id.etcaj);
        etpiez = findViewById(R.id.etpiez);

        evcaj = findViewById(R.id.evcaj);
        evpiez = findViewById(R.id.evpiez);

        evpiez.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6, 3)});
        evcaj.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6, 3)});

        // ------------------
        // -- PREFERENCIAS --
        // ------------------
        // -- Trae del auditor logeado
        SharedPreferences prefe = getSharedPreferences("conf_usr", Context.MODE_PRIVATE);
        txtusrname.setText(prefe.getString("namess", ""));
        evusrcrt1.setText(prefe.getString("namecut", ""));

        evcodmbt = (EditText) findViewById(R.id.evcodmbt);
        etconec = (TextView) findViewById(R.id.etconec);

        btnacc2 = findViewById(R.id.btnacc7);

        //-- Trae la configuracion del servidor
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


        // ** Busca que el usuario corresponda al area asignada muestra (COD INV, ZONA, ALMACEN Y AREA) **
        // ** Desde el dashboard                                                                        **
        // ** Visualiza la informacion del usuario                                                      **
        bsausrinfor();
        //Trae el porcentaje de avance y la cantidad de errores
        bscErrPorc();
        evcodinv.setBackgroundColor(Color.parseColor("#98FB98"));
        //-- Cuenta los errores
        String numberetinc = etinc.getText().toString();
        double numberetincf = Double.parseDouble(numberetinc);

        if (numberetincf >= 4) {
            mesajeerrores();
        }

        evcodinv.setEnabled(false);

        // ** ENTER AUTOMATICO **
        // ** Al muestra la informacion automaticamente al buscar el codigo de barras **
        //cambia a campo cantidad al dar enter (funcion trigger)
        evcodmbt.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                try {
                    evcodbardes.setTextColor(Color.parseColor("#186A3B"));
                    //-- Detecta que el campo de buscar no este en blanco
                    if (evcodmbt.getText().toString().trim().equalsIgnoreCase("")) {
                        evcodbardes.setText(R.string.NoEncontrado);
                        evcodbardes.setTextColor(Color.parseColor("#E74C3C"));
                        evart.setText("---");
                        evemp.setText("---");
                        evemp2.setText("---");
                        evprec.setText("---");
                        exiscap.setText("---");
                        //Bloquea evcaj y evpiez cuando no se encuentra el articulo
                        evcaj.setEnabled(false);
                        evpiez.setEnabled(false);
                        limpiarmbtnoencon();
                        evcodbar.setText("---");
                    }
                    if (evcodinv.getSelectedItem() == null) {
                        vibrador();
                        Toast.makeText(this, "Es necesario ingresar a una area antes de buscar un articulo", Toast.LENGTH_LONG).show();
                        evcodmbt.requestFocus();
                        evcodmbt.selectAll();
                        evcaj.setEnabled(false);
                        evpiez.setEnabled(false);
                    } else {
                        //Obtiene el codigo de barras
                        String codigo = evcodmbt.getText().toString();
                        //Otiene almacen
                        String almacen = txtsuc.getText().toString();
                        //Obtiene salm
                        String salm = txtalm.getText().toString();
                        //Ejecuta consulta
                        MainActivity4.Articulo art = ValidarArticulo(codigo, salm, almacen);
                        //Trae el porcentaje de avance y la cantidad de errores
                        bscErrPorc();

                        if (art != null) {
                            limpiarmbtnoencon();
                            // ** Busca la cantidad de errores del capturista **
                            // **  Porcentaje de Avance  ***
                            //Verificar que exista el articulo en el area
                            ResultSet n = ExsArtArea(String.valueOf(art.cve));
                            ArrayList<AssignmentUserModel> listaMarb = new ArrayList<>();
                            if (n != null) {
                                int cant = 0;
                                while (n.next()) {
                                    cant++;
                                    listaMarb.add(new AssignmentUserModel(
                                            n.getString("marbetsys"),
                                            n.getString("art"),
                                            df.format(n.getDouble("capexistencia")),
                                            df.format(n.getDouble("audexistencia")),
                                            n.getString("userconto"),
                                            n.getString("fecuserconto"),
                                            n.getString("tipocap")
                                    ));
                                }
                                if (cant > 0) {
                                    // -- Limpia las etiquetas --
                                    //Set articulo
                                    evart.setText(String.valueOf(art.cve));
                                    //Muestra nombre del articulo
                                    evcodbardes.setText(art.descripcion);
                                    //Muestra precio
                                    evprec.setText(formatoImporte.format(Double.parseDouble(art.precio)));
                                    //Guarda clave art
                                    evcodbar.setText(codigo);
                                    //Unidades
                                    evemp.setText(art.unidad);
                                    //Empaque
                                    evemp2.setText(df.format(Double.parseDouble(art.empaque)));
                                    showCustomAlertDialogBoxForUserList(listaMarb);
                                } else {
                                    //No existe
                                    vibrador();
                                    limpiarmbtnoencon();
                                    // *** Mensaje de codigo de barras no existe **
                                    alertass();
                                    //Bloquea evcaj y evpiez cuando no encuentra el articulo en el area
                                    evcaj.setEnabled(false);
                                    evpiez.setEnabled(false);
                                    evcodmbt.requestFocus();
                                    evcodmbt.selectAll();
                                }

                            } else {
                                //No existe
                                vibrador();
                                limpiarmbtnoencon();
                                // *** Mensaje de codigo de barras no existe **
                                alertass();
                                //Bloquea evcaj y evpiez cuando no encuentra el articulo en el area
                                evcaj.setEnabled(false);
                                evpiez.setEnabled(false);
                                evcodmbt.requestFocus();
                                evcodmbt.selectAll();
                            }
                        } else {
                            evcodbardes.setText(R.string.NoEncontrado);
                            evcodbardes.setTextColor(Color.parseColor("#E74C3C"));
                            evart.setText("---");
                            evemp.setText("---");
                            evemp2.setText("---");
                            evprec.setText("---");
                            exiscap.setText("---");
                            evcodbar.setText("---");
                            evcodmbt.requestFocus();
                            evcodmbt.selectAll();
                            //Bloquea evcaj y evpiez cuando no se encuentra el articulo
                            evcaj.setEnabled(false);
                            evpiez.setEnabled(false);
                        }
                    }
                } catch (Exception ex) {
                    savetoLog("evcodmbt.setOnKeyListener " + ex.toString());
                }

                return true;
            } else {
                return false;
            }
        });


        btnacc2.setOnClickListener(view -> {
            try {
                //Graba cuando se presione la nueva area jeje
                savetoLog(txtusrname.getText().toString() + " presiona boton nueva area");
                //----------------------------------------//
                //-- Porcentaje de avance no deja salir --//
                //----------------------------------------//
                String numberAVG = avancess.getText().toString();
                double numberAVGf = Double.parseDouble(numberAVG);

                if (numberAVGf >= 0 && numberAVGf <= 39) {
                    vibrador();
                    evcodmbt.requestFocus();
                    evcodmbt.selectAll();

                    Snackbar.make(view, "EL PORCENTAJE ES MENOR AL 40 % !", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }

                AlertDialog.Builder diaConfirmar = new AlertDialog.Builder(MainActivity7.this);
                diaConfirmar.setTitle("Nueva area");
                diaConfirmar.setMessage("¿ Deseas continuar al area siguiente ? ");
                diaConfirmar.setCancelable(false);
                diaConfirmar.setPositiveButton("Si", (dialogo1, id) -> {
                    //Graba cuando se presione la nueva area
                    savetoLog(txtusrname.getText().toString() + " presiona boton aceptar guardar");

                    etinc.setText("0");
                    avancess.setText("0");

                    evarea.setBackgroundColor(Color.WHITE);
                    evarea.setTextColor(Color.BLACK);

                    // ** Actualiza la hora y fecha y estado a abierto del auditor **
                    actualizarlogsaudix();

                    // ** actualiza estado cerrado del auditor cuando ya finalizo **
                    actualizarlogscerrado();


                    limpiarall();
                    evarea.setText("---");
                    llenarspinner();

                    // ***********************************************************************************************
                    // ** Busca que el usuario corresponda al area asignada muestra (COD INV, ZONA, ALMACEN Y AREA) **
                    // ** Desde el dashboard                                                                        **
                    // ** Visualiza la informacion del usuario                                                      **
                    // ***********************************************************************************************
                    bsausrinfor();
                    if (newareaasignada.equals("0")) { // -- no encontrado
                        // **************************************
                        // ** mensaje no mas areas disponibles **
                        // **************************************
                        mesajenonewarea();

                    } else if (newareaasignada.equals("1")) {// -- encontrado

                        // **************************************
                        // ** mensaje mas areas disponibles **
                        // **************************************
                        mesajenewarea();
                    }

                });
                diaConfirmar.setNegativeButton("No", (dialog, which) -> {
                    //Graba cuando se presione la nueva area
                    savetoLog(txtusrname.getText().toString() + " presiona boton cancelar guardar");
                });
                diaConfirmar.show();
            } catch (Exception ex) {
                savetoLog(ex.toString());
            }
        });


        // -----------
        // -- ENTER --
        // -----------
        // -- Captura la cantidad en piezas , accion despues del Enter
        evpiez.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                try {
                    //-- Detecta si es Punto
                    String evpzapunto = evpiez.getText().toString();
                    if (evpzapunto.equals(".")) {
                        vibrador();
                        Toast.makeText(MainActivity7.this, "Este articulo no permite el simbolo de punto", Toast.LENGTH_SHORT).show();
                        evpiez.requestFocus();
                        evpiez.setText("");
                        evpiez.selectAll();
                        return true;
                    }

                    //-- Detecta que el campo de buscar no este en blanco
                    String evareabco = evpiez.getText().toString();
                    if (evareabco.equals("")) {
                        vibrador();
                        Toast.makeText(MainActivity7.this, "Este articulo no permite espacios en blanco", Toast.LENGTH_SHORT).show();
                        evpiez.setText("");
                        evpiez.requestFocus();
                        evpiez.selectAll();
                        return true;
                    }

                    if (evemp.getText().toString().trim().equalsIgnoreCase("---")) {
                        vibrador();
                        Toast.makeText(MainActivity7.this, "Falta algun campo", Toast.LENGTH_SHORT).show();
                        evpiez.setText("");
                        evpiez.requestFocus();
                        evpiez.selectAll();
                        return true;
                    }

                    //-- Detecta que el campo aplique decimal
                    String decimal = verificaempaque(evart.getText().toString());
                    String evpzadec = evpiez.getText().toString();
                    int punt = evpzadec.indexOf(".");

                    if (decimal.equals("N") && punt != -1) {
                        vibrador();
                        Toast.makeText(MainActivity7.this, "Este articulo no permite decimales", Toast.LENGTH_SHORT).show();
                        evpiez.setText("");
                        evpiez.requestFocus();
                        evpiez.selectAll();
                        return true;
                    }

                    //-- Busca el limite de piezas
                    //Buscarlimpiezas();

                    String number3 = evpiez.getText().toString();
                    String numbre5 = limiteCantidadpzs;
                    double result3 = Double.parseDouble(number3);
                    double result5 = Double.parseDouble(numbre5);

                    //-- Valida que el valor sea mayor a ceros
                    if (result3 == 0) {
                        vibrador();
                        Toast.makeText(MainActivity7.this, "Este articulo no permite el cero", Toast.LENGTH_SHORT).show();
                        evpiez.setText("");
                        evpiez.requestFocus();
                        evpiez.selectAll();
                        return true;
                    }

                    //-- Valida que el valor no sea mayor a lo permitido
                    if (result3 > result5) {
                        vibrador();
                        Toast.makeText(MainActivity7.this, "Este articulo permite cantidase menor a " + result5, Toast.LENGTH_SHORT).show();
                        evpiez.setText("");
                        evpiez.requestFocus();
                        evpiez.selectAll();
                        return true;
                    }

                    //-- Valida que el valor no se mayor a lo permitido
                    if (result3 <= result5) {
                        evpiez.setEnabled(false);
                        evpiez.clearFocus();
                        evcaj.setEnabled(false);
                        evcaj.clearFocus();

                        mesajeCantPza();
                        //gravapiezas2();

                    }
                } catch (Exception ex) {
                    savetoLog(ex.toString());
                }
                //-- Termina
                return true;
            }
            return false;
        });

        // -----------
        // -- ENTER --
        // -----------
        // -- Captura la cantidad en cajas , accion despues del Enter
        evcaj.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                try {
                    //-- Detecta si es Punto
                    String evcajaspunto = evcaj.getText().toString();
                    if (evcajaspunto.equals(".")) {
                        vibrador();
                        Toast.makeText(MainActivity7.this, "Este articulo no permite el simbolo de punto", Toast.LENGTH_SHORT).show();
                        evcaj.requestFocus();
                        evcaj.setText("");
                        evcaj.selectAll();
                        return true;
                    }

                    //-- Detecta que el campo de buscar no este en blanco
                    String evcajasbco = evcaj.getText().toString();
                    if (evcajasbco.equals("")) {
                        vibrador();
                        Toast.makeText(MainActivity7.this, "Este articulo no permite espacios en blanco", Toast.LENGTH_SHORT).show();
                        evcaj.setText("");
                        evcaj.requestFocus();
                        evcaj.selectAll();
                        return true;
                    }

                    if (evemp.getText().toString().trim().equalsIgnoreCase("---")) {
                        vibrador();
                        Toast.makeText(MainActivity7.this, "Falta algun campo", Toast.LENGTH_SHORT).show();
                        evcaj.setText("");
                        evcaj.requestFocus();
                        evcaj.selectAll();
                        return true;
                    }

                    //-- Detecta que el campo aplique decimal
                    String decimal = verificaempaque(evart.getText().toString());
                    String evcajasdec = evcaj.getText().toString();
                    int punt = evcajasdec.indexOf(".");

                    if (decimal.equals("N") && punt != -1) {
                        vibrador();
                        Toast.makeText(MainActivity7.this, "Este articulo no permite decimales", Toast.LENGTH_SHORT).show();
                        evcaj.setText("");
                        evcaj.requestFocus();
                        evcaj.selectAll();
                        return true;
                    }

                    String number6 = evcaj.getText().toString();
                    String numbre7 = limiteCantidadcajs;
                    double result6 = Double.parseDouble(number6);
                    double result7 = Double.parseDouble(numbre7);

                    //-- Valida que el valor sea mayor a ceros
                    if (result6 == 0) {
                        vibrador();
                        Toast.makeText(MainActivity7.this, "Este articulo no permite el cero", Toast.LENGTH_SHORT).show();
                        evcaj.setText("");
                        evcaj.requestFocus();
                        evcaj.selectAll();
                        return true;
                    }

                    //-- Valida que el valor no se mayor a lo permitido
                    if (result6 > result7) {
                        vibrador();
                        Toast.makeText(MainActivity7.this, "Este articulo permite cantidase menor a " + result7, Toast.LENGTH_SHORT).show();
                        evcaj.setText("");
                        evcaj.requestFocus();
                        evcaj.selectAll();
                        return true;
                    }

                    //-- Valida que el valor no se mayor a lo permitido
                    if (result6 <= result7) {
                        evpiez.setEnabled(false);
                        evpiez.clearFocus();
                        evcaj.setEnabled(false);
                        evcaj.clearFocus();
                        evpiez.requestFocus();
                        mesajeCantCaj();
                    }
                } catch (Exception ex) {
                    savetoLog(ex.toString());
                }
                //-- Termina
                return true;
            }
            return false;
        });

        // ************************************************
        // ** Llena el spinner con las areas disponibles **
        // ************************************************
        String txtcdinvtemp = txtcdinv.getText().toString();
        String usrcvecortatemp = evusrcrt1.getText().toString();
//        String query = "Select area from tblinventario_tic where ninventario='" + txtcdinvtemp + "' and audito='" + usrcvecortatemp + "' and audasig='2' and fechahrfi_cap is not null";
        String query = "Select area from tblinventario_tic where ninventario='" + txtcdinvtemp + "' and audito='" + usrcvecortatemp + "' and audasig='2' and fechahrfi_cap is not null and ninventario in (select codigo from invmar)";
        ArrayList<String> data = new ArrayList<String>();
        try {
            connect = CONN(un, passwords, db, ip);
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();


            //-- Llena el Spinner
            while (rs.next()) {
                String id = rs.getString("area");
                data.add(id);
            }
            //-- Creamos el ArrayAdapter que necesitará el spinner, dándole como parámetros, (Contexto, referencia al layout de elemento, valores (opciones)
            ArrayAdapter NoCoreAdapter = new ArrayAdapter(this,
                    android.R.layout.simple_list_item_1, data);
            evcodinv.setAdapter(NoCoreAdapter);

        } catch (SQLException e) {
            savetoLog("llenaSpinnerevcodinvActivity7: " + query + " | " + e.toString());
            e.printStackTrace();
        }

        btnrefresh.setEnabled(data.size() <= 0);

        //-- indicamos al Spinner que utiliza el Adapter recien creado
        evcodinv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //-- Detecta la posicion del Items
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    msges = position;
                    if (msges == 0) {
                        limpiarmbtnoencon();
                        evarea.setText(evcodinv.getSelectedItem().toString());
                        evcodmbt.requestFocus();
                        evcodmbt.selectAll();
                        // *************************************************
                        // ** Busca la cantidad de errores del capturista y el porcentaje de avance**
                        // *************************************************
                        bscErrPorc();
                    } else if (msges > 0) {
                        limpiarmbtnoencon();
                        evarea.setText(evcodinv.getSelectedItem().toString());
                        evcodmbt.requestFocus();
                        evcodmbt.selectAll();
                        // *************************************************
                        // ** Busca la cantidad de errores del capturista y el porcentaje de avance**
                        // *************************************************
                        bscErrPorc();
                    }
                } catch (Exception ex) {
                    savetoLog(ex.toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        //inhabilita campos de piezas y cajas
        evcaj.setEnabled(false);
        evpiez.setEnabled(false);


        // ********************
        // ** ICONO flotante **
        // ********************

        FloatingActionButton fab = findViewById(R.id.floatingActionButton2);
        fab.setOnClickListener(view -> {
            try {
                String var = evcodinv.getSelectedItem().toString();
                if (!var.equals("")) {
                    iracaptura();
                } else {
                    Toast.makeText(getApplicationContext(), "No tienes area asignada", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                savetoLog(e.toString());
                Toast.makeText(getApplicationContext(), "No tienes una area asignada ", Toast.LENGTH_LONG).show();
            }
        });

        progressDialog = new ProgressDialog(this);

        //End onCreate
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

    //Verifica que el articulo escaneado haya sido capturado
    public ResultSet ExsArtArea(String art) {
        String txtcdinvtemp = txtcdinv.getText().toString();
        String evareatemp = evarea.getText().toString();

        String query = "select id,marbetsys,ninventario,area, capturo, codmarbet, marbet, capexistencia,audexistencia, inv.art as [art], inv.des1, usr.nombre_lar as [userconto], fechahr as [fecuserconto], tipocap " +
                "from tblinventariodet_tic det inner join inviar inv on det.art = inv.art inner join tcausr usr on det.capturo = usr.nom_cto " +
                "where inv.art = '" + art + "' and ninventario='" + txtcdinvtemp + "' and area='" + evareatemp + "' " +
                //SE AGREGA
                "and ninventario in (select codigo from invmar)" +
                "order by det.art ";
        try {
            Statement st = conexionBD().createStatement();
            return st.executeQuery(query);
        } catch (SQLException e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            savetoLog("ExsArtArea: " + query + " | " + e.toString());
            return null;
        }
    }


    // ***************
    // ** BUSQUEDAS **
    // ***************
    // ***********************************************************************************************
    // ** Busca que el usuario corresponda al area asignada muestra (COD INV, ZONA, ALMACEN Y AREA) **
    // ** Desde el dashboard                                                                        **
    // ** Visualiza la informacion del usuario                                                      **
    // ***********************************************************************************************
    public void bsausrinfor() {
        newareaasignada = "0";
        String evusrcrt1tmpl = evusrcrt1.getText().toString();
        String query = "SELECT tblinventario_tic.ninventario, tblinventariodet_tic.zona, tblinventariodet_tic.sucursal, Min(tblinventariodet_tic.area) AS MínDearea " +
                "FROM tblinventario_tic INNER JOIN tblinventariodet_tic ON tblinventario_tic.area = tblinventariodet_tic.area AND tblinventario_tic.ninventario = tblinventariodet_tic.ninventario " +
                //SE AGREGA
                "WHERE tblinventario_tic.ninventario in (select codigo from invmar)" +
                "GROUP BY tblinventario_tic.ninventario, tblinventariodet_tic.zona, tblinventariodet_tic.sucursal, tblinventario_tic.audasig, tblinventario_tic.audito " +
                "HAVING tblinventario_tic.audasig='2' AND tblinventario_tic.audito='" + evusrcrt1tmpl + "' ORDER BY Min(tblinventariodet_tic.area)";

        try {
            connect = CONN(un, passwords, db, ip);
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                txtcdinv.setText(rs.getString("ninventario"));
                txtalm.setText(rs.getString("zona"));
                txtsuc.setText(rs.getString("sucursal"));
                evarea.setText(rs.getString("MínDearea"));
                evarea.setBackgroundColor(Color.YELLOW);
                evarea.setTextColor(Color.RED);
                newareaasignada = "1";
            }
        } catch (SQLException e) {
            savetoLog("bsausrinfor: " + query + " | " + e.toString());
            e.printStackTrace();
        }

    }

    public void bscErrPorc() {
        String numInv = txtcdinv.getText().toString();
        String area = evarea.getText().toString();
        String alm = txtalm.getText().toString();
        String query = "proc_Inv_TraeAvance @Num_inventario = '" + numInv + "', @area = '" + area + "', @alm = '" + alm + "'";

        try {
            ResultSet n = queryDT(query);
            while (n.next()) {
                etinc.setText(df.format(n.getDouble("Errores")));
                avancess.setText(n.getString("PorcentajeAvance"));
            }
        } catch (Exception ex) {
            savetoLog("bscErrPorc: " + query + " | " + ex.toString());
        }
    }


    //-- Busca el numero de marbete maximo --
    public void bscfecinicap() {
        fecinicaptura = "0";
        String txtcdinvtmp = txtcdinv.getText().toString();
        String evareatmp = evarea.getText().toString();
        String evusrcrt1tmp = evusrcrt1.getText().toString();
        String query = "select * " +
                "From tblinventario_tic " +
                "Where ninventario='" + txtcdinvtmp + "' and area='" + evareatmp + "' and audito='" + evusrcrt1tmp + "' and estado='2' and fechahrin_aud is null and ninventario in (select codigo from invmar)";
        try {
            connect = CONN(un, passwords, db, ip);
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            //-- Muestra la informacion
            while (rs.next()) {
                // -- Bandera de existe
                fecinicaptura = "1"; // -- Registro encontrado
            }
        } catch (SQLException e) {
            savetoLog("bscfecinicap: " + query + " | " + e.toString());
        }
    }

    // ---------------
    // -- ACTUALIZA --
    // ---------------
    //-- Actualiza la cantidad auditada en piezas << tblinventariodet_tic >>
    public void actcantidadmbtpzas(String cantidad) {
        String mrbtsystetmp = mrbtsyste.getText().toString();
        //-- convierte el string a double ejem: 25.00 OK

        DecimalFormat df3 = new DecimalFormat("#.###");
        df3.setRoundingMode(RoundingMode.DOWN);
        double resultpzas = Double.parseDouble(cantidad);
        String codInv = txtcdinv.getText().toString();
        String logusrs3x = evusrcrt1.getText().toString();

        //Si encuentra que el marbete ya tiene diferencia de auditoria, al actualizar solo se hara en la cantidad y la hora. Si el campo de diferencia esta en 0, entonces al actulizar cambiara el status a 1
        String query = "proc_Inv_UpdateAud @resultpzas = '" + df3.format(resultpzas) + "', @logusrs3x = '" + logusrs3x + "'," + "@mrbtsystetmp = '" + mrbtsystetmp + "', @codigoinv = '" + codInv + "'";
        try {
            queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("actcantidadmbtpzas: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }

    //-- Actualiza la cantidad auditada en piezas << INVMAR >>
    public void actcantidadmbtpzasinvmar(String cantidad) {

        DecimalFormat df3 = new DecimalFormat("#.###");
        df3.setRoundingMode(RoundingMode.DOWN);
        double resultpzasim = Double.parseDouble(cantidad);
        String txtcdinvim = txtcdinv.getText().toString();
        String evartim = evart.getText().toString();
        String evareaim = evarea.getText().toString();
        String mrbtsysteim = mrbtsyste.getText().toString();
        String subalm = txtsuc.getText().toString();
        String zona = txtalm.getText().toString();
        String query = ("proc_Inv_UpdateAudInvmar @Tipo = 'PIEZAS'," +
                "@Cantidad = '" + df3.format(resultpzasim) + "'," +
                "@Marbete = '" + mrbtsysteim + "'," +
                "@Codigo ='" + txtcdinvim + "'," +
                "@Art = '" + evartim + "'," +
                "@Area='" + evareaim + "'," +
                "@Subalm = '" + subalm + "'," +
                "@Zona = '" + zona + "'");

        //String query = ("update invmar set existencia='" + df3.format(resultpzasim) + "', exi_pzas='" + df3.format(resultpzasim) + "' where ibuff='RTF' AND codigo='" + txtcdinvim + "' AND art='" + evartim + "' AND Area='" + evareaim + "' and marbete='" + mrbtsysteim + "'");
        try {
            queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("actcantidadmbtpzasinvmar: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }


    //-- Actualiza la cantidad auditada en cajas << tblinventariodet_tic >>
    public void actcantidadmbtcajas(String cantidad) {
        String mrbtsystetmp = mrbtsyste.getText().toString();
        DecimalFormat df3 = new DecimalFormat("#.###");
        df3.setRoundingMode(RoundingMode.DOWN);
        double resultcjs = Double.parseDouble(cantidad);
        String codInv = txtcdinv.getText().toString();
        String logusrs3xx = evusrcrt1.getText().toString();

        //String query = ("update tblinventariodet_tic set audexistencia='" + resultcjs + "',audito='" + logusrs3xx + "',fechahraud='" + currentDateandTimexx + "',auddif='" + diferenciascjs1 + "',capexistencia='" + resultpzasexaud + "' where marbetsys='" + mrbtsystetmp + "'");
        String query = "proc_Inv_UpdateAud @resultpzas = '" + df3.format(resultcjs) + "', @logusrs3x = '" + logusrs3xx + "'," +
                "@mrbtsystetmp = '" + mrbtsystetmp + "', @codigoinv = '" + codInv + "'";

        try {
            queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("actcantidadmbtcajas: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }

    //-- Actualiza la cantidad auditada en piezas << INVMAR >>
    public void actcantidadmbtcajasinvmar(String cantidad) {
        DecimalFormat df3 = new DecimalFormat("#.###");
        df3.setRoundingMode(RoundingMode.DOWN);
        double resultcjssim = Double.parseDouble(cantidad);
        String txtcdinvim = txtcdinv.getText().toString();
        String evartim = evart.getText().toString();
        String evareaim = evarea.getText().toString();
        String marb = mrbtsyste.getText().toString();
        String subalm = txtsuc.getText().toString();
        String zona = txtalm.getText().toString();
        String query = ("proc_Inv_UpdateAudInvmar @Tipo = 'CAJA'," +
                "@Cantidad = '" + df3.format(resultcjssim) + "'," +
                "@Marbete = '" + marb + "'," +
                "@Codigo ='" + txtcdinvim + "'," +
                "@Art = '" + evartim + "'," +
                "@Area='" + evareaim + "'," +
                "@Subalm = '" + subalm + "'," +
                "@Zona = '" + zona + "'");
        try {
            queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("actcantidadmbtcajasinvmar: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }


    //-- Actualiza es estado a abierto en auditoria  --
    public void actualizarlogsaudi() {
        String logcodinv3 = txtcdinv.getText().toString();
        String logarea3 = evarea.getText().toString();
        String logusrs3 = evusrcrt1.getText().toString();
        String query = ("update tblinventario_tic set audasig='2',audito='" + logusrs3 + "' where ninventario='" + logcodinv3 + "' AND area='" + logarea3 + "' AND audasig='1'");
        try {
            queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("actualizarlogsaudi: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }

    // **************************************************************
    // ** Actualiza la hora y fecha y estado a abierto del auditor **
    // **************************************************************
    public void actualizarlogsaudix() {
        String logcodinv3x = txtcdinv.getText().toString();
        String logarea3x = evarea.getText().toString();
        String logusrs3x = evusrcrt1.getText().toString();
        String query = ("update tblinventario_tic set audasig='3',audito='" + logusrs3x + "' where ninventario='" + logcodinv3x + "' AND area='" + logarea3x + "' AND audasig='2'");
        try {
            queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("actualizarlogsaudix: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }


    //-- Actualiza la cantidad auditada en piezas << INVMAR >>
    public void ActErrusrinvmar() {

        String txtcdinvim = txtcdinv.getText().toString();
        String txtalmm = txtalm.getText().toString();
        String txtsucm = txtsuc.getText().toString();
        String evaream = evarea.getText().toString();
        String query = ("update invmar set ibuff='ERR', existencia='0', exi_cjas='0',exi_pzas='0' where ibuff='RTF' AND codigo='" + txtcdinvim + "' And alm='" + txtalmm + "' And salm='" + txtsucm + "' AND Area='" + evaream + "'");
        try {
            queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("ActErrusrinvmar: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }


    //-- Actualiza la cantidad auditada en piezas << INVMAR >>
    public void ActErrusrTBLINVTIC() {

        String txtcdinvim = txtcdinv.getText().toString();
        String evaream = evarea.getText().toString();
        String query = ("update tblinventariodet_tic set ninventario='999999', area='9999', codmarbet='00000000000000' where ninventario='" + txtcdinvim + "' And area='" + evaream + "'");
        try {
            queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("ActErrusrTBLINVTIC: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }

    //-- Habilita el depto para asingar un nuevo usuario en error de captura << tblinventario_tic >>
    public void ActDispUsrDp() {

        String txtcdinvimx = txtcdinv.getText().toString();
        String evareamx = evarea.getText().toString();
        String query = ("update tblinventario_tic set capturo=Null, estado='5',audasig='1', audito=Null, fechahrin_cap=Null, fechahrfi_cap=Null,fechahrin_aud=Null,fechahrfi_aud=Null,sts_auditor=Null " +
                "where ninventario='" + txtcdinvimx + "' And area='" + evareamx + "'");
        try {
            queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("ActDispUsrDp: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }

    //-- Actualiza la fecha y hora de inicio de captura --
    public void actualizarfechraud() {
        String txtcdinvtm = txtcdinv.getText().toString();
        String evareatm = evarea.getText().toString();
        String evusrcrt1tm = evusrcrt1.getText().toString();
        String activonoactivo = "ACTIVO";
        String query = ("update tblinventario_tic set fechahrin_aud=Concat(Convert(char(10),GETDATE(),105),' ',CONVERT(varchar,GETDATE(),8)),sts_auditor='" + activonoactivo + "' where ninventario='" + txtcdinvtm + "' and area='" + evareatm + "' and audito='" + evusrcrt1tm + "'");
        try {
            queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("actualizarfechraud: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }

    // *************************************************************
    // ** actualiza estado cerrado del auditor cuando ya finalizo **
    // *************************************************************
    public void actualizarlogscerrado() {
        String logcodinv3cd = txtcdinv.getText().toString();
        String logarea3cd = evarea.getText().toString();
        String evusrcrt13cd = evusrcrt1.getText().toString();
        String activonoactivo = "NOACTIVO";
        String query = ("update tblinventario_tic set estado='2',fechahrfi_aud=Concat(Convert(char(10),GETDATE(),105),' ',CONVERT(varchar,GETDATE(),8)),sts_auditor='" + activonoactivo + "'  where ninventario='" + logcodinv3cd + "' AND area='" + logarea3cd + "' AND audito='" + evusrcrt13cd + "'");
        try {
            queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("actualizarlogscerrado: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }

    // -----------
    // -- CLEAR --
    // -----------
    //-- Limpia etiquetas
    private void limpiar() {
        try {
            //-- Cuenta los errores
            String numberetinc = etinc.getText().toString();
            double numberetincf = Double.parseDouble(numberetinc);
            if (numberetincf == 4) {
                mesajeerrores();
            }
            evart.setText("---");
            evcodbar.setText("---");
            evcodbardes.setText("---");
            evemp.setText("---");
            evemp2.setText("---");
            evprec.setText("---");
            mrbtsyste.setText("---");
            evfechor.setText("---");
            evcodmbt.requestFocus();
            evcodmbt.selectAll();
            exiscap.setText("---");
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    private void limpiarall() {
        try {
            //-- Cuenta los errores
            String numberetinc = etinc.getText().toString();
            double numberetincf = Double.parseDouble(numberetinc);
            if (numberetincf == 4) {
                mesajeerrores();
            }
            evart.setText("---");
            evcodbar.setText("---");
            evcodbardes.setText("---");
            evemp.setText("---");
            evemp2.setText("---");
            evpiez.setText("");
            evcaj.setText("");
            evprec.setText("---");
            evusrcrt.setText("---");
            mrbtsyste.setText("---");
            evconto.setText("---");
            evfechor.setText("---");
            evcodmbt.requestFocus();
            evcodmbt.selectAll();
            exiscap.setText("---");
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    // **********************
    // ** Limpia etiquetas **
    // **********************
    private void limpiarmbtnoencon() {
        try {
            evart.setText("---");
            evcodbar.setText("---");
            evcodbardes.setText("---");
            evemp.setText("---");
            evemp2.setText("---");
            evprec.setText("---");
            evusrcrt.setText("-");
            mrbtsyste.setText("-");
            evpiez.setText("");
            evcaj.setText("");
            evconto.setText("---");
            evfechor.setText("---");
            exiscap.setText("---"); //55555555555555555555
            //etinc.setText("---");
            //avancess.setText("0");
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }


    // *************
    // ** MESAJES **
    // *************

    // **********************************************
    // ** mesaje de cantidad es correcta en piezas **
    // **********************************************
    private void mesajeCantPza() {
        try {
            String cantidad = evpiez.getText().toString();
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle(evemp.getText().toString() + " DE:" + cantidad);
            dialogo1.setMessage("¿ Esta cantidad es correcta ?");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("Si", (dialogo112, id) -> ConfirmarPza(cantidad));
            dialogo1.setNegativeButton("No", (dialogo11, id) -> NoConfirmarPza());
            dialogo1.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void NoConfirmarPza() {
        try {
            evpiez.setText(null);
            evpiez.requestFocus();
            evpiez.selectAll();
            evpiez.setEnabled(true);
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void ConfirmarPza(String cantidad) {
        try {
            String evartblank1 = evart.getText().toString();
            //-- Graba en la tabla INVMAR en cajas
            if (evartblank1.equals("")) {
                Toast.makeText(MainActivity7.this, "No es posible realizar la operacion", Toast.LENGTH_SHORT).show();
                evpiez.setText(null);
                evcodmbt.requestFocus();
                evcodmbt.selectAll();
            } else if (!evartblank1.equals("")) {
                mbtprintsino = "3";

                //-- Graba en la tabla INVMAR en piezas
                actcantidadmbtpzas(cantidad);
                actcantidadmbtpzasinvmar(cantidad);
                actualizarlogsaudi();


                // -- Fecha y hora de inicio de la AUDITORIA
                bscfecinicap();
                if (fecinicaptura.equals("1")) {
                    // -- Existe NULL
                    // -- Update a tblinventario_tic (fechahrin_aud)
                    actualizarfechraud();
                }

                // *************************************************
                // ** Busca la cantidad de errores del capturista **
                // *************************************************
                bscErrPorc();

                limpiar();
                evpiez.setText("");
            }
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }


    //-- Alerta al seleccionar el CAJAS --
    private void mesajeCantCaj() {
        try {
            String cantidad = evcaj.getText().toString();
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle(cantidad + " " + evemp.getText().toString());
            dialogo1.setMessage("¿ Esta cantidad es correcta ? ");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("Si", (dialogo11, id) -> ConfirmarCaj(cantidad));
            dialogo1.setNegativeButton("No", (dialogo112, id) -> NoConfirmarCaj());
            dialogo1.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void NoConfirmarCaj() {
        try {
            evcaj.setText(null);
            evcaj.requestFocus();
            evcaj.selectAll();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void ConfirmarCaj(String cantidad) {
        try {
            String evartblank = evart.getText().toString();
            //-- Graba en la tabla INVMAR en cajas
            if (evartblank.equals("")) {

                Toast.makeText(MainActivity7.this, "No es posible realizar la operacion", Toast.LENGTH_SHORT).show();
                evcaj.setText(null);
                evcodmbt.requestFocus();
                evcodmbt.selectAll();

            } else {

                //-- Graba en la tabla INVMAR en cajas
                actcantidadmbtcajas(cantidad);
                actcantidadmbtcajasinvmar(cantidad);
                actualizarlogsaudi();

                // -- Fecha y hora de inicio de la AUDITORIA
                bscfecinicap();
                if (fecinicaptura.equals("1")) {
                    // -- Existe NULL
                    // -- Update a tblinventario_tic (fechahrin_aud)
                    actualizarfechraud();
                }

                // *************************************************
                // ** Busca la cantidad de errores del capturista **
                // *************************************************
                bscErrPorc();

                limpiar();
                evcaj.setText("");
            }
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }

    }

    //-- Alerta eL LIMITE DE ERRORES --
    private void mesajeerrores() {
        try {
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle(etinc.getText().toString());
            dialogo1.setMessage(" ¡Se ha llegado al limite de errores en la auditoria!  ");
            dialogo1.setCancelable(false);
            dialogo1.setNegativeButton("OK", (dialogo11, id) -> NoERRORES());
            dialogo1.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    // **************************************
    // ** mensaje mas areas disponibles **
    // **************************************
    private void mesajenewarea() {
        try {
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle("Tiene una nueva área");
            dialogo1.setMessage(" asignada para auditar.  ");
            dialogo1.setCancelable(false);
            dialogo1.setNegativeButton("OK", (dialogo11, id) -> {
                // REGRESAR
            });
            dialogo1.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    // **************************************
    // ** mensaje no mas areas disponibles **
    // **************************************
    private void mesajenonewarea() {
        try {
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle("ALERTA!!");
            dialogo1.setMessage(" No tienes mas areas asignadas.  ");
            dialogo1.setCancelable(false);
            dialogo1.setNegativeButton("OK", (dialogo11, id) -> {
                // REGRESAR
            });
            dialogo1.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }


    public void NoERRORES() {
        // ** imprime el listado al finalizar la auditoria **
        //-- Actualizar tablas invmar Y tblinventariodet_tic
        try {
            ActErrusrinvmar();
            ActErrusrTBLINVTIC();
            ActDispUsrDp();
            //-- Terminar
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            Log.d("MainActivity", "onBackPressed()");
            finish();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    // -------------
    // -- ALERTAS --
    // -------------

    // ********************************************
    // *** Mensaje de codigo de barras no existe **
    // ********************************************
    public void alertass() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Importante");
            builder.setMessage("¡El codigo de barras escaneado no corresponde al area asignada!");
            builder.setPositiveButton("OK", null);
            builder.create();
            builder.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
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
                return rs.getString("art_ser");
            } else {
                savetoLog("verificaempaque: " + SQL + " | No encontró resultados");
                return "";
            }
        } catch (Exception e) {
            savetoLog("verificaempaque: " + SQL + " | " + e.toString());
            return "";
        }
    }

    //-- Regresar al menu principal
    public void onBackPressed(View view) {
        try {
            savetoLog(txtusrname.getText().toString() + " presiona boton terminar");
            //-- Porcentaje de avance no deja salir --//
            String numberAVG = avancess.getText().toString();
            double numberAVGf = Double.parseDouble(numberAVG);
            //-- Salir sin hacer nada
            if (numberAVGf == 0 && evcodinv.getSelectedItem() == null) {
                if (mesajeCierreapp()) {
                    AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
                    dialogo1.setTitle("Alerta!!");
                    dialogo1.setMessage("¿Desea salir de la aplicacion?");
                    dialogo1.setCancelable(false);
                    dialogo1.setPositiveButton("Si", (dialogo11, id) -> {
                        savetoLog(txtusrname.getText().toString() + " presiona boton si para terminar auditoria");
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    });
                    dialogo1.setNegativeButton("No", (dialogo114, id) -> {

                    });
                    dialogo1.show();
                }
                return;
            }
            if (numberAVGf == 0 && evcodinv.getSelectedItem() != null) {
                Toast.makeText(this, "No puedes salir de la aplicacion.\nTienes una area activa", Toast.LENGTH_LONG).show();
                return;
            }

            if (numberAVGf >= 1 && numberAVGf <= 39) {
                //-- Fin
                vibrador();
                evcodmbt.requestFocus();
                evcodmbt.selectAll();
                Toast.makeText(MainActivity7.this, "¡No es posible seleccionar un area nueva!", Toast.LENGTH_SHORT).show();
                return;
            }
            //-- Dejar salir cuando sea mayor a 41
            if (numberAVGf >= 40) {
                if (mesajeCierreapp()) {
                    AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
                    dialogo1.setTitle("Alerta!!");
                    dialogo1.setMessage(R.string.TermArea);
                    dialogo1.setCancelable(false);
                    dialogo1.setPositiveButton("Si", (dialogo113, id) -> {

                        //Actualiza la hora y fecha y estado a abierto del auditor
                        actualizarlogsaudix();
                        //Actualiza estado cerrado del auditor cuando ya finalizo
                        actualizarlogscerrado();

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    });
                    dialogo1.setNegativeButton("No", (dialogo112, id) -> {
                    });
                    dialogo1.show();
                }
            }
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }


    // -- Metodo de Consulta desde el Metodo Global --
    private boolean mesajeCierreapp() {

        //Verifica que se no se tenga areas en proceso cuando se haga un inventario anual
        boolean continuar = false;

        String query = "select * from tblinventario_tic where audito = '" + evusrcrt1.getText().toString() +
                "' and estado = 1 and ninventario = '" + txtcdinv.getText().toString() + "' and fechahrin_aud is null and ninventario in (select codigo from invmar)";
        ResultSet rs = queryDT(query);
        if (rs != null) {
            int t = 0;
            try {
                while (rs.next()) {
                    t++;
                }
                if (t > 0) {
                    /* El usuario aun tiene areas por capturar */
                    Toast to = Toast.makeText(getBaseContext(), "El usuario aun tiene " + t + " area(s) por Auditar.\nNo puede salir", Toast.LENGTH_LONG);
                    to.show();
                    continuar = false;
                } else {
                    continuar = true;
                }
            } catch (SQLException e) {
                savetoLog("mesajeCierreapp | " + e.toString());
                e.printStackTrace();
            }
        }
        return continuar;
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
    private Connection CONN(String _user, String _pass, String _DB, String _server) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
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
            savetoLog("listadomrbtoaud: " + se.getMessage() + " | " + se.toString());
        }
        return conn;
    }

    // --  conexion a WIFI ---
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

    private void onNetworkChange(NetworkInfo networkInfo) {
        //- Encuentra conexion a Wifi
        if (networkInfo != null) {

            if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                String network = networkInfo.getExtraInfo();
                etconec.setText(getString(R.string.RedConectada, network));
                etconec.setTextColor(Color.parseColor("#186A3B"));
                hideProgressDialogWithTitle();
            }
            //- No encuentra conexion a Wifi
        } else {
            etconec.setText(R.string.RedDesconectada);
            etconec.setTextColor(Color.parseColor("#E74C3C"));
            //Intent iii = new Intent(this, MainActivity.class);
            //startActivity(iii);
            //Muestra el dialogo de espera
            showProgressDialogWithTitle("Espere...");
        }
    }

    // Method to show Progress bar
    private void showProgressDialogWithTitle(String substring) {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //Without this user can hide loader by tapping outside screen
        progressDialog.setCancelable(false);
        progressDialog.setMessage(substring);
        progressDialog.show();
    }

    // Method to hide/ dismiss Progress bar
    private void hideProgressDialogWithTitle() {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.dismiss();
    }


    private void vibrador() {
        //Compruebe si dispositivo tiene un vibrador.
        if (vibrator.hasVibrator()) {        //Si tiene vibrador
            long tiempo = 500; //en milisegundos
            vibrator.vibrate(tiempo);
        } else {//no tiene
            savetoLog("El dispositivo no tiene vibracion");
            Log.v("VIBRATOR", "Este dispositivo NO puede vibrar");
        }
    }

    // -- Mascara de 2 decimales
    static class DecimalDigitsInputFilter implements InputFilter {
        private final Pattern mPattern;

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

    // ********************
    // ** ICONO flotante **
    // ********************
    private void iracaptura() {
        try {
            // -- Formulario contador o auditor
            Intent i = new Intent(this, MainActivity8.class);
            startActivity(i);

            // -- lleva el valor a otra actividad
            Intent intent = new Intent(this, MainActivity8.class);
            intent.putExtra("variable_txtcdinv", txtcdinv.getText());
            intent.putExtra("variable_txtalm", txtalm.getText());
            intent.putExtra("variable_txtsuc", txtsuc.getText());
            intent.putExtra("variable_evarea", evarea.getText());
            startActivity(intent);
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void botonrefrr(View view) {
        llenarspinner();
    }

    // --------------------------------------------------------
    // -- Llena el spinner con las areas disponibles ----------
    // --------------------------------------------------------
    public void llenarspinner() {
        newareaasignada = "0";

        // -- No es programacion del dashboar
        // -- Sentencia SQL para llenar el spinner
        String txtcdinvtemp = txtcdinv.getText().toString();
        String usrcvecortatemp = evusrcrt1.getText().toString();

        //String query = "Select area from tblinventario_tic where ninventario='" + txtcdinvtemp + "' and audito='" + usrcvecortatemp + "' and audasig='2' and fechahrfi_cap is not null";
        String query = "Select area from tblinventario_tic where ninventario='" + txtcdinvtemp + "' and audito='" + usrcvecortatemp + "' and audasig='2' and fechahrfi_cap is not null and ninventario in (select codigo from invmar)";
        ArrayList<String> data = new ArrayList<String>();
        try {
            connect = CONN(un, passwords, db, ip);
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            //ArrayList<String> data = new ArrayList<String>();

            //-- Llena el Spinner
            if (rs.next()) {
                String id = rs.getString("area");
                data.add(id);
                newareaasignada = "1";

            } else {
                Toast.makeText(this, "No tienes areas disponibles para auditar", Toast.LENGTH_LONG).show();
            }

            //-- Creamos el ArrayAdapter que necesitará el spinner, dándole como parámetros, (Contexto, referencia al layout de elemento, valores (opciones)
            ArrayAdapter NoCoreAdapter = new ArrayAdapter(this,
                    android.R.layout.simple_list_item_1, data);
            evcodinv.setAdapter(NoCoreAdapter);

        } catch (SQLException e) {

            savetoLog("llenarspinner: " + query + " | " + e.toString());
            e.printStackTrace();
        }

        btnrefresh.setEnabled(data.size() <= 0);

        //-- indicamos al Spinner que utiliza el Adapter recien creado
        evcodinv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //-- Detecta la posicion del Items
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    msges = position;
                    if (msges == 0) {
                        evarea.setText(evcodinv.getSelectedItem().toString());
                    } else if (msges > 0) {
                        evarea.setText(evcodinv.getSelectedItem().toString());
                    }
                } catch (Exception ex) {
                    savetoLog(ex.toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

    }

    // *******************************************************
    // **  llena el items al selecionar el codigo de barras **
    // *******************************************************
    public class UserListAdapter extends ArrayAdapter<AssignmentUserModel> {
        private final Context context;
        private final List<AssignmentUserModel> userList;

        public UserListAdapter(@NonNull Context context, int resource, @NonNull List<AssignmentUserModel> objects) {
            super(context, resource, objects);
            userList = objects;
            this.context = context;
        }

        @SuppressLint("ViewHolder")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.item_assignment_dialog_list_layout, parent, false);
            TextView id = rowView.findViewById(R.id.idMarbete);
            TextView cant = rowView.findViewById(R.id.Cant);
            TextView idCapAud = rowView.findViewById(R.id.idCapAud);
            AssignmentUserModel user = userList.get(position);

            id.setText("MK:" + user.getId());
            cant.setText("CAPT:" + user.getCant());
            idCapAud.setText("AUD:" + user.getidCapAud());
            return rowView;
        }

    }


    private void showCustomAlertDialogBoxForUserList(ArrayList<AssignmentUserModel> allUsersList) {
        try {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.assignment_dialog_list_view);
            // -- Evita que alertdialog.builder se cierre si no se selecciona una opcion
            dialog.setCancelable(false);
            ListView listView = dialog.findViewById(R.id.lv_assignment_users);
            ArrayAdapter arrayAdapter = new UserListAdapter(getApplicationContext(), R.layout.item_assignment_dialog_list_layout, allUsersList);
            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener((adapterView, view, which, l) -> {
                mrbtsyste.setText(allUsersList.get(which).getId());
                exiscap.setText(allUsersList.get(which).getCant());
                evconto.setText(allUsersList.get(which).getUserConto());
                evfechor.setText(allUsersList.get(which).getFecuserConto());
                switch (allUsersList.get(which).getTipocap()) {
                    case "1": /* Piezas */
                        etpiez.setTextColor(Color.parseColor("#186A3B"));
                        evpiez.setEnabled(true);
                        evpiez.setText("");
                        evpiez.requestFocus();
                        etcaj.setTextColor(Color.parseColor("#080808"));
                        evcaj.setEnabled(false);
                        evcaj.setText("0.00");
                        break;
                    case "2": /* Cajas */
                        etcaj.setTextColor(Color.parseColor("#186A3B"));
                        evcaj.setEnabled(true);
                        evcaj.setText("");
                        evcaj.requestFocus();
                        etpiez.setTextColor(Color.parseColor("#080808"));
                        evpiez.setEnabled(false);
                        evpiez.setText("0.00");
                        break;
                    default:
                        evpiez.setEnabled(false);
                        evcaj.setEnabled(false);
                        break;
                }
                dialog.dismiss();
            });
            dialog.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public Connection conexionBD() {
        Connection conexion = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            conexion = DriverManager.getConnection("jdbc:jtds:sqlserver://" + ip + ";" + "databaseName=" + db + ";user=" + un + ";password=" + passwords + ";");
        } catch (Exception e) {
            savetoLog("conexionBD: " + e.getMessage() + " | " + e.toString());
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return conexion;
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

    // -- Consulta con Metodo con Metodo Global --
    public ResultSet queryDT(String query) {
        Connection con = null;
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


    // -- Update o Insert con Metodo Global --
    public Boolean queryUpdIns(String query) {
        Connection con;
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
                savetoLog("queryUpdIns | query: " + query + " |" + s.toString());
                return false;
            }
        } catch (Exception e) {
            dialogerror(e.toString());
            savetoLog("queryUpdIns | query: " + query + " |" + e.toString());
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

}