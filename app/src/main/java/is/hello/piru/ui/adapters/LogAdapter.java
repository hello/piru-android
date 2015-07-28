package is.hello.piru.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import is.hello.piru.R;

public class LogAdapter extends ArrayRecyclerAdapter<Pair<Integer, String>, LogAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final Resources resources;

    public LogAdapter(@NonNull Context context) {
        this.inflater = LayoutInflater.from(context);
        this.resources = context.getResources();
    }

    private int getColor(int logLevel) {
        switch (logLevel) {
            case Log.ERROR:
                return resources.getColor(R.color.log_error);
            case Log.WARN:
                return resources.getColor(R.color.log_warning);
            default:
                return resources.getColor(R.color.log_ok);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Pair<Integer, String> entry = getItem(position);
        holder.text.setText(entry.second);
        holder.text.setTextColor(getColor(entry.first));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView text;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.text = (TextView) itemView.findViewById(R.id.item_log_text);
        }
    }
}
