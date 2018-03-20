package org.kidcontact.focussis.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.kidcontact.focussis.fragments.AboutFragment;
import org.kidcontact.focussis.fragments.AbsencesFragment;
import org.kidcontact.focussis.fragments.AddressFragment;
import org.kidcontact.focussis.fragments.CalendarFragment;
import org.kidcontact.focussis.fragments.DemographicFragment;
import org.kidcontact.focussis.fragments.EmptyFragment;
import org.kidcontact.focussis.fragments.LoadingFragment;
import org.kidcontact.focussis.fragments.NetworkErrorFragment;
import org.kidcontact.focussis.fragments.NetworkTabAwareFragment;
import org.kidcontact.focussis.fragments.PageFragment;
import org.kidcontact.focussis.fragments.PortalFragment;
import org.kidcontact.focussis.fragments.ReferralsFragment;
import org.kidcontact.focussis.fragments.ScheduleFragment;
import org.kidcontact.focussis.R;
import org.kidcontact.focussis.network.FocusApi;
import org.kidcontact.focussis.network.FocusApiSingleton;
import org.kidcontact.focussis.network.RequestSingleton;
import org.kidcontact.focussis.views.adapters.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FrameLayout fragmentContainer;
    private PageFragment currentFragment;
    private LinearLayout loadingLayout;
    private LinearLayout networkErrorLayout;

    private boolean threadExit = false;
    private boolean inOnLoad = false;

    private FocusApi api;
    // stored for keeping the session alive after it expires
    String username;
    String password;

    private boolean isVisible;

    // used by session thread to avoid spamming reauthenticate() calls
    private boolean authenticating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Toolbar init");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,  mDrawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();

        api = FocusApiSingleton.getApi();
        Log.d(TAG, "Unpacking intent and adding user's name + email to nav bar");
        Intent intent = getIntent();
        username = intent.getStringExtra(getString(R.string.EXTRA_USERNAME));
        password = intent.getStringExtra(getString(R.string.EXTRA_PASSWORD));

        isVisible = true;

        View header = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);
        TextView name = (TextView) header.findViewById(R.id.nav_text_name);
        TextView email = (TextView) header.findViewById(R.id.nav_text_email);

        String[] n = username.replace(".", " ").split(" "); // for some reason you can't split on spaces?
        String first = n[0].substring(0, 1).toUpperCase() + n[0].substring(1);
        String last = n[1].substring(0, 1).toUpperCase() + n[1].substring(1);
        name.setText(first + " " + last);
        email.setText(username + "@asdnh.org");

        Log.d(TAG, "Configure viewpager + tab layout + portal fragment");

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);

        loadingLayout = (LinearLayout) findViewById(R.id.layout_loading);
        networkErrorLayout = (LinearLayout) findViewById(R.id.layout_network_failure);

        tabLayout = (TabLayout) findViewById(R.id.tabs);

        currentFragment = new PortalFragment();
        switchFragment(currentFragment);
        tabLayout.setupWithViewPager(viewPager);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Log.d(TAG, "Starting session keep alive thread");
        final Thread sessionKeepAliveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isSessionExpired() && !authenticating && isVisible) {
                        authenticating = true;
                        Log.i(TAG, "Session timed out, reauthenticating from thread");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                reauthenticate();
                            }
                        });
                    }
                }
            }
        });
        sessionKeepAliveThread.start();

    }

    private void switchFragment(final PageFragment fragment) {
        Log.i(TAG, "Switching to fragment " + fragment.getClass().getCanonicalName());
        threadExit = true;
        if (inOnLoad) {
            Thread waitForOnLoad = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (inOnLoad) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switchFragment(fragment);
                        }
                    });
                }
            });
            waitForOnLoad.start();
        }
        RequestSingleton.getInstance(this).getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, (Fragment) fragment);
        networkErrorLayout.setVisibility(View.GONE);
        if (fragment instanceof NetworkTabAwareFragment) {
            Log.d(TAG, "Fragment is a network tab fragment");
            NetworkTabAwareFragment nFragment = (NetworkTabAwareFragment) fragment;

            if (nFragment.hasTabs()) {
                Log.d(TAG, "Configuring with tabs");
                tabLayout.setVisibility(View.VISIBLE);
                fragmentContainer.setVisibility(View.GONE);
                loadingLayout.setVisibility(View.GONE);
                setupViewPager(viewPager, nFragment.getTabNames(), nFragment.getTabFragments());
            }
            else {
                Log.d(TAG, "Configuring without tabs");
                fragmentContainer.setVisibility(View.GONE);
                loadingLayout.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.GONE);
                ((ViewPagerAdapter) viewPager.getAdapter()).clear();
                viewPager.getAdapter().notifyDataSetChanged();
            }
        }
        else {
            Log.d(TAG, "Configuring static non-tab page");
            tabLayout.setVisibility(View.GONE);
            loadingLayout.setVisibility(View.GONE);
            fragmentContainer.setVisibility(View.VISIBLE);
            ((ViewPagerAdapter) viewPager.getAdapter()).clear();
            viewPager.getAdapter().notifyDataSetChanged();
        }

        Log.d(TAG, "Committing transaction");
        transaction.commitNow();

        if (fragment instanceof NetworkTabAwareFragment) {
            Log.d(TAG, "Starting thread for network fragment load");
            final NetworkTabAwareFragment nFragment = (NetworkTabAwareFragment) fragment;
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(!nFragment.isRequestFinished() && !threadExit) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!threadExit) {
                        inOnLoad = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onFragmentLoad(nFragment);
                            }
                        });
                    }
                }
            });
            threadExit = false;
            thread.start();
        }

        Log.d(TAG, "Setting title");
        setTitle(fragment.getTitle());

    }

    private void onFragmentLoad(NetworkTabAwareFragment fragment) {
        if (currentFragment == fragment) {
            Log.i(TAG, "Fragment " + fragment.getClass().getCanonicalName() + " loaded");
            if (!fragment.hasNetworkError()) {
                Log.d(TAG, "Fragment does not have a network error");
                if (fragment.hasTabs()) {
                    Log.d(TAG, "Setting tab fragments");
                    ((ViewPagerAdapter) viewPager.getAdapter()).setFragmentList(fragment.getTabFragments());
                    viewPager.getAdapter().notifyDataSetChanged();
                    Log.d(TAG, "Running fragment's onLoad()");
                    fragment.onFragmentLoad();
                    fragmentContainer.setVisibility(View.GONE);
                }
                else {
                    Log.d(TAG, "Displaying untabbed fragment");
                    loadingLayout.setVisibility(View.GONE);
                    fragmentContainer.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.GONE);
                    ((ViewPagerAdapter) viewPager.getAdapter()).clear();
                    viewPager.getAdapter().notifyDataSetChanged();
                }
            }
            else {
                if (fragment.getNetworkError().networkResponse != null) {
                    Log.d(TAG, "Fragment has network error, " + Integer.toString(fragment.getNetworkError().networkResponse.statusCode));
                    if (fragment.getNetworkError().networkResponse.statusCode == 403) {
                        Log.d(TAG, "Automatically refreshing session");
                        loadingLayout.setVisibility(View.GONE);
                        if (fragment.hasTabs()) {
                            List<Fragment> emptyFragments = new ArrayList<>();
                            for (int i = 0; i < fragment.getTabNames().size(); i++) {
                                emptyFragments.add(new EmptyFragment());
                            }
                            ((ViewPagerAdapter) viewPager.getAdapter()).setFragmentList(emptyFragments);
                            viewPager.getAdapter().notifyDataSetChanged();
                        }
                        reauthenticate();
                    } else {
                        Log.d(TAG, "Showing generic retry page");
                        showNetworkError();
                    }
                }
                else {
                    Log.d(TAG, "Null network response");
                    Log.d(TAG, "Showing generic retry page");
                    showNetworkError();
                }
            }
        }
        inOnLoad = false;
    }

    private void setupViewPager(ViewPager viewPager, List<String> tabNames, List<Fragment> tabFragments) {
        Log.d(TAG, "Updating viewpager");
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.clear();
        for (int i = 0; i < tabNames.size(); i++) {
            if (tabFragments != null) {
                adapter.addFragment(tabFragments.get(i), tabNames.get(i));
            }
            else {
                adapter.addFragment(new LoadingFragment(), tabNames.get(i));
            }
        }

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
    }

    @Override
    public void onBackPressed() {

        if (!(currentFragment instanceof NetworkTabAwareFragment)
                || !((NetworkTabAwareFragment) currentFragment).hasTabs()) {
            super.onBackPressed();
            return;
        }

        FragmentManager fm = getSupportFragmentManager();

        // We retrieve the fragment container showed right now
        // The viewpager assigns tags to fragment automatically like this
        // mPager is our ViewPager instance
        Fragment fragment = ((ViewPagerAdapter) viewPager.getAdapter()).getItem(viewPager.getCurrentItem());

        // And thanks to the fragment container, we retrieve its child fragment manager
        // holding our fragment in the back stack
        FragmentManager childFragmentManager = fragment.getChildFragmentManager();

        // And here we go, if the back stack is empty, we let the back button doing its job
        // Otherwise, we show the last entry in the back stack (our FragmentToShow)
        if(childFragmentManager.getBackStackEntryCount() <= 1){
            super.onBackPressed();
        } else {
            Log.d(TAG, "Popping backstack of child fragment manager");
            childFragmentManager.popBackStack();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            refresh();
            return true;
        }
        else if (id == R.id.action_logout) {
            logout();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public void refresh() {
        Log.i(TAG, "Refreshing fragment");
        if (currentFragment instanceof NetworkTabAwareFragment) {
            final NetworkTabAwareFragment nFragment = (NetworkTabAwareFragment) currentFragment;
            nFragment.refresh();
            if (!nFragment.isCurrentFragmentNested()) {
                showLoading();
                final Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(!nFragment.isRequestFinished() && !threadExit) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (!threadExit) {
                            inOnLoad = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onFragmentLoad(nFragment);
                                }
                            });
                        }
                    }
                });
                threadExit = false;
                thread.start();
            }
        }
    }

    public void onClickRetry(View v) {
        networkErrorLayout.setVisibility(View.GONE);
        refresh();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            if (!(currentFragment instanceof PortalFragment)) {
                currentFragment = new PortalFragment();
                switchFragment(currentFragment);
            }
        } else if (id == R.id.nav_schedule) {
            if (!(currentFragment instanceof ScheduleFragment)) {
                currentFragment = new ScheduleFragment();
                switchFragment(currentFragment);
            }
        } else if (id == R.id.nav_calendar) {
            if (!(currentFragment instanceof CalendarFragment)) {
                currentFragment = new CalendarFragment();
                switchFragment(currentFragment);
            }
        } else if (id == R.id.nav_demographic) {
            if (!(currentFragment instanceof DemographicFragment)) {
                currentFragment = new DemographicFragment();
                switchFragment(currentFragment);
            }
        } else if (id == R.id.nav_address) {
            if (!(currentFragment instanceof AddressFragment)) {
                currentFragment = new AddressFragment();
                switchFragment(currentFragment);
            }
        } else if (id == R.id.nav_referrals) {
            if (!(currentFragment instanceof ReferralsFragment)) {
                currentFragment = new ReferralsFragment();
                switchFragment(currentFragment);
            }
        } else if (id == R.id.nav_absences) {
            if (!(currentFragment instanceof AbsencesFragment)) {
                currentFragment = new AbsencesFragment();
                switchFragment(currentFragment);
            }
        } else if (id == R.id.nav_final_grades) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_about) {
            if (!(currentFragment instanceof AboutFragment)) {
                currentFragment = new AboutFragment();
                switchFragment(currentFragment);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isSessionExpired() {
        return api.getSessionTimeout() <= System.currentTimeMillis();
    }

    public void reauthenticate() {
        Log.i(TAG, "Reauthenticating user");
        final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this,
                getString(R.string.timeout_progress_dialog_title),
                getString(R.string.timeout_progress_dialog_message),
                true);

        api.login(new Response.Listener<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                if (response) {
                    Log.d(TAG, "Login successful");
                    progressDialog.dismiss();
                    authenticating = false;
                    refresh();
                } else {
                    progressDialog.dismiss();
                    showAuthenticateRetryDialog(-1);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                if (error.networkResponse != null) {
                    showAuthenticateRetryDialog(error.networkResponse.statusCode);
                }
                else {
                    showAuthenticateRetryDialog(-1);
                }
            }
        });
    }

    private void showAuthenticateRetryDialog(int status) {
        Log.i(TAG, "Reauth failed, status " + Integer.toString(status));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (status == 504) {
            builder.setMessage(getString(R.string.retry_dialog_message_timeout));
        }
        else {
            if (status == -1) {
                builder.setMessage(getString(R.string.retry_dialog_message_timeout));
            }
            else {
                builder.setMessage(String.format(getString(R.string.retry_dialog_message_general), status));
            }
        }
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.retry_dialog_button), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                reauthenticate();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void logout() {
        Log.i(TAG, "Logging out user");
        final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this,
                null, getString(R.string.logout_progress_dialog_message), true);

        api.logout(new Response.Listener<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                Log.d(TAG, "Logout successful");
                progressDialog.dismiss();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                if (error.networkResponse != null) {
                    showLogoutRetryDialog(error.networkResponse.statusCode);
                }
                else {
                    showLogoutRetryDialog(-1);
                }
            }
        });

    }

    private void showLogoutRetryDialog(int status) {
        Log.i(TAG, "Logout failed, status " + Integer.toString(status));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (status == 504) {
            builder.setMessage(getString(R.string.retry_dialog_message_timeout));
        }
        else {
            if (status == -1) {
                builder.setMessage(getString(R.string.retry_dialog_message_timeout));
            }
            else {
                builder.setMessage(String.format(getString(R.string.retry_dialog_message_general), status));
            }
        }
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.retry_dialog_button), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                logout();
            }
        });
        builder.setNegativeButton(getString(R.string.retry_dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showNetworkError() {
        Log.i(TAG, "Switching to network error view");
        if (currentFragment instanceof NetworkTabAwareFragment) {
            NetworkTabAwareFragment nFragment = (NetworkTabAwareFragment) currentFragment;
            if (nFragment.hasTabs()) {
                List<Fragment> errorFrags = new ArrayList<>();
                for (int i = 0; i < nFragment.getTabNames().size(); i++) {
                    errorFrags.add(new NetworkErrorFragment());
                }
                ((ViewPagerAdapter) viewPager.getAdapter()).setFragmentList(errorFrags);
                viewPager.getAdapter().notifyDataSetChanged();
            }
            else {
                loadingLayout.setVisibility(View.GONE);
                fragmentContainer.setVisibility(View.GONE);
                networkErrorLayout.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.GONE);
                ((ViewPagerAdapter) viewPager.getAdapter()).clear();
                viewPager.getAdapter().notifyDataSetChanged();
            }
        }
    }

    private void showLoading() {
        Log.i(TAG, "Switching to loading view");
        if (currentFragment instanceof NetworkTabAwareFragment) {
            NetworkTabAwareFragment nFragment = (NetworkTabAwareFragment) currentFragment;
            if (nFragment.hasTabs()) {
                List<Fragment> loadingFrags = new ArrayList<>();
                for (int i = 0; i < nFragment.getTabNames().size(); i++) {
                    loadingFrags.add(new LoadingFragment());
                }
                ((ViewPagerAdapter) viewPager.getAdapter()).setFragmentList(loadingFrags);
                viewPager.getAdapter().notifyDataSetChanged();
            }
            else {
                fragmentContainer.setVisibility(View.GONE);
                networkErrorLayout.setVisibility(View.GONE);
                loadingLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.isVisible = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.isVisible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.isVisible = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.isVisible = false;
    }

}
