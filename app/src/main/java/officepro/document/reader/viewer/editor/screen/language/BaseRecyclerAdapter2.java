package office.pdf.document.reader.viewer.editor.screen.language;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public abstract class BaseRecyclerAdapter2<T> extends RecyclerView.Adapter<BaseViewHolder> {
    protected List<T> list;
    protected Context context;
    protected CallBackAdapter<T> callBackAdapter;
    private long timeLastClick;

    public BaseRecyclerAdapter2(List<T> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    public abstract BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(@NonNull BaseViewHolder holder, int position);

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public List<T> getList() {
        return list;
    }

    public void addDatas(List<T> datas) {
        list.clear();
        list.addAll(datas);
        notifyDataSetChanged();
    }

    public void addData(T item) {
        list.add(item);
        notifyItemChanged(getItemCount() - 1);
    }

    public void updateData(final int position, T item) {
        list.set(position, item);
        notifyItemChanged(position);
    }

    public void remove(int position) {
        list.remove(position);
        this.notifyItemRemoved(position);
    }

    public void removeItem(T item) {
        list.remove(item);
        this.notifyDataSetChanged();
    }

    public void removeNotifyAll(int position) {
        list.remove(position);
        this.notifyDataSetChanged();
    }

    public void addDataWithPosition(T item, int pos) {
        list.add(pos, item);
        notifyItemChanged(pos);
    }

    public void addDataWithPositionNotifiAll(T item, int pos) {
        list.add(pos, item);
        notifyDataSetChanged();
    }

    public void onClickItem(T item) {
        if (callBackAdapter != null) {
            callBackAdapter.onClickItem(item);
        }
    }

    public boolean duplicateClick() {
        if (System.currentTimeMillis() - timeLastClick < 500L) {
            return true;
        }
        timeLastClick = System.currentTimeMillis();
        return false;
    }

    public void setCallBackAdapter(CallBackAdapter<T> callBackAdapter) {
        this.callBackAdapter = callBackAdapter;
    }

    public interface CallBackAdapter<T> {
        void onClickItem(T item);
    }
}
