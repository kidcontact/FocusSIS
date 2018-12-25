package com.slensky.focussis.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.slensky.focussis.data.AddressContactDetail;
import com.slensky.focussis.network.FocusApi;
import com.slensky.focussis.util.CardViewAnimationController;
import com.slensky.focussis.views.IconWithTextView;

import org.json.JSONObject;
import com.slensky.focussis.R;
import com.slensky.focussis.data.Address;
import com.slensky.focussis.data.AddressContact;
import com.slensky.focussis.network.FocusApiSingleton;

import java.util.List;

/**
 * Created by slensky on 5/22/17.
 */

public class AddressFragment extends NetworkTabAwareFragment {
    private final static String TAG = "AddressFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = FocusApiSingleton.getApi();
        title = getString(R.string.address_label);
        refresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_address, container, false);
        return view;
    }

    @Override
    public boolean hasTabs() {
        return false;
    }

    @Override
    public List<String> getTabNames() {
        return null;
    }

    protected void onSuccess(Address address) {
        View view = getView();
        if (view != null) {
            CardViewAnimationController animationController = new CardViewAnimationController(getContext());
            LinearLayout addressLayout = (LinearLayout) view.findViewById(R.id.address_layout);
            for (int i = 0; i < address.getContacts().size(); i++) {
                final AddressContact c = address.getContacts().get(i);
                CardView cview = (CardView) LayoutInflater.from(getContext()).inflate(R.layout.view_address_contact, addressLayout, false);

                TextView title = (TextView) cview.findViewById(R.id.text_contact_title);
                title.setText(getString(R.string.address_contact_title) + " " + (i + 1));

                IconWithTextView name = (IconWithTextView) cview.findViewById(R.id.view_name);
                name.setText(c.getName());
                IconWithTextView relationship = (IconWithTextView) cview.findViewById(R.id.view_relationship);
                if (c.hasRelationship()) {
                    relationship.setText(c.getRelationship());
                }
                else {
                    relationship.setVisibility(View.GONE);
                }
                IconWithTextView custody = (IconWithTextView) cview.findViewById(R.id.view_custody);
                if (c.isCustody()) {
                    custody.setText(boolToYesNo(c.isCustody()));
                }
                else {
                    custody.setVisibility(View.GONE);
                }
                IconWithTextView emergency = (IconWithTextView) cview.findViewById(R.id.view_emergency);
                if (c.isEmergency()) {
                    emergency.setText(boolToYesNo(c.isEmergency()));
                }
                else {
                    emergency.setVisibility(View.GONE);
                }
                IconWithTextView contactAddress = (IconWithTextView) cview.findViewById(R.id.view_address);
                if (c.hasAddress()) {
                    contactAddress.setText(c.getAddress() + "\n" + c.getCity() + " " + c.getState() + ", " + c.getZip());
                    contactAddress.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri geoLocation = Uri.parse("geo:0,0?q=" + Uri.encode(c.getAddress() + " " + c.getCity() + " " + c.getState() + " " + c.getZip()));
                            intent.setData(geoLocation);
                            if (getContext() != null && intent.resolveActivity(getContext().getPackageManager()) != null) {
                                startActivity(intent);
                            }
                        }
                    });
                }
                else {
                    contactAddress.setVisibility(View.GONE);
                }
                IconWithTextView phone = (IconWithTextView) cview.findViewById(R.id.view_phone);
                if (c.hasPhone()) {
                    phone.setText(formatPhone(c.getPhone()));
                    phone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + c.getPhone()));
                            if (getContext() != null && intent.resolveActivity(getContext().getPackageManager()) != null) {
                                startActivity(intent);
                            }
                        }
                    });
                }
                else {
                    phone.setVisibility(View.GONE);
                }
                IconWithTextView email = (IconWithTextView) cview.findViewById(R.id.view_email);
                if (c.hasEmail()) {
                    email.setText(c.getEmail());
                    email.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{c.getEmail()});
                            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                            //startActivity(intent);
                            if (getContext() != null && intent.resolveActivity(getContext().getPackageManager()) != null) {
                                Log.i(TAG, "Emailing " + c.getEmail());
                                startActivity(intent);
                            }
                        }
                    });
                }
                else {
                    email.setVisibility(View.GONE);
                }

                IconWithTextView details = (IconWithTextView) cview.findViewById(R.id.view_details);
                if (c.hasDetails()) {
                    StringBuilder sb = new StringBuilder();
                    for (AddressContactDetail d : c.getDetails()) {
                        sb.append(d.getTitle()).append(": ");
                        if (d.getType() == AddressContactDetail.Type.PHONE) {
                            sb.append(formatPhone(d.getValue()));
                        }
                        else {
                            sb.append(d.getValue());
                        }
                        sb.append("\n");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    details.setText(sb.toString());
                }
                else {
                    details.setVisibility(View.GONE);
                }

                // i have no idea why i have to reset the margins like this, but it won't work without it
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                int margin = dpToPixel(16);
                params.setMargins(margin, margin, margin, margin);
                cview.setLayoutParams(params);

                cview.setAnimation(animationController.nextAnimation());

                addressLayout.addView(cview);
            }
        }
        requestFinished = true;
    }

    @Override
    protected void makeRequest() {
        api.getAddress(new FocusApi.Listener<Address>() {
            @Override
            public void onResponse(Address response) {
                onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onError(error);
            }
        });
    }

    private String formatPhone(String phone) {
        if (phone.length() < 4) {
            return phone;
        }
        phone = phone.substring(0, phone.length() - 4) + "-" + phone.substring(phone.length() - 4);
        if (phone.length() > 8) {
            phone = "(" + phone.substring(0, 3) + ") " + phone.substring(3);
        }
        return phone;
    }

    private int dpToPixel(int dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private String boolToYesNo(boolean b) {
        if (b) {
            return getString(R.string.yes);
        }
        return getString(R.string.no);
    }

}
