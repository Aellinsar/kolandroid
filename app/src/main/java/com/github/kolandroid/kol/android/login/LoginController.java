package com.github.kolandroid.kol.android.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.github.kolandroid.kol.android.R;
import com.github.kolandroid.kol.android.chat.service.ChatService;
import com.github.kolandroid.kol.android.controller.LinkedModelController;
import com.github.kolandroid.kol.android.screen.Screen;
import com.github.kolandroid.kol.android.screen.ScreenSelection;
import com.github.kolandroid.kol.model.models.login.LoginModel;
import com.github.kolandroid.kol.model.models.login.LoginStatus;
import com.github.kolandroid.kol.model.models.login.PasswordHash;

public class LoginController extends LinkedModelController<LoginStatus, LoginModel> {
    protected static final String LOGIN_STORAGE = "KoLLogin";
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -1263867061071715065L;
    private String savedUser = null;
    private PasswordHash savedPass = null;
    private boolean enterChatImmediately = true;


    public LoginController() {
        super(new LoginModel());
    }

    @Override
    public int getView() {
        return R.layout.fragment_login_screen;
    }

    @Override
    public void chooseScreen(ScreenSelection choice) {
        choice.displayExternal(this);
    }

    @Override
    public void connect(View view, LoginModel model, final Screen host) {
        final EditText user = (EditText) view
                .findViewById(R.id.login_username);
        final EditText pass = (EditText) view
                .findViewById(R.id.login_password);
        final Button login = (Button) view
                .findViewById(R.id.login_btnlogin);
        final CheckBox checkpass = (CheckBox) view.findViewById(R.id.login_savepassword);
        final CheckBox checkchat = (CheckBox) view.findViewById(R.id.login_enterchat);

        SharedPreferences settings = host.getActivity().getSharedPreferences(LOGIN_STORAGE, 0);
        user.setText(settings.getString("username", ""));
        checkpass.setChecked(settings.getBoolean("savepass", true));
        checkchat.setChecked(settings.getBoolean("enterchat", true));

        if (settings.contains("password")) {
            pass.setHint("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022"); //unicode dot x10

            savedUser = settings.getString("username", "");
            savedPass = new PasswordHash(settings.getString("password", ""), true);
        }

        pass.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    login.performClick();
                    return true;
                }
                return false;
            }

        });

        login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String username = user.getText().toString();
                String password = pass.getText().toString();

                PasswordHash pass;

                if (username == null || username.length() == 0)
                    return;

                if (savedUser != null && username.equalsIgnoreCase(savedUser) && (password == null || password.length() == 0)) {
                    pass = savedPass;
                } else {
                    if (password == null || password.length() == 0)
                        return;
                    pass = new PasswordHash(password, false);
                }

                InputMethodManager inputManager = (InputMethodManager) host.getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(host.getActivity()
                                .getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                getModel().login(host.getViewContext(), username, pass);


                SharedPreferences settings = host.getActivity().getSharedPreferences(LOGIN_STORAGE, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("savepass", checkpass.isChecked());
                editor.putBoolean("enterchat", checkchat.isChecked());

                enterChatImmediately = checkchat.isChecked();

                if (checkpass.isChecked()) {
                    Log.i("Credentials", "Saving username: " + username);
                    Log.i("Credentials", "Saving password hash: " + pass.getBaseHash());
                    editor.putString("username", username);
                    editor.putString("password", pass.getBaseHash());
                } else {
                    editor.remove("username");
                    editor.remove("password");
                }
                editor.apply();
            }
        });
    }

    public void receiveProgress(View view, LoginModel model, LoginStatus message, Screen host) {
        switch (message) {
            case SUCCESS:
                Toast.makeText(host.getActivity(), "Logged in!!",
                        Toast.LENGTH_SHORT).show();

                Context context = host.getActivity().getApplicationContext();
                Intent i = new Intent(context, ChatService.class);
                i.putExtra("session", getModel().getSession());
                i.putExtra("start", enterChatImmediately);
                context.startService(i);

                break;
            default:
                break;
        }
    }
}
