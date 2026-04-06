package de.k3b.intentintercept;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.k3b.GuiUtil;

/**
 * Class to format an Intent.
 * All Intent-Domain specific knowledge like flags, Action, .... is inside this class;
 * All formatting specific knowledge (bold, key-value, section) is in base class or
 * in child classes that overwrites methods in base class.
 *
 */
public class IntentFormatter extends BaseFormatter {

    /** maximum number of characters in one item. If item is longer, it will be truncated */
    protected static final int MAX_TEXT_LEN = 128;
    /** maximum number of subitem in array or bundle. If there are more subitem the list will be truncated */
    private static final int MAX_LIST_COUNT = 20;
    private final boolean withMatchingActivities;

    public IntentFormatter(Context context, boolean withMatchingActivities) {
        super(context);
        this.withMatchingActivities = withMatchingActivities;
    }

    public String getIntentDetailsString(Intent editableIntent,
                                                 ComponentName callingActivity,
                                                 Integer lastResultCode, Intent lastResultIntent) {
        // k3b so intent can be reloaded using
        // Intent.parseUri("Intent:....", Intent.URI_INTENT_SCHEME)
        String uri = getUri(editableIntent);
        this.result.append(uri)
                .append(getNEWLINE())
                .append(getNEW_SEGMENT());

        // #40 support for callingActivity
        if (callingActivity != null) {
            this.appendNameValue(R.string.intent_calling_activity_title, callingActivity.flattenToShortString());
        }

        // support for onActivityResult
        if (lastResultCode != null) {
            this.result.append(getNEW_SEGMENT());
            this.appendHeader(R.string.last_result_header_title);
            this.appendNameValue(R.string.last_result_code_title, lastResultCode);

            if (lastResultIntent != null) {
                appendIntentDetails(lastResultIntent, false);
            }
        }

        appendIntentDetails(editableIntent, true)
                .append(getNEW_SEGMENT());

        if (withMatchingActivities) appendMatchingActivities(editableIntent, true);

        // close last Header
        this.appendHeader(0);

        result.append(getNEWLINE());
        result.append(GuiUtil.getAppVersionName(context, R.string.report_app_version)).append(getNEWLINE());

        return this.result.toString();
    }

    @NonNull
    private String getUri(Intent editableIntent) {
        String uri = IntentHelper.getUri(editableIntent);
        uri = uri.replace(";",";"+getNEWLINE());
        return uri;
    }

    public IntentFormatter clr() {
        result.setLength(0);
        return this;
    }
    public StringBuilder appendMatchingActivities(Intent editableIntent, boolean withHeader) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentActivities(
                editableIntent, 0);


        // -1 because Intent Intercept is removed from matching activities
        int numberOfMatchingActivities = resolveInfo.size() - 1;

        if (numberOfMatchingActivities > 0) {
            if (withHeader) {
                this.appendHeader(R.string.intent_matching_activities_title);
            }
            for (int i = 0; i <= numberOfMatchingActivities; i++) {
                ResolveInfo info = resolveInfo.get(i);
                ActivityInfo activityinfo = info.activityInfo;
                if (!activityinfo.packageName.equals(context.getPackageName())) {
                    result.append(getListItem())
                            .append(getBOLD_START())
                            .append(activityinfo.loadLabel(pm))
                            .append(getBOLD_END_BLANK()).append(" (")
                            .append(activityinfo.packageName)
                            .append(" - ")
                            .append(activityinfo.name)
                            .append(")")
                            .append(getNEWLINE());
                }
            }
        }
        return result;
    }

    private StringBuilder appendIntentDetails(Intent intent, boolean detailed) {
        if (detailed) this.appendNameValue(R.string.intent_action_title, intent.getAction());

        this.appendNameValue(R.string.intent_data_title, intent.getData());
        this.appendNameValue(R.string.intent_mime_type_title, intent.getType());
        this.appendNameValue(R.string.intent_uri_title, getUri(intent));

        appendCategories(intent, true);

        if (detailed) {
            appendFlags(intent, true);
        }

        appendExtras(intent, true, true);
        return this.result;
    }

    public StringBuilder appendExtras(Intent intent, boolean withHeader, boolean includeBitmaps) {
        try {
            Bundle intentBundle = intent.getExtras();
            if (intentBundle != null) {
                Set<String> keySet = intentBundle.keySet();
                if (withHeader) this.appendHeader(R.string.intent_extras_title);
                int count = 0;

                for (String key : keySet) {
                    count++;
                    Object value = intentBundle.get(key);
                    if (includeBitmaps || !(value instanceof Bitmap)) {
                        appendExtra(count, key, value);
                    }
                }
            }
        } catch (Exception e) {
            this.appendHeader(R.string.intent_extras_title);
            this.result.append("<font color='red'>").append(this.context.getString(R.string.error_extracting_extras)).append("</font>").append(getNEWLINE());
            e.printStackTrace();
        }
        return this.result;
    }

    public StringBuilder appendExtra(int count, String key, Object value) {
        if (value != null && key != null) {
            result
                    .append(getListItem())
                    .append(getBOLD_START())
                    .append(count)
                    .append(getBOLD_END_BLANK());
            String thisClass = value.getClass().getName();
            if (thisClass != null) {
                result.append(this.context.getString(R.string.extra_item_type_name_title)).append(BLANK)
                        .append(thisClass).append(getNEWLINE());
            }
            result.append(this.context.getString(R.string.extra_item_key_title)).append(BLANK)
                    .append(key).append(getNEWLINE());

            if (value instanceof ArrayList) {
                result.append(this.context.getString(R.string.extra_item_type_name_list)).append(getNEWLINE());
                ArrayList<Object> thisArrayList = (ArrayList<Object>) value;
                int listCount = 0;
                for (Object thisArrayListObject : thisArrayList) {
                    if (++listCount < MAX_LIST_COUNT) {
                        result.append(thisArrayListObject.toString()).append(getNEWLINE());
                    }
                }
                if (listCount >= MAX_LIST_COUNT) {
                    result.append("...").append(getNEWLINE());
                }

            } else if (value instanceof Bundle) {
                Bundle bundle = (Bundle) value;
                result.append("Bundle{").append(getNEWLINE());
                int listCount = 0;
                for (String bkey : bundle.keySet()) {
                    if (++listCount < MAX_LIST_COUNT)
                        appendNameValue(bkey, ": " + bundle.get(bkey));
                }
                if (listCount >= MAX_LIST_COUNT) {
                    result.append("...").append(getNEWLINE());
                }
                result.append("}").append(getNEWLINE());
            } else  {
                String thisObjAsString = value.toString();
                if (thisObjAsString.length() > MAX_TEXT_LEN) thisObjAsString = thisObjAsString.substring(0, MAX_TEXT_LEN-3) + "...";
                this.result.append(this.context.getString(R.string.extra_item_value_title)).append(BLANK)
                        .append(thisObjAsString)
                        .append(getNEWLINE());
                if (thisObjAsString.contains("%")) {
                    // data may be encoded with "% ..." also add the decoded string
                    this.result.append(this.context.getString(R.string.extra_item_value_title_unescaped)).append(BLANK)
                            .append(IntentHelper.urlDecode(IntentHelper.urlDecode(thisObjAsString)))
                            .append(getNEWLINE());
                }
            }
        }
        return result;
    }

    public StringBuilder appendFlags(Intent intent, boolean withHeader) {
        int flags = intent.getFlags();
        if (flags != 0) {
            if (withHeader) this.appendHeader(R.string.intent_flags_title);
            ArrayList<String> flagsStrings = IntentHelper.getFlags(flags);
            if (!flagsStrings.isEmpty()) {
                for (String thisFlagString : flagsStrings) {
                    this.result.append(thisFlagString).append(getNEWLINE());
                }
            }
        }
        return result;
    }

    public StringBuilder appendCategories(Intent intent, boolean withHeader) {
        Set<String> categories = intent.getCategories();
        if ((categories != null) && (categories.size() > 0)) {
            if (withHeader) this.appendHeader(R.string.intent_categories_title);
            for (String category : categories) {
                this.result.append(category).append(getNEWLINE());
            }
        }
        return result;
    }

}
