package moonlapse.com.padelwear;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.SwipeDismissFrameLayout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import moonlapse.com.comun.DireccionesGestureDetector;
import moonlapse.com.comun.Partida;

/**
 * Created by marzzelo on 24/6/2017.
 */

public class ContadorForSwipe extends WearableActivity implements MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks, DataApi.DataListener {
    private Partida partida;
    private TextView misPuntos, misJuegos, misSets, susPuntos, susJuegos, susSets;
    private Vibrator vibrador;
    private long[] vibrEntrada = {0l, 500};
    private long[] vibrDeshacer = {0l, 500, 500, 500};
    private DismissOverlayView dismissOverlay;

    private Typeface fuenteNormal = Typeface.create("sans-serif", 0);
    private Typeface fuenteFina = Typeface.create("sans-serif-thin", 0);
    private TextView hora;
    private Calendar c;

    private static final String WEAR_PUNTUACION = "/puntuacionWear";
    private static final String MOBILE_PUNTUACION = "/puntuacionMobile";
    private static final String KEY_MIS_PUNTOS = "moonlapse.com.padel.key.mis_puntos";
    private static final String KEY_MIS_JUEGOS = "moonlapse.com.padel.key.mis_juegos";
    private static final String KEY_MIS_SETS = "moonlapse.com.padel.key.mis_sets";
    private static final String KEY_SUS_PUNTOS = "moonlapse.com.padel.key.sus_puntos";
    private static final String KEY_SUS_JUEGOS = "moonlapse.com.padel.key.sus_juegos";
    private static final String KEY_SUS_SETS = "moonlapse.com.padel.key.sus_sets";
    private static final String KEY_EQUIPO = "moonlapse.com.padel.key.equipo";

    private String misP, susP;
    private byte misJ, susJ, misS, susS;

    private static final String MOVIL_ARRANCAR_ACTIVIDAD = "/arrancar_actividad";
    private GoogleApiClient apiClient;

    private static final String ITEM_FOTO = "/item_foto";
    private static final String ASSET_FOTO = "/asset_foto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contador_swipedismiss);
        setAmbientEnabled();
        c = Calendar.getInstance();
        c.setTime(new Date());

        dismissOverlay = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
        dismissOverlay.setIntroText("Para salir de la aplicación, haz una pulsación larga");
        dismissOverlay.showIntroIfNecessary();

        apiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        mandarMensaje(MOVIL_ARRANCAR_ACTIVIDAD, "");

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
            GestureDetector detector = new DireccionesGestureDetector(ContadorForSwipe.this, new DireccionesGestureDetector.SimpleOnDireccionesGestureListener() {
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

                @Override
                public void onLongPress(MotionEvent e) {
                    dismissOverlay.show();
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent evento) {
                detector.onTouchEvent(evento);
                return true;
            }
        });
        misPuntos.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector detector = new DireccionesGestureDetector(ContadorForSwipe.this, new DireccionesGestureDetector.SimpleOnDireccionesGestureListener() {
                @Override
                public boolean onDerecha(MotionEvent e1, MotionEvent e2, float distX, float distY) {
                    partida.puntoPara(true);
                    vibrador.vibrate(vibrEntrada, -1);
                    actualizaNumeros(1);
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    dismissOverlay.show();
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent evento) {
                detector.onTouchEvent(evento);
                return true;
            }
        });
        susPuntos.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector detector = new DireccionesGestureDetector(ContadorForSwipe.this, new DireccionesGestureDetector.SimpleOnDireccionesGestureListener() {
                @Override
                public boolean onDerecha(MotionEvent e1, MotionEvent e2, float distX, float distY) {
                    partida.puntoPara(false);
                    vibrador.vibrate(vibrEntrada, -1);
                    actualizaNumeros(2);
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    dismissOverlay.show();
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent evento) {
                detector.onTouchEvent(evento);
                return true;
            }
        });

        PendingResult<DataItemBuffer> resultado = Wearable.DataApi.getDataItems(apiClient);
        resultado.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                for (DataItem dataItem : dataItems) {
                    if (dataItem.getUri().getPath().equals(WEAR_PUNTUACION)) {
                        DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
                        misP = dataMap.getString(KEY_MIS_PUNTOS);
                        susP = dataMap.getString(KEY_SUS_PUNTOS);
                        misJ = dataMap.getByte(KEY_MIS_JUEGOS);
                        susJ = dataMap.getByte(KEY_SUS_JUEGOS);
                        misS = dataMap.getByte(KEY_MIS_SETS);
                        susS = dataMap.getByte(KEY_SUS_SETS);

                        Log.e("WEAR", "getBytes ---------------");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                misPuntos.setText(misP);
                                misJuegos.setText(Integer.toString(misJ));
                                misSets.setText(Integer.toString(misS));
                                susPuntos.setText(susP);
                                susJuegos.setText(Integer.toString(susJ));
                                susSets.setText(Integer.toString(susS));
                                Log.e("WEAR", "On UI run setText ---------------");
                            }
                        });
                    }
                }
                dataItems.release();
            }
        });

        SwipeDismissFrameLayout root = (SwipeDismissFrameLayout) findViewById(R.id.swipe_dismiss_root);
        root.addCallback(new SwipeDismissFrameLayout.Callback() {

            @Override
            public void onDismissed(SwipeDismissFrameLayout layout) {

                finish();
            }
        });

    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        misPuntos.setTypeface(fuenteFina);
        misPuntos.getPaint().setAntiAlias(false);
        susPuntos.setTypeface(fuenteFina);
        susPuntos.getPaint().setAntiAlias(false);
        misJuegos.setTypeface(fuenteFina);
        misJuegos.getPaint().setAntiAlias(false);
        susJuegos.setTypeface(fuenteFina);
        susJuegos.getPaint().setAntiAlias(false);
        misSets.setTypeface(fuenteFina);
        misSets.getPaint().setAntiAlias(false);
        susSets.setTypeface(fuenteFina);
        susSets.getPaint().setAntiAlias(false);
        hora.setVisibility(View.VISIBLE);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        misPuntos.setTypeface(fuenteNormal);
        misPuntos.getPaint().setAntiAlias(true);
        susPuntos.setTypeface(fuenteNormal);
        susPuntos.getPaint().setAntiAlias(true);
        misJuegos.setTypeface(fuenteNormal);
        misJuegos.getPaint().setAntiAlias(true);
        susJuegos.setTypeface(fuenteNormal);
        susJuegos.getPaint().setAntiAlias(true);
        misSets.setTypeface(fuenteNormal);
        misSets.getPaint().setAntiAlias(true);
        susSets.setTypeface(fuenteNormal);
        susSets.getPaint().setAntiAlias(true);
        hora.setVisibility(View.GONE);
    }

    void actualizaNumeros(int b) {
        misPuntos.setText(partida.getMisPuntos());
        susPuntos.setText(partida.getSusPuntos());
        misJuegos.setText(partida.getMisJuegos());
        susJuegos.setText(partida.getSusJuegos());
        misSets.setText(partida.getMisSets());
        susSets.setText(partida.getSusSets());
        sincronizarDatos(b);
    }

    void sincronizarDatos(int b) {
        Log.e("Padel Wear", "Sincronizando");
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(
                MOBILE_PUNTUACION);
        putDataMapReq.getDataMap().putString(KEY_MIS_PUNTOS, partida
                .getMisPuntos());
        putDataMapReq.getDataMap().putByte(KEY_MIS_JUEGOS, partida
                .getMisJuegosByte());
        putDataMapReq.getDataMap().putByte(KEY_MIS_SETS, partida
                .getMisSetsByte());
        putDataMapReq.getDataMap().putString(KEY_SUS_PUNTOS, partida
                .getSusPuntos());
        putDataMapReq.getDataMap().putByte(KEY_SUS_JUEGOS, partida
                .getSusJuegosByte());
        putDataMapReq.getDataMap().putByte(KEY_SUS_SETS, partida
                .getSusSetsByte());
        putDataMapReq.getDataMap().putInt(KEY_EQUIPO, b);

        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(apiClient, putDataReq);
        Log.e("Padel Wear", "Request: " + putDataReq.toString());
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        hora.setText(c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE));
    }

    private void mandarMensaje(final String path, final String texto) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodos = Wearable.NodeApi.getConnectedNodes(apiClient).await();
                for (Node nodo : nodos.getNodes()) {
                    Wearable.MessageApi.sendMessage(apiClient, nodo.getId(), path, texto.getBytes())
                            .setResultCallback(
                                    new ResultCallback<MessageApi.SendMessageResult>() {
                                        @Override
                                        public void onResult(MessageApi.SendMessageResult resultado) {
                                            if (!resultado.getStatus().isSuccess()) {
                                                Log.e("sincronizacion", "Error al mandar mensaje. Código:" + resultado.getStatus().getStatusCode());
                                            }
                                        }
                                    });
                }
            }
        }).start();
    }

    @Override
    protected void onStop() {
        if (apiClient != null && apiClient.isConnected()) {
            apiClient.disconnect();
        }
        Wearable.DataApi.addListener(apiClient, this);
        Wearable.MessageApi.removeListener(apiClient, this);

        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();
        Wearable.DataApi.addListener(apiClient, this);
        Wearable.MessageApi.addListener(apiClient, this);
    }

    @Override
    public void onDataChanged(DataEventBuffer eventos) {
        Log.e("WEAR", "onDataChanget ---------------");
        for (DataEvent evento : eventos) {
            if (evento.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = evento.getDataItem();
                if (item.getUri().getPath().equals(WEAR_PUNTUACION)) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    misP = dataMap.getString(KEY_MIS_PUNTOS);
                    susP = dataMap.getString(KEY_SUS_PUNTOS);
                    misJ = dataMap.getByte(KEY_MIS_JUEGOS);
                    susJ = dataMap.getByte(KEY_SUS_JUEGOS);
                    misS = dataMap.getByte(KEY_MIS_SETS);
                    susS = dataMap.getByte(KEY_SUS_SETS);

                    switch (dataMap.getInt(KEY_EQUIPO)) {
                        case -1:
                            partida.deshacerPunto();
                            break;
                        case 0:
                            partida.rehacerPunto();
                            break;
                        case 1:
                            partida.puntoPara(true);
                            break;
                        case 2:
                            partida.puntoPara(false);
                            break;
                        default:
                            break;
                    }

                    Log.e("WEAR", "getBytes ---------------");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            misPuntos.setText(misP);
                            misJuegos.setText(Integer.toString(misJ));
                            misSets.setText(Integer.toString(misS));
                            susPuntos.setText(susP);
                            susJuegos.setText(Integer.toString(susJ));
                            susSets.setText(Integer.toString(susS));
                            Log.e("WEAR", "On UI run setText ---------------");
                        }
                    });
                } else if (item.getUri().getPath().equals(ITEM_FOTO)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(item);
                    Asset asset = dataMapItem.getDataMap().getAsset(ASSET_FOTO);
                    LoadBitmapFromAsset tarea = new LoadBitmapFromAsset();
                    tarea.execute(asset);
                }
            } else if (evento.getType() == DataEvent.TYPE_DELETED) {
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(apiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Wearable.DataApi.removeListener(apiClient, this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

    }


    class LoadBitmapFromAsset extends AsyncTask<Asset, Void, Bitmap> {
        private static final int TIMEOUT_MS = 2000;

        @Override
        protected Bitmap doInBackground(Asset... assets) {
            if (assets.length < 1 || assets[0] == null) {
                throw new IllegalArgumentException("El asset no puede ser null");
            }
            ConnectionResult resultado =
                    apiClient.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (!resultado.isSuccess()) {
                return null;
            }
            // convertimos el asset en Stream, bloqueando hasta tenerlo
            InputStream assetInputStream = Wearable.DataApi.getFdForAsset(apiClient, assets[0]).await().getInputStream();
            if (assetInputStream == null) {
                Log.w("Sincronización", "Asset desconocido");
                return null;
            }
            // decodificamos el Stream en un Bitmap
            return BitmapFactory.decodeStream(assetInputStream);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ((LinearLayout) findViewById(R.id.fondo)).setBackground(new BitmapDrawable(getResources(), bitmap));
        }
    }
}
