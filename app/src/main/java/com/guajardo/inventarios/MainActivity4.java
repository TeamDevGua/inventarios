package com.guajardo.inventarios;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tscdll.TSCActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
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
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity4 extends AppCompatActivity {
    private TextView txtusrname, usrcvecorta, txtcdinv, txtalm, txtsuc;
    private EditText evbusqueda;
    private TextView evdesc, evart, evpzass, evpzascant, evprecio;
    private TextView etcajas, etpiezas;
    private EditText evcajas, evpza;

    private EditText evcodbar;
    private TextView evcodbar_tmp;

    private EditText evarea;
    private TextView Last_bmt;

    // -- Llena el spinner
    private Spinner evcodinv;
    // -- Muestra la conexion de la red
    private TextView etconec;

    private ImageButton btnrefresh;
    private TextView txtcodarea;

    private ProgressDialog progressDialog;

    //-- conexiones SQL
    String ip, db, un, passwords;
    Integer msges;

    // **********************
    //String areausractivo;

    String rbcajtm1;
    String rbpzastm1;

    String limiteCantidadArea = "10000";
    String activonoactivo;
    String activonoactivoMismousr;
    String limiteCantidadpzs;
    String limiteCantidadcajs;
    Button btnacc2;

    String varcdinvact;
    String varcdinvact0;

    Integer folioBuscdinvactmax;
    String mbtsfolio;
    String tipocaptura;

    //-- determina si el marbete fue impreso al salir
    String mbtprintsino = "0";
    //-- Determina si el contador fue registrado desde el dashborad
    String dasbprogcontador = "0";
    //-- Detecta si aun existen areas que auditar
    String newareaasignada = "0";

    double resultcantidadpzscjs;
    //-- Determina si el contador fue registrado desde el dashborad
    String fecinicaptura = "0";

    String tipodetomainv = "0";

    //-- convierte el valor a pesos
    NumberFormat formatoImporte = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    //-- numero cerrado
    //-- valor con decimales
    DecimalFormat df = new DecimalFormat("####0.000");
    // Obtiene instancia a Vibrator
    private Vibrator vibrator;

    TSCActivity TscDll = new TSCActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        savetoLog("Formulario Captura abierto");
        // -- Titulo de la aplicacion
        setTitle("C A P T U R A");

        // Obtiene instancia a Vibrator
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //-- Variables
        txtusrname = findViewById(R.id.txtusrname);
        usrcvecorta = findViewById(R.id.usrcvecorta);
        txtcdinv = findViewById(R.id.txtcdinv);
        txtalm = findViewById(R.id.txtalm);
        txtsuc = findViewById(R.id.txtsuc);

        ImageButton btnBorraCod = findViewById(R.id.btnBorraCod);

        // ------------------
        // -- PREFERENCIAS --
        // ------------------
        // -- Codigo de inventario
        SharedPreferences prefe = getSharedPreferences("conf_codinv", Context.MODE_PRIVATE);
        txtusrname.setText(prefe.getString("nameusr", ""));
        usrcvecorta.setText(prefe.getString("codeusr", ""));
        txtcdinv.setText(prefe.getString("codinvt", ""));
        txtalm.setText(prefe.getString("alm", ""));
        txtsuc.setText(prefe.getString("zona", ""));

        //Se agrega para obtener los limites de cantidades en piezas y cajas
        SharedPreferences limites = getSharedPreferences("limites", Context.MODE_PRIVATE);
        limiteCantidadpzs = limites.getString("limPiezas", "10000");
        limiteCantidadcajs = limites.getString("limCajas", "10000");

        etcajas = findViewById(R.id.etcajas);
        etpiezas = findViewById(R.id.etpiezas);
        //-- Define si la captura es en piezas o cajas
        SharedPreferences prefe2 = getSharedPreferences("conf_cjspzs", Context.MODE_PRIVATE);
        rbcajtm1 = prefe2.getString("rbcajtm", "");
        rbpzastm1 = prefe2.getString("rbpzstm", "");
        tipocaptura = "0";
        if (rbcajtm1.equals("true")) {
            etcajas.setTextColor(Color.parseColor("#186A3B"));
            etpiezas.setTextColor(Color.parseColor("#080808"));
            tipocaptura = "2";
        }

        if (rbpzastm1.equals("true")) {
            etpiezas.setTextColor(Color.parseColor("#186A3B"));
            etcajas.setTextColor(Color.parseColor("#080808"));
            tipocaptura = "1";
        }

        // -- Lupa
        evbusqueda = findViewById(R.id.evbusqueda);
        evbusqueda.setEnabled(false);
        evbusqueda.setVisibility(View.GONE);

        evdesc = findViewById(R.id.evdesc);
        evart = findViewById(R.id.evart);
        evpzass = findViewById(R.id.evpzass);
        evpzascant = findViewById(R.id.evpzascant);
        evprecio = findViewById(R.id.evprecio);

        evcodbar = findViewById(R.id.evcodbar);
        evcodbar.setLongClickable(false);
        evcodbar.setSelectAllOnFocus(true);
        evcodbar.setEnabled(false);
        evcodbar_tmp = findViewById(R.id.evcodbar_tmp);

        evcajas = findViewById(R.id.evcajas);
        evpza = findViewById(R.id.evpza);
        evcajas.setEnabled(false);
        evpza.setEnabled(false);
        // -- Mascara de 2 decimales
        evpza.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6, 3)});
        evcajas.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6, 3)});
        evarea = findViewById(R.id.evarea);
        btnacc2 = findViewById(R.id.btnacc7);
        etconec = findViewById(R.id.etconec);
        evcodinv = findViewById(R.id.evcodinv);

        btnrefresh = findViewById(R.id.btnrefresh);

        txtcodarea = findViewById(R.id.txtcodarea);
        txtcodarea.setTextColor(Color.parseColor("#FFFFFF"));

        Last_bmt = findViewById(R.id.Last_bmt);

        /* Desactiva completamente el teclado en el EditText evcodbar */
        evcodbar.setShowSoftInputOnFocus(false);

        /* Esconde el teclado cuando el EditText evcodbar obtiene el foco */
        evcodbar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(evcodbar.getWindowToken(), 0);
            }
        });

        /* Trae la informacion grabada de las preferencias */
        SharedPreferences pref = getSharedPreferences("conf_red", Context.MODE_PRIVATE);
        ip = pref.getString("server", "");
        db = pref.getString("database", "");
        un = pref.getString("user", "");
        passwords = pref.getString("password", "");

        // -----------------------
        // -- Ocurre al INICIAR --
        // -----------------------
        // -- Trae las preferencias del tipo de toma del inventario
        SharedPreferences prefx1 = getSharedPreferences("conf_tipoinv", Context.MODE_PRIVATE);
        tipodetomainv = prefx1.getString("conftipoinv", "");

        // -- 01 dic
        // -- Toma de ciclicos -- Revisado
        // -- Habilita la captura de area manual
        if (tipodetomainv.equalsIgnoreCase("1")) {
            evcodinv.setVisibility(View.GONE);
            btnrefresh.setVisibility(View.GONE);
            evarea.setVisibility(View.VISIBLE);
            evarea.setEnabled(true);
            evarea.selectAll();
            evarea.requestFocus();

            evbusqueda.setEnabled(false);

            if (rbcajtm1.equals("true")) {
                //evcajas.setEnabled(true);
                evpza.setText("0.00");
            }

            if (rbpzastm1.equals("true")) {
                //evpza.setEnabled(true);
                evcajas.setText("0.00");
            }
        }

        // -- Toma anual
        // -- Habilita la captura en SPINNER Y oculta la captura manual
        if (tipodetomainv.equalsIgnoreCase("2")) {

            // -- Llena es spinner
            llenarspinner();

            evarea.setEnabled(false);
            evcodinv.setVisibility(View.VISIBLE);
            evarea.setVisibility(View.GONE);
            btnrefresh.setVisibility(View.VISIBLE);
            btnacc2.setEnabled(false);
            evcodinv.setBackgroundColor(Color.parseColor("#98FB98"));

            if (rbcajtm1.equals("true")) {
                //evcajas.setEnabled(true);
                evpza.setText("0.00");
            }

            if (rbpzastm1.equals("true")) {
                //evpza.setEnabled(true);
                evcajas.setText("0.00");
            }
        }

        // ---------------------
        // -- OCURRE AL ENTER --
        // ---------------------
        // -- Claves: corta, larga o alterna
        // -- La accion despues al ENTER
        evbusqueda.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                try {
                    //Obtiene el codigo de barras
                    String codigo = evbusqueda.getText().toString();
                    //Otiene almacen
                    String almacen = txtsuc.getText().toString();
                    //Obtiene salm
                    String salm = txtalm.getText().toString();

                    //BuscarArticulo();
                    //Ejecuta consulta
                    Articulo art = ValidarArticulo(codigo, almacen, salm);
                    if (art != null) {
                        // -- Limpia las etiquetas --
                        limpiardespuesdecodigobar();
                        //Set articulo
                        evart.setText(String.valueOf(art.cve));
                        //Muestra nombre del articulo
                        evdesc.setText(art.descripcion);
                        //Muestra precio
                        evprecio.setText(formatoImporte.format(Double.parseDouble(art.precio)));
                        //Guarda clave art
                        evcodbar_tmp.setText(codigo);
                        //Unidades
                        evpzass.setText(art.unidad);
                        //Empaque
                        evpzascant.setText(df.format(Double.parseDouble(art.empaque)));
                        //Posiciona el cursos en cajas o piezas
                        colorestext();
                        if (rbcajtm1.equals("true")) {
                            evcajas.setText("");
                            evcajas.requestFocus();
                            evcajas.setEnabled(true);
                            evpza.setEnabled(false);
                        }
                        if (rbpzastm1.equals("true")) {
                            evpza.setText("");
                            evpza.setEnabled(true);
                            evpza.requestFocus();
                            evcajas.setEnabled(false);
                        }
                    } else {
                        evdesc.setText(R.string.NoEncontrado);
                        evdesc.setTextColor(Color.parseColor("#E74C3C"));
                        evcodbar.requestFocus();
                        evcodbar.selectAll();
                        evart.setText("");
                        evpzass.setText("");
                        evpzascant.setText("");
                        evprecio.setText("");
                        evcodbar_tmp.setText("");
                        evpza.setEnabled(false);
                        evcajas.setEnabled(false);
                        Toast.makeText(MainActivity4.this, "No existe el articulo", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    savetoLog(ex.toString());
                }
                //-- Termina
                return true;
            }
            return false;
        });
        // -----------------------------------
        // -- OCURRE EN AUTOMATICO EL ENTER --
        // -----------------------------------
        // -- Claves: corta, larga o alterna
        // -- La accion despues de escanear el codigo de barras

        evcodbar.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                try {
                    //Obtiene el codigo de barras
                    String codigo = evcodbar.getText().toString();
                    //Otiene almacen
                    String almacen = txtsuc.getText().toString();
                    //Obtiene salm
                    String salm = txtalm.getText().toString();
                    //Ejecuta consulta
                    Articulo art = ValidarArticulo(codigo, almacen, salm);
                    if (art != null) {
                        // -- Limpia las etiquetas --
                        limpiardespuesdecodigobar();
                        //Set articulo
                        evart.setText(String.valueOf(art.cve));
                        //Muestra nombre del articulo
                        evdesc.setText(art.descripcion);
                        //Muestra precio
                        evprecio.setText(formatoImporte.format(Double.parseDouble(art.precio)));
                        //Guarda clave art
                        evcodbar_tmp.setText(codigo);
                        //Unidades
                        evpzass.setText(art.unidad);
                        //Empaque
                        evpzascant.setText(df.format(Double.parseDouble(art.empaque)));
                        //Posiciona el cursos en cajas o piezas
                        colorestext();
                        if (rbcajtm1.equals("true")) {
                            evcajas.setText("");
                            evcajas.setEnabled(true);
                            evcajas.requestFocus();
                            evpza.setEnabled(false);
                            return true;
                        }
                        if (rbpzastm1.equals("true")) {
                            evcajas.setEnabled(false);
                            evpza.setEnabled(true);
                            evpza.requestFocus();
                            evpza.setText("");
                            return true;

                        }
                    } else {
                        evdesc.setText(R.string.NoEncontrado);
                        evdesc.setTextColor(Color.parseColor("#E74C3C"));
                        evcodbar.requestFocus();
                        evcodbar.selectAll();
                        evart.setText("");
                        evpzass.setText("");
                        evpzascant.setText("");
                        evprecio.setText("");
                        evcodbar_tmp.setText("");
                        evpza.setEnabled(false);
                        evcajas.setEnabled(false);
                        Toast.makeText(MainActivity4.this, "No existe el articulo", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    savetoLog(ex.toString());
                }
            } else {
                //no es una tecla Intro; deje que otros manejen el evento
                return false;
            }
            return false;
        });
        // -- Obligatorio
        // -- Cerrar ciclo para dar por teminada el area de capturar
        btnacc2.setOnClickListener(view -> {
            try {
                //Graba cuando se presione la nueva area
                savetoLog(txtusrname.getText().toString() + " presiona boton nueva area");
                AlertDialog.Builder diaConfirmar = new AlertDialog.Builder(MainActivity4.this);
                diaConfirmar.setTitle("Nueva area");
                diaConfirmar.setMessage("¿Deseas continuar al area siguiente? ");
                diaConfirmar.setCancelable(false);
                diaConfirmar.setPositiveButton("Si", (dialogo1, id) -> {
                    //Graba cuando se confirme el guardar
                    savetoLog(txtusrname.getText().toString() + " presiona boton confirmar");
                    if (Last_bmt.getText().toString().trim().equalsIgnoreCase("0")) {
                        llenarspinner();
                    } else {
                        // -- Si no tiene capturas no Cerrar --
                        // -- Si tiene captura cerrar
                        // -- Busca que el usuario logeado tenga un area abierta si es si lo muestra
                        btnrefresh.setEnabled(true);
                        evcodinv.setEnabled(true);
                        btnacc2.setEnabled(false);
                        // -- en caso que no es por programacion de dashboard
                        // -- Si no tiene capturas no Cerrar --
                        // -- Si tiene captura cerrar
                        actualizarlogscerrado();
                        limpiarnewarea();
                        if (tipodetomainv.equalsIgnoreCase("2")) {
                            //ResultSet rs = queryDT("select * from tblinventario_tic where capturo = '" + usrcvecorta.getText().toString() + "' and estado = 1 and ninventario = '" + txtcdinv.getText().toString() + "'");
                            ResultSet rs = queryDT("select * from tblinventario_tic where capturo = '" + usrcvecorta.getText().toString() + "' and estado = 1 and ninventario = '" + txtcdinv.getText().toString() + "' and ninventario in (select codigo from invmar)");
                            if (rs != null) {
                                int t = 0;
                                try {
                                    while (rs.next()) {
                                        t++;
                                    }
                                    if (t > 0) {
                                        /* El usuario aun tiene areas por capturar */
                                        newareaasignada = "1";
                                    } else {
                                        newareaasignada = "0";
                                    }
                                } catch (SQLException e) {
                                    savetoLog("btnacc2onclick | " + e.toString());
                                    e.printStackTrace();
                                }
                            } else {
                                newareaasignada = "0";
                            }
                        }
                        Last_bmt.setText("0");
                        // -- alerta de area asignadas

                        if (tipodetomainv.equalsIgnoreCase("1")) {
                            try {
                                AlertDialog.Builder d = new AlertDialog.Builder(this);
                                d.setTitle(R.string.Alerta);
                                d.setMessage("Ingrese una nueva area");
                                d.setCancelable(false);
                                d.setNegativeButton("OK", (dialogo11, da) -> {
                                    evarea.requestFocus();
                                });
                                d.show();
                            } catch (Exception ex) {
                                savetoLog(ex.toString());
                            }
                        } else if (tipodetomainv.equalsIgnoreCase("2")) {
                            if (newareaasignada.equals("0")) { // -- no encontrado
                                mesajenonewarea();
                            } else if (newareaasignada.equals("1")) {// -- encontrado
                                mesajenewarea();
                            }
                        }


                    }
                });
                diaConfirmar.setNegativeButton("No", (dialogo1, id) -> {
                });
                diaConfirmar.show();
            } catch (Exception ex) {
                savetoLog(ex.toString());
            }
        });

        // -- (Campo area) valida que el area no este abierta por otro usuario
        // -- (Campo area) valida que el area no este cerrada
        // -- La accion despues del Enter
        evarea.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                try {
                    //-- Detecta que el campo de buscar no este en blanco
                    String evareabco = evarea.getText().toString();
                    if (evareabco.equals("")) {
                        Toast.makeText(MainActivity4.this, "Espacio esta en blanco, no valido!!!", Toast.LENGTH_SHORT).show();
                        evarea.setText("");
                        evarea.requestFocus();
                        evarea.selectAll();
                        return true;
                    }
                    // -- Busca la catidad maxima a aceptar
                    //Buscarlimarea();
                    String number1 = evarea.getText().toString();
                    String numbre2 = limiteCantidadArea;
                    int result1 = Integer.parseInt(number1);
                    int result2 = Integer.parseInt(numbre2);
                    // -- Valida que el valor sea mayor a ceros
                    if (result1 == 0) {
                        Toast.makeText(MainActivity4.this, "El valor del area debe ser mayor a cero", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    // -- Valida que el valor no sea mayor a lo permitido
                    if (result1 > result2) {
                        Toast.makeText(MainActivity4.this, "El valor del area debe ser menor a " + result2, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    //-- Valida que el valor no se mayor a lo permitido
                    if (result1 <= result2) {
                        //Busco que no este activo el codigo de area
                        Buscaractivonoactivo();
                        String actnocts = activonoactivo;
                        int actnoctsss = Integer.parseInt(actnocts);
                        //-- Area existe pero esta Activa
                        if (actnoctsss == 1) {
                            //-- Validar que sea el mismo usuario
                            //-- Que sea igual al codigo de inventario, area y usuario
                            //-- No grabar de nuevo como log
                            BuscaractivonoactivoMismousr();
                            String actnoctsMismoUsr = activonoactivoMismousr;
                            int actnoctsssMismoUsr = Integer.parseInt(actnoctsMismoUsr);
                            if (actnoctsssMismoUsr == 1) {
                                limpiardespuesdearea();
                                mesajearea2();
                            }
                            //-- Esta activo pero no hay coincidencia, esta activo por otro usuario
                            // **** PASA OK ***
                            if (actnoctsssMismoUsr == 3) {
                                mesajearea4();
                            }
                        }
                        //-- Area si existe pero esta cerrado **** PASA OK ***
                        if (actnoctsss == 2) {
                            //-- volver a capturar y grabar como log
                            //-- Mensaje de alerta area capturada desea abrirla
                            vibrador();
                            limpiardespuesdearea();
                            mesajearea3();
                        }
                        //-- El area no existe grabar por primera vez **** PASA OK ***
                        //-- incluye log
                        if (actnoctsss == 3) {
                            limpiardespuesdearea();
                            mesajearea();
                        }
                    }
                } catch (Exception ex) {
                    savetoLog(ex.toString());
                }
                //-- Termina
                return true;
            }
            return false;
        });
        // -- (Campo area) valida que el area no este abierta por otro usuario
        // -- (Campo area) valida que el area no este cerrada
        // -- La accion despues del Enter
        evpza.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                try {
                    //-- Detecta si es Punto
                    String evpzapunto = evpza.getText().toString();
                    if (evpzapunto.equals(".")) {
                        vibrador();
                        Toast.makeText(MainActivity4.this, "Este articulo no permite el simbolo de punto", Toast.LENGTH_SHORT).show();
                        evpza.setText(null);
                        evpza.requestFocus();
                        evpza.selectAll();
                        return true;
                    }
                    //-- Detecta que el campo de buscar no este en blanco
                    String evareabco = evpza.getText().toString();
                    if (evareabco.equals("")) {
                        vibrador();
                        Toast.makeText(MainActivity4.this, "Este articulo no permite espacios en blanco", Toast.LENGTH_SHORT).show();
                        evpza.setText(null);
                        evpza.requestFocus();
                        evpza.selectAll();
                        return true;
                    }
                    if (evpzass.getText().toString().trim().equalsIgnoreCase("---")) {
                        vibrador();
                        Toast.makeText(MainActivity4.this, "Falta algun campo", Toast.LENGTH_SHORT).show();
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
                        vibrador();
                        Toast.makeText(MainActivity4.this, "Este articulo no permite decimales", Toast.LENGTH_SHORT).show();
                        evpza.setText(null);
                        evpza.requestFocus();
                        evpza.selectAll();
                        return true;
                    }
                    //-- Busca el limite de piezas
//                    double result3 = Double.parseDouble(evpza.getText().toString());
//                    double result5 = Double.parseDouble(limiteCantidadpzs);
                    BigDecimal result3 = new BigDecimal(evpza.getText().toString());
                    BigDecimal result5 = new BigDecimal(limiteCantidadpzs);


                    //-- Valida que el valor sea mayor a ceros
                    if (result3.compareTo(BigDecimal.ZERO) == 0) {
                        vibrador();
                        Toast.makeText(MainActivity4.this, "Este articulo no permite cantidades en 0", Toast.LENGTH_SHORT).show();
                        evpza.setText(null);
                        evpza.requestFocus();
                        evpza.selectAll();
                        return true;
                    }
                    //-- Valida que el valor no sea mayor a lo permitido
                    if (result3.compareTo(result5) > 0) { //0 = Igual que, 1 = Mayor que, -1 = Menor que
                        vibrador();
                        Toast.makeText(MainActivity4.this, "Este articulo NO permite cantidades mayores a " + result5, Toast.LENGTH_SHORT).show();
                        evpza.setText(null);
                        evpza.requestFocus();
                        evpza.selectAll();
                        return true;
                    }
                    //-- Valida que el valor no se mayor a lo permitido
                    if (result3.compareTo(result5) <= 0) {
                        evpza.setEnabled(false);
                        evpza.clearFocus();
                        evcajas.setEnabled(false);
                        evcajas.clearFocus();
                        evpza.requestFocus();
                        mesajeCantPza();
                    }

                    //-- Termina
                } catch (Exception ex) {
                    savetoLog(ex.toString());
                }
                return true;
            }
            return false;
        });

        // -- (Campo area) valida que el area no este abierta por otro usuario
        // -- (Campo area) valida que el area no este cerrada
        // -- La accion despues del Enter
        evcajas.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                try {
                    //-- Detecta si es Punto
                    String evcajaspunto = evcajas.getText().toString();
                    if (evcajaspunto.equals(".")) {
                        vibrador();
                        Toast.makeText(MainActivity4.this, "Este articulo no permite el simbolo de punto", Toast.LENGTH_SHORT).show();
                        evcajas.setText(null);
                        evcajas.requestFocus();
                        evcajas.selectAll();
                        return true;
                    }
                    //-- Detecta que el campo de buscar no este en blanco
                    String evcajasbco = evcajas.getText().toString();
                    if (evcajasbco.equals("")) {
                        vibrador();
                        Toast.makeText(MainActivity4.this, "Este articulo no permite espacios en blanco", Toast.LENGTH_SHORT).show();
                        evcajas.setText(null);
                        evcajas.requestFocus();
                        evcajas.selectAll();
                        return true;
                    }
                    if (evpzass.getText().toString().trim().equalsIgnoreCase("---")) {
                        vibrador();
                        Toast.makeText(MainActivity4.this, "Falta algun campo", Toast.LENGTH_SHORT).show();
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
                        vibrador();
                        Toast.makeText(MainActivity4.this, "Este articulo no permite decimales", Toast.LENGTH_SHORT).show();
                        evcajas.setText(null);
                        evcajas.requestFocus();
                        evcajas.selectAll();
                        return true;
                    }
                    //-- Busca el limite de cajas
                    String number6 = evcajas.getText().toString();
                    String numbre7 = limiteCantidadcajs;
                    double result6 = Double.parseDouble(number6);
                    double result7 = Double.parseDouble(numbre7);
                    //-- Valida que el valor sea mayor a ceros
                    if (result6 == 0) {
                        vibrador();
                        Toast.makeText(MainActivity4.this, "Este articulo no permite el cero", Toast.LENGTH_SHORT).show();
                        evcajas.setText(null);
                        evcajas.requestFocus();
                        evcajas.selectAll();
                        return true;
                    }
                    //-- Valida que el valor no se mayor a lo permitido
                    if (result6 > result7) {
                        vibrador();
                        Toast.makeText(MainActivity4.this, "Este articulo permite cantidades mayores a " + result7, Toast.LENGTH_SHORT).show();
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
                        evpza.requestFocus();
                        mesajeCantCaj();
                    }
                    //-- Termina
                } catch (Exception ex) {
                    savetoLog(ex.toString());
                }
                return true;
            }
            return false;
        });
        // -- No es programacion del dashboarf
        // -- Sentencia SQL para llenar el spinner
        // -- Inicio
        String txtcdinvtemp = txtcdinv.getText().toString();
        String usrcvecortatemp = usrcvecorta.getText().toString();
//        String query = "Select area from tblinventario_tic where ninventario='" + txtcdinvtemp + "' and capturo='" + usrcvecortatemp + "' and estado='1'";
        String query = "Select area from tblinventario_tic where ninventario='" + txtcdinvtemp + "' and capturo='" + usrcvecortatemp + "' and estado='1' and ninventario in (select codigo from invmar)";

        try {
            ResultSet n = queryDT(query);
            ArrayList<String> data = new ArrayList<String>();
            //-- Llena el Spinner
            while (n.next()) {
                String id = n.getString("area");
                data.add(id);
            }
            //-- Creamos el ArrayAdapter que necesitará el spinner, dándole como parámetros, (Contexto, referencia al layout de elemento, valores (opciones)
            String[] array = data.toArray(new String[0]);
            ArrayAdapter NoCoreAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, data);
            evcodinv.setAdapter(NoCoreAdapter);
        } catch (SQLException e) {
            savetoLog("evcajas.setOnKeyListener | query: " + query + " | " + e.toString());
            e.printStackTrace();
        }
        evcodinv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //-- Detecta la posicion del Items
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                msges = position;
                try {
                    if (msges == 0) {
                        evarea.setText(evcodinv.getSelectedItem().toString());
                        //-- Lee el QR para liberar la captura
                        withEditText();
                    } else if (msges > 0) {
                        evarea.setText(evcodinv.getSelectedItem().toString());
                        //-- Lee el QR para liberar la captura
                        withEditText();
                    }
                } catch (Exception ex) {
                    savetoLog(ex.toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        progressDialog = new ProgressDialog(this);
        // ------------------------------------------
        // ** habilita la re-impresion del marbete **
        // ------------------------------------------
        if (tipodetomainv.equalsIgnoreCase("1")) {
            Last_bmt.setEnabled(true);
        }else {
            Last_bmt.setEnabled(false);
        }
        // ** re-impresion del marbete **
        Last_bmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(v.getContext());
                builder.setTitle("Re-impresión !!!");
                builder.setMessage("¿ Deseas re-imprimir el marbete del último  artículo seleccionado ?");
                builder.setIcon(android.R.drawable.ic_menu_info_details);
                builder.setCancelable(false);
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            ImprimeMarbInvCiclico(mbtsys_rimpr,ninv_rimpr);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(v.getContext(), "Cancelado", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        });



        // End onCreate
    }

    public void limp(View view) {
        evart.setText(null);
        evdesc.setText(null);
        evpzass.setText(null);
        evpzascant.setText(null);
        evprecio.setText(null);
        evpza.setText(null);
        evcajas.setText(null);
        evpza.setEnabled(false);
        evcajas.setEnabled(false);
        evcodbar.setEnabled(true);
        evcodbar.setText(null);
        evcodbar.requestFocus();

        if (rbcajtm1.equals("true")) {
            evpza.setText("0.00");
        }
        if (rbpzastm1.equals("true")) {
            evcajas.setText("0.00");
        }
    }


    public void limpiar() {
        evart.setText(null);
        evdesc.setText(null);
        evpzass.setText(null);
        evpzascant.setText(null);
        evprecio.setText(null);
        evpza.setText(null);
        evcajas.setText(null);
        evpza.setEnabled(false);
        evcajas.setEnabled(false);
        evcodbar.setEnabled(true);
        evcodbar.setText(null);
        evcodbar.requestFocus();

        if (rbcajtm1.equals("true")) {
            evpza.setText("0.00");
        }
        if (rbpzastm1.equals("true")) {
            evcajas.setText("0.00");
        }
    }

    private Articulo ValidarArticulo(String codigo, String almacen, String salm) {
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
                        Articulo art = new Articulo();
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

    public static class Articulo {
        public String descripcion, unidad, estatus, decimales, precio, empaque;
        public int cve;
    }

    //-- Buscar area que no este activo --
    public void Buscaractivonoactivo() {
        activonoactivo = "0";
        String inventariocodigo = txtcdinv.getText().toString();
        String inventarioarea = evarea.getText().toString();
//        String query = "Select estado from tblinventario_tic where ninventario='" + inventariocodigo + "' and area='" + inventarioarea + "'";
        String query = "Select estado from tblinventario_tic where ninventario='" + inventariocodigo + "' and area='" + inventarioarea + "' and ninventario in (select codigo from invmar)";
        try {
            ResultSet n = queryDT(query);
            while (n.next()) {
                activonoactivo = n.getString("estado");
            }
        } catch (SQLException e) {
            savetoLog("Buscaractivonoactivo | query: " + query + " | " + e.toString());
            e.printStackTrace();
        }
        //-- Asigna valor 3 si no encuentra nada
        if (activonoactivo.equals("0")) {
            activonoactivo = "3";
        }
    }

    //-- Buscar area que no este activo --
    public void BuscaractivonoactivoMismousr() {
        activonoactivoMismousr = "0";
        String inventariocodigoMismousr = txtcdinv.getText().toString();
        String inventarioareaMismousr = evarea.getText().toString();
        String logusrsMismousr = usrcvecorta.getText().toString();
//        String query = "Select estado from tblinventario_tic where ninventario='" + inventariocodigoMismousr + "' and area='" + inventarioareaMismousr + "' and capturo='" + logusrsMismousr + "'";
        String query = "Select estado from tblinventario_tic where ninventario='" + inventariocodigoMismousr + "' and area='" + inventarioareaMismousr + "' and capturo='" + logusrsMismousr + "' and ninventario in (select codigo from invmar)";
        try {
            ResultSet n = queryDT(query);
            while (n.next()) {
                activonoactivoMismousr = n.getString("estado");
            }
        } catch (SQLException e) {
            savetoLog("BuscaractivonoactivoMismousr | query: " + query + " | " + e.toString());
            e.printStackTrace();
        }
        //-- Asigna valor 3 si no encuentra nada
        if (activonoactivoMismousr.equals("0")) {
            activonoactivoMismousr = "3";
        }
    }

    //-- Buscar area que no este activo --
    public void Buscdinvact() {
        varcdinvact = "0";
        varcdinvact0 = "0";
        String cdinvact1 = txtcdinv.getText().toString();
        String areacact1 = evarea.getText().toString();
//        String query = "Select ninventario From tblinventariocdimbt_tic Where ninventario='" + cdinvact1 + "' and area='" + areacact1 + "'";
        String query = "Select ninventario From tblinventariocdimbt_tic Where ninventario='" + cdinvact1 + "' and area='" + areacact1 + "' and ninventario in (select codigo from invmar)";
        try {
            ResultSet n = queryDT(query);
            while (n.next()) {
                varcdinvact = n.getString("ninventario");
                varcdinvact0 = "1";
            }
        } catch (SQLException e) {
            savetoLog("Buscdinvact | query: " + query + " | " + e.toString());
            e.printStackTrace();
        }
        //-- Asigna valor 3 si no encuentra nada
        if (varcdinvact0.equals("0")) {
            varcdinvact0 = "3";
        }
    }

    //-- Busca que el area contenga informacion
    //-- Si no tiene no cerrara el area
    public void Bscareavacia() {
        dasbprogcontador = "0";
        String txtcdinvtem = txtcdinv.getText().toString();
        String txtsuctem = txtsuc.getText().toString();
        String evareatem = evarea.getText().toString();
        String query = "select existencia from invmar where ibuff='RTF' and codigo='" + txtcdinvtem + "' and alm='" + txtsuctem + "' and area='" + evareatem + "'";
        try {
            ResultSet n = queryDT(query);
            while (n.next()) {
                dasbprogcontador = "1";
            }
        } catch (SQLException e) {
            savetoLog("Bscareavacia | query: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }

    //-- Busca no tenga fecha de inicio de captura --
    public void bscfecinicap() {
        fecinicaptura = "0";
        String txtcdinvtmp = txtcdinv.getText().toString();
        String evareatmp = evarea.getText().toString();
        String usrcvecortatmp = usrcvecorta.getText().toString();
//        String query = "select * from tblinventario_tic where ninventario='" + txtcdinvtmp + "' and area='" + evareatmp + "' and capturo='" + usrcvecortatmp + "' and estado='1' and fechahrin_cap is null";
        String query = "select * from tblinventario_tic where ninventario='" + txtcdinvtmp + "' and area='" + evareatmp + "' and capturo='" + usrcvecortatmp + "' and estado='1' and fechahrin_cap is null and ninventario in (select codigo from invmar)";
        try {
            ResultSet n = queryDT(query);
            while (n.next()) {
                fecinicaptura = "1";
            }
        } catch (SQLException e) {
            savetoLog("bscfecinicap | query: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }


    // -----------------------
    // --  conexion a WIFI ---
    // -----------------------
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
            // -- Formulario contador o auditor
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


    // ------------------
    // --   SPINNER    --
    // ------------------
    // -- llena el espinner cada vez que oprime las piezas, cajas , Refresh y area
    public void llenarspinner() {
        newareaasignada = "0";
        String txtcdinvtemp = txtcdinv.getText().toString();
        String usrcvecortatemp = usrcvecorta.getText().toString();
        //Se agrega "and ninventario in (select codigo from invmar)" para verificar que se traiga de acuerdo al numero de inventario actual
        String query = "Select area from tblinventario_tic where ninventario='" + txtcdinvtemp + "' and capturo='" + usrcvecortatemp + "' and estado='1' and ninventario in (select codigo from invmar)";
        //String query = "Select area from tblinventario_tic where ninventario='" + txtcdinvtemp + "' and capturo='" + usrcvecortatemp + "' and estado='1'";
        ArrayList<String> data = new ArrayList<String>();
        try {
            ResultSet n = queryDT(query);
            while (n.next()) {
                String id = n.getString("area");
                data.add(id);
                newareaasignada = "1";
            }
            ArrayAdapter NoCoreAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, data);
            evcodinv.setAdapter(NoCoreAdapter);
        } catch (SQLException e) {
            savetoLog("llenarspinner | query: " + query + " | " + e.getMessage());
            e.printStackTrace();
        }

        btnrefresh.setEnabled(data.size() <= 0);

        //-- indicamos al Spinner que utiliza el Adapter recien creado
        evcodinv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //-- Detecta la posicion del Items
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                msges = position;
                try {
                    if (msges == 0) {
                        evarea.setText(evcodinv.getSelectedItem().toString());
                        //-- Lee el QR para liberar la captura
                        withEditText();
                    } else if (msges > 0) {
                        evarea.setText(evcodinv.getSelectedItem().toString());
                        //-- Lee el QR para liberar la captura
                        withEditText();
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

    /**
     * Limpia etiquetas al capturar el codigo de barras
     */
    private void limpiardespuesdecodigobar() {
        evart.setText("");
        evdesc.setText("");
        evpzass.setText("");
        evpzascant.setText("");
        evprecio.setText("");
        evbusqueda.setText("");
    }

    //-- Limpia etiquetas al capturar el area
    private void limpiardespuesdearea() {
        evart.setText(null);
        evdesc.setText(null);
    }

    //-- Limpia etiquetas despues de teclear las cantidades piezas o cajas
    private void limpiardespuesdecant() {
        try {
            evbusqueda.setText(null);
            evcodbar.setText(null);
            evart.setText(null);
            evdesc.setText(null);
            evpzass.setText(null);
            evpzascant.setText(null);
            evprecio.setText(null);
            evpza.setText(null);
            evcodbar_tmp.setText(null);
            evcodbar.requestFocus();
            evcodbar.selectAll();
            if (rbcajtm1.equals("true")) {
                evpza.setText("0.00");
                evcajas.setText(null);
            }
            if (rbpzastm1.equals("true")) {
                evcajas.setText("0.00");
                evpza.setText(null);
                evcodbar.requestFocus();
                evcodbar.selectAll();
            }
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    //-- Limpia etiquetas despues de teclear las cantidades piezas o cajas
    private void limpiarnewarea() {
        try {
            evbusqueda.setText(null);
            evart.setText(null);
            evdesc.setText(null);
            evpzass.setText(null);
            evpzascant.setText(null);
            evprecio.setText(null);
            evpza.setText(null);
            evcajas.setText(null);
            evbusqueda.setEnabled(false);
            evcodbar.setEnabled(false);
            evarea.setText(null);
            evarea.setEnabled(true);
            evarea.requestFocus();
            evarea.selectAll();

            if (rbcajtm1.equals("true")) {
                evpza.setText("0.00");
            }
            if (rbpzastm1.equals("true")) {
                evcajas.setText("0.00");
            }
        } catch (Exception ex) {
            savetoLog(ex.toString());
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

    // -------------------
    // --   MENSAJES    --
    // -------------------
    // -- Alerta, cuando va a continuar grabando el mismo usuario en el area asignada
    // -- El codigo del area es correcta
    private void mesajearea2() {
        try {
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle("Area: " + evarea.getText().toString());
            dialogo1.setMessage("¿ El codigo de area es correcto ?");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("No", (dialogo112, id) -> NoConfirmar2());
            dialogo1.setNegativeButton("Si", (dialogo11, id) -> Confirmar2());
            dialogo1.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void NoConfirmar2() {
        try {
            evarea.selectAll();
            evarea.requestFocus();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void Confirmar2() {
        try {
            evbusqueda.setEnabled(true);
            evcodbar.setEnabled(true);
            evcodbar.requestFocus();
            evcodbar.selectAll();
            evarea.setEnabled(false);
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    //-- Alerta codigo abierto por otro usuario
    private void mesajearea4() {
        try {
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle("Alerta!! :" + evarea.getText().toString());
            dialogo1.setMessage("Codigo de area abierto por otro usuario. ");
            dialogo1.setCancelable(false);
            dialogo1.setNegativeButton("Ok", (dialogo11, id) -> Confirmar4());
            dialogo1.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void Confirmar4() {
        try {
            evarea.requestFocus();
            evarea.selectAll();
            evarea.setText(null);
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    //-- Alerta, area cerrada --
    //-- Pregunta si desea abrir de nuevo ya sea por el mismo usuario o otro diferente
    //-- Es es en caso de error de captura, acepta abierto por el mismo usuario en caso de corte de wifi o apgado del equipo
    //-- Validar que sea el mismo usuario
    //-- Que sea igual al codigo de inventario, area y usuario
    //-- No grabar de nuevo como log
    //-- Si es SI
    //-- Cambiar el estado a 1 si es el mismo codigo de inventario, codigo de area y usuario
    //-- Anexar log si es diferente usuario
    private void mesajearea3() {
        try {
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle("Area cerrada : " + evarea.getText().toString());
            dialogo1.setMessage("¿ Desea abrir la captura ?");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("No", (dialogo112, id) -> NoConfirmar3());
            dialogo1.setNegativeButton("Si", (dialogo11, id) -> Confirmar3());
            dialogo1.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void NoConfirmar3() {
        try {
            evarea.selectAll();
            evarea.requestFocus();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void Confirmar3() {
        try {
            BuscaractivonoactivoMismousr();
            String actnoctsMismoUsr1 = activonoactivoMismousr;
            int actnoctsssMismoUsr1 = Integer.parseInt(actnoctsMismoUsr1);
            if (actnoctsssMismoUsr1 == 2) {
                evbusqueda.setEnabled(true);
                evcodbar.setEnabled(true);
                evcodbar.requestFocus();
                evcodbar.selectAll();
                evarea.setEnabled(false);
                actualizarlogs();
            }
            if (actnoctsssMismoUsr1 == 3) {
                evbusqueda.setEnabled(true);
                evcodbar.setEnabled(true);
                evcodbar.requestFocus();
                evcodbar.selectAll();
                evarea.setEnabled(false);
                grabarlogs();
            }
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    // -- Alerta, cuando no hay ninguna captura de area --
    private void mesajearea() {
        try {
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle("Area: " + evarea.getText().toString());
            dialogo1.setMessage("¿ El codigo del area es correcta ?");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("No", (dialogo112, id) -> NoConfirmar());
            dialogo1.setNegativeButton("Si", (dialogo11, id) -> Confirmar());
            dialogo1.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void NoConfirmar() {
        try {
            evarea.selectAll();
            evarea.requestFocus();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void Confirmar() {
        try {
            evbusqueda.setEnabled(true);
            evcodbar.setEnabled(true);
            evcodbar.requestFocus();
            evcodbar.selectAll();
            evarea.setEnabled(false);
            grabarlogs();

            // -- Valida ei que codigo de inventario exista en la tabla de los unumeros consecutivos
            // -- En caso que no abre el consecutivo
            Buscdinvact();
            if (varcdinvact0.trim().equalsIgnoreCase("3")) {
                grabarcdincons();
            }
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    //-- Alerta al seleccionar en PIEZAS --
    private void mesajeCantPza() {
        try {
            String cantidad = evpza.getText().toString();
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle(evpzass.getText().toString() + " DE: " + cantidad);
            dialogo1.setMessage("¿ Esta cantidad es correcta ?");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("Si", (d, id) -> {
                ConfirmarPza(cantidad);
            });
            dialogo1.setNegativeButton("No", (d2, id) -> {
                NoConfirmarPza();
                if (rbcajtm1.equals("true")) {
                    evcajas.setEnabled(true);
                }

                if (rbpzastm1.equals("true")) {
                    evpza.setEnabled(true);
                }
            });
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
            String evartblank1 = evart.getText().toString();
            //-- Graba en la tabla INVMAR en cajas
            if (evartblank1.equals("")) {
                Toast.makeText(MainActivity4.this, "No es posible realizar la operacion", Toast.LENGTH_SHORT).show();
                evpza.setText(null);
                evcodbar.requestFocus();
                evcodbar.selectAll();
            } else {

                String valida = ValidaArticuloInvmar();
                if (valida.equals("")) {
                    AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
                    dialogo1.setTitle(R.string.Alerta);
                    dialogo1.setMessage("El articulo no se encuentra en el codigo de inventario, desea agregarlo");
                    dialogo1.setCancelable(false);
                    dialogo1.setPositiveButton("Si", (dialogo11, id) -> {
                        // -- oculta el teclado
                        ocultarkeypza();
                        novisiblebuscar();
                        // -- Graba en la tabla INVMAR en piezas
                        String marbetsys = grabarinvmarpzas(cantidad);
                        if (marbetsys.equals("USUARIONOCORRESPONDE")) {
                            savetoLog("Usuario no corresponde al area " + evarea.getText().toString());
                            AlertDialog.Builder dialogo2 = new AlertDialog.Builder(this);
                            dialogo1.setTitle(R.string.Alerta);
                            dialogo1.setMessage("Este usuario no corresponde al area " + evarea.getText().toString());
                            dialogo1.setCancelable(false);
                            dialogo1.setNegativeButton("OK", (dialogo12, idd) -> {
                                savetoLog("Usuario " + usrcvecorta.getText().toString() + " llena spinner");
                                llenarspinner();
                                limpiarnewarea();
                            });
                            dialogo1.show();
                        } else if (marbetsys.equals("ERROR")) {
                            savetoLog("Error de captura");
                            AlertDialog.Builder dialogo2 = new AlertDialog.Builder(this);
                            dialogo1.setTitle(R.string.Alerta);
                            dialogo1.setMessage(R.string.ArtNoGuardado);
                            dialogo1.setCancelable(false);
                            dialogo1.setNegativeButton("OK", (dialogo12, idd) -> {
                                evpza.setText(null);
                                evpza.setEnabled(true);
                            });
                            dialogo1.show();
                        } else {
                            // -- Busca el ultimo numero consecutivo --
                            // -- Add uno mas
                            Buscdinvactmax();

                            ///Imprime marbete si se encuentra en modo ciclico
                            if (tipodetomainv.equalsIgnoreCase("1")) {
                                try {
                                    ImprimeMarbInvCiclico(marbetsys, txtcdinv.getText().toString());
                                }catch(Exception ex)
                                {
                                    Toast.makeText(getApplicationContext(),"No se imprimió",Toast.LENGTH_LONG).show();
                                }

                            }


                            // -- Grabar codigo del marbete --
                            // -- Add a la tabla tblinventariodet_tic
                            grabarmbt(marbetsys, cantidad);
                            // -- consecutivo de mks
                            bsc_max_mbtmksyts();
                            btnrefresh.setEnabled(false);
                            evcodinv.setEnabled(false);
                            btnacc2.setEnabled(true);
                            evpza.setEnabled(false);
                            evcajas.setEnabled(false);
                            evcodbar.requestFocus();
                            // -- Fecha y hora de inicio de la captura
                            bscfecinicap();
                            if (fecinicaptura.equals("1")) { // -- Existe NULL
                                // -- Update a tblinventario_tic (fechahrin_cap)
                                actualizarfechrcap();
                            }
                            limpiardespuesdecant();
                        }
                    });
                    dialogo1.setNegativeButton("NO", (dialogo11, id) -> {
                        limpiar();
                    });
                    dialogo1.show();
                } else {
                    // -- oculta el teclado
                    ocultarkeypza();
                    novisiblebuscar();
                    // -- Graba en la tabla INVMAR en piezas
                    String marbetsys = grabarinvmarpzas(cantidad);
                    if (marbetsys.equals("USUARIONOCORRESPONDE")) {
                        savetoLog("Usuario no corresponde al area " + evarea.getText().toString());
                        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
                        dialogo1.setTitle(R.string.Alerta);
                        dialogo1.setMessage("Este usuario no corresponde al area " + evarea.getText().toString());
                        dialogo1.setCancelable(false);
                        dialogo1.setNegativeButton("OK", (dialogo11, id) -> {
                            savetoLog("Usuario " + usrcvecorta.getText().toString() + " llena spinner");
                            llenarspinner();
                            limpiarnewarea();
                        });
                        dialogo1.show();
                    } else if (marbetsys.equals("ERROR")) {
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
                    } else {
                        // -- Busca el ultimo numero consecutivo --
                        // -- Add uno mas
                        Buscdinvactmax();
                        // -- Grabar codigo del marbete --
                        // -- Add a la tabla tblinventariodet_tic
                        grabarmbt(marbetsys, cantidad);


                        ///Imprime marbete si se encuentra en modo ciclico
                        if (tipodetomainv.equalsIgnoreCase("1")) {
                            try {
                                ImprimeMarbInvCiclico(marbetsys, txtcdinv.getText().toString());
                            }catch(Exception ex)
                            {
                                Toast.makeText(getApplicationContext(),"No se pudo realizar la impresion",Toast.LENGTH_LONG).show();
                            }


                        }


                        // -- consecutivo de mks
                        bsc_max_mbtmksyts();
                        btnrefresh.setEnabled(false);
                        evcodinv.setEnabled(false);
                        btnacc2.setEnabled(true);
                        evpza.setEnabled(false);
                        evcajas.setEnabled(false);
                        evcodbar.requestFocus();
                        // -- Fecha y hora de inicio de la captura
                        bscfecinicap();
                        if (fecinicaptura.equals("1")) { // -- Existe NULL
                            // -- Update a tblinventario_tic (fechahrin_cap)
                            actualizarfechrcap();
                        }
                        limpiardespuesdecant();
                    }

                }
            }
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }


    public void ImprimeMarbInvCiclico(String marbsys, String ninventario) {
        Context cont = MainActivity4.this;
        //ProgressDialog progress = ProgressDialog.show(cont, "","Imprimiendo Marbete..", true);
        ProgressDialog progress = new ProgressDialog(cont);
        progress.setInverseBackgroundForced(true);
        progress.setMessage("");
        progress.show();
        new Thread(new Runnable() {
            @Override
            public void run() {

                String cod_mar = "", ninv = "", area = "", art = "", des1 = "", cant = "", nombre_lar = "", nom_cto = "", fec = "";
                progress.setMessage("Buscando informacion...");
                ResultSet rs = queryDT("SELECT tblinventariodet_tic.codmarbet, tblinventariodet_tic.ninventario, " +
                        "tblinventariodet_tic.area, tblinventariodet_tic.art, " +
                        "inviar.des1, tblinventariodet_tic.capexistencia, tcausr.nombre_lar, " +
                        "tcausr.nom_cto, tblinventariodet_tic.fechahr " +
                        "FROM tblinventariodet_tic INNER JOIN inviar ON tblinventariodet_tic.art = inviar.art INNER JOIN tcausr ON tblinventariodet_tic.capturo = tcausr.nom_cto " +
                        "WHERE tblinventariodet_tic.marbetsys = '" + marbsys + "' and ninventario = '" + ninventario + "'");
                if (rs != null) {
                    try {
                        while (rs.next()) {
                            cod_mar = rs.getString("codmarbet");
                            ninv = rs.getString("ninventario");
                            area = rs.getString("area");
                            art = rs.getString("art");
                            des1 = rs.getString("des1");
                            cant = rs.getString("capexistencia");
                            nombre_lar = rs.getString("nombre_lar");
                            nom_cto = rs.getString("nom_cto");
                            fec = rs.getString("fechahr");
                        }
                    } catch (SQLException e) {
                        savetoLog("ImprimeMarbInvCiclico | " + e.toString());
                    }
                }

                String numbercantidadpzs =cant; double dx = Double.parseDouble(numbercantidadpzs);
                double resultpzas = Double.parseDouble(numbercantidadpzs);
                String cant_tmpimpx = String.valueOf(resultpzas);


                try {
                // do the thing that takes a long time
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                    if (mBluetoothAdapter == null) {
                        // device doesn't support bluetooth
                    }
                    else {

                        // bluetooth is off, ask user to on it.
                        if(!mBluetoothAdapter.isEnabled()) {
                        }else
                        {
                            // Do whatever you want to do with your bluetoothAdapter
                            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                            if (pairedDevices.size() > 0) {
                                String MAC = "";

                                List<String> s = new ArrayList<String>();
                                for (BluetoothDevice bt : pairedDevices) {
                                    s.add(bt.getName() + "-" + bt.getAddress());
                                }
                                for (int indice = 0; indice < s.size(); indice++) {
                                    String[] Nombre = s.get(indice).split("-");
                                    String nom = Nombre[0];
                                    MAC = Nombre[2];
                                }



                                TscDll.openport(MAC);

                                if (TscDll.IsConnected) {
                                    progress.setMessage("Imprimiendo...");
                                    TscDll.downloadpcx("UL.PCX");
                                    TscDll.downloadbmp("Triangle.bmp");
                                    TscDll.downloadttf("ARIAL.TTF");

                                    TscDll.setup(70, 100, 4, 4, 0, 0, 0);
                                    TscDll.clearbuffer();

                                    String a = TscDll.queryprinter(); //-1 apagada/

                                    if (a.equals("-1")) {
                                        //APAGADA
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                Toast.makeText(cont, "La impresora se encuentra desconectada.\nNo es posible imprimir el voucher.", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        TscDll.closeport(5000);
                                    } else if (a.equals("0")) {
                                        //ENCENDIDA
                                        TscDll.sendcommand("SET TEAR ON\n");

                                        TscDll.printerfont(50, 50, "4", 0, 1, 1, "- MARBETE -");
                                        TscDll.qrcode(100, 105, "Q", "8", "A", "0", "M2", "S7", cod_mar);
                                        TscDll.printerfont(50, 280, "3", 0, 1, 1, cod_mar);
                                        TscDll.printerfont(2, 310, "1", 0, 1, 1, "--------------------------------------");
                                        TscDll.printerfont(2, 330, "1", 0, 1, 1, "COD.INV.:" + ninv + "             " + "AREA:" + area + "");
                                        TscDll.printerfont(2, 350, "1", 0, 1, 1, "--------------------------------------");
                                        TscDll.printerfont(130, 380, "2", 0, 1, 1, "ARTICULO:");
                                        TscDll.printerfont(140, 410, "3", 0, 1, 1, art);

                                        TscDll.printerfont(5, 480, "2", 0, 1, 1, des1);
                                        TscDll.printerfont(130, 530, "2", 0, 1, 1, "CANTIDAD:");
                                        TscDll.printerfont(100, 580, "5", 0, 1, 1, cant_tmpimpx);
                                        TscDll.printerfont(2, 710, "1", 0, 1, 1, "CONTO:" + nombre_lar);
                                        TscDll.printerfont(2, 730, "1", 0, 1, 1, "CLAVE CONTADOR:" + nom_cto);
                                        TscDll.printerfont(2, 750, "1", 0, 1, 1, "CLAVE FECHA::" + fec);


                                        TscDll.printlabel(1, 1);
                                        String status = TscDll.printerstatus();
                                        // -------------------------------------
                                        // -- Cierra la conexion al bluetooth --
                                        // -------------------------------------
                                        TscDll.closeport(5000);

                                        if (status.equals("00")) {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Toast.makeText(cont, "Marbete impreso con exito!", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    //Toast.makeText(getApplicationContext(),"Embarque "+id_emb+" no se pudo facturar, error: "+json,Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }

                                    }

                                }

                            }else
                            {

                            }
                        }


                    }


                } catch (Exception e) {
                    savetoLog("ImprimeMarbInvCiclico | " + e.toString());
                }

                MainActivity4.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                    }
                });
            }
        }).start();

    }


    private String ValidaArticuloInvmar() {
        String codinvGinvmar = txtcdinv.getText().toString();
        String artGinvmar = quitaEspacios(evart.getText().toString());

        String query = "select art from invmar where codigo = '" + codinvGinvmar + "' and art = '" + artGinvmar + "'";

        try {
            ResultSet n = queryDT(query);
            String res = "";
            if (n != null) {
                while (n.next()) {
                    res = n.getString("art");
                }
                return res;
            } else {
                return "ERROR";
            }
        } catch (SQLException e) {
            savetoLog("ValidaArticuloCiclico | query: " + query + " | " + e.toString());
            return "ERROR";
        }

    }

    //-- Alerta al seleccionar en CAJAS --
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
            String evartblank = evart.getText().toString();
            //-- Graba en la tabla INVMAR en cajas
            if (evartblank.equals("")) {
                Toast.makeText(MainActivity4.this, "No es posible realizar la operacion", Toast.LENGTH_SHORT).show();
                evcajas.setText(null);
                evcodbar.requestFocus();
                evcodbar.selectAll();
            } else {
                // -- Oculta el teclado
                ocultarkeycajas();
                novisiblebuscar();
                //-- Graba en la tabla INVMAR en cajas
                String marbetsys = grabarinvmarcajas(cantidad);
                if (marbetsys.equals("USUARIONOCORRESPONDE")) {
                    savetoLog("Usuario no corresponde al area " + evarea.getText().toString());
                    AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
                    dialogo1.setTitle(R.string.Alerta);
                    dialogo1.setMessage("Este usuario no corresponde al area " + evarea.getText().toString());
                    dialogo1.setCancelable(false);
                    dialogo1.setNegativeButton("OK", (dialogo11, id) -> {
                        savetoLog("Usuario " + usrcvecorta.getText().toString() + " actualiza spinner");
                        llenarspinner();
                        limpiarnewarea();
                    });
                    dialogo1.show();
                } else if (marbetsys.equals("ERROR")) {
                    savetoLog("Error de captura, reintentar guardar");
                    AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
                    dialogo1.setTitle(R.string.Alerta);
                    dialogo1.setMessage(R.string.ArtNoGuardado);
                    dialogo1.setCancelable(false);
                    dialogo1.setNegativeButton("OK", (dialogo11, id) -> {
                        evcajas.setText(null);
                        evcajas.setEnabled(true);
                    });
                    dialogo1.show();
                } else {
                    // -- Busca el ultimo numero consecutivo --
                    // -- Add uno mas
                    Buscdinvactmax();

                    ///Imprime marbete si se encuentra en modo ciclico
                    if (tipodetomainv.equalsIgnoreCase("1")) {

                       ImprimeMarbInvCiclico(marbetsys, txtcdinv.getText().toString());
                    }



                    // -- Grabar codigo del marbete --
                    // -- Add a la tabla tblinventariodet_tic
                    grabarmbt(marbetsys, cantidad);
                    // -- consecutivo de mks
                    bsc_max_mbtmksyts();

                    btnrefresh.setEnabled(false);
                    evcodinv.setEnabled(false);
                    btnacc2.setEnabled(true);
                    evpza.setEnabled(false);
                    evcajas.setEnabled(false);
                    // -- Fecha y hora de inicio de la captura
                    bscfecinicap();
                    if (fecinicaptura == "1") {
                        // -- Existe NULL
                        // -- Update a tblinventario_tic (fechahrin_cap)
                        actualizarfechrcap();
                    }
                    limpiardespuesdecant();
                }
            }
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    //-- Alerta eL LIMITE DE ERRORES --
    private void mesajenewarea() {
        try {
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle("Tiene una nueva área");
            dialogo1.setMessage(" asignada para capturar " + "");
            dialogo1.setCancelable(false);
            dialogo1.setNegativeButton("OK", (dialogo11, id) -> {
                // -- llenar las areas disponibles
                llenarspinner();
            });
            dialogo1.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    //-- Alerta eL LIMITE DE ERRORES --
    private void mesajenonewarea() {
        try {
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle(R.string.Alerta);
            dialogo1.setMessage(" No tienes mas areas asignadas. ");
            dialogo1.setCancelable(false);
            dialogo1.setNegativeButton("OK", (dialogo11, id) -> {
                evcodinv.setAdapter(null);
            });
            dialogo1.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    // -- Metodo de Consulta desde el Metodo Global --
    private void mesajeCierreapp() {
        try {
            savetoLog(txtusrname.getText().toString() + " presiona boton terminar");
            //Verifica que se no se tenga areas en proceso cuando se haga un inventario anual
            boolean continuar = false;
            if (tipodetomainv.equalsIgnoreCase("2")) {
                //ResultSet rs = queryDT("select * from tblinventario_tic where capturo = '" + usrcvecorta.getText().toString() + "' and estado = 1 and ninventario = '" + txtcdinv.getText().toString() + "'");
                ResultSet rs = queryDT("select * from tblinventario_tic where capturo = '" + usrcvecorta.getText().toString() + "' and estado = 1 and ninventario = '" + txtcdinv.getText().toString() + "' and ninventario in (select codigo from invmar)");
                if (rs != null) {
                    int t = 0;
                    try {
                        while (rs.next()) {
                            t++;
                        }
                        if (t > 0) {
                            /* El usuario aun tiene areas por capturar */
                            Toast.makeText(getBaseContext(), "El usuario aun tiene " + Integer.toString(t) + " area(s) por capturar.\nNo puede salir", Toast.LENGTH_LONG).show();
                            continuar = false;
                        } else {
                            continuar = true;
                        }
                    } catch (SQLException e) {
                        savetoLog("mesajeCierreapp | " + e.toString());
                        e.printStackTrace();
                    }
                }
            } else {
                continuar = true;
            }

            if (continuar) {
                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
                dialogo1.setTitle("Alerta!!");
                dialogo1.setMessage("¿ Deseas terminar la captura ? " + "");
                dialogo1.setCancelable(false);
                dialogo1.setPositiveButton("Si", (dialogo112, id) -> ConfirmarSalir());
                dialogo1.setNegativeButton("No", (dialogo11, id) -> NoConfirmarSalir());
                dialogo1.show();
            }
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void ConfirmarSalir() {
        try {
            // Busca que el area contenga informacion
            //-----------------------------------------------------------------------------------------
            Bscareavacia();
            if (dasbprogcontador.equals("0")) {
                /* Al presionar el boton terminar redirige en automatico al activity 1 (Login) */
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else if (dasbprogcontador.equals("1")) {
                // -- Existe
                actualizarlogscerrado();
                // -- captura de area ciclico
                if (tipodetomainv.equalsIgnoreCase("1")) {
                    Intent intent = new Intent(this, MainActivity2.class);
                    startActivity(intent);
                    finish();
                }
                // -- captura de anual
                if (tipodetomainv.equalsIgnoreCase("2")) {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    public void NoConfirmarSalir() {

    }

    // ---------------------
    // -- ACTUALIZACIONES --
    // ---------------------
    //-- Actualiza es estado codigo de inventario a abierto --
    public void actualizarlogs() {
        String logcodinv3 = txtcdinv.getText().toString();
        String logarea3 = evarea.getText().toString();
        String logusrs3 = usrcvecorta.getText().toString();
        String query = ("update tblinventario_tic set estado='1' where ninventario='" + logcodinv3 + "' AND area='" + logarea3 + "' AND capturo='" + logusrs3 + "'");
        try {
            Boolean res = queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("actualizarlogs | query: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }

    //-- Actualiza es estado codigo de inventario a cerrado --
    public void actualizarlogscerrado() {
        String logcodinv3cd = txtcdinv.getText().toString();
        String logarea3cd = evarea.getText().toString();
        String logusrs3cd = usrcvecorta.getText().toString();
        String query = ("update tblinventario_tic set estado='2',fechahrfi_cap=Concat(Convert(char(10),GETDATE(),105),' ',CONVERT(varchar,GETDATE(),8)) where ninventario='" + logcodinv3cd + "' AND area='" + logarea3cd + "' AND capturo='" + logusrs3cd + "'");
        try {
            Boolean res = queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("actualizarlogscerrado | query: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }


    //-- Actualiza es estado codigo de marbete --
    public void actualizarcdincose() {
        String codinvcons = txtcdinv.getText().toString();
        String areassscons = evarea.getText().toString();
        String query = ("update tblinventariocdimbt_tic set valor_int='" + folioBuscdinvactmax + "' where ninventario='" + codinvcons + "' and area='" + areassscons + "'");
        try {
            Boolean res = queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("actualizarcdincose | query: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }

    //-- Actualiza la fecha y hora de inicio de captura --
    public void actualizarfechrcap() {
        String txtcdinvtm = txtcdinv.getText().toString();
        String evareatm = evarea.getText().toString();
        String usrcvecortatm = usrcvecorta.getText().toString();
        String query = ("update tblinventario_tic set fechahrin_cap=Concat(Convert(char(10),GETDATE(),105),' ',CONVERT(varchar,GETDATE(),8)) where ninventario='" + txtcdinvtm + "' and area='" + evareatm + "' and capturo='" + usrcvecortatm + "'");
        try {
            Boolean res = queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("actualizarfechrcap | query: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }

    // --------------
    // -- GRABAR   --
    // --------------
    // --Graba el los de los codigos de inventarios y areas abiertos con edintififacion del usuario --
    public void grabarlogs() {
        String logcodinv = txtcdinv.getText().toString();
        String logarea = evarea.getText().toString();
        String logusrs = usrcvecorta.getText().toString();
        String dptpss = "noa";
        String query = ("INSERT INTO tblinventario_tic (ninventario,area,capturo,estado,depto,audasig,audito) VALUES ('" + logcodinv + "','" + logarea + "','" + logusrs + "','1','" + dptpss + "','1',null) ");
        try {
            Boolean res = queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("grabarlogs | query: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }

    //-- Grabar a tabla invmar piezas--
    public String grabarinvmarpzas(String cantidad) {
        String codinvGinvmar = txtcdinv.getText().toString();
        String zonaGinvmar = txtsuc.getText().toString();
        String almGinvmar = txtalm.getText().toString();
        String artGinvmar = quitaEspacios(evart.getText().toString());

        DecimalFormat df3 = new DecimalFormat("#.###");
        df3.setRoundingMode(RoundingMode.DOWN);
        double resultpzas = Double.parseDouble(cantidad);
        String usrGinvmar = usrcvecorta.getText().toString();
        String areaGinvmar = evarea.getText().toString();
        String empaqGinvmar = evpzascant.getText().toString();

        String query = "proc_Inv_GrabaInvmar @pzacaja = 'pieza', @codinvGinvmar = '" + codinvGinvmar + "', @zonaGinvmar = '" + zonaGinvmar + "', " + "@almGinvmar = '" + almGinvmar + "', @artGinvmar = '" + artGinvmar + "', @cantidad = '" + df3.format(resultpzas) + "', " + "@usrGinvmar = '" + usrGinvmar + "', @areaGinvmar = '" + areaGinvmar + "', @empaqGinvmar = '" + empaqGinvmar + "', @tipoUsuario ='capturista'";

        try {
            ResultSet n = queryDT(query);
            String marbsys = "";
            if (n != null) {
                while (n.next()) {
                    marbsys = n.getString("Marbete");
                }
                return marbsys;
            } else {
                return "ERROR";
            }
        } catch (SQLException e) {
            savetoLog("grabarinvmarpzas | query: " + query + " | " + e.toString());
            return "ERROR";
        }

    }

    // -- Busca el ultimo numero consecutivo --
    // -- Add uno mas CAJAS
    public void Buscdinvactmax() {
        folioBuscdinvactmax = 0;
        String Buscdinvactmax1 = txtcdinv.getText().toString();
        String Busareaactmax1 = evarea.getText().toString();
//        String query = "Select valor_int from tblinventariocdimbt_tic Where ninventario ='" + Buscdinvactmax1 + "' and area='" + Busareaactmax1 + "'";
        String query = "Select valor_int from tblinventariocdimbt_tic Where ninventario ='" + Buscdinvactmax1 + "' and area='" + Busareaactmax1 + "' and ninventario in (select codigo from invmar)";
        try {
            ResultSet n = queryDT(query);
            //-- Muestra la informacion
            while (n.next()) {
                String Buscdinvactmax2 = n.getString("valor_int");
                int resultBuscdinvactmax2 = Integer.parseInt(Buscdinvactmax2);
                folioBuscdinvactmax = resultBuscdinvactmax2 + 1;
                actualizarcdincose();
            }
        } catch (SQLException e) {
            savetoLog("Buscdinvactmax | query: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }

    // -- Grabar codigo del marbete --
    // -- Add a la tabla tblinventariodet_tic
    public void grabarmbt(String marbsys, String cantidad) {
        try {
            String mbtsevcodbar = evcodbar_tmp.getText().toString();

            String mbtscodinv = txtcdinv.getText().toString();
            String mbtsarea = evarea.getText().toString();
            String mbtscapturo = usrcvecorta.getText().toString();
            String mbtsart = quitaEspacios(evart.getText().toString());

            Formatter fmt1 = new Formatter();
            fmt1.format("%06d", folioBuscdinvactmax);

            String evarea0izq = evarea.getText().toString();
            int result1_evarea0izq = Integer.parseInt(evarea0izq);
            Formatter fmt2 = new Formatter();
            fmt2.format("%05d", result1_evarea0izq);

            mbtsfolio = mbtscodinv + fmt2 + fmt1;

            //int marbetesis =bmbtsystema;
            int marbetesis = Integer.parseInt(marbsys);
            Formatter fmtsis = new Formatter();
            fmtsis.format("%06d", marbetesis);

            String mbtszona = txtsuc.getText().toString();
            String mbtssucursal = txtalm.getText().toString();

            //-- convierte el string a double ejem: 25.00 OK
            String numbercantpzscaj = "0";
            resultcantidadpzscjs = Double.parseDouble(numbercantpzscaj);
            DecimalFormat df3 = new DecimalFormat("#.###");
            df3.setRoundingMode(RoundingMode.DOWN);
            double result = Double.parseDouble(cantidad);
            String query = ("INSERT INTO tblinventariodet_tic (ninventario,area,capturo,codmarbet,art,marbet,marbetsys,fechahr,zona,sucursal,tipocap,audexistencia,audito,fechahraud,auddif,capexistencia,codbar) VALUES ('" + mbtscodinv + "','" + mbtsarea + "','" + mbtscapturo + "','" + mbtsfolio + "','" + mbtsart + "','" + fmt1 + "','" + fmtsis + "',Concat(Convert(char(10),GETDATE(),105),' ',CONVERT(varchar,GETDATE(),8)),'" + mbtszona + "','" + mbtssucursal + "','" + tipocaptura + "','" + resultcantidadpzscjs + "',null,null,'0','" + df3.format(result) + "','" + mbtsevcodbar + "')");
            try {
                Boolean res = queryUpdIns(query);
            } catch (Exception e) {
                savetoLog("grabarmbt | query: " + query + " | " + e.toString());
                e.printStackTrace();
            }
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    // -- muestra el ultimo marbete de mersksyt
    public void bsc_max_mbtmksyts() {
        String txtcdinvtmp = txtcdinv.getText().toString();
        String evareatmp = evarea.getText().toString();
        String txtsuctmp = txtsuc.getText().toString();
        String txtalmtmp = txtalm.getText().toString();
        //String query = "select Max(marbetsys) as max_mt from tblinventariodet_tic Where ninventario='" + txtcdinvtmp + "' and area='" + evareatmp + "' and zona='" + txtsuctmp + "' and sucursal='" + txtalmtmp + "'";
        String query = "select Max(marbetsys) as max_mt from tblinventariodet_tic Where ninventario='" + txtcdinvtmp + "' and area='" + evareatmp + "' and zona='" + txtsuctmp + "' and sucursal='" + txtalmtmp + "' and ninventario in (select codigo from invmar)";
        try {
            ResultSet n = queryDT(query);
            while (n.next()) {
                String maxmbt = n.getString("max_mt");
                Last_bmt.setText(maxmbt);
                btnacc2.setEnabled(true);
                //-- Determina la impresion del listado
                if (maxmbt == null) {
                    Last_bmt.setText("0");
                } else {
                    mbtprintsino = "3";
                }
                bsc_utimo_codbar_capturado();
            }
        } catch (SQLException e) {
            savetoLog("bsc_max_mbtmksyts | query: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }

    String mbtsys_rimpr, ninv_rimpr;

    // -- ultimo codigo de barras capturado
    public void bsc_utimo_codbar_capturado() {
        String Last_bmttmp = Last_bmt.getText().toString();
        String codinvGinvmar = txtcdinv.getText().toString();
        //String query = "Select codbar, capexistencia, des1 From tblinventariodet_tic det inner join inviar inv on det.art = inv.art Where marbetsys='" + Last_bmttmp + "'" + " and det.ninventario = '" + codinvGinvmar + "'";
        //String query = "Select codbar, capexistencia, des1 From tblinventariodet_tic det inner join inviar inv on det.art = inv.art Where marbetsys='" + Last_bmttmp + "'" + " and det.ninventario = '" + codinvGinvmar + "' and ninventario in (select codigo from invmar)";
        String query = "Select TOP 1 det.codbar, det.capexistencia, inv.des1,det.marbetsys,det.ninventario From tblinventariodet_tic det inner join inviar inv on det.art = inv.art Where marbetsys='" + Last_bmttmp + "'" + " and det.ninventario = '" + codinvGinvmar + "' and ninventario in (select codigo from invmar)";
        try {
            ResultSet n = queryDT(query);
            while (n.next()) {
                String maxcodbar = n.getString("codbar");
                String maxcapexistencia = n.getString("capexistencia");
                String desc = n.getString("des1");
                mbtsys_rimpr = n.getString("marbetsys");
                ninv_rimpr = n.getString("ninventario");

                Last_bmt.setText(getString(R.string.DisplayInfoUltMarb, maxcodbar, maxcapexistencia, desc.substring(0, 30)));
            }
        } catch (SQLException e) {
            savetoLog("bsc_utimo_codbar_capturado | query: " + query + " | " + e.toString());
            e.printStackTrace();
        }
    }

    //-- Grabar a tabla invmar CAJAS--
    public String grabarinvmarcajas(String cantidad) {
        String codinvGinvmar = txtcdinv.getText().toString();
        String zonaGinvmar = txtsuc.getText().toString();
        String almGinvmar = txtalm.getText().toString();
        String artGinvmar = quitaEspacios(evart.getText().toString());
        String usrGinvmar = usrcvecorta.getText().toString();
        String areaGinvmar = evarea.getText().toString();
        String empaqGinvmar = evpzascant.getText().toString();

        //-- convierte el string a double ejem: 25.00 OK
        DecimalFormat df3 = new DecimalFormat("#.###");
        df3.setRoundingMode(RoundingMode.DOWN);
        double resultcajass = Double.parseDouble(cantidad);

        String query = "proc_Inv_GrabaInvmar @pzacaja = 'caja', @codinvGinvmar = '" + codinvGinvmar + "', @zonaGinvmar = '" + zonaGinvmar + "', " + "@almGinvmar = '" + almGinvmar + "', @artGinvmar = '" + artGinvmar + "', @cantidad = '" + df3.format(resultcajass) + "', " + "@usrGinvmar = '" + usrGinvmar + "', @areaGinvmar = '" + areaGinvmar + "', @empaqGinvmar = '" + empaqGinvmar + "', @tipoUsuario ='capturista'";

        try {
            ResultSet n = queryDT(query);
            String marbsys = "";
            if (n != null) {
                while (n.next()) {
                    marbsys = n.getString("Marbete");
                }
                return marbsys;
            } else {
                return "ERROR";
            }
        } catch (SQLException e) {
            savetoLog("grabarinvmarcajas | query: " + query + " | " + e.toString());
            return "ERROR";
        }
    }

    // -- Valida ei que codigo de inventario exista en la tabla de los unumeros consecutivos
    // -- En caso que no abre el consecutivo
    public void grabarcdincons() {
        String logcodinv = txtcdinv.getText().toString();
        String logareas = evarea.getText().toString();
        int valorint = 0;
        String query = ("INSERT INTO tblinventariocdimbt_tic (ninventario,area,valor_int) VALUES ('" + logcodinv + "','" + logareas + "','" + valorint + "') ");
        try {
            Boolean res = queryUpdIns(query);
        } catch (Exception e) {
            savetoLog("grabarcdincons | query: " + query + " | " + e.toString());
            e.printStackTrace();
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

    private void ocultarkeycajas() {
        try {
            //Lineas para ocultar el teclado virtual (Hide keyboard)
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(evcajas.getWindowToken(), 0);
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
    }

    // --------------
    // -- DECIMALES --
    // --------------
    public String verificaempaque(String art) {
        try {
            String articulos = art.trim();
            articulos = articulos.replace("'", "");
            String SQL = "select art_ser from inviar where art = '" + articulos + "'";
            try {
                ResultSet n = queryDT(SQL);
                if (n.next()) {
                    return n.getString("art_ser");
                } else {
                    return "";
                }
            } catch (Exception e) {
                savetoLog("verificaempaque | query: " + SQL + " | " + e.toString());
                return "";
            }
        } catch (Exception ex) {
            return "";
        }
    }

    // -----------------------
    // -- ELIMINA ESPACIOS --
    // -----------------------
    public String quitaEspacios(String texto) {
        try {
            java.util.StringTokenizer tokens = new java.util.StringTokenizer(texto);
            texto = "";
            while (tokens.hasMoreTokens()) {
                texto += " " + tokens.nextToken();
            }
            texto = texto.trim();
            return texto;
        } catch (Exception ex) {
            savetoLog(ex.toString());
            return "";
        }
    }

    //-- Regresar al menu principal
    public void onBackPressed(View view) {
        mesajeCierreapp();
    }

    //-- Cancelar boton retroceso
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                //Acción
            }
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
        return true;
    }

    public void botonrefrr(View view) {
        llenarspinner();
    }


    // -- Activa el vibrador --
    private void vibrador() {
        //Compruebe si dispositivo tiene un vibrador.
        if (vibrator.hasVibrator()) {        //Si tiene vibrador
            long tiempo = 500; //en milisegundos
            vibrator.vibrate(tiempo);
        } else {//no tiene
            savetoLog("El dispositivo no tiene vibracion");
        }
    }

    // -- Mascara de 2 decimales --
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

    public void visiblebuscar(View view) {
        evbusqueda.setVisibility(View.VISIBLE);

    }

    public void novisiblebuscar() {
        evbusqueda.setVisibility(View.GONE);
    }

    //-- Lee el QR para liberar la captura
    // Alerta de mensaje
    public void withEditText() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Escanea el código QR del área, para iniciar la captura:");

            final EditText input = new EditText(MainActivity4.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);

            // -- Oculta el teclado
            input.setShowSoftInputOnFocus(false);
            input.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        //no hacer nada todavía
                    } else if (event.getAction() == KeyEvent.ACTION_UP) {
                        input.setText(input.getText().toString().replace("\n", ""));
                    }
                }
                return false;
            });

            // -- Evita que alertdialog.builder se cierre si no se selecciona una opcion
            builder.setCancelable(false);
            builder.setView(input);
            builder.setPositiveButton("OK", (DialogInterface.OnClickListener) (dialogInterface, i) -> {
                String resultadomsg1 = input.getText().toString();
                String evarea_qr = evarea.getText().toString();
                if (evarea_qr.equals(resultadomsg1)) {
                    evbusqueda.setEnabled(true);
                    evcodbar.setEnabled(true);
                    evcodbar.requestFocus();
                    evcodbar.selectAll();
                    evarea.setEnabled(false);
                    // -- Muestra el ultimo marbeta capturado --
                    bsc_max_mbtmksyts();
                    // -- Trae informacion del consecuivo para el marbete de la preferencias
                    // -- Al aceptar el QR
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Código escaneado no encontrado", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
                    View view = toast.getView();
                    TextView view1 = (TextView) view.findViewById(android.R.id.message);
                    view1.setTextColor(Color.YELLOW);
                    view.setBackgroundResource(R.color.design_default_color_primary_dark);
                    toast.show();
                    withEditText();
                }
            });
            builder.show();
        } catch (Exception ex) {
            savetoLog(ex.toString());
        }
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

    // -- Consulta con Metodo con Metodo Global
    public ResultSet queryDT(String query) {
        Connection con = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            String ConnURL = "jdbc:jtds:sqlserver://" + ip + ";databaseName=" + db + ";user=" + un + ";password=" + passwords + "";
            con = DriverManager.getConnection(ConnURL);
            Statement st = con.createStatement();
            st.setQueryTimeout(10);
            ResultSet rs = st.executeQuery(query);
            return rs;
        } catch (Exception s) {
            savetoLog("queryDT " + query + " | " + s.toString());
            return null;
        }
    }


    // -- Update o Insert con Metodo Global
    public Boolean queryUpdIns(String query) {
        Connection con;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            String ConnURL = "jdbc:jtds:sqlserver://" + ip + ";databaseName=" + db + ";user=" + un + ";password=" + passwords + "";
            con = DriverManager.getConnection(ConnURL);
            try {
                Statement st = con.createStatement();
                String resbool = "false";
                st.setQueryTimeout(10);
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


//    public void BuscarArticulo(){
//        //Obtiene el codigo de barras
//        String codigo = evbusqueda.getText().toString();
//        //Otiene almacen
//        String almacen = txtsuc.getText().toString();
//        //Obtiene salm
//        String salm = txtalm.getText().toString();
//
//        try {
//            PreparedStatement pst = connDb().prepareStatement("proc_Inv_SerachArticle ?,?,?");
//            //se asignan valores a los parametros '?'
//            pst.setString(1, codigo);
//            pst.setString(2, almacen);
//            pst.setString(3, salm);
//
//            //se envian los parametros del contexto y e preparedstatement
//            @SuppressLint("StaticFieldLeak") ConexionSQL conexionSQL = new ConexionSQL(this, pst) {
//                //se sobreescribe el metodo onPostExecute para realizar algunas acciones despues de cargar
//                @Override
//                protected void onPostExecute(ResultSet resultSet) {
//                    super.onPostExecute(resultSet);
//                    if(resultSet!= null){
//                        Articulo art = null;
//                        try{
//                            while (resultSet.next()) {
//                                art = new Articulo();
//                                art.descripcion = resultSet.getString("des1");
//                                art.cve = Integer.parseInt(resultSet.getString("art").trim());
//                                art.unidad = resultSet.getString("UnidadMinima");
//                                art.estatus = resultSet.getString("Estatus");
//                                art.decimales = resultSet.getString("Decimales");
//                                art.precio = resultSet.getString("Precio");
//                                art.empaque = resultSet.getString("fac_ent_sal");
//                            }
//                        }
//                        catch (Exception e){
//                          art = null;
//                        }
//
//                        if (art != null) {
//                            // -- Limpia las etiquetas --
//                            limpiardespuesdecodigobar();
//                            //Set articulo
//                            evart.setText(String.valueOf(art.cve));
//                            //Muestra nombre del articulo
//                            evdesc.setText(art.descripcion);
//                            //Muestra precio
//                            evprecio.setText(formatoImporte.format(Double.parseDouble(art.precio)));
//                            //Guarda clave art
//                            evcodbar_tmp.setText(codigo);
//                            //Unidades
//                            evpzass.setText(art.unidad);
//                            //Empaque
//                            evpzascant.setText(df.format(Double.parseDouble(art.empaque)));
//                            //Posiciona el cursos en cajas o piezas
//                            colorestext();
//                            if (rbcajtm1.equals("true")) {
//                                evcajas.setText("");
//                                evcajas.requestFocus();
//                                evcajas.setEnabled(true);
//                                evpza.setEnabled(false);
//                            }
//                            if (rbpzastm1.equals("true")) {
//                                evpza.setText("");
//                                evpza.requestFocus();
//                                evpza.setEnabled(true);
//                                evcajas.setEnabled(false);
//                            }
//                        } else {
//                            evdesc.setText(R.string.NoEncontrado);
//                            evdesc.setTextColor(Color.parseColor("#E74C3C"));
//                            evcodbar.requestFocus();
//                            evcodbar.selectAll();
//                            evart.setText("");
//                            evpzass.setText("");
//                            evpzascant.setText("");
//                            evprecio.setText("");
//                            evcodbar_tmp.setText("");
//                            evpza.setEnabled(false);
//                            evcajas.setEnabled(false);
//                            Toast.makeText(MainActivity4.this, "No existe el articulo", Toast.LENGTH_LONG).show();
//                        }
//
//
//                    }
//                    else {
//                        evdesc.setText(R.string.NoEncontrado);
//                        evdesc.setTextColor(Color.parseColor("#E74C3C"));
//                        evcodbar.requestFocus();
//                        evcodbar.selectAll();
//                        evart.setText("");
//                        evpzass.setText("");
//                        evpzascant.setText("");
//                        evprecio.setText("");
//                        evcodbar_tmp.setText("");
//                        evpza.setEnabled(false);
//                        evcajas.setEnabled(false);
//                        Toast.makeText(MainActivity4.this, "No existe el articulo", Toast.LENGTH_LONG).show();
//                    }
//                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//                    progressDialog.dismiss();
//                }
//            };
//            conexionSQL.execute();
//        } catch (SQLException e) {
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//
//    }
//
//
//    public Connection connDb(){
//        Connection conn = null;
//        try{
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
//            conn = DriverManager.getConnection("jdbc:jtds:sqlserver://192.168.1.115;databaseName=TCADBGUA;user=sa;password=tca;");
//        }catch (Exception e){
//            Toast.makeText(this, e.getMessage(),Toast.LENGTH_LONG).show();
//        }
//        return conn;
//    }
//
//    private class ConexionSQL extends AsyncTask<Void, String, ResultSet> {
//    public ProgressDialog progressDialog;
//    public Context context;
//    public PreparedStatement pst;
//
//    public ConexionSQL(Context cn, PreparedStatement st ){
//        this.context = cn;
//        this.pst = st;
//        progressDialog = new ProgressDialog(cn);
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        progressDialog.setCancelable(false);
//        progressDialog.setMessage("Buscando...");
//        progressDialog.show();
//    }
//
//    @Override
//    protected ResultSet doInBackground(Void... voids) {
//        try{
//            ResultSet rs = pst.executeQuery();
//            return rs;
//        }catch (SQLException sqle){
//            return null;
//        }
//    }
//
//    @Override
//    protected void onPostExecute(ResultSet resultSet) {
//        super.onPostExecute(resultSet);
//    }
//}

    // End Activity

}