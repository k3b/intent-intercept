intentintercept SettingsActivity SEND text/plain 2026-04-19 12:56
## **intentintercept SettingsActivity SEND text/plain 2026-04-19 12:56**
 `intent:#Intent;
action=android.intent.action.SEND;
type=text/plain;
launchFlags=0x1b080000;
S.android.support.v4.app.EXTRA_CALLING_PACKAGE=de.k3b.android.intentintercept;
S.android.intent.extra.TEXT=Test%20Intent;
S.androidx.core.app.EXTRA_CALLING_PACKAGE=de.k3b.android.intentintercept;
end


intent:#Intent;
action=android.intent.action.SEND;
type=text/plain;
launchFlags=0x1b080000;
S.android.support.v4.app.EXTRA_CALLING_PACKAGE=de.k3b.android.intentintercept;
S.android.intent.extra.TEXT=Test Intent;
S.androidx.core.app.EXTRA_CALLING_PACKAGE=de.k3b.android.intentintercept;
end
 `

------------
* **Calling Activity:** de.k3b.android.intentintercept/de.k3b.intentintercept.SettingsActivity
* **ACTION:** android.intent.action.SEND
* **MIME:** text/plain
* **URI:** intent:#Intent;
action=android.intent.action.SEND;
type=text/plain;
launchFlags=0x1b080000;
S.android.support.v4.app.EXTRA_CALLING_PACKAGE=de.k3b.android.intentintercept;
S.android.intent.extra.TEXT=Test%20Intent;
S.androidx.core.app.EXTRA_CALLING_PACKAGE=de.k3b.android.intentintercept;
end

* **URI:** intent:#Intent;
action=android.intent.action.SEND;
type=text/plain;
launchFlags=0x1b080000;
S.android.support.v4.app.EXTRA_CALLING_PACKAGE=de.k3b.android.intentintercept;
S.android.intent.extra.TEXT=Test Intent;
S.androidx.core.app.EXTRA_CALLING_PACKAGE=de.k3b.android.intentintercept;
end

## **FLAGS:**
FLAG_ACTIVITY_MULTIPLE_TASK
FLAG_ACTIVITY_PREVIOUS_IS_TOP
FLAG_ACTIVITY_NEW_TASK
FLAG_ACTIVITY_FORWARD_RESULT
FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
## **EXTRAS:**
* **1** Typ: java.lang.String
Key: android.support.v4.app.EXTRA_CALLING_PACKAGE
Wert: de.k3b.android.intentintercept
* **2** Typ: android.content.ComponentName
Key: android.support.v4.app.EXTRA_CALLING_ACTIVITY
Wert: ComponentInfo{de.k3b.android.intentintercept/de.k3b.intentintercept.SettingsActivity}
* **3** Typ: java.lang.String
Key: android.intent.extra.TEXT
Wert: Test Intent
* **4** Typ: java.lang.String
Key: androidx.core.app.EXTRA_CALLING_PACKAGE
Wert: de.k3b.android.intentintercept
* **5** Typ: android.content.ComponentName
Key: androidx.core.app.EXTRA_CALLING_ACTIVITY
Wert: ComponentInfo{de.k3b.android.intentintercept/de.k3b.intentintercept.SettingsActivity}

------------

Created with [Intent Intercept](https://github.com/k3b/intent-intercept) version 4.1.0(408) on 2026-04-19 12:56
