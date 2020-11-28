package com.github.sofiman.inventory.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sofiman.inventory.ui.home.HomeActivity;
import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.Server;
import com.github.sofiman.inventory.impl.Fetcher;
import com.github.sofiman.inventory.impl.RequestError;
import com.github.sofiman.inventory.model.SampleListAdapter;
import com.github.sofiman.inventory.utils.Callback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerListFragment extends Fragment {

    private List<Pair<Server, Pair<String, String>>> serverList;
    private ConstraintLayout loadingLayout;

    public ServerListFragment(List<Pair<Server, Pair<String, String>>> serverList, ConstraintLayout loadingLayout){
        this.serverList = serverList;
        this.loadingLayout = loadingLayout;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose_server_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView serverList = view.findViewById(R.id.login_server_list);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        serverList.setLayoutManager(llm);

        Map<String, Pair<Server, Pair<String, String>>> links = new HashMap<>();
        for (Pair<Server, Pair<String, String>> entry : this.serverList){
            links.put(entry.first.getName(), entry);
        }

        view.findViewById(R.id.login_offline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), HomeActivity.class);
                intent.putExtra("selectedItemId", R.id.navigation_settings);
                startActivity(intent);
                getActivity().finish();;
            }
        });

        SampleListAdapter adapter = new SampleListAdapter(getContext(), new ArrayList<String>(links.keySet()));
        adapter.setOnClickListener(new Callback<String>() {
            @Override
            public void run(String data) {
                loadingLayout.setVisibility(View.VISIBLE);
                Pair<Server, Pair<String, String>> entry = links.get(data);
                if(entry == null) return;
                login(entry.first, entry.second.first, entry.second.second, new Callback<RequestError>() {
                    @Override
                    public void run(RequestError e) {
                        System.err.println("Could not connect to server " + entry.first.getEndpoint() + ":" + e);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingLayout.setVisibility(View.INVISIBLE);
                                Toast.makeText(getContext(),  getString(R.string.login_page_no_connection, e.toString()), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        });
        serverList.setAdapter(adapter);
    }

    private void login(Server server, String id, String secret, Callback<RequestError> onFail) {
        final Fetcher fetcher = Fetcher.getInstance();
        fetcher.init(getContext(), server);
        fetcher.login(id, secret, new Callback<RequestError>() {
            @Override
            public void run(RequestError data) {
                if (data == null) {
                    Intent intent = new Intent(getActivity(), HomeActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                } else if (onFail != null) {
                    onFail.run(data);
                }
            }
        });
    }

}
