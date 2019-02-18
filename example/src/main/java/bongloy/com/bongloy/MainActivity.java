package bongloy.com.bongloy;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.stripe.android.Bongloy;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import com.cooltechworks.creditcarddesign.pager.CardFragmentAdapter;
import com.cooltechworks.creditcarddesign.pager.CardFragmentAdapter.ICardEntryCompleteListener;

import com.cooltechworks.creditcarddesign.CardSelector;
import com.cooltechworks.creditcarddesign.CreditCardView;
import com.cooltechworks.creditcarddesign.CreditCardUtils;
import com.cooltechworks.creditcarddesign.pager.IFocus;

import java.util.HashMap;
import java.util.Map;

import bongloy.com.bongloy.controller.ErrorDialogHandler;
import bongloy.com.bongloy.controller.ProgressDialogController;
import bongloy.com.bongloy.service.BongloyService;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.cooltechworks.creditcarddesign.CreditCardUtils.CARD_NAME_PAGE;
import static com.cooltechworks.creditcarddesign.CreditCardUtils.EXTRA_CARD_CVV;
import static com.cooltechworks.creditcarddesign.CreditCardUtils.EXTRA_CARD_EXPIRY;
import static com.cooltechworks.creditcarddesign.CreditCardUtils.EXTRA_CARD_HOLDER_NAME;
import static com.cooltechworks.creditcarddesign.CreditCardUtils.EXTRA_CARD_NUMBER;
import static com.cooltechworks.creditcarddesign.CreditCardUtils.EXTRA_ENTRY_START_PAGE;

/**
 * Created by glarencezhao on 10/23/16.
 */

public class MainActivity extends AppCompatActivity implements IFocus {

    int mLastPageSelected = 0;
    private CreditCardView mCreditCardView;

    private String mCardNumber;
    private String mCVV;
    private String mCardHolderName;
    private String mExpiry;
    private int mStartPage = 0;
    private CardFragmentAdapter mCardAdapter;
    private Context mContext;

    private CompositeSubscription mCompositeSubscription;

    private ErrorDialogHandler mErrorDialogHandler;
    private ProgressDialogController mProgressDialogController;

    private static final String PUBLISHABLE_KEY =
            "pk_test_56b6630429c92062953adac23cd7420386e55ce60eb465cd0ad3ab226c09eb6a";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        PaymentConfiguration.init(PUBLISHABLE_KEY);

        Button buyButton = findViewById(R.id.btnBuy);

        mCompositeSubscription = new CompositeSubscription();

        setKeyboardVisibility(true);
        mCreditCardView = (CreditCardView) findViewById(R.id.credit_card_view);
        Bundle args = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        loadPager(args);
        checkParams(args);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        mProgressDialogController = new ProgressDialogController(fragmentManager);

        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createToken();
            }
        });
    }

    private void createToken(){

        mProgressDialogController.startProgress();

        Card cardParam = new Card(mCardNumber, Integer.parseInt(splitEx(mExpiry)[0]), Integer.parseInt(splitEx(mExpiry)[1]), mCVV);
        new Bongloy(mContext).createToken(
                cardParam,
                PaymentConfiguration.getInstance().getPublishableKey(),
                new TokenCallback() {
                    public void onSuccess(Token token) {
                        completePurchase(token.getId(), amount());
                    }
                    public void onError(Exception error) {
                        mErrorDialogHandler.showError(error.getLocalizedMessage());
                    }
                });
    }

    private String[] splitEx(String expiry){
        return expiry.split("/");
    }

    private String amount(){
        EditText amountxt = findViewById(R.id.amount);
        return String.valueOf(amountxt.getText());
    }


    ViewPager getViewPager() {
        return (ViewPager) findViewById(R.id.card_field_container_pager);
    }

    private void checkParams(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        mCardHolderName = bundle.getString(EXTRA_CARD_HOLDER_NAME);
        mCVV = bundle.getString(EXTRA_CARD_CVV);
        mExpiry = bundle.getString(EXTRA_CARD_EXPIRY);
        mCardNumber = bundle.getString(EXTRA_CARD_NUMBER);
        mStartPage = bundle.getInt(EXTRA_ENTRY_START_PAGE);

        final int maxCvvLength = CardSelector.selectCard(mCardNumber).getCvvLength();
        if (mCVV != null && mCVV.length() > maxCvvLength) {
            mCVV = mCVV.substring(0, maxCvvLength);
        }

        mCreditCardView.setCVV(mCVV);
        mCreditCardView.setCardHolderName(mCardHolderName);
        mCreditCardView.setCardExpiry(mExpiry);
        mCreditCardView.setCardNumber(mCardNumber);

        if (mCardAdapter != null) {
            mCreditCardView.post(new Runnable() {
                @Override
                public void run() {
                    mCardAdapter.setMaxCVV(maxCvvLength);
                    mCardAdapter.notifyDataSetChanged();
                }});
        }

        int cardSide =  bundle.getInt(CreditCardUtils.EXTRA_CARD_SHOW_CARD_SIDE, CreditCardUtils.CARD_SIDE_FRONT);
        if (cardSide == CreditCardUtils.CARD_SIDE_BACK) {
            mCreditCardView.showBack();
        }
        if (mStartPage > 0 && mStartPage <= CARD_NAME_PAGE) {
            getViewPager().setCurrentItem(mStartPage);
        }
    }

    public void loadPager(Bundle bundle) {
        ViewPager pager = getViewPager();
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

                mCardAdapter.focus(position);

                if ((mCreditCardView.getCardType() != CreditCardUtils.CardType.AMEX_CARD) && (position == 2)) {
                    mCreditCardView.showBack();
                } else if (((position == 1) || (position == 3)) && (mLastPageSelected == 2) && (mCreditCardView.getCardType() != CreditCardUtils.CardType.AMEX_CARD)) {
                    mCreditCardView.showFront();
                }

                mLastPageSelected = position;

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        pager.setOffscreenPageLimit(4);

        mCardAdapter = new CardFragmentAdapter(getSupportFragmentManager(), bundle);
        mCardAdapter.setOnCardEntryCompleteListener(new ICardEntryCompleteListener() {
            @Override
            public void onCardEntryComplete(int currentIndex) {
                showNext();
            }

            @Override
            public void onCardEntryEdit(int currentIndex, String entryValue) {
                if(TextUtils.isEmpty(entryValue) && currentIndex != 0){
                    showPrevious();
                }

                switch (currentIndex) {
                    case 0:
                        mCardNumber = entryValue.replace(CreditCardUtils.SPACE_SEPERATOR, "");
                        mCreditCardView.setCardNumber(mCardNumber);
                        if (mCardAdapter != null) {
                            mCardAdapter.setMaxCVV(CardSelector.selectCard(mCardNumber).getCvvLength());
                        }
                        break;
                    case 1:
                        mExpiry = entryValue;
                        mCreditCardView.setCardExpiry(entryValue);
                        break;
                    case 2:
                        mCVV = entryValue;
                        mCreditCardView.setCVV(entryValue);

                        break;
                    case 3:
                        mCardHolderName = entryValue;
                        mCreditCardView.setCardHolderName(entryValue);
                        break;
                }
            }
        });

        pager.setAdapter(mCardAdapter);
    }

    private void setKeyboardVisibility(boolean visible) {
        final EditText editText = (EditText) findViewById(R.id.card_number_field);

        if (!visible) {

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public void showNext() {
        final ViewPager pager = (ViewPager) findViewById(R.id.card_field_container_pager);
        CardFragmentAdapter adapter = (CardFragmentAdapter) pager.getAdapter();

        int max = adapter.getCount();
        int currentIndex = pager.getCurrentItem();
        if (currentIndex + 1 < max) {
            pager.setCurrentItem(currentIndex + 1);
        } else {
            // completed the card entry.
            setKeyboardVisibility(false);
        }

    }

    public void showPrevious() {
        final ViewPager pager = (ViewPager) findViewById(R.id.card_field_container_pager);
        int currentIndex = pager.getCurrentItem();

        if (currentIndex == 0) {
            setResult(RESULT_CANCELED);
            finish();
        }

        if (currentIndex - 1 >= 0) {
            pager.setCurrentItem(currentIndex - 1);
        }

    }

    @Override
    public void focus() {

    }

    private Map<String, Object> createParams(long price, String token){
        Map<String, Object> params = new HashMap<>();
        params.put("amount", Long.toString(price));
        params.put("token", token);
        params.put("currency", "USD");
        return params;
    }

    private void completePurchase(String sourceId, String amount) {
        Retrofit retrofit = RetrofitFactory.getInstance();
        BongloyService bongloyService = retrofit.create(BongloyService.class);
        long price = Long.parseLong(amount);

        Observable<Void> stripeResponse = bongloyService.createQueryCharge(createParams(price, sourceId));
        mCompositeSubscription.add(stripeResponse
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(
                        new Action0() {
                            @Override
                            public void call() {
                                mProgressDialogController.finishProgress();

                                new SweetAlertDialog(mContext, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Success")
                                        .setContentText("Payment successful")
                                        .show();
                            }

                        })
                .subscribe(
                        new Action1<Void>() {
                            @Override
                            public void call(Void aVoid) {

                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                displayError(throwable.getLocalizedMessage());
                            }
                        }));
    }

    private void displayError(String errorMessage) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage(errorMessage);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}