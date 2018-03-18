package de.symeda.sormas.app.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import de.symeda.sormas.app.R;
import de.symeda.sormas.app.core.INotificationContext;
import de.symeda.sormas.app.core.notification.NotificationType;
import de.symeda.sormas.app.core.VibrationHelper;
import de.symeda.sormas.app.core.notification.NotificationHelper;

/**
 * Created by Orson on 28/01/2018.
 * <p>
 * www.technologyboard.org
 * sampson.orson@gmail.com
 * sampson.orson@technologyboard.org
 */

public abstract class EditTeboPropertyField<T> extends TeboPropertyField<T> {

    private VisualState state;
    private INotificationContext communicator;
    private boolean errorState;
    private String errorMessage;
    private OnInputErrorListener onInputErrorListener;
    private OnShowInputErrorListener onShowInputErrorListener;
    private OnHideInputErrorListener onHideInputErrorListener;

    public EditTeboPropertyField(Context context) {
        super(context);
    }

    public EditTeboPropertyField(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTeboPropertyField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setVisualState(VisualState state) {
        if (state == VisualState.ERROR)
            return;

        this.state = state;
        changeVisualState(state);
    }

    public VisualState getVisualState() {
        return this.state;
    }

    public boolean inErrorState() {
        return errorState;
    }



    public void enableErrorState(INotificationContext communicator, int messageResId) {
        if (messageResId <= 0)
            return;

        String message  = getResources().getString(messageResId);

        this.communicator = communicator;
        this.errorState = true;
        this.errorMessage = message;
        //this.txtControlInput.setError(this.errorMessage);

        if (this.onInputErrorListener != null)
            this.onInputErrorListener.onInputErrorChange(this, this.errorMessage, errorState);

    }

    public void enableErrorState(INotificationContext communicator, String message) {
        this.communicator = communicator;
        this.errorState = true;
        this.errorMessage = message;
        //this.txtControlInput.setError(this.errorMessage);

        if (this.onInputErrorListener != null)
            this.onInputErrorListener.onInputErrorChange(this, this.errorMessage, errorState);

    }


    /*public void enableErrorState(int messageResId) {
        if (messageResId <= 0)
            return;

        String message  = getResources().getString(messageResId);

        this.errorState = true;
        this.errorMessage = message;
        //this.txtControlInput.setError(this.errorMessage);


        if (this.onInputErrorListener != null)
            this.onInputErrorListener.onInputErrorChange(this, this.errorMessage, errorState);

    }

    public void enableErrorState(String message) {
        this.errorState = true;
        this.errorMessage = message;
        //this.txtControlInput.setError(this.errorMessage);


        if (this.onInputErrorListener != null)
            this.onInputErrorListener.onInputErrorChange(this, this.errorMessage, errorState);

    }*/

    /*public void disableErrorState() {
        this.errorState = false;
        this.errorMessage = "";
        //this.txtControlInput.setError(null);

        if (this.onInputErrorListener != null)
            this.onInputErrorListener.onInputErrorChange(this, this.errorMessage, errorState);

    }*/

    public void disableErrorState(INotificationContext communicator) {
        this.communicator = communicator;
        this.errorState = false;
        this.errorMessage = "";
        //this.txtControlInput.setError(null);


        if (this.onInputErrorListener != null)
            this.onInputErrorListener.onInputErrorChange(this, this.errorMessage, errorState);

    }

    protected void showNotification() {
        if (!errorState || communicator == null || errorMessage == null || errorMessage.isEmpty())
            return;

        NotificationHelper.showNotification(communicator, NotificationType.ERROR, errorMessage);

        if (onShowInputErrorListener != null) {
            onShowInputErrorListener.onShowInputErrorShowing(this, errorMessage, errorState);
        }
    }

    protected void hideNotification() {
        if (communicator == null)
            return;

        NotificationHelper.hideNotification(communicator);

        if (onHideInputErrorListener != null) {
            onHideInputErrorListener.onInputErrorHiding(this, errorState);
        }
    }

    private void setOnInputErrorListener(OnInputErrorListener listener) {
        this.onInputErrorListener = listener;
    }

    public void setOnShowInputErrorListener(OnShowInputErrorListener listener) {
        this.onShowInputErrorListener = listener;
    }

    public void setOnHideInputErrorListener(OnHideInputErrorListener listener) {
        this.onHideInputErrorListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setOnInputErrorListener(new OnInputErrorListener() {
            @Override
            public void onInputErrorChange(View v, String message, boolean errorState) {
                if(!v.isEnabled())
                    return;

                if (errorState) {
                    VibrationHelper.onInputFieldError();
                    changeVisualState(VisualState.ERROR);

                    showNotification();
                } else if(v.isFocused()) {
                    changeVisualState(VisualState.FOCUSED);
                    hideNotification();
                } else {
                    changeVisualState(VisualState.NORMAL);
                    hideNotification();
                }
            }
        });
    }

    public abstract void changeVisualState(VisualState state);

    @Override
    public int getCaptionColor() {
        return getResources().getColor(R.color.controlTextColor);
    }
}