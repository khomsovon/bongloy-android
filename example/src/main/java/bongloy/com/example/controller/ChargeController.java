package bongloy.com.example.controller;

import android.content.Context;
import android.support.annotation.NonNull;

import com.stripe.android.Bongloy;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

public class ChargeController {
    private ErrorDialogHandler mErrorDialogHandler;
    private ProgressDialogController mProgressDialogController;
    private Context mContext;
    private Card mCardParam;

    public ChargeController(
            @NonNull Card cardParam,
            @NonNull Context context,
            @NonNull ErrorDialogHandler errorDialogHandler,
            @NonNull ProgressDialogController progressDialogController
    ){
        mErrorDialogHandler = errorDialogHandler;
        mProgressDialogController = progressDialogController;
        mContext = context;
        mCardParam = cardParam;

        saveCard();
    }

    private void saveCard() {
        if (mCardParam == null) {
            mErrorDialogHandler.showError("Invalid Card Data");
            return;
        }
        mProgressDialogController.startProgress();

        new Bongloy(mContext).createToken(
                mCardParam,
                PaymentConfiguration.getInstance().getPublishableKey(),
                new TokenCallback() {
                    public void onSuccess(Token token) {
                        mProgressDialogController.finishProgress();
                    }
                    public void onError(Exception error) {
                        mErrorDialogHandler.showError(error.getLocalizedMessage());
                        mProgressDialogController.finishProgress();
                    }
                });
    }
}
