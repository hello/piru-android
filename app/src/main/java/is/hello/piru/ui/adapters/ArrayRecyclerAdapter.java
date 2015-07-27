package is.hello.piru.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ArrayRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private final List<T> storage;

    protected ArrayRecyclerAdapter(@NonNull List<T> storage) {
        this.storage = storage;
    }

    protected ArrayRecyclerAdapter() {
        this(new ArrayList<>());
    }

    //region Operations

    @Override
    public int getItemCount() {
        return storage.size();
    }

    public boolean isEmpty() {
        return storage.isEmpty();
    }

    public T getItem(int position) {
        return storage.get(position);
    }

    public boolean addAll(Collection<? extends T> collection) {
        int oldSize = storage.size();
        if (storage.addAll(collection)) {
            notifyItemRangeInserted(oldSize, collection.size() - oldSize);
            return true;
        } else {
            return false;
        }
    }

    public boolean add(T object) {
        int oldSize = storage.size();
        if (storage.add(object)) {
            notifyItemInserted(oldSize);
            return true;
        } else {
            return false;
        }
    }

    public T remove(int location) {
        T removed = storage.remove(location);
        notifyItemRemoved(location);
        return removed;
    }

    public T set(int location, T object) {
        T changed = storage.set(location, object);
        notifyItemChanged(location);
        return changed;
    }

    public int indexOf(T needle) {
        return storage.indexOf(needle);
    }

    public void clear() {
        int oldSize = storage.size();
        storage.clear();
        notifyItemRangeRemoved(0, oldSize);
    }

    //endregion
}
