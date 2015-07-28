package is.hello.piru.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import is.hello.piru.R;
import is.hello.piru.bluetooth.PillPeripheral;

public class PillsAdapter extends ArrayRecyclerAdapter<PillPeripheral, PillsAdapter.ViewHolder> {
    private final LayoutInflater inflater;

    public PillsAdapter(@NonNull Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_pill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PillPeripheral pill = getItem(position);
        holder.nameText.setText(pill.getName());
        holder.addressText.setText(pill.getAddress() + " – " + pill.getScanTimeRssi());
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView nameText;
        final TextView addressText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.nameText = (TextView) itemView.findViewById(R.id.item_pill_name);
            this.addressText = (TextView) itemView.findViewById(R.id.item_pill_address);

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View ignored) {
            dispatchItemClicked(getAdapterPosition());
        }
    }
}
