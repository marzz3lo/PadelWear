package moonlapse.com.padelwear;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Calendar;
import java.util.Date;

import moonlapse.com.comun.DireccionesGestureDetector;
import moonlapse.com.comun.Partida;

/**
 * Created by marzzelo on 24/6/2017.
 */

public class Contador extends Activity implements MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks, DataApi.DataListener {
    private Partida partida;
    private TextView misPuntos, misJuegos, misSets, susPuntos, susJuegos, susSets;
    private Vibrator vibrador;
    private long[] vibrEntrada = {0l, 500};
    private long[] vibrDeshacer = {0l, 500, 500, 500};

    private GoogleApiClient apiClient;

    private static final String WEAR_PUNTUACION = "/puntuacionWear";
    private static final String MOBILE_PUNTUACION = "/puntuacionMobile";
    private static final String KEY_MIS_PUNTOS = "moonlapse.com.padel.key.mis_puntos";
    private static final String KEY_MIS_JUEGOS = "moonlapse.com.padel.key.mis_juegos";
    private static final String KEY_MIS_SETS = "moonlapse.com.padel.key.mis_sets";
    private static final String KEY_SUS_PUNTOS = "moonlapse.com.padel.key.sus_puntos";
    private static final String KEY_SUS_JUEGOS = "moonlapse.com.padel.key.sus_juegos";
    private static final String KEY_SUS_SETS = "moonlapse.com.padel.key.sus_sets";
    private static final String KEY_EQUIPO = "moonlapse.com.padel.key.equipo";

    private byte misP, susP, misJ, susJ, misS, susS;

    private TextView hora;
    private Calendar c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contador);
        apiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).addConnectionCallbacks(this).build();
        c = Calendar.getInstance();
        c.setTime(new Date());

        partida = new Partida();
        vibrador = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        misPuntos = (TextView) findViewById(R.id.misPuntos);
        susPuntos = (TextView) findViewById(R.id.susPuntos);
        misJuegos = (TextView) findViewById(R.id.misJuegos);
        susJuegos = (TextView) findViewById(R.id.susJuegos);
        misSets = (TextView) findViewById(R.id.misSets);
        susSets = (TextView) findViewById(R.id.susSets);
        hora = (TextView) findViewById(R.id.hora);

        hora.setText(c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE));


        actualizaNumeros(3);
        View fondo = findViewById(R.id.fondo);
        fondo.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector detector = new DireccionesGestureDetector(Contador.this, new DireccionesGestureDetector.SimpleOnDireccionesGestureListener() {
                @Override
                public boolean onArriba(MotionEvent e1, MotionEvent e2, float distX, float distY) {
                    partida.rehacerPunto();
                    vibrador.vibrate(vibrDeshacer, -1);
                    actualizaNumeros(0);
                    return true;
                }

                @Override
                public boolean onAbajo(MotionEvent e1, MotionEvent e2, float distX, float distY) {
                    partida.deshacerPunto();
                    vibrador.vibrate(vibrDeshacer, -1);
                    actualizaNumeros(-1);
                    return true;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent evento) {
                detector.onTouchEvent(evento);
                return true;
            }
        });
        misPuntos.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector detector = new DireccionesGestureDetector(Contador.this, new DireccionesGestureDetector.SimpleOnDireccionesGestureListener() {
                @Override
                public boolean onDerecha(MotionEvent e1, MotionEvent e2, float distX, float distY) {
                    partida.puntoPara(true);
                    vibrador.vibrate(vibrEntrada, -1);
                    actualizaNumeros(1);
                    return true;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent evento) {
                detector.onTouchEvent(evento);
                return true;
            }
        });
        susPuntos.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector detector = new DireccionesGestureDetector(Contador.this, new DireccionesGestureDetector.SimpleOnDireccionesGestureListener() {
                @Override
                public boolean onDerecha(MotionEvent e1, MotionEvent e2, float distX, float distY) {
                    partida.puntoPara(false);
                    vibrador.vibrate(vibrEntrada, -1);
                    actualizaNumeros(2);
                    return true;
                }

            });

            @Override
            public boolean onTouch(View v, MotionEvent evento) {
                detector.onTouchEvent(evento);
                return true;
            }
        });
    }

    void actualizaNumeros(int i) {
        misPuntos.setText(partida.getMisPuntos());
        susPuntos.setText(partida.getSusPuntos());
        misJuegos.setText(partida.getMisJuegos());
        susJuegos.setText(partida.getSusJuegos());
        misSets.setText(partida.getMisSets());
        susSets.setText(partida.getSusSets());
        sincronizarDatos(i);
    }

    void sincronizarDatos(int i) {
        Log.e("Padel Wear", "Sincronizando");
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(
                WEAR_PUNTUACION);
        putDataMapReq.getDataMap().putByte(KEY_MIS_PUNTOS, partida
                .getMisPuntosByte());
        putDataMapReq.getDataMap().putByte(KEY_MIS_JUEGOS, partida
                .getMisJuegosByte());
        putDataMapReq.getDataMap().putByte(KEY_MIS_SETS, partida
                .getMisSetsByte());
        putDataMapReq.getDataMap().putByte(KEY_SUS_PUNTOS, partida
                .getSusPuntosByte());
        putDataMapReq.getDataMap().putByte(KEY_SUS_JUEGOS, partida
                .getSusJuegosByte());
        putDataMapReq.getDataMap().putByte(KEY_SUS_SETS, partida
                .getSusSetsByte());
        putDataMapReq.getDataMap().putInt(KEY_EQUIPO, i);

        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(apiClient, putDataReq);
        Log.e("Padel Wear", "Request: " + putDataReq.toString());
    }

    @Override
    protected void onStop() {
        if (apiClient != null && apiClient.isConnected()) {
            apiClient.disconnect();
        }
        Wearable.DataApi.removeListener(apiClient, this);
        Wearable.MessageApi.removeListener(apiClient, this);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();
        Wearable.MessageApi.addListener(apiClient, this);
    }


    @Override
    public void onDataChanged(DataEventBuffer eventos) {
        Log.e("MOVIL", "onDataChanget ---------------");
        for (DataEvent evento : eventos) {
            if (evento.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = evento.getDataItem();
                if (item.getUri().getPath().equals(MOBILE_PUNTUACION)) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    misP = dataMap.getByte(KEY_MIS_PUNTOS);
                    susP = dataMap.getByte(KEY_SUS_PUNTOS);
                    misJ = dataMap.getByte(KEY_MIS_JUEGOS);
                    susJ = dataMap.getByte(KEY_SUS_JUEGOS);
                    misS = dataMap.getByte(KEY_MIS_SETS);
                    susS = dataMap.getByte(KEY_SUS_SETS);

                    Log.e("MOVIL", "getBytes ---------------");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            misPuntos.setText(Integer.toString(misP));
                            misJuegos.setText(Integer.toString(misJ));
                            misSets.setText(Integer.toString(misS));
                            susPuntos.setText(Integer.toString(susP));
                            susJuegos.setText(Integer.toString(susJ));
                            susSets.setText(Integer.toString(susS));
                            Log.e("MOVIL", "On UI run setText ---------------");
                        }
                    });
                }
            } else if (evento.getType() == DataEvent.TYPE_DELETED) {
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(apiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

    }
}
