package office.pdf.document.reader.viewer.editor.screen.language;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

public abstract class BaseViewHolder<BD extends ViewBinding> extends RecyclerView.ViewHolder {
    public BD binding;

    public BaseViewHolder(BD binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
