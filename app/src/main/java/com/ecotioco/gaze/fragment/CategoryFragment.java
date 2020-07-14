package com.ecotioco.gaze.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.ecotioco.gaze.R;
import com.ecotioco.gaze.adapter.AdapterCategory;
import com.ecotioco.gaze.connection.GazeAPI;
import com.ecotioco.gaze.connection.VolleyCallback;
import com.ecotioco.gaze.model.Category;
import com.ecotioco.gaze.utils.NetworkCheck;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CategoryFragment extends Fragment {
    public static final String TAG = "Category";

    private View rootView;
    private RecyclerView recyclerView;
    private AdapterCategory adapter;

    public CategoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentHome.
     */
    // TODO: Rename and change types and number of parameters
    public static CategoryFragment newInstance() {
        CategoryFragment fragment = new CategoryFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_category, null);
        initComponent();
        recyclerView.setVisibility(View.VISIBLE);
//        requestListCategory();
        List<Category> listCategory = new ArrayList<>();
        for (long i = 0; i < 10; i++) {
            Category info = new Category();
            info.id = i;
            info.brief = i + "ABC";
            info.createdDate = new Date();
            info.name = i + "News";
            info.color = "#db303" + i;
            listCategory.add(info);
        }
        adapter.setItems(listCategory);
        return rootView;
    }

    private void initComponent() {
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));

        //set data and list adapter
        adapter = new AdapterCategory(getActivity(), new ArrayList<Category>());
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setVisibility(View.GONE);

        adapter.setOnItemClickListener(new AdapterCategory.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Category obj) {
                Snackbar.make(rootView, obj.name, Snackbar.LENGTH_SHORT).show();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                fragmentTransaction.replace(R.id.dynamic_fragment, CategoryDetailsFragment.newInstance(obj), CategoryDetailsFragment.TAG);
                fragmentTransaction.addToBackStack(CategoryDetailsFragment.TAG);
                fragmentTransaction.commit();
            }
        });
    }

    private void requestListCategory() {
        GazeAPI api = GazeAPI.getInstance(getContext());
        VolleyCallback<JSONArray> callback = new VolleyCallback<JSONArray>() {
            @Override
            public void handleResponse(JSONArray result) {
                if (result != null) {
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setItems(Arrays.asList(GazeAPI.get(result, Category[].class)));
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void handleError(VolleyError error) {
                onFailRequest();
            }
        };
        api.getListCategory(callback);
    }

    private void onFailRequest() {
        if (NetworkCheck.isConnect(getActivity())) {
            showFailedView(R.string.msg_failed_load_data);
        } else {
            showFailedView(R.string.no_internet_text);
        }
    }

    private void showFailedView(@StringRes int message) {
    }

}
