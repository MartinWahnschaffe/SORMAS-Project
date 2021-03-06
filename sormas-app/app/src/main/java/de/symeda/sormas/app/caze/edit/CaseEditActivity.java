/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.symeda.sormas.app.caze.edit;

import android.content.Context;
import android.os.AsyncTask;
import android.view.Menu;

import java.util.Date;
import java.util.List;

import de.symeda.sormas.api.DiseaseHelper;
import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.ValidationException;
import de.symeda.sormas.app.BaseActivity;
import de.symeda.sormas.app.BaseEditActivity;
import de.symeda.sormas.app.BaseEditFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.common.DaoException;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.caze.CaseSection;
import de.symeda.sormas.app.component.menu.PageMenuItem;
import de.symeda.sormas.app.component.validation.FragmentValidator;
import de.symeda.sormas.app.contact.edit.ContactNewActivity;
import de.symeda.sormas.app.core.async.AsyncTaskResult;
import de.symeda.sormas.app.core.async.SavingAsyncTask;
import de.symeda.sormas.app.core.async.TaskResultHolder;
import de.symeda.sormas.app.core.notification.NotificationHelper;
import de.symeda.sormas.app.person.edit.PersonEditFragment;
import de.symeda.sormas.app.sample.edit.SampleNewActivity;
import de.symeda.sormas.app.symptoms.SymptomsEditFragment;
import de.symeda.sormas.app.task.edit.TaskNewActivity;
import de.symeda.sormas.app.util.Bundler;
import de.symeda.sormas.app.util.Consumer;

import static de.symeda.sormas.app.core.notification.NotificationType.ERROR;
import static de.symeda.sormas.app.core.notification.NotificationType.WARNING;

public class CaseEditActivity extends BaseEditActivity<Case> {

    public static final String TAG = CaseEditActivity.class.getSimpleName();

    private AsyncTask saveTask;

    public static void startActivity(Context context, String recordUuid, CaseSection section) {
        BaseActivity.startActivity(context, CaseEditActivity.class, buildBundle(recordUuid, section));
    }

    public static Bundler buildBundle(String recordUuid, CaseSection section) {
        return BaseEditActivity.buildBundle(recordUuid, section.ordinal());
    }


    @Override
    protected Case queryRootEntity(String recordUuid) {
        return DatabaseHelper.getCaseDao().queryUuidWithEmbedded(recordUuid);
    }

    @Override
    protected Case buildRootEntity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CaseClassification getPageStatus() {
        return getStoredRootEntity() == null ? null : getStoredRootEntity().getCaseClassification();
    }

    @Override
    public List<PageMenuItem> getPageMenuData() {
        List<PageMenuItem> menuItems = PageMenuItem.fromEnum(CaseSection.values(), getContext());
        Case caze = getStoredRootEntity();
        if (caze != null && !DiseaseHelper.hasContactFollowUp(caze.getDisease(), caze.getPlagueType())) {
            menuItems.remove(CaseSection.CONTACTS.ordinal());
        }
        return menuItems;
    }

    @Override
    protected BaseEditFragment buildEditFragment(PageMenuItem menuItem, Case activityRootData) {
        CaseSection section = CaseSection.fromOrdinal(menuItem.getKey());
        BaseEditFragment fragment;
        switch (section) {

            case CASE_INFO:
                fragment = CaseEditFragment.newInstance(activityRootData);
                break;
            case PERSON_INFO:
                fragment = PersonEditFragment.newInstance(activityRootData);
                break;
            case HOSPITALIZATION:
                fragment = CaseEditHospitalizationFragment.newInstance(activityRootData);
                break;
            case SYMPTOMS:
                fragment = SymptomsEditFragment.newInstance(activityRootData);
                break;
            case EPIDEMIOLOGICAL_DATA:
                fragment = CaseEditEpidemiologicalDataFragment.newInstance(activityRootData);
                break;
            case CONTACTS:
                fragment = CaseEditContactListFragment.newInstance(activityRootData);
                break;
            case SAMPLES:
                fragment = CaseEditSampleListFragment.newInstance(activityRootData);
                break;
            case TASKS:
                fragment = CaseEditTaskListFragment.newInstance(activityRootData);
                break;
            default:
                throw new IndexOutOfBoundsException(DataHelper.toStringNullable(section));
        }

        return fragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getSaveMenu().setTitle(R.string.action_save_case);

        return true;
    }

    @Override
    protected int getActivityTitle() {
        return R.string.heading_case_edit;
    }

    @Override
    public void saveData() {
        saveData(new Consumer<Case>() {
            @Override
            public void accept(Case parameter) {
                goToNextPage();
            }
        });
    }

    public void saveData(final Consumer<Case> successCallback) {

        if (saveTask != null) {
            NotificationHelper.showNotification(this, WARNING, getString(R.string.message_already_saving));
            return; // don't save multiple times
        }

        final Case changedCase = getStoredRootEntity();

        try {
            FragmentValidator.validate(getContext(), getActiveFragment().getContentBinding());
        } catch (ValidationException e) {
            NotificationHelper.showNotification(this, ERROR, e.getMessage());
            return;
        }

        saveTask = new SavingAsyncTask(getRootView(), changedCase) {
            @Override
            protected void onPreExecute() {
                showPreloader();
            }

            @Override
            protected void doInBackground(TaskResultHolder resultHolder) throws DaoException {
                synchronized (CaseEditActivity.this) {
                    DatabaseHelper.getPersonDao().saveAndSnapshot(changedCase.getPerson());
                    DatabaseHelper.getCaseDao().saveAndSnapshot(changedCase);
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<TaskResultHolder> taskResult) {
                hidePreloader();
                super.onPostExecute(taskResult);
                if (taskResult.getResultStatus().isSuccess()) {
                    successCallback.accept(changedCase);
                } else {
                    onResume(); // reload data
                }
                saveTask = null;
            }
        }.executeOnThreadPool();
    }

    @Override
    public void goToNewView() {
        CaseSection activeSection = CaseSection.fromOrdinal(getActivePage().getKey());

        if (activeSection == CaseSection.CONTACTS) {
            ContactNewActivity.startActivity(getContext(), getRootUuid());
        } else if (activeSection == CaseSection.SAMPLES) {
            SampleNewActivity.startActivity(getContext(), getRootUuid());
        } else if (activeSection == CaseSection.TASKS) {
            TaskNewActivity.startActivityFromCase(getContext(), getRootUuid());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (saveTask != null && !saveTask.isCancelled())
            saveTask.cancel(true);
    }

}