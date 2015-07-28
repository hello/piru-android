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

    public HorizontalDividerDecoration(int color, int height) {
        paint.setColor(color);
        this.height = height;
    }

    public HorizontalDividerDecoration(@NonNull Resources resources) {
        this(resources.getColor(R.color.divider),
             resources.getDimensionPixelSize(R.dimen.divider_height));
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int lastPosition = parent.getAdapter().getItemCount() - 1;
        int position = parent.getChildAdapterPosition(view);

        if (position < lastPosition) {
            outRect.bottom += height;
        }
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int lastPosition = parent.getAdapter().getItemCount() - 1;

        for (int i = 0, count = parent.getChildCount(); i < count; i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);
            if (position < lastPosition) {
                canvas.drawRect(child.getLeft(), child.getBottom() - height,
                        child.getRight(), child.getBottom(), paint);
            }
        }
    }
}
