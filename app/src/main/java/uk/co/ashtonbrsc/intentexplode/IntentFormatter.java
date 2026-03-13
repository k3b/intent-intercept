package uk.co.ashtonbrsc.intentexplode;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import uk.co.ashtonbrsc.android.intentintercept.R;

public class IntentFormatter {
    private static final String NEWLINE = "\n<br>";
    private static final String NEW_SEGMENT = NEWLINE + "------------" + NEWLINE;
    static final String BLANK = " ";

    private static final String BOLD_START = "<b><u>";
    private static final String BOLD_END_BLANK = "</u></b>" + BLANK;
    private static final String BOLD_END_NL = "</u></b>" + NEWLINE;

    static public Spanned getIntentDetailsString(Context context, Intent editableIntent, Integer lastResultCode, Intent lastResultIntent) {
        StringBuilder result = new StringBuilder();

        // k3b so intent can be reloaded using
        // Intent.parseUri("Intent:....", Intent.URI_INTENT_SCHEME)
        result.append(IntentHelper.getUri(editableIntent))
                .append(NEW_SEGMENT);

        appendIntentDetails(context, result, editableIntent, true)
                .append(NEW_SEGMENT);

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentActivities(
                editableIntent, 0);

        // Remove Intent Intercept from matching activities
        int numberOfMatchingActivities = resolveInfo.size() - 1;

        appendHeader(context, result, R.string.intent_matching_activities_title);
        if (numberOfMatchingActivities < 1) {
            appendHeader(context, result, R.string.no_items);
        } else {
            for (int i = 0; i <= numberOfMatchingActivities; i++) {
                ResolveInfo info = resolveInfo.get(i);
                ActivityInfo activityinfo = info.activityInfo;
                if (!activityinfo.packageName.equals(context.getPackageName())) {
                    result.append(BOLD_START).append(activityinfo.loadLabel(pm))
                            .append(BOLD_END_BLANK).append(" (")
                            .append(activityinfo.packageName)
                            .append(" - ")
                            .append(activityinfo.name)
                            .append(")").append(NEWLINE);
                }
            }
        }

        // support for onActivityResult
        if (lastResultCode != null) {
            result.append(NEW_SEGMENT);
            appendHeader(context, result, R.string.last_result_header_title);
            appendNameValue(context, result, R.string.last_result_code_title, lastResultCode);

            if (lastResultIntent != null) {
                appendIntentDetails(context, result, lastResultIntent, false);
            }
        }

        return Html.fromHtml(result.toString());
    }

    static private StringBuilder appendIntentDetails(Context context, StringBuilder result, Intent intent, boolean detailed) {
        if (detailed) appendNameValue(context, result, R.string.intent_action_title, intent.getAction());

        appendNameValue(context, result, R.string.intent_data_title, intent.getData());
        appendNameValue(context, result, R.string.intent_mime_type_title, intent.getType());
        appendNameValue(context, result, R.string.intent_uri_title, IntentHelper.getUri(intent));

        Set<String> categories = intent.getCategories();
        if ((categories != null) && (categories.size() > 0)) {
            appendHeader(context, result, R.string.intent_categories_title);
            for (String category : categories) {
                result.append(category).append(NEWLINE);
            }
        }

        if (detailed) {
            appendHeader(context, result, R.string.intent_flags_title);
            ArrayList<String> flagsStrings = IntentHelper.getFlags(intent);
            if (!flagsStrings.isEmpty()) {
                for (String thisFlagString : flagsStrings) {
                    result.append(thisFlagString).append(NEWLINE);
                }
            } else {
                result.append(context.getString(R.string.no_items)).append(NEWLINE);
            }
        }

        try {
            Bundle intentBundle = intent.getExtras();
            if (intentBundle != null) {
                Set<String> keySet = intentBundle.keySet();
                appendHeader(context, result, R.string.intent_extras_title);
                int count = 0;

                for (String key : keySet) {
                    count++;
                    Object thisObject = intentBundle.get(key);
                    result.append(BOLD_START).append(count).append(BOLD_END_BLANK);
                    String thisClass = thisObject.getClass().getName();
                    if (thisClass != null) {
                        result.append(context.getString(R.string.extra_item_type_name_title)).append(BLANK)
                                .append(thisClass).append(NEWLINE);
                    }
                    result.append(context.getString(R.string.extra_item_key_title)).append(BLANK)
                            .append(key).append(NEWLINE);

                    if (thisObject instanceof String || thisObject instanceof Long
                            || thisObject instanceof Integer
                            || thisObject instanceof Boolean
                            || thisObject instanceof Uri) {
                        String thisObjAsString = thisObject.toString();
                        result.append(context.getString(R.string.extra_item_value_title)).append(BLANK)
                                .append(thisObjAsString)
                                .append(NEWLINE);
                        if (thisObjAsString.contains("%")) {
                            // data may be encoded with "% ..." also add the decoded string
                            result.append(context.getString(R.string.extra_item_value_title_unescaped)).append(BLANK)
                                    .append(IntentHelper.urlDecode(IntentHelper.urlDecode(thisObjAsString)))
                                    .append(NEWLINE);
                        }
                    } else if (thisObject instanceof ArrayList) {
                        result.append(context.getString(R.string.extra_item_type_name_list)).append(NEWLINE);
                        ArrayList<Object> thisArrayList = (ArrayList<Object>) thisObject;
                        for (Object thisArrayListObject : thisArrayList) {
                            result.append(thisArrayListObject.toString()).append(NEWLINE);
                        }
                    }
                }
            }
        } catch (Exception e) {
            appendHeader(context, result, R.string.intent_extras_title);
            result.append("<font color='red'>").append(context.getString(R.string.error_extracting_extras)).append("</font>").append(NEWLINE);
            e.printStackTrace();
        }
        return result;
    }

    static private StringBuilder appendNameValue(Context context, StringBuilder result, int keyId, Object value) {
        if (value != null) {
            result.append(BOLD_START).append(context.getString(keyId)).append(BOLD_END_BLANK)
                    .append(value).append(NEWLINE);
        }
        return result;
    }

    static private StringBuilder appendHeader(Context context, StringBuilder result, int keyId) {
        result.append(BOLD_START).append(context.getString(keyId)).append(BOLD_END_NL);
        return result;
    }
}
