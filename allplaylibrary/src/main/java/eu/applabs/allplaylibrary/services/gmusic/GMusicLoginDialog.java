package eu.applabs.allplaylibrary.services.gmusic;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import eu.applabs.allplaylibrary.R;

public class GMusicLoginDialog extends Dialog implements View.OnClickListener {

    private Context mContext = null;

    public static final String TAG = Dialog.class.getSimpleName();
    public static final String ACTION_LOGIN_COMPLETED = TAG + "::ActionLoginCompleted";
    public static final String EXTRA_USER = "User";
    public static final String EXTRA_PASSWORD = "Password";

    private EditText mUser = null;
    private EditText mPassword = null;

    public GMusicLoginDialog(Context context) {
        super(context);
        mContext = context;
        setContentView(R.layout.dialog_login);

        Button btn = (Button) findViewById(R.id.id_dialog_login_btn_ok);
        btn.setOnClickListener(this);

        btn = (Button) findViewById(R.id.id_dialog_login_btn_cancel);
        btn.setOnClickListener(this);

        mUser = (EditText) findViewById(R.id.id_dialog_login_label_user);
        mPassword = (EditText) findViewById(R.id.id_dialog_login_label_password);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.id_dialog_login_btn_ok) {
            Intent intent = new Intent();
            intent.setAction(ACTION_LOGIN_COMPLETED);
            Bundle extras = new Bundle();
            extras.putString(EXTRA_USER, mUser.getText().toString());
            extras.putString(EXTRA_PASSWORD, mPassword.getText().toString());
            intent.putExtras(extras);

            mContext.sendBroadcast(intent);
            this.dismiss();
            return;
        } else if(id == R.id.id_dialog_login_btn_cancel) {
            this.dismiss();
            return;
        }
    }
}
