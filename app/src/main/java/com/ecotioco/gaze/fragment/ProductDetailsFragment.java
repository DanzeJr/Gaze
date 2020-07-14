package com.ecotioco.gaze.fragment;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.balysv.materialripple.MaterialRippleLayout;
import com.ecotioco.gaze.R;
import com.ecotioco.gaze.ThisApplication;
import com.ecotioco.gaze.adapter.AdapterProductImage;
import com.ecotioco.gaze.connection.GazeAPI;
import com.ecotioco.gaze.connection.VolleyCallback;
import com.ecotioco.gaze.data.Constant;
import com.ecotioco.gaze.data.DatabaseHandler;
import com.ecotioco.gaze.model.Cart;
import com.ecotioco.gaze.model.Category;
import com.ecotioco.gaze.model.Product;
import com.ecotioco.gaze.model.ProductImage;
import com.ecotioco.gaze.model.Wishlist;
import com.ecotioco.gaze.utils.NetworkCheck;
import com.ecotioco.gaze.utils.Tools;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProductDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductDetailsFragment extends Fragment {
    public static final String TAG = "ProductDetails";
    private static final String EXTRA_PRODUCT_ID = "key.EXTRA_PRODUCT_ID";
    private static final String EXTRA_FROM_NOTIFICATION = "key.EXTRA_FROM_NOTIFICATION";

    private View rootView;
    private MenuItem wishlistMenu;
    private long mProductId;
    private Product mProduct;
    private DatabaseHandler db;
    private JsonObjectRequest mRequest;
    private boolean fromNoti;
    private boolean flag_wishlist = false;
    private boolean flag_cart = false;

    private JsonObjectRequest request = null;

    private int mFailedPage = 0;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param productId          Product ID
     * @param isFromNotification Is From Notification
     * @return A new instance of fragment CategoryDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProductDetailsFragment newInstance(long productId, boolean isFromNotification) {
        ProductDetailsFragment fragment = new ProductDetailsFragment();
        Bundle args = new Bundle();
        args.putLong(EXTRA_PRODUCT_ID, productId);
        args.putBoolean(EXTRA_FROM_NOTIFICATION, isFromNotification);
        fragment.setArguments(args);
        return fragment;
    }

    public ProductDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        db = new DatabaseHandler(getContext());

        if (getArguments() != null) {
            mProductId = getArguments().getLong(EXTRA_PRODUCT_ID, -1L);
            fromNoti = getArguments().getBoolean(EXTRA_FROM_NOTIFICATION, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_product_details, container, false);

        initToolbar();
        initComponent();
        requestAction();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_product_details, menu);
        wishlistMenu = menu.findItem(R.id.action_wish);
        refreshWishlistMenu();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().getSupportFragmentManager().popBackStack();
            if (fromNoti) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.dynamic_fragment, HomeFragment.newInstance(), HomeFragment.TAG)
                        .commit();
            }
        } else if (id == R.id.action_wish) {
            if (mProduct.name == null || mProduct.name.equals("")) {
                Toast.makeText(getContext(), R.string.cannot_add_wishlist, Toast.LENGTH_SHORT).show();
                return true;
            }
            if (flag_wishlist) {
                db.deleteWishlist(mProductId);
                Toast.makeText(getContext(), R.string.remove_wishlist, Toast.LENGTH_SHORT).show();
            } else {
                Wishlist w = new Wishlist(mProduct.id, mProduct.name, mProduct.image, new Date());
                db.saveWishlist(w);
                Toast.makeText(getContext(), R.string.add_wishlist, Toast.LENGTH_SHORT).show();
            }
            refreshWishlistMenu();
        } else if (id == R.id.action_cart) {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            fragmentTransaction.replace(R.id.dynamic_fragment, ShoppingCartFragment.newInstance(), ShoppingCartFragment.TAG);
            fragmentTransaction.addToBackStack(ShoppingCartFragment.TAG);
            fragmentTransaction.commit();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("");
    }

    private void initComponent() {
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestAction();
            }
        });

        rootView.findViewById(R.id.lyt_add_cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mProduct == null || (mProduct.name != null && mProduct.name.equals(""))) {
                    Toast.makeText(getActivity(), R.string.please_wait_text, Toast.LENGTH_SHORT).show();
                    return;
                }
                toggleCartButton();
            }
        });

    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                requestNewsInfoDetailsApi();
                mProduct = new Product();
                mProduct.id = mProductId;
                mProduct.name = "Sunglass 2020";
                mProduct.image = "https://picsum.photos/" + (mProductId * 15);
                mProduct.discount = mProductId / 10d;
                mProduct.description = "Description " + 1;
                mProduct.price = mProductId + 22d;
                mProduct.createdDate = new Date();
                mProduct.lastUpdate = new Date();
                mProduct.productImages = new ArrayList<>();
                for (int j = 0; j < 5; j++) {
                    ProductImage image = new ProductImage();
                    image.url = "https://picsum.photos/" + (mProductId * 8);
                    image.productId = mProduct.id;
                    mProduct.productImages.add(image);
                }
                mProduct.status = 2 % (int)mProductId;
                mProduct.stock = (long) mProductId + 2;
                mProduct.categories = new ArrayList<>();
                for (long i = 0; i < mProductId; i++) {
                    Category info = new Category();
                    info.id = i;
                    info.brief = i + "ABC";
                    info.createdDate = new Date();
                    info.name = i + "News";
                    info.color = "#db303" + i;
                    mProduct.categories.add(info);
                }
                displayPostData();
                swipeProgress(false);
            }
        }, 2000);
    }

    private void onFailRequest() {
        swipeProgress(false);
        if (NetworkCheck.isConnect(getActivity())) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.no_internet_text));
        }
    }

    private void requestNewsInfoDetailsApi() {
        GazeAPI api = GazeAPI.getInstance(getContext());
        VolleyCallback<JSONObject> callback = new VolleyCallback<JSONObject>() {
            @Override
            public void handleResponse(JSONObject result) {
                if (result != null) {
                    mProduct = GazeAPI.get(result, Product.class);
                    displayPostData();
                    swipeProgress(false);
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void handleError(VolleyError error) {
                if (request != null && !request.isCanceled()) onFailRequest();
            }
        };
        request = api.getProductDetails(mProductId, callback);
    }

    private void displayPostData() {
        TextView titleTextView = rootView.findViewById(R.id.title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            titleTextView.setText(Html.fromHtml(mProduct.name, Html.FROM_HTML_MODE_LEGACY));
        } else {
            titleTextView.setText(Html.fromHtml(mProduct.name));
        }

        WebView webView = rootView.findViewById(R.id.content);
        String html_data = "<style>img{max-width:100%;height:auto;} iframe{width:100%;}</style> ";
        html_data += mProduct.description;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadData(html_data, "text/html; charset=UTF-8", null);
        // disable scroll on touch
        webView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        ((TextView) rootView.findViewById(R.id.date)).setText(Tools.getFormattedDate(mProduct.lastUpdate));

        TextView price = rootView.findViewById(R.id.price);
        TextView priceStrike = rootView.findViewById(R.id.price_strike);

        // handle discount view
        if (mProduct.discount > 0) {
            price.setText(Tools.getFormattedPrice(mProduct.discount, getActivity()));
            priceStrike.setText(Tools.getFormattedPrice(mProduct.price, getActivity()));
            priceStrike.setPaintFlags(priceStrike.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            priceStrike.setVisibility(View.VISIBLE);
        } else {
            price.setText(Tools.getFormattedPrice(mProduct.price, getActivity()));
            priceStrike.setVisibility(View.GONE);
        }

        if (mProduct.status == Product.READY_STOCK) {
            ((TextView) rootView.findViewById(R.id.status)).setText(getString(R.string.ready_stock));
        } else if (mProduct.status == Product.OUT_OF_STOCK) {
            ((TextView) rootView.findViewById(R.id.status)).setText(getString(R.string.out_of_stock));
        } else if (mProduct.status == Product.SUSPEND) {
            ((TextView) rootView.findViewById(R.id.status)).setText(getString(R.string.suspend));
        } else {
            ((TextView) rootView.findViewById(R.id.status)).setText(mProduct.status);
        }

        // display Image slider
        displayImageSlider();

        // display category list at bottom
        displayCategoryProduct();

        Toast.makeText(getContext(), R.string.msg_data_loaded, Toast.LENGTH_SHORT).show();

        // analytics track
        ThisApplication.getInstance().saveLogEvent(mProduct.id, mProduct.name, "PRODUCT_DETAILS");
    }

    private void displayImageSlider() {
        final LinearLayout layout_dots = (LinearLayout) rootView.findViewById(R.id.layout_dots);
        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.pager);
        final AdapterProductImage adapterSlider = new AdapterProductImage(getActivity(), new ArrayList<ProductImage>());

        final List<ProductImage> productImages = new ArrayList<>();
        ProductImage p = new ProductImage();
        p.productId = mProduct.id;
        p.url = mProduct.image;
        productImages.add(p);
        if (mProduct.productImages != null) productImages.addAll(mProduct.productImages);
        adapterSlider.setItems(productImages);
        viewPager.setAdapter(adapterSlider);

        // displaying selected image first
        viewPager.setCurrentItem(0);
        addBottomDots(layout_dots, adapterSlider.getCount(), 0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int pos, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int pos) {
                addBottomDots(layout_dots, adapterSlider.getCount(), pos);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        final ArrayList<String> images_list = new ArrayList<>();
        for (ProductImage img : productImages) {
            images_list.add(Constant.getURLimgProduct(img.url));
        }

        adapterSlider.setOnItemClickListener(new AdapterProductImage.OnItemClickListener() {
            @Override
            public void onItemClick(View view, ProductImage obj, int pos) {
//                Intent i = new Intent(ActivityProductDetails.this, ActivityFullScreenImage.class);
//                i.putExtra(ActivityFullScreenImage.EXTRA_POS, pos);
//                i.putStringArrayListExtra(ActivityFullScreenImage.EXTRA_IMGS, images_list);
//                startActivity(i);
            }
        });
    }

    private void displayCategoryProduct() {
        TextView category = (TextView) rootView.findViewById(R.id.category);
        String html_data = "";
        for (int i = 0; i < mProduct.categories.size(); i++) {
            html_data += (i + 1) + ". " + mProduct.categories.get(i).name + "\n";
        }
        category.setText(html_data);
    }

    private void addBottomDots(LinearLayout layout_dots, int size, int current) {
        ImageView[] dots = new ImageView[size];

        layout_dots.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(getActivity());
            int width_height = 15;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(width_height, width_height));
            params.setMargins(10, 10, 10, 10);
            dots[i].setLayoutParams(params);
            dots[i].setImageResource(R.drawable.shape_circle);
            dots[i].setColorFilter(ContextCompat.getColor(getContext(), R.color.darkOverlaySoft));
            layout_dots.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[current].setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimaryLight));
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = (View) rootView.findViewById(R.id.lyt_failed);
        View lyt_main_content = (View) rootView.findViewById(R.id.lyt_main_content);

        ((TextView) rootView.findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lyt_main_content.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            lyt_main_content.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        ((Button) rootView.findViewById(R.id.failed_retry)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction();
            }
        });
    }

    private void swipeProgress(final boolean show) {
        final SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
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

    private void refreshWishlistMenu() {
        Wishlist wishlist = db.getWishlist(mProductId);
        flag_wishlist = (wishlist != null);
        if (flag_wishlist) {
            wishlistMenu.setIcon(R.drawable.ic_wish);
        } else {
            wishlistMenu.setIcon(R.drawable.ic_wish_outline);
        }
    }

    private void toggleCartButton() {
        if (flag_cart) {
            db.deleteActiveCart(mProductId);
            Toast.makeText(getActivity(), R.string.remove_cart, Toast.LENGTH_SHORT).show();
        } else {
            // check stock product
            if (mProduct.stock == 0 || mProduct.status == Product.OUT_OF_STOCK) {
                Toast.makeText(getActivity(), R.string.msg_out_of_stock, Toast.LENGTH_SHORT).show();
                return;
            }
            if (mProduct.status == Product.SUSPEND) {
                Toast.makeText(getActivity(), R.string.msg_suspend, Toast.LENGTH_SHORT).show();
                return;
            }
            Cart cart = new Cart(mProduct, 1, new Date());
//            db.saveCart(cart);
            Toast.makeText(getActivity(), R.string.add_cart, Toast.LENGTH_SHORT).show();
        }
        refreshCartButton();
    }

    private void refreshCartButton() {
        Product product = new Product();
        product.id = mProductId;
        product.price = mProductId * 5d;
        product.color = "#FF704" + mProductId % 9;
        product.createdDate = new Date();
        product.lastUpdate = product.createdDate;
        product.name = "Product " + mProductId;
        product.stock = mProductId % 17;
        product.status = (int) (mProductId % 2);
        product.image = "";
        product.size = (int) (mProductId % 2);
        product.discount = mProductId / 3d;
        Cart c = new Cart(product, (int)mProductId % 12, new Date()); //db.getCart(mProductId);
        flag_cart = (c != null);
        MaterialRippleLayout layoutAddCart = rootView.findViewById(R.id.lyt_add_cart);
        TextView tvAddCart = rootView.findViewById(R.id.tv_add_cart);
        if (flag_cart) {
            layoutAddCart.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorRemoveCart));
            tvAddCart.setText(R.string.bt_remove_cart);
        } else {
            layoutAddCart.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAddCart));
            tvAddCart.setText(R.string.bt_add_cart);
        }
    }
}