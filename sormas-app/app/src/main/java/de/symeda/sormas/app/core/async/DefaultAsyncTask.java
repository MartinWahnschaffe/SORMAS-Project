package de.symeda.sormas.app.core.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import de.symeda.sormas.api.utils.ValidationException;
import de.symeda.sormas.app.SormasApplication;
import de.symeda.sormas.app.backend.common.AbstractDomainObject;
import de.symeda.sormas.app.backend.common.DaoException;
import de.symeda.sormas.app.util.ErrorReportingHelper;

public abstract class DefaultAsyncTask extends AsyncTask<Void, Void, AsyncTaskResult<TaskResultHolder>> {

    // for error reporting
    private final WeakReference<SormasApplication> applicationReference;
    private final Class entityClass;
    private final String entityUuid;

    private ITaskResultCallback resultCallback;

    public DefaultAsyncTask(Context context) {
        this(context, null);
    }

    public DefaultAsyncTask(Context context, AbstractDomainObject relatedEntity) {
        this.applicationReference = new WeakReference<>((SormasApplication) context.getApplicationContext());
        if (relatedEntity != null) {
            entityClass = relatedEntity.getClass();
            entityUuid = relatedEntity.getUuid();
        } else {
            entityClass = null;
            entityUuid = "";
        }
    }

    protected abstract void doInBackground(TaskResultHolder resultHolder) throws Exception;

    @Override
    protected AsyncTaskResult<TaskResultHolder> doInBackground(Void... voids) {

        TaskResultHolder resultHolder = new TaskResultHolder();

        try {
            doInBackground(resultHolder);
            return new AsyncTaskResult<>(resultHolder.getResultStatus(), resultHolder);
        } catch (ValidationException e) {
            return new AsyncTaskResult<>(e);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Override
    protected void onPostExecute(AsyncTaskResult<TaskResultHolder> taskResult) {
        if (resultCallback != null) {
            resultCallback.taskResult(taskResult.getResultStatus(), taskResult.getResult());
        }
    }

    protected AsyncTaskResult handleException(Exception e) {
        Log.e(getClass().getName(), "Error executing an async task", e);
        Log.e(getClass().getName(), "- root cause: ", ErrorReportingHelper.getRootCause(e));

        SormasApplication application = applicationReference.get();
        if (application != null) {
            ErrorReportingHelper.sendCaughtException(application.getDefaultTracker(), e, entityClass, entityUuid, true);
        }

        return new AsyncTaskResult<>(e);
    }

    protected  WeakReference<SormasApplication> getApplicationReference() {
        return applicationReference;
    }

    @Deprecated
    public AsyncTask execute(ITaskResultCallback resultCallback) {
        this.resultCallback = resultCallback;
        return executeOnThreadPool();
    }

    public AsyncTask executeOnThreadPool() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return this;
    }
}