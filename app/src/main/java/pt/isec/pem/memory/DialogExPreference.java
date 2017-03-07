package pt.isec.pem.memory;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

import pt.isec.pem.memory.R;

public class DialogExPreference extends DialogPreference
{
    public DialogExPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public DialogExPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected void onBindDialogView(View view) {
        // load shared preferences
        // init views
        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            // save shared preferences
        }
    }

    private void init(Context context) {
        setPersistent(false);
        setDialogLayoutResource(R.layout.settings_about_dialog);
    }
}