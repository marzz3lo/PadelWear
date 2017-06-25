package moonlapse.com.padelwear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.util.Log;

/**
 * Created by marzzelo on 22/6/2017.
 */

public class WearReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Notificaciones.ACTION_DEMAND)) {
            String extras = intent.getStringExtra(Notificaciones.EXTRA_MESSAGE);
            Log.v("Notificaciones", "Se recibe ACTION_DEMAND; extras = " + extras);
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            CharSequence reply = remoteInput.getCharSequence(Notificaciones.EXTRA_RESPUESTA_POR_VOZ);
            Log.v("Notificaciones", "Respuesta dictada desde el wearable: " + reply);
        }
    }
}
