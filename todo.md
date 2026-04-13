todo.md
	v layout+= result + calling_activity
	explodegui
		v show result: lastResultCode + lastResultIntent
		v calling_activity: lastCallingActivityString
		v calculate lastCallingActivityString
        v api umstellen auf ComponentName
        v settings dialog with
            v Version info
        v Refactored: Separated static IntentHelper and IntentFormatter from Explode Activity. No functional changes 
        v Eigene Klasse BaseFormatter IntentFormatterMD
		 
---- todo next
    v VersionInfo -> IntentFormatter 
        v created with Intent Intercept#4.0.7(407) on 2026-04-04
    v SendTo
        eventTitle
        android.intent.extra.SUBJECT
        android.intent.extra.TITLE

    v   Activity  close