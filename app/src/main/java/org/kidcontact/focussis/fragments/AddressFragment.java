package org.kidcontact.focussis.fragments;

import android.graphics.drawable.Icon;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.kidcontact.focussis.R;
import org.kidcontact.focussis.data.Address;
import org.kidcontact.focussis.data.AddressContact;
import org.kidcontact.focussis.data.AddressContactDetail;
import org.kidcontact.focussis.network.ApiBuilder;
import org.kidcontact.focussis.network.FocusApiSingleton;
import org.kidcontact.focussis.views.IconWithTextView;

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

    @Override
    protected void onSuccess(JSONObject response) {
        Address address = new Address(response);
        View view = getView();
        if (view != null) {
//            IconWithTextView userAddress = (IconWithTextView) view.findViewById(R.id.view_address);
//            userAddress.setText(address.getAddress() + "\n" + address.getCity() + " " + address.getState() + ", " + address.getZip());
//            IconWithTextView userPhone = (IconWithTextView) view.findViewById(R.id.view_phone);
//            userPhone.setText(formatPhone(address.getPhone()));

            LinearLayout addressLayout = (LinearLayout) view.findViewById(R.id.address_layout);
            for (int i = 0; i < address.getContacts().size(); i++) {
                AddressContact c = address.getContacts().get(i);
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
                }
                else {
                    contactAddress.setVisibility(View.GONE);
                }
                IconWithTextView phone = (IconWithTextView) cview.findViewById(R.id.view_phone);
                if (c.hasPhone()) {
                    phone.setText(formatPhone(c.getPhone()));
                }
                else {
                    phone.setVisibility(View.GONE);
                }
                IconWithTextView email = (IconWithTextView) cview.findViewById(R.id.view_email);
                if (c.hasEmail()) {
                    email.setText(c.getEmail());
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

                addressLayout.addView(cview);
            }
        }
        requestFinished = true;
    }

    @Override
    public void refresh() {
        requestFinished = false;
        networkFailed = false;
        api.getAddress(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
