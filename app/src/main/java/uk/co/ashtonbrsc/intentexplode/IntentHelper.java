package uk.co.ashtonbrsc.intentexplode;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IntentHelper {
    private static final Map<Integer, String> FLAGS_MAP = new HashMap<Integer, String>() {
        {
            put(Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    "FLAG_GRANT_READ_URI_PERMISSION");
            put(Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                    "FLAG_GRANT_WRITE_URI_PERMISSION");
            put(Intent.FLAG_FROM_BACKGROUND,
                    "FLAG_FROM_BACKGROUND");
            put(Intent.FLAG_DEBUG_LOG_RESOLUTION,
                    "FLAG_DEBUG_LOG_RESOLUTION");
            put(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES,
                    "FLAG_EXCLUDE_STOPPED_PACKAGES");
            put(Intent.FLAG_INCLUDE_STOPPED_PACKAGES,
                    "FLAG_INCLUDE_STOPPED_PACKAGES");
            put(Intent.FLAG_ACTIVITY_NO_HISTORY,
                    "FLAG_ACTIVITY_NO_HISTORY");
            put(Intent.FLAG_ACTIVITY_SINGLE_TOP,
                    "FLAG_ACTIVITY_SINGLE_TOP");
            put(Intent.FLAG_ACTIVITY_NEW_TASK,
                    "FLAG_ACTIVITY_NEW_TASK");
            put(Intent.FLAG_ACTIVITY_MULTIPLE_TASK,
                    "FLAG_ACTIVITY_MULTIPLE_TASK");
            put(Intent.FLAG_ACTIVITY_CLEAR_TOP,
                    "FLAG_ACTIVITY_CLEAR_TOP");
            put(Intent.FLAG_ACTIVITY_FORWARD_RESULT,
                    "FLAG_ACTIVITY_FORWARD_RESULT");
            put(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP,
                    "FLAG_ACTIVITY_PREVIOUS_IS_TOP");
            put(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
                    "FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS");
            put(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT,
                    "FLAG_ACTIVITY_BROUGHT_TO_FRONT");
            put(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED,
                    "FLAG_ACTIVITY_RESET_TASK_IF_NEEDED");
            put(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY,
                    "FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY");
            put(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET,
                    "FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET");
            put(Intent.FLAG_ACTIVITY_NO_USER_ACTION,
                    "FLAG_ACTIVITY_NO_USER_ACTION");
            put(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
                    "FLAG_ACTIVITY_REORDER_TO_FRONT");
            put(Intent.FLAG_ACTIVITY_NO_ANIMATION,
                    "FLAG_ACTIVITY_NO_ANIMATION");
            put(Intent.FLAG_ACTIVITY_CLEAR_TASK,
                    "FLAG_ACTIVITY_CLEAR_TASK");
            put(Intent.FLAG_ACTIVITY_TASK_ON_HOME,
                    "FLAG_ACTIVITY_TASK_ON_HOME");
            put(Intent.FLAG_RECEIVER_REGISTERED_ONLY,
                    "FLAG_RECEIVER_REGISTERED_ONLY");
            put(Intent.FLAG_RECEIVER_REPLACE_PENDING,
                    "FLAG_RECEIVER_REPLACE_PENDING");
            put(Intent.FLAG_RECEIVER_FOREGROUND,
                    "FLAG_RECEIVER_FOREGROUND");
            put(0x08000000,
                    "FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT");
            put(0x04000000, "FLAG_RECEIVER_BOOT_UPGRADE");
        }
    };

    static ArrayList<String> getFlags(Intent editableIntent) {
        ArrayList<String> flagsStrings = new ArrayList<>();
        int flags = editableIntent.getFlags();
        Set<Map.Entry<Integer, String>> set = FLAGS_MAP.entrySet();
        for (Map.Entry<Integer, String> thisFlag : set) {
            if ((flags & thisFlag.getKey()) != 0) {
                flagsStrings.add(thisFlag.getValue());
            }
        }
        return flagsStrings;
    }

    static String urlDecode(String fileName) {
        try {
            return URLDecoder.decode(fileName,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return fileName;
        }
    }

    static String getUri(Intent src) {
        return (src != null) ? src.toUri(Intent.URI_INTENT_SCHEME) : null;
    }

    static private ComponentName toComponentName(Object o) {
        ComponentName result = null;
        if (o instanceof ComponentName) {
            result = (ComponentName) o;
        } else if (o != null) {
            String string = o.toString();
            if (string != null && !string.trim().isEmpty()) {
                result = new ComponentName("?", string);
            }
        }
        return result;
    }

    static ComponentName getLastCallingActivity(Activity context) {
        ComponentName result = toComponentName(context.getCallingActivity());
        if (result == null) result = toComponentName(getExtra(context.getIntent(),"CALLING_ACTIVITY"));
        if (result == null) result = toComponentName(context.getCallingPackage());
        if (result == null) result = toComponentName(getExtra(context.getIntent(),"CALLING_PACKAGE"));;

        return result;
    }

    /**
     * @param intent where extras are searched
     * @param  keySuffix last part of the extras-key
     * @return intent.get("xxxx" + keySuffix)
     */
    static private Object getExtra(Intent intent, String keySuffix) {
        if (intent != null) {
            final Bundle extrasMap = intent.getExtras();

            if (extrasMap != null) {
                for (String key : extrasMap.keySet()) {
                    if (key != null) {
                        if (key.endsWith(keySuffix)) {
                            return extrasMap.get(key);
                        }
                    }
                }
            }
        }
        return null;
    }

    static Intent cloneIntent(String intentUri, Bundle additionalExtras) {
        if (intentUri != null) {
            try {
                Intent clone = Intent.parseUri(intentUri, Intent.URI_INTENT_SCHEME);

                // bugfix #14: restore extras that are lost in the intent <-> string conversion
                if (additionalExtras != null) {
                    clone.putExtras(additionalExtras);
                }

                return clone;
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
