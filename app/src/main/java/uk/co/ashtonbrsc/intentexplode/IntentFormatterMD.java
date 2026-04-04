package uk.co.ashtonbrsc.intentexplode;

import android.content.Context;

import androidx.annotation.NonNull;
/**
 * Class to format an Intent in MarkDown.
 * All domain specic logic is inside {@link IntentFormatter}.
 * This class reformats output to MarkDown;
 */
public class IntentFormatterMD extends IntentFormatter {
    private final String headerPrefix;
    public IntentFormatterMD(Context context, String headerPrefix) {
        super(context);
        this.headerPrefix = headerPrefix;
    }

    @NonNull
    @Override
    protected StringBuilder appendHeader(int keyId) {
        if (keyId != 0) {
            result.append(headerPrefix);
            super.appendHeader(keyId);
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
