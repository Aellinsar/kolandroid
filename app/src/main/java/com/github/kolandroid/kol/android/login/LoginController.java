package com.github.kolandroid.kol.android.login;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.github.kolandroid.kol.android.R;
import com.github.kolandroid.kol.android.chat.ChatService;
import com.github.kolandroid.kol.android.controller.LinkedModelController;
import com.github.kolandroid.kol.android.screen.Screen;
import com.github.kolandroid.kol.android.screen.ScreenSelection;
import com.github.kolandroid.kol.gamehandler.SettingsContext;
import com.github.kolandroid.kol.model.models.login.LoginModel;
import com.github.kolandroid.kol.model.models.login.LoginStatus;
import com.github.kolandroid.kol.model.models.login.PasswordHash;

import java.util.ArrayList;

public class LoginController extends LinkedModelController<LoginStatus, LoginModel> {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -1263867061071715065L;
    private String savedUser = null;
    private PasswordHash savedPass = null;
    private boolean enterChatImmediately = true;

    public LoginController(LoginModel model) {
        super(model);
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

        final SettingsContext settings = host.getViewContext().getSettingsContext();
        user.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String[] passwordHash = settings.get("login_defaultPassword", ":").split(":");
                if (passwordHash.length == 2 && user.getText().toString().equalsIgnoreCase(passwordHash[0])) {
                    pass.setHint("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022"); //unicode dot x10
                } else {
                    pass.setHint("Password");
                }
            }
        });

        user.setText(settings.get("login_defaultUsername", ""));

        String[] passwordHash = settings.get("login_defaultPassword", ":").split(":");
        savedUser = passwordHash[0];
        savedPass = new PasswordHash(passwordHash[1], true);

        checkpass.setChecked(settings.get("login_savePassword", true));
        checkchat.setChecked(settings.get("login_enterChat", true));


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

                getModel().login(username, pass);

                settings.set("login_savePassword", checkpass.isChecked());
                settings.set("login_enterChat", checkchat.isChecked());
                enterChatImmediately = checkchat.isChecked();

                if (checkpass.isChecked()) {
                    settings.set("login_defaultUsername", username);
                    settings.set("login_defaultPassword", username + ":" + pass.getBaseHash());
                } else {
                    settings.remove("login_defaultUsername");
                    settings.remove("login_defaultPassword");
                }
            }
        });

        Button createAccount = (Button) view.findViewById(R.id.login_newaccount);
        createAccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getModel().createAccount();
            }
        });

        ArrayList<LoginModel.MagicLoginAction> magicActions = model.getMagicCharacters();
        if (magicActions.size() > 0) {
            View panel = view.findViewById(R.id.login_magicpanel);
            panel.setVisibility(View.VISIBLE);

            ViewGroup container = (ViewGroup) view.findViewById(R.id.login_magiccontainer);
            for (LoginModel.MagicLoginAction magicAction : magicActions) {
                final LoginModel.MagicLoginAction action = magicAction;
                Button magicButton = new Button(host.getActivity());
                magicButton.setText(action.getCharacter());
                magicButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        action.magicLogin();
                    }
                });
                container.addView(magicButton);
            }
        }

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
