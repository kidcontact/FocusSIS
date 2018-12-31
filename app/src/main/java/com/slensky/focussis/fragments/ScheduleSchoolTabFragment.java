package com.slensky.focussis.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.gson.Gson;
import com.slensky.focussis.R;
import com.slensky.focussis.data.Schedule;
import com.slensky.focussis.util.GsonSingleton;
import com.slensky.focussis.util.JSONUtil;
import com.slensky.focussis.util.SchoolSingleton;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

/**
 * Created by slensky on 5/9/18.
 */

public class ScheduleSchoolTabFragment extends Fragment {
    private static final String TAG = "ScheduleSchoolTabFrag";
    private Schedule schedule;
    private SubsamplingScaleImageView map;

    View[] bellSchedules;

    public ScheduleSchoolTabFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gson gson = GsonSingleton.getInstance();
        schedule = gson.fromJson(getArguments().getString(getString(R.string.EXTRA_SCHEDULE)), Schedule.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_school_tab, container, false);
        map = view.findViewById(R.id.image_school_map);
        Bitmap mapBitmap = BitmapFactory.decodeResource(getResources(), SchoolSingleton.getInstance().getSchool().getMapDrawableId());
        map.setImage(ImageSource.bitmap(mapBitmap));
        map.setMaxScale(3);

        final TextInputLayout roomSearchWrapper = view.findViewById(R.id.room_search_wrapper);
        final EditText roomSearch = view.findViewById(R.id.room_search);
        roomSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    roomSearch.clearFocus();
                    if (getActivity() != null) {
                        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (in != null) {
                            in.hideSoftInputFromWindow(roomSearch.getWindowToken(), 0);
                        }
                    }

                    if (performSearch(roomSearch.getText().toString())) {
                        roomSearchWrapper.setErrorEnabled(false);
                    }
                    else {
                        roomSearchWrapper.setError(getString(R.string.map_search_error));
                    }
                    return true;
                }
                return false;
            }
        });

        int scheduleTypesId = SchoolSingleton.getInstance().getSchool().getBellScheduleTypesId();
        if (scheduleTypesId != -1) {
            Spinner spinner = view.findViewById(R.id.spinner_bell_schedule);
            ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(), scheduleTypesId, android.R.layout.simple_spinner_item);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerAdapter);
            bellSchedules = new View[spinnerAdapter.getCount()];

            final HorizontalScrollView scrollView = view.findViewById(R.id.scrollview_bell_schedule);

            final SharedPreferences prefs = getContext().getSharedPreferences(getString(R.string.schedule_prefs), Context.MODE_PRIVATE);
            spinner.setSelection(prefs.getInt(getString(R.string.schedule_prefs_spinner_selection), 0));

            final int[] scheduleLayouts = SchoolSingleton.getInstance().getSchool().getBellScheduleLayouts();

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt(getString(R.string.schedule_prefs_spinner_selection), i);
                    editor.apply();

                    scrollView.removeAllViews();
                    if (bellSchedules[i] != null) {
                        if (bellSchedules[i].getParent() != null) {
                            ((ViewGroup) bellSchedules[i].getParent()).removeView(bellSchedules[i]);
                        }
                        scrollView.addView(bellSchedules[i]);
                    }
                    else {
                        bellSchedules[i] = inflater.inflate(scheduleLayouts[i], scrollView, false);
                        scrollView.addView(bellSchedules[i]);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        return view;
    }

    private boolean performSearch(String text) {
        final int roomNumbersId = SchoolSingleton.getInstance().getSchool().getMapRoomNumbersId();
        final int roomKeywordsId = SchoolSingleton.getInstance().getSchool().getMapKeywordsId();
        // no point in doing anything if we don't even have room numbers
        if (roomNumbersId == -1) {
            return false;
        }

        text = StringUtils.stripAccents(text);
        text = text.replace(".", "")
                .replace("'s", "")
                .replace("'", "");

        try {
            JSONObject roomNumbers = roomNumbers = JSONUtil.JSONFromRawResource(getResources(), R.raw.map_asd_room_numbers);
            JSONObject roomKeywords = null;
            if (roomKeywordsId != -1) {
                roomKeywords = JSONUtil.JSONFromRawResource(getResources(), R.raw.map_asd_room_keywords);
            }


            if (NumberUtils.isDigits(text) && roomNumbers.has(text)) {
                Log.d(TAG, "Zooming to room " + text);
                zoomMapToCoordinates(roomNumbers.getString(text));
                return true;
            }

            // nothing else to do if there are no keywords for this school
            if (roomKeywords == null) {
                return false;
            }

            boolean isBathroom = text.equals("bathroom");
            JSONArray bathroomKeywords = roomKeywords.getJSONArray("bathroom");
            for (int i = 0; i < bathroomKeywords.length(); i++) {
                if (text.equals(bathroomKeywords.getString(i))) {
                    isBathroom = true;
                    break;
                }
            }

            if (isBathroom) {
                Log.d(TAG, "Zooming to nearest bathroom");
                // move to the nearest bathroom
                JSONArray bathrooms = roomNumbers.getJSONArray("bathroom");
                PointF currentCenter;
                if (map != null && (currentCenter = map.getCenter()) != null) {
                    double closestDistanceSquared = Integer.MAX_VALUE;
                    int closestBathroomIndex = 0;
                    for (int i = 0; i < bathrooms.length(); i++) {
                        String[] coords = bathrooms.getString(i).split(" ");
                        int x = Integer.parseInt(coords[0]);
                        int y = Integer.parseInt(coords[1]);
                        double distanceSquared = Math.pow(x - currentCenter.x, 2) + Math.pow(y - currentCenter.y, 2);
                        if (distanceSquared < closestDistanceSquared) {
                            closestDistanceSquared = distanceSquared;
                            closestBathroomIndex = i;
                        }
                    }
                    Log.d(TAG, "closestBathroomIndex: " + closestBathroomIndex);
                    zoomMapToCoordinates(bathrooms.getString(closestBathroomIndex));
                    return true;
                }
                Log.d(TAG, "Map is null, zooming to bathroom 0");
                zoomMapToCoordinates(bathrooms.getString(0));
                return true;
            }

            Map<String, String> keywords = new HashMap<>();
            Iterator<?> keys = roomKeywords.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (!key.equals("bathroom") && roomKeywords.get(key) instanceof JSONArray) {
                    JSONArray keywordsArr = roomKeywords.getJSONArray(key);
                    for (int i = 0; i < keywordsArr.length(); i++) {
                        keywords.put(keywordsArr.getString(i), key);
                    }
                }
            }

            keys = roomNumbers.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (!NumberUtils.isDigits(key) && !key.equals("bathroom")) {
                    keywords.put(key, key);
                }
            }

            ExtractedResult result = FuzzySearch.extractOne(text, keywords.keySet());
            if (result.getScore() > 85) {
                Log.d(TAG, "Zooming to room " + keywords.get(result.getString()) + " (score: " + result.getScore() + ")");
                zoomMapToCoordinates(roomNumbers.getString(keywords.get(result.getString())));
                return true;
            }
            else {
                Log.d(TAG, "Result " + result.getString() + " with score " + result.getScore() + "did not match input text " + text + "closely enough");
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSONException while performing search", e);
        }

        return false;
    }

    private void zoomMapToCoordinates(String coords) {
        String[] split = coords.split(" ");
        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        if (map != null) {
            float scaleAmount = (float) 2.75;
            // scale by less on small displays
            if (getActivity() != null) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int width = displayMetrics.widthPixels;
                if (width <= 720) {
                    scaleAmount = 2;
                }
            }

            map.animateScaleAndCenter(scaleAmount, new PointF(x, y))
                .withDuration(500)
                .withEasing(SubsamplingScaleImageView.EASE_OUT_QUAD)
                .withInterruptible(false)
                .start();
        }
    }

}
