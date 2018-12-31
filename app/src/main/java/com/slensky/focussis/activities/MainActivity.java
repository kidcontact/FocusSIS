package com.slensky.focussis.activities;

import android.Manifest;
import android.accounts.Account;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.BackOff;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.slensky.focussis.FocusApplication;
import com.slensky.focussis.R;
import com.slensky.focussis.data.CalendarEvent;
import com.slensky.focussis.data.CalendarEventDetails;
import com.slensky.focussis.data.CourseAssignment;
import com.slensky.focussis.data.GoogleCalendarEvent;
import com.slensky.focussis.data.PortalAssignment;
import com.slensky.focussis.data.PortalEvent;
import com.slensky.focussis.fragments.AboutFragment;
import com.slensky.focussis.fragments.AbsencesFragment;
import com.slensky.focussis.fragments.AddressFragment;
import com.slensky.focussis.fragments.CalendarFragment;
import com.slensky.focussis.fragments.DemographicFragment;
import com.slensky.focussis.fragments.FinalGradesFragment;
import com.slensky.focussis.fragments.NetworkErrorFragment;
import com.slensky.focussis.fragments.NetworkFragment;
import com.slensky.focussis.fragments.PageFragment;
import com.slensky.focussis.fragments.PortalFragment;
import com.slensky.focussis.fragments.ReferralsFragment;
import com.slensky.focussis.fragments.ScheduleFragment;
import com.slensky.focussis.fragments.SettingsFragment;
import com.slensky.focussis.network.FocusApi;
import com.slensky.focussis.network.FocusApiSingleton;
import com.slensky.focussis.network.FocusDebugApi;

import com.slensky.focussis.fragments.EmptyFragment;
import com.slensky.focussis.fragments.LoadingFragment;
import com.slensky.focussis.fragments.NetworkTabAwareFragment;
import com.slensky.focussis.util.SchoolSingleton;
import com.slensky.focussis.util.Syncable;
import com.slensky.focussis.views.adapters.ViewPagerAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private static final String USERNAME_BUNDLE_KEY = "username";
    private static final String PASSWORD_BUNDLE_KEY = "password";
    private static final String SESSION_TIMEOUT_BUNDLE_KEY = "session_timeout";
    private static final String FRAGMENT_ID_BUNDLE_KEY = "fragment_id";

    // tag set on reauthenticate request to prevent that request from getting cancelled by switchFragment()
    private static final String REAUTH_REQUEST_TAG = "reauth";

    private NavigationView navigationView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FrameLayout fragmentContainer;
    private PageFragment currentFragment;
    private int currentFragmentId;
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

    // save old status bar color when action bar is created, restore when action bar is finished
    int statusBarColor;

    // for exporting to calendar with google play services
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String[] SCOPES = { CalendarScopes.CALENDAR};
    private GoogleSignInOptions googleSignInOptions;
    private GoogleSignInClient googleSignInClient;
    GoogleAccountCredential credential;
    MaterialDialog calendarExportProgress;
    private Collection<GoogleCalendarEvent> eventsToExport;
    private boolean updateEvents;
    Runnable onExportTaskFinishedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(com.slensky.focussis.R.style.AppTheme_Light);
        if (savedInstanceState != null) {
            Log.d(TAG, "Restoring saved instance state");
            username = savedInstanceState.getString(USERNAME_BUNDLE_KEY);
            password = savedInstanceState.getString(PASSWORD_BUNDLE_KEY);
            if (FocusApplication.USE_DEBUG_API) {
                api = new FocusDebugApi(username, password, getApplicationContext());
            }
            else {
                api = new FocusApi(username, password, getApplicationContext());
            }
            FocusApiSingleton.setApi(api);
            super.onCreate(savedInstanceState);
            setContentView(com.slensky.focussis.R.layout.activity_main);
            if (api.hasSession()) {
                api.setSessionTimeout(savedInstanceState.getLong(SESSION_TIMEOUT_BUNDLE_KEY));
                if (!api.isSessionExpired()) {
                    api.setLoggedIn(true);
                }
                else {
                    Log.d(TAG, "Session timed out, reauthenticating from saved instance state");
                    authenticating = true;
                    reauthenticate();
                }
            }
            else {
                Log.d(TAG, "API has no session, reauthenticating from saved instance state");
                authenticating = true;
                reauthenticate();
            }
        }
        else {
            super.onCreate(savedInstanceState);
            setContentView(com.slensky.focussis.R.layout.activity_main);
            api = FocusApiSingleton.getApi();
            Log.d(TAG, "Unpacking intent");
            Intent intent = getIntent();
            username = intent.getStringExtra(getString(com.slensky.focussis.R.string.EXTRA_USERNAME));
            password = intent.getStringExtra(getString(com.slensky.focussis.R.string.EXTRA_PASSWORD));
        }

        Log.d(TAG, "Toolbar init");
        Toolbar toolbar = (Toolbar) findViewById(com.slensky.focussis.R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(com.slensky.focussis.R.id.main_drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,  mDrawerLayout, toolbar,
                com.slensky.focussis.R.string.navigation_drawer_open, com.slensky.focussis.R.string.navigation_drawer_close
        );

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();

        isVisible = true;

        navigationView = (NavigationView) findViewById(com.slensky.focussis.R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        TextView name = (TextView) header.findViewById(com.slensky.focussis.R.id.nav_text_name);
        TextView email = (TextView) header.findViewById(com.slensky.focussis.R.id.nav_text_email);

        String[] n = username.split("\\.");
        if (n.length > 1) {
            String first = n[0].substring(0, 1).toUpperCase() + n[0].substring(1);
            String last = n[1].substring(0, 1).toUpperCase() + n[1].substring(1);
            name.setText(first + " " + last);
        }
        else {
            name.setText(n[0]);
        }

        String domain = SchoolSingleton.getInstance().getSchool().getDomainName();
        if (domain != null) {
            email.setText(username + "@" + domain);
        }

        Log.d(TAG, "Configure viewpager + tab layout");

        viewPager = (ViewPager) findViewById(com.slensky.focussis.R.id.viewpager);
        viewPager.setSaveEnabled(false);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (currentFragment instanceof NetworkTabAwareFragment) {
                    ((NetworkTabAwareFragment) currentFragment).setSelectedTab(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        fragmentContainer = (FrameLayout) findViewById(com.slensky.focussis.R.id.fragment_container);

        loadingLayout = (LinearLayout) findViewById(com.slensky.focussis.R.id.layout_loading);
        networkErrorLayout = (LinearLayout) findViewById(com.slensky.focussis.R.id.layout_network_failure);

        tabLayout = (TabLayout) findViewById(com.slensky.focussis.R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
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
                    if (api.isSessionExpired() && !authenticating && isVisible) {
                        authenticating = true;
                        Log.d(TAG, "Session timed out, reauthenticating from thread");
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

        // Initialize progress dialog for event export, credentials, and service object.
        calendarExportProgress = new MaterialDialog.Builder(this)
                .content(R.string.export_progress_dialog)
                .progress(false, 0, true)
                .negativeText(R.string.cancel)
                .canceledOnTouchOutside(false)
                .build();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff() {
                    int tries = 0;
                    @Override
                    public long nextBackOffMillis() throws IOException {
                        tries += 1;
                        if (tries < 3) {
                            return super.nextBackOffMillis();
                        }
                        else {
                            return BackOff.STOP;
                        }
                    }
                });

        if (savedInstanceState == null) {
            currentFragment = new PortalFragment();
            currentFragmentId = com.slensky.focussis.R.id.nav_home;
            switchFragment(currentFragment);
        }
        else {
            // switch back to correct fragment
            currentFragmentId = savedInstanceState.getInt(FRAGMENT_ID_BUNDLE_KEY);
            navigationView.getMenu().findItem(currentFragmentId).setChecked(true);
            switchFragmentFromNav(currentFragmentId);
        }
    }

    private void switchFragment(final PageFragment fragment) {
        Log.d(TAG, "Switching to fragment " + fragment.getClass().getCanonicalName());
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

        api.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return !REAUTH_REQUEST_TAG.equals(request.getTag());
            }
        });
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(com.slensky.focussis.R.id.fragment_container, (Fragment) fragment);
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
                if (viewPager.getAdapter() != null) {
                    ((ViewPagerAdapter) viewPager.getAdapter()).clear();
                    viewPager.getAdapter().notifyDataSetChanged();
                }
            }
        }
        else {
            Log.d(TAG, "Configuring static non-tab page");
            tabLayout.setVisibility(View.GONE);
            loadingLayout.setVisibility(View.GONE);
            fragmentContainer.setVisibility(View.VISIBLE);
            if (viewPager.getAdapter() != null) {
                ((ViewPagerAdapter) viewPager.getAdapter()).clear();
                viewPager.getAdapter().notifyDataSetChanged();
            }
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
            Log.d(TAG, "Fragment " + fragment.getClass().getCanonicalName() + " loaded");
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
                    if (viewPager.getAdapter() != null) {
                        ((ViewPagerAdapter) viewPager.getAdapter()).clear();
                        viewPager.getAdapter().notifyDataSetChanged();
                    }
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem refreshItem = findMenuItem(menu, getString(R.string.toolbar_menu_refresh));
        MenuItem syncItem = findMenuItem(menu, getString(R.string.toolbar_menu_sync_to_calendar));
        MenuItem resetCourseItem = findMenuItem(menu, getString(R.string.toolbar_menu_delete_saved_assignments));
        if (currentFragment instanceof NetworkFragment) {
            refreshItem.setVisible(true);
        }
        else {
            refreshItem.setVisible(false);
        }

        if (currentFragment instanceof NetworkFragment
                && currentFragment instanceof Syncable
                && ((NetworkFragment) currentFragment).isRequestFinished()
                && !((NetworkFragment) currentFragment).hasNetworkError()) {
            syncItem.setVisible(true);
        }
        else {
            syncItem.setVisible(false);
        }

        if (currentFragment instanceof PortalFragment && ((PortalFragment) currentFragment).isCurrentFragmentNested()) {
            resetCourseItem.setVisible(true);
        }
        else {
            resetCourseItem.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    private MenuItem findMenuItem(Menu menu, CharSequence title) {
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getTitle().equals(title)) {
                return menu.getItem(i);
            }
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.slensky.focussis.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == com.slensky.focussis.R.id.action_refresh) {
            refresh();
            return true;
        }
        else if (id == com.slensky.focussis.R.id.action_logout) {
            logout();
            return true;
        }
        else if (id == R.id.action_sync_to_calendar) {
            if (currentFragment instanceof Syncable) {
                ((Syncable) currentFragment).sync();
            }
            return true;
        }
        else if (id == R.id.action_delete_saved_assignments) {
            if (currentFragment instanceof PortalFragment && ((PortalFragment) currentFragment).getCourseFragment() != null) {
                ((PortalFragment) currentFragment).getCourseFragment().resetCourse();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refresh() {
        Log.d(TAG, "Refreshing fragment");
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
        currentFragmentId = id;
        switchFragmentFromNav(id);
        DrawerLayout drawer = (DrawerLayout) findViewById(com.slensky.focussis.R.id.main_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void switchFragmentFromNav(int id) {
        if (id == com.slensky.focussis.R.id.nav_home) {
            if (!(currentFragment instanceof PortalFragment)) {
                currentFragment = new PortalFragment();
                switchFragment(currentFragment);
            }
        } else if (id == com.slensky.focussis.R.id.nav_schedule) {
            if (!(currentFragment instanceof ScheduleFragment)) {
                currentFragment = new ScheduleFragment();
                switchFragment(currentFragment);
            }
        } else if (id == com.slensky.focussis.R.id.nav_calendar) {
            if (!(currentFragment instanceof CalendarFragment)) {
                currentFragment = new CalendarFragment();
                switchFragment(currentFragment);
            }
        } else if (id == com.slensky.focussis.R.id.nav_demographic) {
            if (!(currentFragment instanceof DemographicFragment)) {
                currentFragment = new DemographicFragment();
                switchFragment(currentFragment);
            }
        } else if (id == com.slensky.focussis.R.id.nav_address) {
            if (!(currentFragment instanceof AddressFragment)) {
                currentFragment = new AddressFragment();
                switchFragment(currentFragment);
            }
        } else if (id == com.slensky.focussis.R.id.nav_referrals) {
            if (!(currentFragment instanceof ReferralsFragment)) {
                currentFragment = new ReferralsFragment();
                switchFragment(currentFragment);
            }
        } else if (id == com.slensky.focussis.R.id.nav_absences) {
            if (!(currentFragment instanceof AbsencesFragment)) {
                currentFragment = new AbsencesFragment();
                switchFragment(currentFragment);
            }
        } else if (id == com.slensky.focussis.R.id.nav_final_grades) {
            if (!(currentFragment instanceof FinalGradesFragment)) {
                currentFragment = new FinalGradesFragment();
                switchFragment(currentFragment);
            }
        } else if (id == com.slensky.focussis.R.id.nav_settings) {
            if (!(currentFragment instanceof SettingsFragment)) {
                currentFragment = new SettingsFragment();
                switchFragment(currentFragment);
            }
        } else if (id == com.slensky.focussis.R.id.nav_about) {
            if (!(currentFragment instanceof AboutFragment)) {
                currentFragment = new AboutFragment();
                switchFragment(currentFragment);
            }
        }
    }

    public void reauthenticate() {
        Log.d(TAG, "Reauthenticating user");
        final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this,
                getString(com.slensky.focussis.R.string.timeout_progress_dialog_title),
                getString(com.slensky.focussis.R.string.timeout_progress_dialog_message),
                true);

        api.login(new FocusApi.Listener<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                if (response) {
                    Log.d(TAG, "Login successful");
                    progressDialog.dismiss();
                    authenticating = false;
                    refresh();
                } else {
                    Log.d(TAG, "Login unsuccessful (response is false)");
                    progressDialog.dismiss();
                    showAuthenticateRetryDialog(-1);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Login unsuccessful (error response)");
                progressDialog.dismiss();
                if (error.networkResponse != null) {
                    showAuthenticateRetryDialog(error.networkResponse.statusCode);
                }
                else {
                    showAuthenticateRetryDialog(-1);
                }
            }
        }).setTag(REAUTH_REQUEST_TAG);
    }

    private void showAuthenticateRetryDialog(int status) {
        Log.d(TAG, "Reauth failed, status " + Integer.toString(status));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (status == 504) {
            builder.setMessage(getString(com.slensky.focussis.R.string.retry_dialog_message_timeout));
        }
        else {
            if (status == -1) {
                builder.setMessage(getString(com.slensky.focussis.R.string.retry_dialog_message_timeout));
            }
            else {
                builder.setMessage(String.format(getString(com.slensky.focussis.R.string.retry_dialog_message_general), status));
            }
        }
        builder.setCancelable(false);
        builder.setPositiveButton(getString(com.slensky.focussis.R.string.retry_dialog_button), new DialogInterface.OnClickListener()
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
        Log.d(TAG, "Logging out user");
        final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this,
                null, getString(com.slensky.focussis.R.string.logout_progress_dialog_message), true);

        api.logout(new FocusApi.Listener<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                Log.d(TAG, "Logout successful");
                progressDialog.dismiss();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(getString(com.slensky.focussis.R.string.EXTRA_DISABLE_AUTO_SIGN_IN), true);
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
        Log.d(TAG, "Logout failed, status " + Integer.toString(status));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (status == 504) {
            builder.setMessage(getString(com.slensky.focussis.R.string.retry_dialog_message_timeout));
        }
        else {
            if (status == -1) {
                builder.setMessage(getString(com.slensky.focussis.R.string.retry_dialog_message_timeout));
            }
            else {
                builder.setMessage(String.format(getString(com.slensky.focussis.R.string.retry_dialog_message_general), status));
            }
        }
        builder.setCancelable(false);
        builder.setPositiveButton(getString(com.slensky.focussis.R.string.retry_dialog_button), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                logout();
            }
        });
        builder.setNegativeButton(getString(com.slensky.focussis.R.string.retry_dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showNetworkError() {
        Log.d(TAG, "Switching to network error view");
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
                if (viewPager != null && viewPager.getAdapter() != null) {
                    ((ViewPagerAdapter) viewPager.getAdapter()).clear();
                    viewPager.getAdapter().notifyDataSetChanged();
                }

            }
        }
    }

    private void showLoading() {
        Log.d(TAG, "Switching to loading view");
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "Saving instance state");
        savedInstanceState.putString(USERNAME_BUNDLE_KEY, username);
        savedInstanceState.putString(PASSWORD_BUNDLE_KEY, password);
        savedInstanceState.putLong(SESSION_TIMEOUT_BUNDLE_KEY, api.getSessionTimeout());
        savedInstanceState.putInt(FRAGMENT_ID_BUNDLE_KEY, currentFragmentId);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onSupportActionModeStarted(@NonNull android.support.v7.view.ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        // save old status bar color
        statusBarColor = getWindow().getStatusBarColor();
        //set gray color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.actionModeDark));
    }

    @Override
    public void onSupportActionModeFinished(@NonNull android.support.v7.view.ActionMode mode) {
        super.onSupportActionModeFinished(mode);
        getWindow().setStatusBarColor(statusBarColor);
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

    public String getUsername() {
        return username;
    }

    public PageFragment getCurrentFragment() {
        return currentFragment;
    }

    // Google play services

    public GoogleSignInClient getGoogleSignInClient() {
        return googleSignInClient;
    }

    public void exportEventsToCalendar(final Collection<GoogleCalendarEvent> events, final boolean updateEvents, final Runnable onExportTaskFinishedListener) {
        PermissionListener dialogOnDeniedListener = DialogOnDeniedPermissionListener.Builder
                .withContext(this)
                .withMessage(R.string.contacts_permission_request_on_denied_message)
                .withButtonText(R.string.ok)
                .build();
        PermissionListener basePermissionListener = new BasePermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                if (account != null) {
                    credential.setSelectedAccountName(account.getEmail());
                }
                eventsToExport = events;
                MainActivity.this.updateEvents = updateEvents;
                MainActivity.this.onExportTaskFinishedListener = onExportTaskFinishedListener;
                getResultsFromApi();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                if (onExportTaskFinishedListener != null) {
                    onExportTaskFinishedListener.run();
                }
            }
        };

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.GET_ACCOUNTS)
                .withListener(new CompositePermissionListener(basePermissionListener, dialogOnDeniedListener))
                .check();
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            new MakeCalendarRequestTask(credential, eventsToExport, updateEvents).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user.
     */
    public void chooseAccount() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    new MaterialDialog.Builder(this)
                            .content(R.string.needs_play_services)
                            .positiveText(R.string.ok)
                            .show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    credential.setSelectedAccount(new Account(account.getEmail(), getPackageName()));
                    // if the settings fragment is selected, update the description of the preference
                    if (currentFragment instanceof SettingsFragment) {
                        ((SettingsFragment) currentFragment).updateAccountPreference();
                    }
                    else {
                        getResultsFromApi();
                    }
                    Log.i(TAG, account.getEmail());
                } catch (ApiException e) {
                    if (e.getStatusCode() == CommonStatusCodes.NETWORK_ERROR || e.getStatusCode() == CommonStatusCodes.TIMEOUT) {
                        Toast.makeText(this, R.string.network_error_timeout, Toast.LENGTH_LONG).show();
                    }
                    else {
                        Log.e(TAG, "Unexpected APIException while choosing account!");
                        e.printStackTrace();
                        Toast.makeText(this, R.string.network_error_unknown, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeCalendarRequestTask extends AsyncTask<Void, Void, Void> {
        private final static String TAG = "MakeCalendarRequestTask";
        private com.google.api.services.calendar.Calendar mService = null;
        private Collection<GoogleCalendarEvent> events;
        private Exception mLastError = null;
        private boolean updateEvents;
        private int eventCount = 0;
        private int assignmentCount = 0;
        private int eventSkippedCount = 0;
        private int assignmentSkippedCount = 0;

        private boolean cancelledByUser = false;

        private String defaultCalendarSummary = getString(R.string.google_calendar_summary, SchoolSingleton.getInstance().getSchool().getShortName());

        MakeCalendarRequestTask(GoogleAccountCredential credential, @NonNull Collection<GoogleCalendarEvent> events, boolean updateEvents) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(getString(R.string.play_servces_app_name))
                    .build();
            this.events = events;
            this.updateEvents = updateEvents;
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (events.size() > 0) {
                    getDataFromApi();
                }
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return null;
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private Void getDataFromApi() throws IOException {
            String calendarId = getSharedPreferences(getString(R.string.google_calendar_prefs), MODE_PRIVATE)
                    .getString(getString(R.string.google_calendar_prefs_id_for_account, credential.getSelectedAccountName()), null);

            if (calendarId != null) {
                Log.d(MakeCalendarRequestTask.TAG, "Loaded saved calendar ID");
                Log.d(MakeCalendarRequestTask.TAG, "Checking to make sure calendar still exists");
                try {
                    mService.calendarList().get(calendarId).execute();
                    Log.d(MakeCalendarRequestTask.TAG, "Calendar still exists");
                } catch (GoogleJsonResponseException e) {
                    Log.d(MakeCalendarRequestTask.TAG, "Calendar no longer exists");
                    calendarId = null;
                }
            }

            if (calendarId == null) {
                // attempt to reacquire id by looking for a calendar with the default name
                String pageToken = null;
                outer: do {
                    CalendarList calendarList = mService.calendarList().list().setMinAccessRole("owner").setPageToken(pageToken).execute();
                    List<CalendarListEntry> items = calendarList.getItems();

                    for (CalendarListEntry calendarListEntry : items) {
//                        System.out.println(calendarListEntry.getSummary());
//                        System.out.println(calendarListEntry.getId());
                        if (calendarListEntry.getSummary().equals(defaultCalendarSummary)) {
                            Log.d(MakeCalendarRequestTask.TAG, "Found existing calendar by name");
                            calendarId = calendarListEntry.getId();
                            saveGoogleCalendarId(calendarId);
                            break outer;
                        }
                    }
                    pageToken = calendarList.getNextPageToken();
                } while (pageToken != null);
            }

            if (calendarId == null) {
                Log.d(MakeCalendarRequestTask.TAG, "Could not find existing calendar, creating calendar " + defaultCalendarSummary);
                Calendar newCalendar = mService.calendars().insert(new Calendar().setSummary(defaultCalendarSummary)).execute();
                calendarId = newCalendar.getId();
                saveGoogleCalendarId(calendarId);
            }

            // first get already existing events in the correct time range
            // to ensure no duplicates are added
            org.joda.time.DateTime timeMin = null;
            org.joda.time.DateTime timeMax = null;
            for (GoogleCalendarEvent e : events) {
                if (timeMin == null) {
                    timeMin = e.getStart();
                }
                else if (e.getStart().isBefore(timeMin)) {
                    timeMin = e.getStart();
                }

                if (timeMax == null) {
                    timeMax = e.getEnd();
                }
                else if (e.getEnd().isAfter(timeMax)) {
                    timeMax = e.getEnd();
                }
            }

            List<Event> existingEvents = mService.events().list(calendarId)
                    .setTimeMin(new DateTime(timeMin.toDate()))
                    .setTimeMax(new DateTime(timeMax.toDate()))
                    .setSingleEvents(true)
                    .execute()
                    .getItems();

            // only add non-duplicate events
            List<Event> eventsToInsert = new ArrayList<>();
            List<Event> eventsToUpdate = new ArrayList<>(); // old event id -> updated event

            outer:
            for (GoogleCalendarEvent e : events) {
                boolean isEvent = isEvent(e);
                boolean isAssignment = isAssignment(e) && !isEvent; // can't be both an assignment and an event
                Event event = e.toGoogleCalendarEvent();
                for (Event existingEvent : existingEvents) {
                    if (event.getSummary().equals(existingEvent.getSummary())
                            && (event.getDescription() != null ? event.getDescription().equals(existingEvent.getDescription()) : existingEvent.getDescription() == null)
                            && (event.getLocation() != null ? event.getLocation().equals(existingEvent.getLocation()) : existingEvent.getLocation() == null)
                            && event.getStart().equals(existingEvent.getStart())
                            && event.getEnd().equals(existingEvent.getEnd())) {
                        Log.d(MakeCalendarRequestTask.TAG, "Skipping duplicate event " + event.getSummary());
                        calendarExportProgress.incrementProgress(1);
                        if (isEvent) {
                            eventSkippedCount += 1;
                        }
                        else if (isAssignment) {
                            assignmentSkippedCount += 1;
                        }
                        continue outer;
                    }
                    else if (event.getSummary().equals(existingEvent.getSummary())
                            && (event.getLocation() != null ? event.getLocation().equals(existingEvent.getLocation()) : existingEvent.getLocation() == null)
                            && event.getStart().equals(existingEvent.getStart())
                            && event.getEnd().equals(existingEvent.getEnd())) {
                        if (updateEvents) {
                            // only description has changed, just update the event
                            eventsToUpdate.add(existingEvent.setDescription(event.getDescription()));
                        }
                        else {
                            Log.d(TAG, "Skipping duplicate event instead of updating " + event.getSummary());
                            calendarExportProgress.incrementProgress(1);
                            if (isEvent) {
                                eventSkippedCount += 1;
                            }
                            else if (isAssignment) {
                                assignmentSkippedCount += 1;
                            }
                        }
                        continue outer;
                    }
                }
                eventsToInsert.add(event);
            }

            for (Event e : eventsToInsert) {
                Log.d(MakeCalendarRequestTask.TAG, "Inserting event " + e.getSummary());
                calendarExportProgress.incrementProgress(1);
                e.setReminders(new Event.Reminders().setUseDefault(false));
                mService.events().insert(calendarId, e).execute();
            }
            if (updateEvents) {
                for (Event e : eventsToUpdate) {
                    Log.d(MakeCalendarRequestTask.TAG, "Updating event " + e.getSummary());
                    calendarExportProgress.incrementProgress(1);
                    mService.events().update(calendarId, e.getId(), e).execute();
                }
            }
            return null;
        }


        @Override
        protected void onPreExecute() {
            for (GoogleCalendarEvent e : events) {
                boolean isEvent = isEvent(e);
                boolean isAssignment = isAssignment(e) && !isEvent; // can't be both an assignment and an event
                if (isEvent) {
                    eventCount += 1;
                }
                else if (isAssignment) {
                    assignmentCount += 1;
                }
            }
            calendarExportProgress.setProgress(0);
            calendarExportProgress.setMaxProgress(eventCount + assignmentCount);
            calendarExportProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    cancelledByUser = true;
                    cancel(true);
                }
            });
            calendarExportProgress.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            calendarExportProgress.dismiss();

            String exportSummaryContent = "";
            boolean none = false;
            if (eventCount - eventSkippedCount == 0 && assignmentCount - assignmentSkippedCount == 0) {
                none = true;
                if (eventCount == 0) {
                    exportSummaryContent = getString(R.string.export_summary_dialog_summary_nothing_exported_assignments_only);
                }
                else if (assignmentCount == 0) {
                    exportSummaryContent = getString(R.string.export_summary_dialog_summary_nothing_exported_events_only);
                }
                else {
                    exportSummaryContent = getString(R.string.export_summary_dialog_summary_nothing_exported);
                }
            }
            else {
                if (eventCount > 0) {
                    exportSummaryContent += "<b>" + getString(R.string.export_summary_dialog_summary_events_exported) + "</b> "
                            + getString(R.string.export_summary_dialog_summary_number_exported, eventCount, eventCount - eventSkippedCount);
                }
                if (assignmentCount > 0) {
                    if (!exportSummaryContent.isEmpty()) {
                        exportSummaryContent += "<br>";
                    }
                    exportSummaryContent += "<b>" + getString(R.string.export_summary_dialog_summary_assignments_exported) + "</b> " +
                            getString(R.string.export_summary_dialog_summary_number_exported, assignmentCount, assignmentCount - assignmentSkippedCount);
                }
            }

            MaterialDialog.Builder builder = new MaterialDialog.Builder(MainActivity.this)
                    .title(R.string.export_summary_dialog_title)
                    .content(Html.fromHtml(exportSummaryContent))
                    .positiveText(R.string.ok);
            if (!none) {
                builder.contentColorRes(R.color.textPrimary);
            }

            if (onExportTaskFinishedListener != null) {
                onExportTaskFinishedListener.run();
            }
            builder.show();
        }

        @Override
        protected void onCancelled() {
            calendarExportProgress.dismiss();
            if (mLastError != null && !cancelledByUser) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else if (mLastError instanceof IOException) {
                    mLastError.printStackTrace();
                    new MaterialDialog.Builder(MainActivity.this)
                            .content(getString(R.string.export_network_error))
                            .positiveText(R.string.ok)
                            .show();
                } else {
                    mLastError.printStackTrace();
                    new MaterialDialog.Builder(MainActivity.this)
                            .content(mLastError.getMessage())
                            .positiveText(R.string.ok)
                            .show();
                }
            }
        }

        private boolean isEvent(Object o) {
            return o instanceof PortalEvent
                    || (o instanceof CalendarEvent && ((CalendarEvent) o).getType().equals(CalendarEvent.EventType.OCCASION))
                    || (o instanceof CalendarEventDetails && ((CalendarEventDetails) o).getType().equals(CalendarEvent.EventType.OCCASION));
        }

        private boolean isAssignment(Object o) {
            return o instanceof PortalAssignment
                    || o instanceof CourseAssignment
                    || (o instanceof CalendarEvent && ((CalendarEvent) o).getType().equals(CalendarEvent.EventType.ASSIGNMENT))
                    || (o instanceof CalendarEventDetails && ((CalendarEventDetails) o).getType().equals(CalendarEvent.EventType.ASSIGNMENT));
        }
    }

    private void saveGoogleCalendarId(String id) {
        Log.d(MakeCalendarRequestTask.TAG, "Saving calendar id " + id);
        SharedPreferences.Editor googleCalendarPrefsEditor = getSharedPreferences(getString(R.string.google_calendar_prefs), MODE_PRIVATE).edit();
        googleCalendarPrefsEditor.putString(getString(R.string.google_calendar_prefs_id_for_account, credential.getSelectedAccountName()), id);
        googleCalendarPrefsEditor.apply();
    }

}
