package com.github.sofiman.inventory.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.github.sofiman.inventory.R;

public class SearchHistoryItemComponent extends Component {

    private String query;
    private @DrawableRes int type;

    public SearchHistoryItemComponent(Context context) {
        super(context, R.layout.component_search_history_item);
    }

    public SearchHistoryItemComponent(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.component_search_history_item);
    }

    public SearchHistoryItemComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle, R.layout.component_search_history_item);
    }

    @Override
    public void update() {
        TextView query = findViewById(R.id.search_history_item_query);
        query.setText(this.query);
        ImageView type = findViewById(R.id.search_history_item_icon);
        type.setImageResource(this.type);
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
