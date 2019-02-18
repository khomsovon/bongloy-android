package bongloy.com.bongloy.controller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.stripe.android.Bongloy;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import java.util.HashMap;
import java.util.Map;

import bongloy.com.bongloy.RetrofitFactory;
import bongloy.com.bongloy.service.BongloyService;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

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
