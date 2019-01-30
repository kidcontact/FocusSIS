package com.slensky.focussis.fragments;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.slensky.focussis.R;

/**
 * Created by slensky on 5/1/17.
 */

public class AboutFragment extends Fragment implements PageFragment {

    String version = "";

    public AboutFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PackageInfo pInfo = null;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

//        ImageView logo = (ImageView) view.findViewById(R.id.image_logo);
//        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) logo.getLayoutParams();
//        lp.setMargins(lp.leftMargin, ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight() / 10, lp.rightMargin, lp.bottomMargin);
//        logo.setLayoutParams(lp);

        TextView version = (TextView) view.findViewById(R.id.text_version);
        version.setText("Version " + this.version);
        //TextView copyright = (TextView) view.findViewById(R.id.text_copyright);
        //copyright.setText(String.format(getString(R.string.copyright), Calendar.getInstance().get(Calendar.YEAR)));
        //TextView license = (TextView) view.findViewById(R.id.text_license);

        ImageView mailIcon = (ImageView) view.findViewById(R.id.email_icon);
        mailIcon.setColorFilter(Color.argb(132, 0, 0, 0), PorterDuff.Mode.MULTIPLY);
        ImageView githubIcon = (ImageView) view.findViewById(R.id.github_icon);
        githubIcon.setColorFilter(Color.argb(132, 0, 0, 0), PorterDuff.Mode.MULTIPLY);
        ImageView licenseIcon = (ImageView) view.findViewById(R.id.version_icon);
        licenseIcon.setColorFilter(Color.argb(132, 0, 0, 0), PorterDuff.Mode.MULTIPLY);

        /*license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parseRequirements(getString(R.string.about_license_link));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });*/

        RelativeLayout emailLayout = (RelativeLayout) view.findViewById(R.id.email_layout);
        emailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.about_email), null));
                startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });

        RelativeLayout githubLayout = (RelativeLayout) view.findViewById(R.id.github_layout);
        githubLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(getString(R.string.about_github_link));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public String getTitle() {
        return getString(R.string.about_label);
    }

}
