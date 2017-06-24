package moonlapse.com.padelwear;

import android.content.Context;
import android.support.wearable.view.CurvedChildLayoutManager;
import android.support.wearable.view.WearableRecyclerView;
import android.view.View;

/**
 * Created by marzzelo on 24/6/2017.
 */

public class MyChildLayoutManager extends CurvedChildLayoutManager {
    private static final float MAX_ICON_PROGRESS = 0.65f; //Escalado máximo
    private float progressToCenter;

    public MyChildLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void updateChild(View child, WearableRecyclerView parent) {
        super.updateChild(child, parent); // Calcula % de avance del hijo desde arriba
        float centerOffset = ((float) child.getHeight() / 2.0f) / (float) parent.getHeight();
        float yRelativeToCenterOffset = (child.getY() / parent.getHeight()) + centerOffset;

        // Normaliza desde el centro
        progressToCenter = Math.abs(0.5f - yRelativeToCenterOffset);
        // Ajusta al máximo de escala
        progressToCenter = Math.min(progressToCenter, MAX_ICON_PROGRESS);
        child.setScaleX(1 - progressToCenter);
        child.setScaleY(1 - progressToCenter);
    }
}
