package com.ecotioco.gaze.fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ecotioco.gaze.R;
import com.ecotioco.gaze.ThisApplication;
import com.ecotioco.gaze.adapter.AdapterProduct;
import com.ecotioco.gaze.connection.GazeAPI;
import com.ecotioco.gaze.connection.VolleyCallback;
import com.ecotioco.gaze.data.AppConfig;
import com.ecotioco.gaze.data.Constant;
import com.ecotioco.gaze.model.Category;
import com.ecotioco.gaze.model.Product;
import com.ecotioco.gaze.model.ProductImage;
import com.ecotioco.gaze.utils.NetworkCheck;
import com.ecotioco.gaze.utils.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoryDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryDetailsFragment extends Fragment {
    public static final String TAG = "CategoryDetails";
    private static final String EXTRA_CATEGORY = "key.CATEGORY";

    private Category mCategory;

    private View mRootView;
    private AdapterProduct mAdapter;
    private JsonObjectRequest mRequest;

    private int mTotalItems = 0;
    private int mFailedPage = 0;

    public CategoryDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param category Category
     * @return A new instance of fragment CategoryDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CategoryDetailsFragment newInstance(@NonNull Category category) {
        CategoryDetailsFragment fragment = new CategoryDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() == null) {
            getActivity().getSupportFragmentManager().popBackStack();
            return;
        }
        mCategory = (Category) getArguments().getSerializable(EXTRA_CATEGORY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_category_details, container, false);

        requestAction(1);
        initToolbar();
        initComponent();

        displayCategoryData();
        return mRootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_category_details, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().getSupportFragmentManager().popBackStack();
        } else if (id == R.id.action_search) {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            fragmentTransaction.replace(R.id.dynamic_fragment, SearchFragment.newInstance(), SearchFragment.TAG);
            fragmentTransaction.addToBackStack(SearchFragment.TAG);
            fragmentTransaction.commit();
        } else if (id == R.id.action_cart) {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            fragmentTransaction.replace(R.id.dynamic_fragment, ShoppingCartFragment.newInstance(), ShoppingCartFragment.TAG);
            fragmentTransaction.addToBackStack(ShoppingCartFragment.TAG);
            fragmentTransaction.commit();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        swipeProgress(false);
        if (mRequest != null) {
            mRequest.cancel();
        }
        super.onDestroyView();
    }

    private void initComponent() {
        RecyclerView recyclerView = mRootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), Tools.getGridSpanCount(getActivity())));
        recyclerView.setHasFixedSize(true);

        //set data and list mAdapter
        mAdapter = new AdapterProduct(getContext(), recyclerView, new ArrayList<Product>());
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterProduct.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Product obj, int position) {
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                fragmentTransaction.replace(R.id.dynamic_fragment, ProductDetailsFragment.newInstance(obj.id, false), ProductDetailsFragment.TAG);
                fragmentTransaction.addToBackStack(ProductDetailsFragment.TAG);
                fragmentTransaction.commit();
            }
        });

        // detect when scroll reach bottom
        mAdapter.setOnLoadMoreListener(new AdapterProduct.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                if (mTotalItems > mAdapter.getItemCount() && current_page != 0) {
                    int next_page = current_page + 1;
                    requestAction(next_page);
                } else {
                    mAdapter.setLoaded();
                }
            }
        });

        // on swipe list
        SwipeRefreshLayout swipeRefreshLayout = mRootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mRequest != null) mRequest.cancel();
                mAdapter.resetListData();
                requestAction(1);
            }
        });

        mRootView.findViewById(R.id.failed_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction(mFailedPage);
            }
        });
    }

    private void displayCategoryData() {
        mRootView.findViewById(R.id.app_bar_layout).setBackgroundColor(Color.parseColor(mCategory.color));
        ((TextView) mRootView.findViewById(R.id.name)).setText(mCategory.name);
        ((TextView) mRootView.findViewById(R.id.brief)).setText(mCategory.brief);
        ImageView icon = mRootView.findViewById(R.id.icon);
        Tools.displayImageOriginal(getActivity(), icon, Constant.getURLimgCategory(mCategory.icon));
        Tools.setSystemBarColorDarker(getActivity(), mCategory.color);
        if (AppConfig.TINT_CATEGORY_ICON) {
            icon.setColorFilter(Color.WHITE);
        }

        // analytics track
        ThisApplication.getInstance().saveLogEvent(mCategory.id, mCategory.name, "CATEGORY_DETAILS");
    }

    private void initToolbar() {
        Toolbar toolbar = mRootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("");
    }

    private void displayApiResult(final List<Product> items) {
        mAdapter.insertData(items);
        swipeProgress(false);
        if (items.size() == 0) showNoItemView(true);
    }

    private void requestListProduct(final int page_no) {
        GazeAPI api = GazeAPI.getInstance(getContext());
        VolleyCallback<JSONObject> callback = new VolleyCallback<JSONObject>() {
            @Override
            public void handleResponse(JSONObject result) {
                if (result != null) {
                    try {
                        mTotalItems = result.getInt("count_total");
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
                onFailRequest(page_no);
            }
        };
        mRequest = api.getListProduct(page_no, Constant.PRODUCT_PER_REQUEST, null, mCategory.id, callback);
    }

    private void onFailRequest(int page_no) {
        mFailedPage = page_no;
        mAdapter.setLoaded();
        swipeProgress(false);
        if (NetworkCheck.isConnect(getContext())) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.no_internet_text));
        }
    }

    private void requestAction(final int page_no) {
        showFailedView(false, null);
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            mAdapter.setLoading();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mTotalItems = 10;
                List<Product> products = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    Product product = new Product();
                    product.id = (long)i;
                    product.name = "Product Name " + i;
                    product.image = "https://picsum.photos/" + (i + 13);
                    product.discount = i / 10d;
                    product.description = "Description " + 1;
                    product.price = i + 22d;
                    product.productImages = new ArrayList<>();
                    for (int j = 0; j < 5; j++) {
                        ProductImage image = new ProductImage();
                        image.url = "https://picsum.photos/" + (i * 8);
                        image.productId = product.id;
                        product.productImages.add(image);
                    }
                    product.status = i + 33;
                    product.stock = (long) i + 2;
                    products.add(product);
                }
                displayApiResult(products);
            }
        }, 2000);
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = mRootView.findViewById(R.id.lyt_failed);
        RecyclerView recyclerView = mRootView.findViewById(R.id.recyclerView);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
            ((TextView) mRootView.findViewById(R.id.failed_message)).setText(message);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = mRootView.findViewById(R.id.lyt_no_item);
        RecyclerView recyclerView = mRootView.findViewById(R.id.recyclerView);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        final SwipeRefreshLayout swipeRefreshLayout = mRootView.findViewById(R.id.swipe_refresh_layout);
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            return;
        }
        if (swipeRefreshLayout.isRefreshing()) {
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