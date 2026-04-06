//   Copyright 2012-2014 Intrications (intrications.com)
//   Copyright 2014-2026 k3b
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package de.k3b.intentintercept;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.LeadingMarginSpan;
import android.text.style.ParagraphStyle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;
import de.k3b.android.widget.HistoryEditText;

//TODO add icon -which icon - app icons???
//TODO add getCallingActivity() - will only give details for startActivityForResult();

/**
 * Should really be called IntentDetailsActivity but this may cause problems with launcher
 * shortcuts and the enabled/disabled state of interception.
 */
public class IntentDetailsActivity extends AppCompatActivity {

    static final int STANDARD_INDENT_SIZE_IN_DIP = 10;
    static final String INTENT_EDITED = "intent_edited";
    public static final int REQUEST_CODE_RESEND = 1; // resend edited intend
    public static final int REQUEST_CODE_SETTINGS = 2; // call settings activity

    private abstract class IntentUpdateTextWatcher implements TextWatcher {
        private final TextView textView;

        IntentUpdateTextWatcher(TextView textView) {
            this.textView = textView;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if (textWatchersActive) {
                try {
                    String modifiedContent = textView.getText().toString();
                    onUpdateIntent(modifiedContent);
                    showTextViewIntentData(textView);
                    showResetIntentButton(true);
                    refreshUI();
                } catch (Exception e) {
                    Toast.makeText(IntentDetailsActivity.this, e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }

        abstract protected void onUpdateIntent(String modifiedContent);

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private ShareActionProvider shareActionProvider;

    private EditText edAction;
    private TextView lblData;
    private EditText edData;
    private EditText edType;

    private TextView lblResult;
    private EditText edResult;
    private TextView lblCallingActivity;
    private EditText edCallingActivity;

    private EditText edUri;

    private HistoryEditText mHistory = null;

    private TextView categoriesHeader;
    private LinearLayoutCompat categoriesLayout;
    private LinearLayoutCompat flagsLayout;
    private LinearLayoutCompat extrasLayout;
    private LinearLayoutCompat activitiesLayout;
    private TextView activitiesHeader;
    private Button resendIntentButton;
    private Button resetIntentButton;
    private float density;

    /**
     * String representation of intent as uri
     */
    private String originalIntent;

    /**
     * Bugfix #14: extras that are lost in the intent <-> string conversion
     */
    private Bundle additionalExtras;

    private Intent editableIntent;

    // support for onActivityResult
    private Integer lastResultCode = null;
    private Intent lastResultIntent = null;

    /**
     * false: text-change-events are not active.
     */
    private boolean textWatchersActive;

    private IntentFormatter guiFormatter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.guiFormatter = new IntentFormatterHtml(this, false);
        setContentView(R.layout.intent_details);

        rememberIntent(getIntent());

        final boolean isVisible = savedInstanceState != null
                && savedInstanceState.getBoolean(INTENT_EDITED);
        showInitialIntent(isVisible);

        if (mHistory != null) mHistory.saveHistory();

    }

    private void rememberIntent(Intent original) {
        this.originalIntent = IntentHelper.getUri(original);

        Intent copy = IntentHelper.cloneIntent(this.originalIntent, additionalExtras);

        final Bundle originalExtras = original.getExtras();

        if (originalExtras != null) {
            // bugfix #14: collect extras that are lost in the intent <-> string conversion
            Bundle additionalExtrasBundle = new Bundle(originalExtras);
            for (String key : originalExtras.keySet()) {
                if (copy.hasExtra(key)) {
                    additionalExtrasBundle.remove(key);
                }
            }

            if (!additionalExtrasBundle.isEmpty()) {
                additionalExtras = additionalExtrasBundle;
            }
        }

    }

    /**
     * creates a clone of originalIntent and displays it for editing
     */
    private void showInitialIntent(boolean isVisible) {
        editableIntent = IntentHelper.cloneIntent(this.originalIntent, additionalExtras);

        editableIntent.setComponent(null);

        setupVariables();

        setupTextWatchers();

        showAllIntentData(null);

        showResetIntentButton(isVisible);
    }

    /**
     * textViewToIgnore is not updated so current selected char in that textview will not change
     */
    private void showAllIntentData(TextView textViewToIgnore) {
        showTextViewIntentData(textViewToIgnore);

        categoriesLayout.removeAllViews();
        if (editableIntent.getCategories() != null) {
            categoriesHeader.setVisibility(View.VISIBLE);
            addHtmlToLayout(guiFormatter.clr().appendCategories(editableIntent,false), categoriesLayout);
        } else {
            categoriesHeader.setVisibility(View.GONE);
        }

        flagsLayout.removeAllViews();
        addHtmlToLayout(guiFormatter.clr().appendFlags(editableIntent,false), flagsLayout);

        extrasLayout.removeAllViews();
        try {
            addHtmlToLayout(guiFormatter.clr().appendExtras(editableIntent,false, false), flagsLayout);

            Bundle intentBundle = editableIntent.getExtras();
            if (intentBundle != null) {
                Set<String> extraKeys = intentBundle.keySet();
                int count = 0;

                for (String extraKey : extraKeys) {
                    count++;
                    Object extraItem = intentBundle.get(extraKey);
                    if (extraItem != null && extraItem instanceof Bitmap) {
                        addBitmapToLayout(Html.fromHtml(guiFormatter.clr().appendExtra(count, extraKey, extraItem).toString()),
                                    Typeface.ITALIC, STANDARD_INDENT_SIZE_IN_DIP,
                                    (Bitmap) extraItem, extrasLayout);
                    }
                }
            }
        } catch (Exception e) {
            // TODO Should make this red to highlight error
            addTextToLayout(getString(R.string.error_extracting_extras), Typeface.NORMAL, extrasLayout);
            e.printStackTrace();
        }

        refreshUI();
    }

    /**
     * textViewToIgnore is not updated so current selected char in that textview will not change
     */
    private void showTextViewIntentData(TextView textViewToIgnore) {
        textWatchersActive = false;
        if (textViewToIgnore != edAction) edAction.setText(editableIntent.getAction());
        String dataString = editableIntent.getDataString();
        if ((textViewToIgnore != edData) && (dataString != null)) {
            edData.setText(dataString);
            if (dataString.contains("%")) {
                // data may be encoded with "% ..." also add the decoded string
                lblData.setText(getText(R.string.intent_data_title) + " (" +
                                IntentHelper.urlDecode(IntentHelper.urlDecode(dataString)) + ")");
            } else {
                lblData.setText(R.string.intent_data_title);
            }

        }
        if (textViewToIgnore != edType) edType.setText(editableIntent.getType());
        if (textViewToIgnore != edUri) edUri.setText(IntentHelper.getUri(editableIntent));

        if (textViewToIgnore != edResult) {
            String resultString = this.lastResultCode + " " + IntentHelper.getUri(this.lastResultIntent);
            int visible = (this.lastResultIntent != null) ? View.VISIBLE : View.GONE;
            edResult.setVisibility(visible);
            lblResult.setVisibility(visible);
            edResult.setText(resultString);
        }

        if (textViewToIgnore != edCallingActivity) {
            ComponentName lastCallingActivity = IntentHelper.getLastCallingActivity(this);
            String callingActivityString = null;
            int visible = View.GONE;
            if (lastCallingActivity != null) {
                visible = View.VISIBLE;
                callingActivityString = lastCallingActivity.flattenToShortString();
            }
            edCallingActivity.setVisibility(visible);
            lblCallingActivity.setVisibility(visible);
            edCallingActivity.setText(callingActivityString);
        }

        textWatchersActive = true;
    }

    private void addBitmapToLayout(CharSequence text, int typeface, int paddingLeft, Bitmap bitmap, LinearLayoutCompat linearLayout) {
        LinearLayoutCompat bitmapLayout = new LinearLayoutCompat(this);
        TextView textView = new TextView(this);
        ParagraphStyle style_para = new LeadingMarginSpan.Standard(0,
                (int) (STANDARD_INDENT_SIZE_IN_DIP * density));
        SpannableString styledText = new SpannableString(text);
        styledText.setSpan(style_para, 0, styledText.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        textView.setText(styledText);
        textView.setTextAppearance(this, R.style.TextFlags);
        textView.setTypeface(null, typeface);
        textView.setTextIsSelectable(true);
        LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
        params.setMargins((int) (paddingLeft * density), 0, 0, 0);

        // At most 144dsp.
        float maxHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 144, getResources().getDisplayMetrics());
        int height = (int) Math.min(bitmap.getHeight(), maxHeight);
        int width = bitmap.getWidth() * (height / bitmap.getHeight());

        LinearLayoutCompat.LayoutParams bitmapParams = new LinearLayoutCompat.LayoutParams(width, height);
        ImageView bitmapView = new ImageView(this);
        bitmapView.setImageBitmap(bitmap);
        bitmapView.setBackgroundResource(R.drawable.checkerboard_pattern);
        bitmapLayout.setOrientation(LinearLayoutCompat.HORIZONTAL);
        bitmapLayout.addView(textView);
        bitmapLayout.addView(bitmapView, bitmapParams);
        linearLayout.addView(bitmapLayout, params);
    }

    private void addTextToLayout(CharSequence text, int typeface, int paddingLeft,
                                 LinearLayoutCompat layout) {
        TextView textView = new TextView(this);
        ParagraphStyle style_para = new LeadingMarginSpan.Standard(0,
                (int) (STANDARD_INDENT_SIZE_IN_DIP * density));
        SpannableString styledText = new SpannableString(text);
        styledText.setSpan(style_para, 0, styledText.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        textView.setText(styledText);
        textView.setTextAppearance(this, R.style.TextFlags);
        textView.setTypeface(null, typeface);
        textView.setTextIsSelectable(true);
        LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
        params.setMargins((int) (paddingLeft * density), 0, 0, 0);
        layout.addView(textView, params);
    }

    private void addTextToLayout(CharSequence text, int typeface, LinearLayoutCompat layout) {
        addTextToLayout(text, typeface, 0, layout);
    }

    private void addHtmlToLayout(StringBuilder html, LinearLayoutCompat layout) {
        addTextToLayout(Html.fromHtml(html.toString()), Typeface.NORMAL,layout);
    }

    private void setupVariables() {
        edAction = findViewById(R.id.action_edit);
        lblData = findViewById(R.id.lbl_data);
        edData = findViewById(R.id.data_edit);
        edType = findViewById(R.id.type_edit);
        edUri = findViewById(R.id.uri_edit);

        lblResult = findViewById(R.id.lbl_result);
        edResult = findViewById(R.id.result_edit);
        lblCallingActivity = findViewById(R.id.lbl_calling_activity);
        edCallingActivity = findViewById(R.id.calling_activity_edit);

        mHistory = new HistoryEditText(this, new int[]{
                R.id.cmd_edit_history,
                R.id.cmd_data_history,
                R.id.cmd_type_history,
                R.id.cmd_uri_history},
                edAction,
                edData,
                edType,
                edUri);

        categoriesHeader = findViewById(R.id.intent_categories_header);
        categoriesLayout = findViewById(R.id.intent_categories_layout);
        flagsLayout = findViewById(R.id.intent_flags_layout);
        extrasLayout = findViewById(R.id.intent_extras_layout);
        activitiesHeader = findViewById(R.id.intent_matching_activities_header);
        activitiesLayout = findViewById(R.id.intent_matching_activities_layout);
        resendIntentButton = findViewById(R.id.resend_intent_button);
        resendIntentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCmdResendIntent(v);
            }
        });

        resetIntentButton = findViewById(R.id.reset_intent_button);
        resetIntentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResetIntent(v);
            }
        });

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        density = metrics.density;
    }

    private void setupTextWatchers() {
        edAction.addTextChangedListener(new IntentUpdateTextWatcher(edAction) {
            @Override
            protected void onUpdateIntent(String modifiedContent) {
                editableIntent.setAction(modifiedContent);
            }
        });
        edData.addTextChangedListener(new IntentUpdateTextWatcher(edData) {
            @Override
            protected void onUpdateIntent(String modifiedContent) {
                // setData clears type so we save it
                String savedType = editableIntent.getType();
                editableIntent.setDataAndType(Uri.parse(modifiedContent), savedType);
            }
        });
        edType.addTextChangedListener(new IntentUpdateTextWatcher(edType) {
            @Override
            protected void onUpdateIntent(String modifiedContent) {
                // setData clears type so we save it
                String dataString = editableIntent.getDataString();
                editableIntent.setDataAndType(Uri.parse(dataString), modifiedContent);
            }
        });
        edUri.addTextChangedListener(new IntentUpdateTextWatcher(edUri) {
            @Override
            protected void onUpdateIntent(String modifiedContent) {
                // no error yet so continue
                editableIntent = IntentHelper.cloneIntent(modifiedContent, additionalExtras);
                // this time must update all content since extras/flags may have been changed
                showAllIntentData(edUri);
            }
        });
        edResult.addTextChangedListener(new IntentUpdateTextWatcher(edResult) {
            @Override
            protected void onUpdateIntent(String modifiedContent) {
                // no error yet so continue
                editableIntent = IntentHelper.cloneIntent(modifiedContent, additionalExtras);
                // this time must update all content since extras/flags may have been changed
                showAllIntentData(edResult);
            }
        });
        edCallingActivity.addTextChangedListener(new IntentUpdateTextWatcher(edCallingActivity) {
            @Override
            protected void onUpdateIntent(String modifiedContent) {
                // no error yet so continue
                editableIntent = IntentHelper.cloneIntent(modifiedContent, additionalExtras);
                // this time must update all content since extras/flags may have been changed
                showAllIntentData(edCallingActivity);
            }
        });
    }

    private void showResetIntentButton(boolean visible) {
        resendIntentButton.setText(R.string.button_title_send_edited_intent);
        resetIntentButton.setVisibility((visible) ? View.VISIBLE : View.GONE);
    }

    private void onCmdResendIntent(View v) {
        try {
            Intent startIntent = IntentHelper.cloneIntent(IntentHelper.getUri(editableIntent), additionalExtras);
            startIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivityForResult(Intent.createChooser(startIntent, resendIntentButton.getText()), REQUEST_CODE_RESEND);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void onCmdSettings() {
        try {
            Intent startIntent = new Intent(this, SettingsActivity.class);

            startActivityForResult(startIntent,REQUEST_CODE_SETTINGS);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void onResetIntent(View v) {
        // this would break onActivityResult
        // startActivity(this.originalIntent); // reload this with original data
        // finish();
        textWatchersActive = false;
        showInitialIntent(false);
        textWatchersActive = true;

        refreshUI();
    }

    private void onCmdCopyIntentDetails() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        String intentDetailsString = createIntentFormatter().getIntentDetailsString(editableIntent,
                IntentHelper.getLastCallingActivity(this),
                lastResultCode, lastResultIntent);
        clipboard.setPrimaryClip(ClipData.newPlainText("Intent Details",intentDetailsString));
        Toast.makeText(this, R.string.message_intent_details_copied_to_clipboard,
                Toast.LENGTH_SHORT).show();
    }

    @NonNull
    private IntentFormatter createIntentFormatter() {
        Settings settings = new Settings(this);
        return new IntentFormatterMD(this, settings.reportWithMatchingActivities() , "## ");
    }

    private void refreshUI() {
        activitiesLayout.removeAllViews();
        addHtmlToLayout(guiFormatter.clr().appendMatchingActivities(editableIntent, false), activitiesLayout);

        if (shareActionProvider != null) {
            Intent share = createShareIntent();
            shareActionProvider.setShareIntent(share);
        }
        showTextViewIntentData(null);

    }

    private Intent createShareIntent() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType(getString(R.string.mime_type_text_plain));
        String intentDetailsString = createIntentFormatter().getIntentDetailsString(editableIntent,
                IntentHelper.getLastCallingActivity(this),
                lastResultCode, lastResultIntent);
        share.putExtra(Intent.EXTRA_TEXT, intentDetailsString);
        return share;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem actionItem = menu.findItem(R.id.menu_share);

        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(actionItem);

        if (shareActionProvider == null) {
            shareActionProvider = new ShareActionProvider(this);
            MenuItemCompat.setActionProvider(actionItem, shareActionProvider);
        }

        shareActionProvider
                .setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        refreshUI();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_copy) {
            onCmdCopyIntentDetails();
        } else if (item.getItemId() == R.id.menu_settings) {
            onCmdSettings();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        textWatchersActive = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0); // inhibit new activity animation when
        // resetting intent details
        textWatchersActive = true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(INTENT_EDITED,
                resetIntentButton.getVisibility() == View.VISIBLE);
        if (mHistory != null) mHistory.saveHistory();
    }

    // support for onActivityResult
    // OriginatorActivity -> IntentIntercept -> resendIntentActivity
    // Forward result of sub-activity {resendIntentActivity}
    // to caller of this activity {OriginatorActivity}.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.lastResultCode = resultCode;
        this.lastResultIntent = data;
        if (requestCode == REQUEST_CODE_SETTINGS) {
            super.onActivityResult(requestCode, resultCode, data);
            refreshUI();
        } else {
            // reply from resend
            super.onActivityResult(requestCode, resultCode, data);
            setResult(resultCode, data);

            refreshUI();

            Uri uri = (data == null) ? null : data.getData();
            Toast.makeText(IntentDetailsActivity.this,
                    getString(R.string.last_result_message, getString(R.string.last_result_header_title), "" + requestCode, uri),
                    Toast.LENGTH_LONG).show();
        }
    }

}