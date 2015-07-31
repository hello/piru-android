package is.hello.piru.ui.adapters;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import is.hello.piru.R;

public class HorizontalDividerDecoration extends RecyclerView.ItemDecoration {
    private final Paint paint = new Paint();
    private final int height;
    private final Rect insets;

    public HorizontalDividerDecoration(int color, int height, @NonNull Rect insets) {
        paint.setColor(color);
        this.height = height;
        this.insets = insets;
    }

    public HorizontalDividerDecoration(@NonNull Resources resources) {
        this(resources.getColor(R.color.divider),
             resources.getDimensionPixelSize(R.dimen.divider_height),
             new Rect(resources.getDimensionPixelSize(R.dimen.gap_outer), 0, 0, 0));
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.bottom += height;
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        for (int i = 0, count = parent.getChildCount(); i < count; i++) {
            View child = parent.getChildAt(i);
            canvas.drawRect(child.getLeft() + insets.left, child.getBottom() - height,
                    child.getRight() - insets.right, child.getBottom(), paint);
        }
    }
}
