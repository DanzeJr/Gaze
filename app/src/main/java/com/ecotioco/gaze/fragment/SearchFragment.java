package com.ecotioco.gaze.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ecotioco.gaze.R;
import com.ecotioco.gaze.ThisApplication;
import com.ecotioco.gaze.adapter.AdapterProduct;
import com.ecotioco.gaze.connection.GazeAPI;
import com.ecotioco.gaze.connection.VolleyCallback;
import com.ecotioco.gaze.data.Constant;
import com.ecotioco.gaze.model.Product;
import com.ecotioco.gaze.utils.NetworkCheck;
import com.ecotioco.gaze.utils.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    public static final String TAG = "Search";
    private static final String EXTRA_CATEGORY_ID = "key.CATEGORY_ID";
    private static final String EXTRA_CATEGORY_NAME = "key.CATEGORY_NAME";

    private View rootView;
    private AdapterProduct adapterProduct;
    private JsonObjectRequest request;

    private int totalItems = 0;
    private int failedPage = 0;
    private long categoryId = -1L;
    private String categoryName;
    private String query = "";

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentSearch.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            categoryId = getArguments().getLong(EXTRA_CATEGORY_ID, -1L);
            categoryName = getArguments().getString(EXTRA_CATEGORY_NAME, getString(R.string.ALL));
        } else {
            categoryName = getString(R.string.ALL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_search, container, false);
        initComponent();
        setupToolbar();

        return rootView;
    }

    @Override
    public void onResume() {
        adapterProduct.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            }
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initComponent() {
        SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        final EditText etSearch = rootView.findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence c, int i, int i1, int i2) {
                if (c.toString().trim().length() == 0) {
                    rootView.findViewById(R.id.bt_clear).setVisibility(View.GONE);
                } else {
                    rootView.findViewById(R.id.bt_clear).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        ((TextView) rootView.findViewById(R.id.category)).setText(getString(R.string.Category) + categoryName);
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), Tools.getGridSpanCount(getActivity())));
        recyclerView.setHasFixedSize(true);
        //set data and list adapter
        adapterProduct = new AdapterProduct(getContext(), recyclerView, new ArrayList<Product>());
        recyclerView.setAdapter(adapterProduct);
        adapterProduct.setOnItemClickListener(new AdapterProduct.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Product obj, int pos) {
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.dynamic_fragment, ProductDetailsFragment.newInstance(obj.id, false), ProductDetailsFragment.TAG);
                fragmentTransaction.addToBackStack(ProductDetailsFragment.TAG);
                fragmentTransaction.commit();
            }
        });

        // detect when scroll reach bottom
        adapterProduct.setOnLoadMoreListener(new AdapterProduct.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                if (totalItems > adapterProduct.getItemCount() && current_page != 0) {
                    int next_page = current_page + 1;
                    requestAction(next_page);
                } else {
                    adapterProduct.setLoaded();
                }
            }
        });

        rootView.findViewById(R.id.bt_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etSearch.setText("");
                adapterProduct.resetListData();
                showNoItemView(true);
            }
        });

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard();
                    searchAction();
                    return true;
                }
                return false;
            }
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (request != null) request.cancel();
                adapterProduct.resetListData();
                requestAction(1);
            }
        });

        showNoItemView(true);
    }

    private void searchAction() {
        query = ((EditText)rootView.findViewById(R.id.et_search)).getText().toString().trim();
        if (!query.equals("")) {
            adapterProduct.resetListData();
            // request action will be here
            requestAction(1);
        } else {
            Toast.makeText(getContext(), R.string.please_fill, Toast.LENGTH_SHORT).show();
        }
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            adapterProduct.setLoading();
        }

        // analytics track
        ThisApplication.getInstance().saveCustomLogEvent("SEARCH_PRODUCT", "keyword", query);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestListProduct(page_no);
            }
        }, 1000);
    }

    private void setupToolbar() {
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void requestListProduct(final int page_no) {
        GazeAPI api = GazeAPI.getInstance(getContext());
        VolleyCallback<JSONObject> callback = new VolleyCallback<JSONObject>() {
            @Override
            public void handleResponse(JSONObject result) {
                if (result != null) {
                    try {
                        totalItems = result.getInt("count_total");
                        displayApiResult(Arrays.asList(GazeAPI.get(result.getJSONArray("products"), Product[].class)));
                    } catch (JSONException e) {
                        onFailRequest(page_no);
                    }
                } else {
                    onFailRequest(page_no);
                }
            }

            @Override
            public void handleError(VolleyError error) {
                if (!request.isCanceled()) onFailRequest(page_no);
            }
        };
        request = api.getListProduct(page_no, Constant.PRODUCT_PER_REQUEST, query, categoryId, callback);
    }

    private void displayApiResult(final List<Product> items) {
        adapterProduct.insertData(items);
        swipeProgress(false);
        if (items.size() == 0) showNoItemView(true);
    }

    private void onFailRequest(int page_no) {
        failedPage = page_no;
        adapterProduct.setLoaded();
        swipeProgress(false);
        if (NetworkCheck.isConnect(getContext())) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.no_internet_text));
        }
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = rootView.findViewById(R.id.lyt_failed);
        ((TextView) rootView.findViewById(R.id.failed_message)).setText(message);
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        (rootView.findViewById(R.id.failed_retry)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction(failedPage);
            }
        });
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = rootView.findViewById(R.id.lyt_no_item);
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        final SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(R.id.fab);
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            return;
        }
        // if it already refreshing, return
        if (swipeRefreshLayout.isRefreshing()){
            return;
        }
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(show);
            }
        });
    }
}