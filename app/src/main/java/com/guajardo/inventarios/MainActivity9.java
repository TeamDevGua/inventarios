package com.guajardo.inventarios;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.StrictMode;
//import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.View;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
// -- Mascara de 2 decimales

// ---------------
// -- IMPRESORA --
// ---------------
// -- CITIZEN --
// -- TSC --
import com.example.tscdll.TSCActivity;
import com.google.android.material.snackbar.Snackbar;

// -- EC Line --
import print.Print;


public class MainActivity9 extends AppCompatActivity {
    // -- TSC --
    TSCActivity TscDll = new TSCActivity();
    final String PRE_KEY_BLUETOOTH_ADDRESS = "Bluetooth_Address";
    final String DEFAULT_BLUETOOTH_ADDRESS = "00:01:90:E8:03:33";

    // -- EC Line --
    private Context thisCon=null;
    private PublicAction PAct=null;


    private EditText editText_Address_Bluetooth;
    private TextView txtmbte;
    private EditText evcodmbt;


    private ImageButton imageButtons;

    //-- conexiones SQL
    String ip, db, un, passwords;
    Connection connect;
    PreparedStatement stmt;
    ResultSet rs;
    Integer msges;

    String v_marbete;
    String v_codinventario;
    String v_area;
    String v_art;
    String v_des_art;
    String v_cantidad;
    String v_con_nombre;
    String v_con_nom_corto;
    String v_fecha;

    String encuentrasino;
    String TscEcltmpSelect;

    double resultcantidadpzscjs;


    //-- valor con decimales
    DecimalFormat df = new DecimalFormat("####0.00");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main9);

        // -- Titulo de la aplicacion
        setTitle("RE-IMPRESION");

        //-- Trae las preferencias guardadas del Bluetooth
        editText_Address_Bluetooth = ( EditText ) findViewById( R.id.editText_Address_WiFi3 );
        SharedPreferences prefeX1x = getSharedPreferences("conf_bluetooth", Context.MODE_PRIVATE);
        editText_Address_Bluetooth.setText(prefeX1x.getString("bluetoothadress",""));
        String ffffgg = editText_Address_Bluetooth.getText().toString();
        TscEcltmpSelect=prefeX1x.getString("bluetoothadressT","");

        // -------------------------------------------------
        // ------- EC - Line -------------------------------
        // -------------------------------------------------
        thisCon=this.getApplicationContext();
        PAct=new PublicAction(thisCon);
        connectBT(editText_Address_Bluetooth.getText().toString());

        // -- Trae la informacion grabada de las preferencias para la conexion a SQL
        SharedPreferences pref = getSharedPreferences("conf_red", Context.MODE_PRIVATE);
        ip= pref.getString("server","");
        db=pref.getString("database","");
        un=pref.getString("user","");
        passwords=pref.getString("password","");
        connect = CONN(un, passwords, db, ip);

        txtmbte=(TextView)findViewById(R.id.txtmbte);
        evcodmbt = (EditText) findViewById(R.id.evcodmbt);

        imageButtons=(ImageButton)findViewById(R.id.imageButtons);

        imageButtons.setEnabled(false);

        imageButtons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TscEcltmpSelect.equalsIgnoreCase("1")){

                    impresionesTSC();

                }

                if (TscEcltmpSelect.equalsIgnoreCase("2")){
                    impresionesECLine();
                }

            }
        });



        // ---------------------
        // -- OCURRE AL ENTER --
        // ---------------------
        // -- (Campo Buscar) los articulos por clave: corta, larga o alterna
        // -- La accion despues del Enter
        evcodmbt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press

                    buscarcodigomarbete();
                    if (encuentrasino.equals("0")){
                        ocultarkey();
                        txtmbte.setText("Esperando...");

                        //evcodmbt.setText(null);
                        evcodmbt.requestFocus();
                        evcodmbt.selectAll();

                        Snackbar.make(v, "Marbete no encontrado", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        return true;
                    }

                    if (evcodmbt.equals("")){
                        txtmbte.setText("Esperando...");
                        evcodmbt.requestFocus();
                        evcodmbt.selectAll();
                        Snackbar.make(v, "Selecciona un marbete valido", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        //evcodmbt.requestFocus();
                        //evcodmbt.selectAll();
                        return true;
                    }
                    if (evcodmbt.equals(".")){
                        txtmbte.setText("Esperando...");
                        evcodmbt.requestFocus();
                        evcodmbt.selectAll();
                        Snackbar.make(v, "Selecciona un marbete valido", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        //evcodmbt.requestFocus();
                        //evcodmbt.selectAll();
                        return true;
                    }



                    if (encuentrasino.equals("1")){

                        ocultarkey();

                        //evcodmbt.setText(null);
                        evcodmbt.requestFocus();
                        evcodmbt.selectAll();

                        Snackbar.make(v, "Puedes re-imprimir el marbete", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    //-- Termina
                    return true;
                }
                return false;
            }
        });


        // End onCreate
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
            Log.e("ERRO", se.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("ERRO", e.getMessage());
        } catch (Exception e) {
            Log.e("ERRO", e.getMessage());
        }
        return conn;
    }


    //-- Busca la clave larga
    //-- Tablas (invart y invar)
    public void buscarcodigomarbete() {
        encuentrasino="0";
        String buscar_bmt = evcodmbt.getText().toString();


        String query = "SELECT tblinventariodet_tic.codmarbet, tblinventariodet_tic.ninventario, tblinventariodet_tic.area, tblinventariodet_tic.art, inviar.des1, tblinventariodet_tic.capexistencia, tcausr.nombre_lar, tcausr.nom_cto, tblinventariodet_tic.fechahr " +
                "FROM tblinventariodet_tic INNER JOIN inviar ON tblinventariodet_tic.art = inviar.art INNER JOIN tcausr ON tblinventariodet_tic.capturo = tcausr.nom_cto " +
                "WHERE tblinventariodet_tic.codmarbet='"+buscar_bmt+"'";

        try {
            connect = CONN(un, passwords, db, ip);
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {

                txtmbte.setText("Inventario: "+rs.getString("ninventario")+"   "+"Area: "+rs.getString("area"));
                encuentrasino="1";
                imageButtons.setEnabled(true);


                v_marbete=rs.getString("codmarbet");
                v_codinventario= rs.getString("ninventario");
                v_area= rs.getString("area");
                v_art= rs.getString("art");
                v_des_art= rs.getString("des1");
                v_cantidad= rs.getString("capexistencia");
                v_con_nombre= rs.getString("nombre_lar");
                v_con_nom_corto= rs.getString("nom_cto");
                v_fecha= rs.getString("fechahr");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ocultarkey(){
        //Lineas para ocultar el teclado virtual (Hide keyboard)
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(evcodmbt.getWindowToken(), 0);
    }


    public void impresionesTSC(){

        String codinv_tmpimp =v_codinventario;
        String area_tmpimp = v_area;
        String art_tmpimp = v_art;
        String desc_tmpimp = quitaEspacios(v_des_art);
        String cant_tmpimp = v_cantidad;
        String cont_tmpimp = quitaEspacios(v_con_nombre);
        String contcve_tmpimp = quitaEspacios(v_con_nom_corto);
        String currentDateandTime = v_fecha;

        // Get Address
        String addr = editText_Address_Bluetooth.getText().toString();

        //-- convierte el string a double ejem: 25.00 OK
        String numbercantidadpzs =cant_tmpimp; double dx = Double.parseDouble(numbercantidadpzs);
        double resultpzas = Double.parseDouble(numbercantidadpzs);
        String cant_tmpimpx = String.valueOf(resultpzas);

        TscDll.openport(addr);
        TscDll.downloadpcx("UL.PCX");
        TscDll.downloadbmp("Triangle.bmp");
        TscDll.downloadttf("ARIAL.TTF");
        try {
            //TscDll.setup(70, 110, 4, 4, 0, 0, 0);
            TscDll.setup(70, 100, 4, 4, 0, 0, 0);
            TscDll.clearbuffer();

            TscDll.sendcommand("SET TEAR ON\n");

            TscDll.printerfont(0, 50, "4", 0, 1, 1, "- MARBETE -");
            TscDll.qrcode(0,105,"Q","8","A","0","M2","S7",v_marbete);
            TscDll.printerfont(0, 280, "3", 0, 1, 1, v_marbete);
            TscDll.printerfont(2, 310, "1", 0, 1, 1, "--------------------------------------");
            TscDll.printerfont(2, 330, "1", 0, 1, 1, "COD.INV.:"+ codinv_tmpimp +"             " + "AREA:" + area_tmpimp +"");
            TscDll.printerfont(2, 350, "1", 0, 1, 1, "--------------------------------------");
            TscDll.printerfont(130, 380, "2", 0, 1, 1, "ARTICULO:");
            TscDll.printerfont(140, 410, "3", 0, 1, 1, art_tmpimp);

            TscDll.printerfont(5, 480, "2", 0, 1, 1, desc_tmpimp);
            TscDll.printerfont(130, 530, "2", 0, 1, 1, "CANTIDAD:");
            TscDll.printerfont(140, 580, "5", 0, 1, 1, cant_tmpimpx);
            TscDll.printerfont(2, 710, "1", 0, 1, 1, "CONTO:"+cont_tmpimp);
            TscDll.printerfont(2, 730, "1", 0, 1, 1, "CLAVE CONTADOR:"+contcve_tmpimp);
            TscDll.printerfont(2, 750, "1", 0, 1, 1, "CLAVE FECHA::"+currentDateandTime);

            imageButtons.setEnabled(false);

            TscDll.printlabel(1, 1);
            // -------------------------------------
            // -- Cierra la conexion al bluetooth --
            // -------------------------------------
            TscDll.closeport(5000);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    // -------------------------------------------------
    // ------- EC-Line ---------------------------------
    // -------------------------------------------------
    private void connectBT(final String  BTmac) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    final int result = Print.PortOpen(thisCon,"Bluetooth," + BTmac);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //text.setText("Esperando conexion");
                        }
                    });
                } catch (Exception e) {
                    String res = e.toString();
                }
            }
        }.start();
    }

    public void impresionesECLine()
    {
        try
        {
            PAct.BeforePrintAction();

            String codinv_tmpimp =v_codinventario;
            String area_tmpimp = v_area;
            String art_tmpimp = v_art;
            String desc_tmpimp = quitaEspacios(v_des_art);
            String cant_tmpimp = v_cantidad;
            String cont_tmpimp = quitaEspacios(v_con_nombre);
            String contcve_tmpimp = quitaEspacios(v_con_nom_corto);
            String currentDateandTime = v_fecha;

            // Get Address
            String addr = editText_Address_Bluetooth.getText().toString();

            //-- convierte el string a double ejem: 25.00 OK
            String numbercantidadpzs =cant_tmpimp; double dx = Double.parseDouble(numbercantidadpzs);
            double resultpzas = Double.parseDouble(numbercantidadpzs);
            String cant_tmpimpx = String.valueOf(resultpzas);


            String strPrintText1="- MARBETE -";
            Print.PrintText("" + strPrintText1+"\n",0,16,0);

            Print.PrintQRCode(v_marbete,(7+1),(3+0x30),0);
            String strPrintText0="::";
            Print.PrintText( strPrintText0,0,1,0);

            Print.PrintText(v_marbete,0,0,0);

            String strPrintText2="---------------------------------------------";
            Print.PrintText( strPrintText2,0,1,0);

            String strPrintText3="COD.INV.: " + codinv_tmpimp;
            String strPrintText4="AREA: " + area_tmpimp;
            Print.PrintText(strPrintText3+"                  " + strPrintText4,0,1,0);

            String strPrintText5="---------------------------------------------";
            Print.PrintText( strPrintText5,0,1,0);

            String strPrintText6="         "+"ARTICULO:";
            Print.PrintText( strPrintText6,0,2,0);

            String strPrintText7="         "+art_tmpimp;
            Print.PrintText( strPrintText7+"\n",0,2,0);

            String strPrintText8= desc_tmpimp;
            Print.PrintText( strPrintText8+"\n",0,2,0);

            String strPrintText9="         "+"CANTIDAD:";
            Print.PrintText( strPrintText9,0,2,0);

            String strPrintText10="    "+cant_tmpimp;
            Print.PrintText( strPrintText10+"\n",0,48,0);

            String strPrintText11="CONTO: " + cont_tmpimp;
            String strPrintText12="CLAVE CONTADOR: " + contcve_tmpimp;
            String strPrintText13="FECHA: " + currentDateandTime;
            Print.PrintText(strPrintText11,0,1,0);
            Print.PrintText(strPrintText12,0,1,0);
            Print.PrintText(strPrintText13+"\n\n\n",0,1,0);

            PAct.AfterPrintAction();
        }
        catch (Exception e)
        {
            Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onClickWIFI ")).append(e.getMessage()).toString());
        }
    }


    // -----------------------
    // -- ELIMINA ESPACIOS --
    // -----------------------
    public String quitaEspacios(String texto) {
        java.util.StringTokenizer tokens = new java.util.StringTokenizer(texto);
        texto = "";
        while(tokens.hasMoreTokens()){
            texto += " "+tokens.nextToken();
        }
        texto = texto.toString();
        texto = texto.trim();
        return texto;
    }

    //-- Regresar al menu principal
    public void onBackPressed(View view){

        Intent intent = new Intent(this, MainActivity4.class);
        startActivity(intent);
        Log.d("Main4Activity","onBackPressed()");
        finish();
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