package com.ecotioco.gaze.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ecotioco.gaze.R;
import com.ecotioco.gaze.data.DatabaseHandler;
import com.ecotioco.gaze.utils.CallbackDialog;
import com.ecotioco.gaze.utils.DialogUtils;
import com.ecotioco.gaze.utils.Tools;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    public static final String TAG = "Home";

    private View rootView;
    private DatabaseHandler db;
    private Dialog failedDialog = null;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentHome.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHandler(getActivity());
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        initToolbar();
        initDrawerMenu();
        initComponent();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateNavCounter((NavigationView) rootView.findViewById(R.id.nav_view));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search: {
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                fragmentTransaction.replace(R.id.dynamic_fragment, SearchFragment.newInstance(), SearchFragment.TAG);
                fragmentTransaction.addToBackStack(SearchFragment.TAG);
                fragmentTransaction.commit();
                break;
            }
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(R.string.app_name);
    }

    private void initDrawerMenu() {
        final DrawerLayout drawerLayout = rootView.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(getActivity(),
                        drawerLayout,
                        (Toolbar) rootView.findViewById(R.id.toolbar),
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        // Calling sync state is necessary to show your hamburger icon...
        // or so I hear. Doesn't hurt including it even if you find it works
        // without it on your test device(s)
        toggle.syncState();

        ((Toolbar) rootView.findViewById(R.id.toolbar)).setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = rootView.findViewById(R.id.drawer_layout);
                if (!drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });

        NavigationView navigationView = rootView.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem item) {
                onNavItemSelected(item.getItemId());
                return true;
            }
        });
        navigationView.setItemIconTintList(ContextCompat.getColorStateList(getContext(), R.color.nav_state_list));
    }

    private long exitTime = 0;

    public void doExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getActivity(), R.string.press_again_exit_app, Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            getActivity().finish();
        }
    }

    private void initComponent() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                DrawerLayout drawer = rootView.findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    doExitApp();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        NestedScrollView nestedScrollView = rootView.findViewById(R.id.nested_content);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY < oldScrollY) { // up
                    animateFab(false);
                }
                if (scrollY > oldScrollY) { // down
                    animateFab(true);
                }
            }
        });

        // Float Action Button
        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.dynamic_fragment, ShoppingCartFragment.newInstance(), ShoppingCartFragment.TAG);
                fragmentTransaction.addToBackStack(ShoppingCartFragment.TAG);
                fragmentTransaction.commit();
            }
        });

        // set on swipe refresh
        SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFragment();
            }
        });

    }

    private void swipeProgress(final boolean show) {
        final SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        if (!show) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        // if it already refreshing, return
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

    private void refreshFragment() {
        swipeProgress(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                initFragment(true, bundle);
            }
        }, 500);
    }

    boolean isFabHide = false;

    private void animateFab(final boolean hide) {
        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        if (isFabHide && hide || !isFabHide && !hide) return;
        isFabHide = hide;
        int moveY = hide ? (2 * fab.getHeight()) : 0;
        fab.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }

    private void showFailedDialog(@StringRes int message) {
        if (failedDialog != null && failedDialog.isShowing()) return;
//        swipeProgress(false);
        failedDialog = new DialogUtils(getActivity()).buildDialogWarning(-1, message, R.string.TRY_AGAIN, R.drawable.img_no_connect, new CallbackDialog() {
            @Override
            public void onPositiveClick(Dialog dialog) {
                dialog.dismiss();
                refreshFragment();
            }

            @Override
            public void onNegativeClick(Dialog dialog) {
            }
        });
        failedDialog.show();
    }

    private void updateNavCounter(NavigationView nav) {
        Menu menu = nav.getMenu();
        // update cart counter
        int cart_count = db.getActiveCartSize();
        ((TextView) menu.findItem(R.id.nav_cart).getActionView().findViewById(R.id.counter)).setText(String.valueOf(cart_count));

        // update wishlist counter
        int wishlist_count = db.getWishlistSize();
        ((TextView) menu.findItem(R.id.nav_wish).getActionView().findViewById(R.id.counter)).setText(String.valueOf(wishlist_count));

        // update notification counter
        int notif_count = db.getUnreadNotificationSize();
        View dot_sign = (View) menu.findItem(R.id.nav_notif).getActionView().findViewById(R.id.dot);
        if (notif_count > 0) {
            dot_sign.setVisibility(View.VISIBLE);
        } else {
            dot_sign.setVisibility(View.GONE);
        }
    }

    public boolean onNavItemSelected(int id) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        switch (id) {
            //sub menu
            case R.id.nav_cart:
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.dynamic_fragment, ShoppingCartFragment.newInstance(), ShoppingCartFragment.TAG);
                fragmentTransaction.addToBackStack(ShoppingCartFragment.TAG);
                fragmentTransaction.commit();
                break;
            case R.id.nav_wish:
//                i = new Intent(getActivity(), ActivityWishlist.class);
//                startActivity(i);
                break;
            case R.id.nav_history:
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.dynamic_fragment, OrderHistoryFragment.newInstance(), OrderHistoryFragment.TAG);
                fragmentTransaction.addToBackStack(OrderHistoryFragment.TAG);
                fragmentTransaction.commit();
                break;
            case R.id.nav_news:
//                i = new Intent(getActivity(), ActivityNewsInfo.class);
//                startActivity(i);
                break;
            case R.id.nav_notif:
//                i = new Intent(getActivity(), ActivityNotification.class);
//                startActivity(i);
                break;
            case R.id.nav_setting:
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.dynamic_fragment, SettingsFragment.newInstance(), SettingsFragment.TAG);
                fragmentTransaction.addToBackStack(SettingsFragment.TAG);
                fragmentTransaction.commit();
                break;
            case R.id.nav_instruction:
//                i = new Intent(getActivity(), ActivityInstruction.class);
//                startActivity(i);
                break;
            case R.id.nav_rate:
                Tools.rateAction(getActivity());
                break;
            case R.id.nav_about:
                Tools.showDialogAbout(getActivity());
                break;
            default:
                break;
        }
        DrawerLayout drawer = rootView.findViewById(R.id.drawer_layout);
        drawer.closeDrawers();
        return true;
    }
}