package moonlapse.com.padelwear;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by marzzelo on 25/6/2017.
 */

public class LinearLayoutForSwipe extends LinearLayout {

    public LinearLayoutForSwipe(Context context) {
        super(context);
    }

    public LinearLayoutForSwipe(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LinearLayoutForSwipe(Context context, @Nullable AttributeSet attrs,
                                int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean canScrollHorizontally(int dir) {
        return true;
    }
}
