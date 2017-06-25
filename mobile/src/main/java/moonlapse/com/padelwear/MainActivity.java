package moonlapse.com.padelwear;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {
    private static final int SOLICITUD_PERMISO_CAMARA = 0;
    private final int PHOTO_CODE = 100;
    private View vista;
    private Bitmap bitmap = null;
    private GoogleApiClient apiClient;
    private static final String ITEM_FOTO = "/item_foto";
    private static final String ASSET_FOTO = "/asset_foto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        apiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        vista = findViewById(R.id.fondo);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent intencion = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intencion.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intencion, 1234);
                    }
                } else {
                    solicitarPermisoCamara();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.accion_contador) {
            startActivity(new Intent(this, Contador.class));
            return true;
        }


        if (id == R.id.action_notificaciones) {
            startActivity(new Intent(this, Notificaciones.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void solicitarPermisoCamara() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Snackbar.make(vista, "Falta el permiso de la camera", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, SOLICITUD_PERMISO_CAMARA);
                }
            }).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, SOLICITUD_PERMISO_CAMARA);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            Asset asset = createAssetFromBitmap(bitmap);
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(ITEM_FOTO);
            putDataMapReq.getDataMap().putAsset(ASSET_FOTO, asset);
            PutDataRequest request = putDataMapReq.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> resultdado = Wearable.DataApi.putDataItem(apiClient, request);
        }


    }


    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    @Override
    protected void onStop() {
        if (apiClient != null && apiClient.isConnected()) {
            apiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();
    }
}
