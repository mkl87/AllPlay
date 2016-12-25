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

    private Context m_Context = null;

    public static final String CLASSNAME = Dialog.class.getSimpleName();
    public static final String ACTION_LOGIN_COMPLETED = CLASSNAME + "::ActionLoginCompleted";
    public static final String EXTRA_USER = "User";
    public static final String EXTRA_PASSWORD = "Password";

    private EditText m_User = null;
    private EditText m_Password = null;

    public GMusicLoginDialog(Context context) {
        super(context);
        m_Context = context;
        setContentView(R.layout.dialog_login);

        Button btn = (Button) findViewById(R.id.id_dialog_login_btn_ok);
        btn.setOnClickListener(this);

        btn = (Button) findViewById(R.id.id_dialog_login_btn_cancel);
        btn.setOnClickListener(this);

        m_User = (EditText) findViewById(R.id.id_dialog_login_label_user);
        m_Password = (EditText) findViewById(R.id.id_dialog_login_label_password);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.id_dialog_login_btn_ok) {
            Intent intent = new Intent();
            intent.setAction(ACTION_LOGIN_COMPLETED);
            Bundle extras = new Bundle();
            extras.putString(EXTRA_USER, m_User.getText().toString());
            extras.putString(EXTRA_PASSWORD, m_Password.getText().toString());
            intent.putExtras(extras);

            m_Context.sendBroadcast(intent);
            this.dismiss();
            return;
        } else if(id == R.id.id_dialog_login_btn_cancel) {
            this.dismiss();
            return;
        }
    }
}
