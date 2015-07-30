package is.hello.piru.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import is.hello.piru.R;
import is.hello.piru.api.model.FirmwareVersion;

public class FirmwareAdapter extends ArrayRecyclerAdapter<FirmwareVersion, FirmwareAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final DateFormat dateFormat = DateFormat.getDateTimeInstance();

    public FirmwareAdapter(@NonNull Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_with_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FirmwareVersion version = getItem(position);
        holder.nameText.setText(version.getName());
        holder.dateText.setText(dateFormat.format(new Date(version.getCreatedAt())));
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView nameText;
        final TextView dateText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.nameText = (TextView) itemView.findViewById(R.id.item_with_detail_big);
            this.dateText = (TextView) itemView.findViewById(R.id.item_with_detail_small);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View ignored) {
            dispatchItemClicked(getAdapterPosition());
        }
    }
}
