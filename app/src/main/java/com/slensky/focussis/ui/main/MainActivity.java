package com.slensky.focussis.ui.main;

import android.Manifest;
import android.accounts.Account;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

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
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.slensky.focussis.R;
import com.slensky.focussis.data.focus.Absences;
import com.slensky.focussis.data.focus.Address;
import com.slensky.focussis.data.focus.CalendarEvent;
import com.slensky.focussis.data.focus.CalendarEventDetails;
import com.slensky.focussis.data.focus.CourseAssignment;
import com.slensky.focussis.data.GoogleCalendarEvent;
import com.slensky.focussis.data.focus.Demographic;
import com.slensky.focussis.data.focus.FinalGrades;
import com.slensky.focussis.data.focus.Portal;
import com.slensky.focussis.data.focus.PortalAssignment;
import com.slensky.focussis.data.focus.PortalEvent;
import com.slensky.focussis.data.focus.Referrals;
import com.slensky.focussis.data.focus.Schedule;
import com.slensky.focussis.data.prefs.PreferencesHelper;
import com.slensky.focussis.ui.base.BaseActivity;
import com.slensky.focussis.ui.contacts.ContactsFragment;
import com.slensky.focussis.ui.about.AboutFragment;
import com.slensky.focussis.ui.absences.AbsencesFragment;
import com.slensky.focussis.ui.calendar.CalendarFragment;
import com.slensky.focussis.ui.demographic.DemographicFragment;
import com.slensky.focussis.ui.finalgrades.FinalGradesFragment;
import com.slensky.focussis.ui.base.NetworkErrorFragment;
import com.slensky.focussis.ui.base.NetworkFragment;
import com.slensky.focussis.ui.base.PageFragment;
import com.slensky.focussis.ui.login.LoginActivity;
import com.slensky.focussis.ui.portal.PortalAssignmentsTabFragment;
import com.slensky.focussis.ui.portal.PortalCoursesTabFragment;
import com.slensky.focussis.ui.portal.PortalEventsTabFragment;
import com.slensky.focussis.ui.portal.PortalFragment;
import com.slensky.focussis.ui.referrals.ReferralsFragment;
import com.slensky.focussis.ui.schedule.ScheduleFragment;
import com.slensky.focussis.ui.settings.SettingsFragment;
import com.slensky.focussis.data.network.FocusApi;

import com.slensky.focussis.ui.base.EmptyFragment;
import com.slensky.focussis.ui.base.LoadingFragment;
import com.slensky.focussis.ui.base.NetworkTabAwareFragment;
import com.slensky.focussis.util.SchoolSingleton;
import com.slensky.focussis.util.Syncable;

import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

public class MainActivity extends BaseActivity
        implements MainContract.ViewActions, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";

    // tag set on reauthenticate request to prevent that request from getting cancelled by switchFragment()
    private static final String REAUTH_REQUEST_TAG = "reauth";

    private NavigationView navigationView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FrameLayout fragmentContainer;
    private PageFragment currentFragment;
    private LinearLayout loadingLayout;
    private LinearLayout networkErrorLayout;

    private boolean threadExit = false;
    private boolean inOnLoad = false;

    // reusable loading fragments
    private LoadingFragment[] loadingFragments = {new LoadingFragment(), new LoadingFragment(), new LoadingFragment()};

    // stored for keeping the session alive after it expires
    @State
    String username;
    @State
    String password;
    @State
    int selectedNavItemId = R.id.nav_home;

    @Inject
    FocusApi api;
    @Inject
    PreferencesHelper preferencesHelper;
    @Inject
    Gson gson;
    @Inject
    MainContract.UserActions<MainContract.ViewActions> presenter;

    private boolean isVisible;

    // used by session thread to avoid spamming reauthenticate() calls
    private boolean authenticating;

    // save old status bar color when action bar is created, restore when action bar is finished
    int statusBarColor;

    // for exporting to calendar with google play services
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    @Inject
    GoogleSignInClient googleSignInClient;
    @Inject
    GoogleAccountCredential credential;

    MaterialDialog calendarExportProgress;
    private Collection<GoogleCalendarEvent> eventsToExport;
    private boolean updateEvents;
    Runnable onExportTaskFinishedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(com.slensky.focussis.R.style.AppTheme_Light);
        super.onCreate(savedInstanceState);

        setContentView(com.slensky.focussis.R.layout.activity_main);

        if (savedInstanceState == null) {
            Log.d(TAG, "Unpacking intent");
            Intent intent = getIntent();
            username = intent.getStringExtra(getString(com.slensky.focussis.R.string.EXTRA_USERNAME));
            password = intent.getStringExtra(getString(com.slensky.focussis.R.string.EXTRA_PASSWORD));
        } else {
            Icepick.restoreInstanceState(this, savedInstanceState);
        }

        getActivityComponent().inject(this);

        ButterKnife.bind(this);

        setupView();

        presenter.onAttach(this);
    }

    @Override
    protected void setupView() {
        Log.d(TAG, "Toolbar init");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,  drawerLayout, toolbar,
                com.slensky.focussis.R.string.navigation_drawer_open, com.slensky.focussis.R.string.navigation_drawer_close
        );

        drawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();

        isVisible = true;

        navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(selectedNavItemId).setChecked(true);
        View header = navigationView.getHeaderView(0);
        TextView name = header.findViewById(R.id.nav_text_header);
        TextView email = header.findViewById(R.id.nav_text_subheader);

        Log.d(TAG, "Configure viewpager + tab layout");

        viewPager = (ViewPager) findViewById(com.slensky.focussis.R.id.viewpager);
        viewPager.setSaveEnabled(false);
        viewPager.setOffscreenPageLimit(2);
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

        // Initialize progress dialog for event export, credentials, and service object.
        calendarExportProgress = new MaterialDialog.Builder(this)
                .content(R.string.export_progress_dialog)
                .progress(false, 0, true)
                .negativeText(R.string.cancel)
                .canceledOnTouchOutside(false)
                .build();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(com.slensky.focussis.R.id.main_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if (item.getItemId() != selectedNavItemId) {
            selectedNavItemId = item.getItemId();
            presenter.onNavigationItemSelected(navIdToNavItem(selectedNavItemId));
        }

        return true;
    }

    private MainContract.NavigationItem navIdToNavItem(@IdRes int id) {
        switch (id) {
            case R.id.nav_home:
                return MainContract.NavigationItem.HOME;
            case R.id.nav_schedule:
                return MainContract.NavigationItem.SCHEDULE;
            case R.id.nav_calendar:
                return MainContract.NavigationItem.CALENDAR;
            case R.id.nav_demographic:
                return MainContract.NavigationItem.DEMOGRAPHIC;
            case R.id.nav_address:
                return MainContract.NavigationItem.CONTACTS;
            case R.id.nav_referrals:
                return MainContract.NavigationItem.REFERRALS;
            case R.id.nav_absences:
                return MainContract.NavigationItem.ABSENCES;
            case R.id.nav_final_grades:
                return MainContract.NavigationItem.FINAL_GRADES;
            case R.id.nav_settings:
                return MainContract.NavigationItem.SETTINGS;
            case R.id.nav_about:
                return MainContract.NavigationItem.ABOUT;
            default:
                return null;
        }
    }

    @Override
    public void removeTabs() {
        tabLayout.setVisibility(View.GONE);
        if (viewPager.getAdapter() != null) {
            ((ViewPagerAdapter) viewPager.getAdapter()).clear();
            viewPager.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void showPortalTabs() {
        String[] tabNames = getResources().getStringArray(R.array.portal_tab_names);
        ViewPagerAdapter adapter = new ViewPagerAdapter(
                (Fragment[]) ArrayUtils.subarray(loadingFragments, 0, 3),
                tabNames, getSupportFragmentManager());
        viewPager.setAdapter(adapter);
    }

    private void showPortalTabs(Fragment[] fragments) {

    }

    @Override
    public void showPortal(Portal portal) {
        Bundle args = new Bundle();
        args.putString(getString(com.slensky.focussis.R.string.EXTRA_PORTAL), gson.toJson(portal));

        Fragment courseFragment = new PortalCoursesTabFragment();
        courseFragment.setArguments(args);
        Fragment eventFragment = new PortalEventsTabFragment();
        eventFragment.setArguments(args);
        Fragment assignmentFragment = new PortalAssignmentsTabFragment();
        assignmentFragment.setArguments(args);

        fragmentContainer.setVisibility(View.GONE);
        if (viewPager.getAdapter() == null) {
            showPortalTabs();
        }
        ((ViewPagerAdapter) viewPager.getAdapter()).setFragments(
                new Fragment[]{courseFragment, eventFragment, assignmentFragment}
        );
        viewPager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void showScheduleTabs() {

    }

    @Override
    public void showSchedule(Schedule schedule) {

    }

    @Override
    public void showCalendar(com.slensky.focussis.data.focus.Calendar calendar) {

    }

    @Override
    public void showDemographic(Demographic demographic) {

    }

    @Override
    public void showContacts(Address address) {

    }

    @Override
    public void showReferrals(Referrals referrals) {

    }

    @Override
    public void showAbsences(Absences absences) {

    }

    @Override
    public void showFinalGrades(FinalGrades finalGrades) {

    }

    @Override
    public void showSettings() {

    }

    @Override
    public void showAbout() {

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

    public void reauthenticate() {
        Log.d(TAG, "Reauthenticating user");
        final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this,
                getString(com.slensky.focussis.R.string.timeout_progress_dialog_title),
                getString(com.slensky.focussis.R.string.timeout_progress_dialog_message),
                true);

        api.login(username, password, new FocusApi.Listener<Boolean>() {
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

    public void showNetworkError() {
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

    @Override
    public void createActionMode(int menuResId, String title) {

    }

    @Override
    public boolean isActionModeCreated() {
        return false;
    }

    @Override
    public void updateActionMode(String title) {

    }

    @Override
    public void destroyActionMode() {

    }

    public void showLoading() {
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
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
        Icepick.saveInstanceState(this, savedInstanceState);
    }

    @Override
    public void onSupportActionModeStarted(@NonNull androidx.appcompat.view.ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        // save old status bar color
        statusBarColor = getWindow().getStatusBarColor();
        //set gray color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.actionModeDark));
    }

    @Override
    public void onSupportActionModeFinished(@NonNull androidx.appcompat.view.ActionMode mode) {
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

    @Override
    public MainContract.NavigationItem getSelectedNavigationItem() {
        return null;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public void setNavigationHeader(String header) {

    }

    @Override
    public void setNavigationSubheader(String subheader) {

    }

    public PageFragment getCurrentFragment() {
        return currentFragment;
    }

    // Google play services

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
            String calendarId = preferencesHelper.getGoogleCalendarIdForUser(credential.getSelectedAccountName());

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
        preferencesHelper.setGoogleCalendarIdForUser(credential.getSelectedAccountName(), id);
    }

}
