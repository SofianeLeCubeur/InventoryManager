package com.github.sofiman.inventory.model;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sofiman.inventory.api.DataField;
import com.github.sofiman.inventory.ui.components.InputFieldComponent;

import java.util.List;

public class InputFieldListAdapter extends RecyclerView.Adapter<InputFieldListAdapter.InputFieldHolder> {

    private final Context context;
    private List<DataField> fields;
    private final int marginBottom;

    public InputFieldListAdapter(Context context, List<DataField> links) {
        this.context = context;
        this.fields = links;
         marginBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
    }

    public List<DataField> getFields() {
        return fields;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return fields.size();
    }

    @NonNull
    @Override
    public InputFieldHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        InputFieldComponent component = new InputFieldComponent(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, marginBottom);
        component.setLayoutParams(params);
        return new InputFieldHolder(component);
    }

    @Override
    public void onBindViewHolder(@NonNull InputFieldHolder holder, int position) {
        InputFieldComponent component = (InputFieldComponent) holder.itemView;
        DataField store = fields.get(position);

        component.resetContinuity();

        if(position == 0){
            component.setFirstInContinuity();
        }
        if(position == fields.size() - 1){
            component.setLastInContinuity();
        }

        component.setIcon(store.getIcon());
        component.setHint(store.getHint());
        component.setContent(store.getValue());
        component.setEditListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                store.setValue(s.toString());
            }
        });
        component.setOnClickListener(view -> component.focus());

        component.update();
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    public static class InputFieldHolder extends RecyclerView.ViewHolder {
        public InputFieldHolder(@NonNull InputFieldComponent itemView) {
            super(itemView);
        }
    }
}
