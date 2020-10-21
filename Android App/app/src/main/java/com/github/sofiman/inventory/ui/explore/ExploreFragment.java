package com.github.sofiman.inventory.ui.explore;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.api.SearchHistoryItem;
import com.github.sofiman.inventory.impl.APIResponse;
import com.github.sofiman.inventory.impl.Fetcher;
import com.github.sofiman.inventory.impl.HistoryDataModel;
import com.github.sofiman.inventory.impl.RequestError;
import com.github.sofiman.inventory.model.ContainerListAdapter;
import com.github.sofiman.inventory.model.InventoryListAdapter;
import com.github.sofiman.inventory.model.ItemListAdapter;
import com.github.sofiman.inventory.model.SearchHistoryAdapter;
import com.github.sofiman.inventory.ui.components.TagComponent;
import com.github.sofiman.inventory.utils.Animations;
import com.github.sofiman.inventory.utils.Callback;
import com.github.sofiman.inventory.utils.LayoutHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExploreFragment extends Fragment {

    private Map<String, TagComponent> tagList = new HashMap<>();
    private String activeTag = "";

    private SearchView query;
    private final HashMap<String, String> matches = new HashMap<>();
    private AtomicBoolean filtersReduced = new AtomicBoolean(false);
    private final Pattern filterRegex = Pattern.compile("([a-zA-Z_-]+):\\s?([^;:]+);");
    private final Pattern rawFilterRegex = Pattern.compile("([a-zA-Z_-]+):\\s?([^;:]+)");
    private String previousQuery = "";

    private HistoryDataModel historyDataModel;
    private SearchHistoryAdapter historyAdapter;
    private Gson gson = new Gson();

    private List<FilterHolder> filters;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_explore, container, false);
        LinearLayout head = root.findViewById(R.id.explore_header);
        LayoutHelper.addStatusBarOffset(getContext(), head);

        historyDataModel = HistoryDataModel.getInstance();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        historyDataModel.load(sharedPreferences);

        query = root.findViewById(R.id.explore_search_bar);

        LinearLayout tags = root.findViewById(R.id.explore_tag_bar);

        for (int i = 0; i < tags.getChildCount(); i++) {
            final TagComponent component = (TagComponent) tags.getChildAt(i);
            final String tagId = component.getTagId();
            this.tagList.put(tagId, component);
            component.setOnToggleListener(new TagComponent.ToggleListener() {
                @Override
                public void onToggle(boolean state) {
                    if (state) {
                        for (Map.Entry<String, TagComponent> tag : tagList.entrySet()) {
                            if (!tag.getKey().equals(tagId)) {
                                tag.getValue().setActive(false);
                            }
                        }
                        activeTag = tagId;
                    }
                }
            });
            if (tagId.equals("all")) {
                component.setActive(true);
                activeTag = tagId;
            }
        }

        filters = new ArrayList<>();

        LinearLayout filtersLayout = root.findViewById(R.id.explorer_filters_layout);
        TextView title = root.findViewById(R.id.explore_filters_title);
        ImageView reduce = root.findViewById(R.id.explore_reduce_filters);

        for (int i = 0; i < filtersLayout.getChildCount(); i++) {
            View v = filtersLayout.getChildAt(i);
            if (!(v instanceof LinearLayout)) continue;

            LinearLayout filterLayout = (LinearLayout) v;
            final FilterHolder filter = new FilterHolder(getContext(), filterLayout);
            filters.add(filter);

            filterLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!filter.isActive()) {
                        if (filtersReduced.get()) {
                            int count = matches.size();
                            String titleStr = getString(R.string.explore_filters_title_reduced, count);
                            title.setText(titleStr);
                        }
                        String s = query.getQuery() + filter.toString();
                        query.setQuery(s, false);
                        query.requestFocus();
                    }
                }
            });
        }

        LinearLayout history = root.findViewById(R.id.explorer_search_history);
        LinearLayout noResult = root.findViewById(R.id.explorer_no_result);
        history.setVisibility(View.VISIBLE);

        query.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(rawFilterRegex.matcher(query.getQuery()).matches()){
                    query.setQuery(query.getQuery() + ";", true);
                    return false;
                } else if(matches.size() == 0 && query.getQuery().length() > 0){
                    query.setQuery("name:" + query.getQuery() + ";", true);
                    return false;
                }
                if (history.getVisibility() == View.VISIBLE) {
                    Animations.collapse(history);
                }
                noResult.setVisibility(View.GONE);
                query();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Matcher m = filterRegex.matcher(s);
                matches.clear();
                while (m.find()) {
                    matches.put(m.group(1), m.group(2));
                }
                int count = matches.size();

                if (s.isEmpty() && history.getVisibility() == View.GONE && historyDataModel.getSearchHistory().size() > 0) {
                    Animations.expand(history);
                }
                noResult.setVisibility(View.GONE);

                for (FilterHolder filter : filters) {
                    String value = matches.get(filter.getFilterPrefix());
                    filter.setContent(value);
                    if (value == null) {
                        filter.setActive(false);
                    } else if (!filter.isActive()) {
                        filter.setActive(true);
                    }
                    if (filtersReduced.get()) {
                        String titleStr = getString(R.string.explore_filters_title_reduced, count);
                        title.setText(titleStr);
                    }
                }
                return false;
            }
        });

        reduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filtersReduced.get()) {
                    filtersReduced.set(false);
                    reduce.animate().rotation(180).setInterpolator(new LinearInterpolator()).setDuration(170);
                    Animations.expand(filtersLayout);
                    title.setText(R.string.explore_filters_title);
                } else {
                    int count = matches.size();
                    filtersReduced.set(true);
                    reduce.animate().rotation(0).setInterpolator(new LinearInterpolator()).setDuration(170);
                    Animations.collapse(filtersLayout);
                    String titleStr = getString(R.string.explore_filters_title_reduced, count);
                    title.setText(titleStr);
                }
            }
        });

        RecyclerView invRecycler = root.findViewById(R.id.explorer_inventories);
        RecyclerView cntRecycler = root.findViewById(R.id.explorer_containers);
        RecyclerView itmRecycler = root.findViewById(R.id.explorer_items);
        RecyclerView searchHistory = root.findViewById(R.id.explorer_history_entries);

        invRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        cntRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        itmRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        searchHistory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        historyAdapter = new SearchHistoryAdapter(getContext(), historyDataModel.getSearchHistory(), new Callback<SearchHistoryItem>() {
            @Override
            public void run(SearchHistoryItem data) {
                query.setQuery(data.getQuery(), false);
                query.requestFocus();
            }
        });

        searchHistory.setAdapter(historyAdapter);
        if(historyAdapter.getItemCount() == 0){
            history.setVisibility(View.GONE);
        }

        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("activeTag", activeTag);
        outState.putCharSequence("query", query.getQuery());
        outState.putBoolean("filtersReduced", filtersReduced.get());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState == null) return;

        this.activeTag = savedInstanceState.getString("activeTag", "");
        if (savedInstanceState.containsKey("query")) {
            this.query.setQuery(savedInstanceState.getCharSequence("query", ""), true);
        }
        this.filtersReduced.set(savedInstanceState.getBoolean("filtersReduced", false));

        LinearLayout filtersLayout = getView().findViewById(R.id.explorer_filters_layout);
        ImageView reduce = getView().findViewById(R.id.explore_reduce_filters);
        if (filtersReduced.get()) {
            filtersReduced.set(false);
            reduce.animate().rotation(180).setInterpolator(new LinearInterpolator()).setDuration(170);
            Animations.expand(filtersLayout);
        } else {
            filtersReduced.set(true);
            reduce.animate().rotation(0).setInterpolator(new LinearInterpolator()).setDuration(170);
            Animations.collapse(filtersLayout);
        }
    }

    private void query() {
        View root = getView();
        if (root == null) return;
        ProgressBar bar = root.findViewById(R.id.explorer_loading);
        bar.setVisibility(View.VISIBLE);
        LinearLayout results = root.findViewById(R.id.explorer_results);
        RecyclerView invRecycler = root.findViewById(R.id.explorer_inventories);
        RecyclerView cntRecycler = root.findViewById(R.id.explorer_containers);
        RecyclerView itmRecycler = root.findViewById(R.id.explorer_items);
        TextView invLabel = root.findViewById(R.id.explorer_inventories_label);
        TextView cntLabel = root.findViewById(R.id.explorer_containers_label);
        TextView itmLabel = root.findViewById(R.id.explorer_items_label);
        ImageView reduce = root.findViewById(R.id.explore_reduce_filters);
        LinearLayout noResult = root.findViewById(R.id.explorer_no_result);
        TextView noResultQuery = root.findViewById(R.id.explorer_no_result_content);
        LinearLayout filtersLayout = root.findViewById(R.id.explorer_filters_layout);

        query.setEnabled(false);
        final long timestamp = System.currentTimeMillis();
        final String activeTag = this.activeTag.toLowerCase();
        final String queryText = this.query.getQuery().toString();

        System.out.println("Performing query with " + matches + " selecting " + activeTag);
        matches.put("type", activeTag);
        Fetcher.getInstance().doQuery(matches, new APIResponse<HashMap<String, Object>>() {
            @Override
            public void response(HashMap<String, Object> callback) {
                bar.setVisibility(View.GONE);

                List<Inventory> inventories = new ArrayList<>();
                List<Container> containers = new ArrayList<>();
                List<Item> items = new ArrayList<>();

                try {
                    ArrayList<LinkedTreeMap<?, ?>> rawInventories = (ArrayList<LinkedTreeMap<?, ?>>) callback.get("inventories");
                    ArrayList<LinkedTreeMap<?, ?>> rawContainers = (ArrayList<LinkedTreeMap<?, ?>>) callback.get("containers");
                    ArrayList<LinkedTreeMap<?, ?>> rawItems = (ArrayList<LinkedTreeMap<?, ?>>) callback.get("items");

                    JsonObject content;
                    for (LinkedTreeMap<?, ?> json : rawInventories) {
                        content = gson.toJsonTree(json).getAsJsonObject();
                        inventories.add(gson.fromJson(content, Inventory.class));
                    }

                    for (LinkedTreeMap<?, ?> json : rawContainers) {
                        content = gson.toJsonTree(json).getAsJsonObject();
                        containers.add(gson.fromJson(content, Container.class));
                    }

                    for (LinkedTreeMap<?, ?> json : rawItems) {
                        content = gson.toJsonTree(json).getAsJsonObject();
                        items.add(gson.fromJson(content, Item.class));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                results.setVisibility(View.VISIBLE);

                boolean hasData = false;
                if (inventories.size() > 0) {
                    invLabel.setVisibility(View.VISIBLE);
                    hasData = true;
                } else {
                    invLabel.setVisibility(View.GONE);
                }

                if (containers.size() > 0) {
                    cntLabel.setVisibility(View.VISIBLE);
                    hasData = true;
                } else {
                    cntLabel.setVisibility(View.GONE);
                }

                if (items.size() > 0) {
                    itmLabel.setVisibility(View.VISIBLE);
                    hasData = true;
                } else {
                    itmLabel.setVisibility(View.GONE);
                }

                invRecycler.setAdapter(new InventoryListAdapter(getContext(), inventories, data -> LayoutHelper.openInventory(getActivity(), data)));
                cntRecycler.setAdapter(new ContainerListAdapter(getContext(), containers, data -> LayoutHelper.openContainer(getActivity(), data)));
                itmRecycler.setAdapter(new ItemListAdapter(getContext(), items, data -> LayoutHelper.openItem(getActivity(), data)));

                if (hasData) {
                    if (!previousQuery.equals(queryText)) {
                        historyDataModel.pushSearchEntry(new SearchHistoryItem(queryText, timestamp, activeTag, inventories.size() + containers.size() + items.size()), getContext());
                        historyAdapter.setSearchHistory(historyDataModel.getSearchHistory());
                        historyAdapter.notifyDataSetChanged();
                    }
                    noResult.setVisibility(View.GONE);
                    filtersReduced.set(true);
                    reduce.animate().rotation(0).setInterpolator(new LinearInterpolator()).setDuration(170);
                    Animations.collapse(filtersLayout);
                    previousQuery = queryText;
                } else {
                    noResult.setVisibility(View.VISIBLE);
                    noResultQuery.setText(query.getQuery());
                }
                query.setEnabled(true);
            }

            @Override
            public void error(RequestError error) {
                System.err.println("Could not fetch query: " + error);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bar.setVisibility(View.GONE);
                        query.setEnabled(true);
                        results.setVisibility(View.GONE);
                        noResult.setVisibility(View.VISIBLE);
                        noResultQuery.setText(error.getDescription());
                        Toast.makeText(getContext(), getString(R.string.full_page_error_query, error.toString()), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private static class FilterHolder {
        private final Context context;
        private final String content;
        private TextView filterLabel;
        private TextView filterContent;
        private String filterPrefix;
        private boolean active = false;

        public FilterHolder(Context context, LinearLayout filterLayout) {
            this.context = context;
            this.filterLabel = (TextView) filterLayout.getChildAt(0);
            this.filterContent = (TextView) filterLayout.getChildAt(1);
            this.content = this.filterContent.getText().toString();
            this.filterPrefix = filterLayout.getTag().toString().split("search_filter:")[1];
        }

        public void setActive(boolean active) {
            this.active = active;
            int foreground, background;
            if (active) {
                foreground = context.getColor(R.color.colorAccent);
                background = R.color.colorAccentTransparent;
            } else {
                foreground = context.getColor(R.color.foreground);
                background = R.color.image_background;
            }
            filterLabel.setTextColor(foreground);
            filterLabel.setBackgroundResource(background);
        }

        public void setContent(String content) {
            if (content == null) {
                this.filterContent.setText(this.content);
            } else {
                this.filterContent.setText(content);
            }
        }

        @NonNull
        public String toString() {
            return filterPrefix + ":";
        }

        public String getFilterPrefix() {
            return filterPrefix;
        }

        public boolean isActive() {
            return active;
        }
    }
}