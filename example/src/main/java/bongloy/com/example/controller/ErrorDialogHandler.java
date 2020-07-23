package bongloy.com.example.controller;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import bongloy.com.example.dialog.ErrorDialogFragment;

/**
 * A convenience class to handle displaying error dialogs.
 */
public class ErrorDialogHandler {

    FragmentManager mFragmentManager;

    public ErrorDialogHandler(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

    public void showError(String errorMessage) {
        DialogFragment fragment = ErrorDialogFragment.newInstance(
                bongloy.com.example.R.string.validationErrors, errorMessage);
        fragment.show(mFragmentManager, "error");
    }
}
