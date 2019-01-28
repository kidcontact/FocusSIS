package com.slensky.focussis.ui.main;

import com.slensky.focussis.data.focus.Absences;
import com.slensky.focussis.data.focus.Address;
import com.slensky.focussis.data.focus.Calendar;
import com.slensky.focussis.data.focus.Demographic;
import com.slensky.focussis.data.focus.FinalGrades;
import com.slensky.focussis.data.focus.Portal;
import com.slensky.focussis.data.focus.Referrals;
import com.slensky.focussis.data.focus.Schedule;
import com.slensky.focussis.ui.base.MvpPresenter;
import com.slensky.focussis.ui.base.MvpView;

import androidx.annotation.IdRes;

public class MainContract {

    public interface ViewActions extends MvpView {

        NavigationItem getSelectedNavigationItem();

        String getUsername();

        String getPassword();

        void setNavigationHeader(String header);

        void setNavigationSubheader(String subheader);

        void showLoading();

        void showNetworkError();

        void removeTabs();

        void showPortalTabs();

        void showPortal(Portal portal);

        void showScheduleTabs();

        void showSchedule(Schedule schedule);

        void showCalendar(); // calendar takes care of its own loading

        void showDemographic(Demographic demographic);

        void showContacts(Address address);

        void showReferrals(Referrals referrals);

        void showAbsences(Absences absences);

        void showFinalGrades(FinalGrades finalGrades);

        void showSettings();

        void showAbout();

        void createActionMode(@IdRes int menuResId, String title);

        boolean isActionModeCreated();

        void updateActionMode(String title);

        void destroyActionMode();

    }

    public interface UserActions<V extends MvpView> extends MvpPresenter<V> {

        void onNavigationItemSelected(NavigationItem navigationItem);

        void onRefresh();

        void onDestroyActionMode();

        void onActionItemClicked();

    }

    public enum NavigationItem {
        HOME,
        SCHEDULE,
        CALENDAR,
        DEMOGRAPHIC,
        CONTACTS,
        REFERRALS,
        ABSENCES,
        FINAL_GRADES,
        SETTINGS,
        ABOUT
    }

}
