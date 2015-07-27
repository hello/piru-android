package is.hello.piru.ui.adapters;

import android.content.res.Resources;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import is.hello.piru.R;

public class CardDecoration extends RecyclerView.ItemDecoration {
    private final int outerPadding;
    private final int interPadding;

    public CardDecoration(@NonNull Resources resources) {
        this.outerPadding = resources.getDimensionPixelSize(R.dimen.gap_outer);
        this.interPadding = resources.getDimensionPixelSize(R.dimen.gap_inter);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int lastPosition = parent.getAdapter().getItemCount() - 1;
        int position = parent.getChildAdapterPosition(view);

        outRect.left = outerPadding;
        outRect.right = outerPadding;

        if (position == 0) {
            outRect.top = outerPadding;
            outRect.bottom = interPadding;
        } else if (position == lastPosition) {
            outRect.top = 0;
            outRect.bottom = outerPadding;
        } else {
            outRect.top = 0;
            outRect.bottom = interPadding;
        }
    }
}
