package de.k3b.intentintercept;

import android.content.Context;

import androidx.annotation.NonNull;

/**
 * Class to format an Intent in Caolapsable MarkDown.
 */
public class IntentFormatterMDColapsable extends IntentFormatterMD{
    private boolean insideHeader = false;

    public IntentFormatterMDColapsable(Context context, boolean withMatchingActivities) {
        super(context, withMatchingActivities, "");
    }

    @NonNull
    @Override
    protected StringBuilder appendHeader(int keyId) {
        if (insideHeader) {
            result.append("</details>")
                    .append(getNEWLINE());
            insideHeader = false;
        }
        if (keyId != 0) {
            result.append("<details markdown=\"1\"><summary> ")
                    .append(getBOLD_START())
                    .append(context.getString(keyId))
                    .append(getBOLD_END_BLANK())
                    .append("</summary>")
            ;
            insideHeader = true;
        }
        return result;
    }

}
