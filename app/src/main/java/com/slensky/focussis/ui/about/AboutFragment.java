package com.slensky.focussis.ui.about;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.slensky.focussis.R;
import com.slensky.focussis.di.component.ActivityComponent;
import com.slensky.focussis.ui.base.BaseActivity;
import com.slensky.focussis.ui.base.BaseFragment;
import com.slensky.focussis.ui.base.PageFragment;

import java.util.Objects;

import javax.inject.Inject;

/**
 * Created by slensky on 5/1/17.
 */

public class AboutFragment extends BaseFragment implements PageFragment, AboutContract.ViewActions {

    private String version = "?";

    @Inject AboutContract.UserActions<AboutContract.ViewActions> presenter;

    public AboutFragment() {
        // required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        ActivityComponent component = getActivityComponent();
        if (component != null) {
            component.inject(this);

            try {
                PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                this.version = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                // should never occur
                e.printStackTrace();
            }

            presenter.onAttach(this);
        }

        return view;
    }

    @Override
    public String getTitle() {
        return getString(R.string.about_label);
    }

    @Override
    public void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.about_email), null));
        startActivity(Intent.createChooser(intent, "Send Email"));
    }

    @Override
    public void showGithubPage() {
        Uri uri = Uri.parse(getString(R.string.about_github_link));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    protected void setupView(View view) {
        TextView version = view.findViewById(R.id.text_version);
        version.setText(getString(R.string.about_version_format, this.version));

        ImageView mailIcon = view.findViewById(R.id.email_icon);
        mailIcon.setColorFilter(Color.argb(132, 0, 0, 0), PorterDuff.Mode.MULTIPLY);
        ImageView githubIcon = view.findViewById(R.id.github_icon);
        githubIcon.setColorFilter(Color.argb(132, 0, 0, 0), PorterDuff.Mode.MULTIPLY);

        RelativeLayout emailLayout = view.findViewById(R.id.email_layout);
        emailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onEmailClick();
            }
        });

        RelativeLayout githubLayout = view.findViewById(R.id.github_layout);
        githubLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onGithubClick();
            }
        });
    }

    @Override
    public void onDestroyView() {
        presenter.onDetach();
        super.onDestroyView();
    }

}
