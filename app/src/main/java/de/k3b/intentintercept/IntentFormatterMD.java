package de.k3b.intentintercept;

import android.content.Context;

import androidx.annotation.NonNull;
/**
 * Class to format an Intent in MarkDown.
 * All domain specic logic is inside {@link IntentFormatter}.
 * This class reformats output to MarkDown;
 */
public class IntentFormatterMD extends IntentFormatter {
    private final String headerPrefix;
    public IntentFormatterMD(Context context, boolean reportWithMatchingActivities, String headerPrefix) {
        super(context, reportWithMatchingActivities);
        this.headerPrefix = headerPrefix;
    }

    @NonNull
    @Override
    protected StringBuilder appendHeader(String headerText) {
        if (headerText != null) {
            result.append(headerPrefix);
            super.appendHeader(headerText);
        }
        return result;
    }

    @NonNull @Override
    protected  String getBOLD_START() {
        return "**";
    }

    @NonNull @Override
    protected  String getBOLD_END() {
        return "**";
    }

    @NonNull @Override
    protected  String getunformat_START() {
        return " `";
    }

    @NonNull  @Override
    protected  String getunformat_END() {
        return " `";
    }

    @NonNull
    @Override
    protected String getNEWLINE() {
        return "\n";
    }

    @NonNull
    @Override
    protected String getListItem() {
        return "* ";
    }


}
