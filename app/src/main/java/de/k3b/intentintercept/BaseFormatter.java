package de.k3b.intentintercept;

import android.content.Context;

import androidx.annotation.NonNull;

/**
 * Android specific but domain independent base class to format an object
 */
public class BaseFormatter {
    public static final String HTML_BOLD_START = "<b><u>";
    public static final String HTML_BOLD_END = "</u></b>";

    protected final Context context;
    protected final StringBuilder result = new StringBuilder();

    static final String BLANK = " ";

    public BaseFormatter(Context context) {
        this.context = context;
    }

    @NonNull
    protected StringBuilder appendNameValue(int keyId, Object value) {
        if (keyId != 0 &&value != null) {
            appendNameValue(context.getString(keyId), value);
        }
        return result;
    }

    @NonNull
    protected StringBuilder appendNameValue(String name, Object value) {
        if (name != null & value != null) {
            result.append(getListItem())
                    .append(getBOLD_START())
                    .append(name)
                    .append(getBOLD_END_BLANK())
                    .append(value).append(getNEWLINE());
        }
        return result;
    }

    @NonNull
    protected StringBuilder appendHeader(int keyId) {
        if (keyId != 0) {
            result.append(getBOLD_START()).append(context.getString(keyId)).append(getBOLD_END_NL());
        }
        return result;
    }

    @NonNull
    protected  String getNEW_SEGMENT() {
        return getNEWLINE() + "------------" + getNEWLINE();
    }

    @NonNull
    protected  String getBOLD_START() {
        return HTML_BOLD_START;
    }

    @NonNull
    protected  String getBOLD_END() {
        return HTML_BOLD_END;
    }

    @NonNull
    protected  String getBOLD_END_BLANK() {
        return getBOLD_END() + BLANK;
    }

    @NonNull
    protected  String getBOLD_END_NL() {
        return getBOLD_END() + getNEWLINE();
    }

    @NonNull
    protected String getNEWLINE() {
        return "\n<br>";
    }

    @NonNull
    protected String getListItem() {
        return "";
    }

    @NonNull
    @Override
    public  String toString() {
        return result.toString();
    }
}
