package org.kidcontact.focussis.fragments;

import android.support.v4.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;
import org.kidcontact.focussis.network.ApiBuilder;
import org.kidcontact.focussis.network.RequestSingleton;

import java.util.List;
import java.util.Map;

/**
 * Created by slensky on 4/19/17.
 */

public abstract class NetworkTabAwareFragment extends NetworkFragment implements PageFragment {

    protected List<Fragment> tabFragments = null;
    protected String title;

    public abstract boolean hasTabs();

    public abstract List<String> getTabNames();
    public List<Fragment> getTabFragments() {
        return tabFragments;
    }

    public void setTabFragments(List<Fragment> tabFragments) {
        this.tabFragments = tabFragments;
    }

    public boolean isCurrentFragmentNested() {
        return false;
    }

    public String getTitle() {
        return title;
    }
}
