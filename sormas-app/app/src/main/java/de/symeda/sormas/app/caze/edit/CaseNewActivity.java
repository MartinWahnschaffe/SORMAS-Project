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
import android.os.Bundle;
import android.view.Menu;

import java.util.Calendar;

import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.ValidationException;
import de.symeda.sormas.app.BaseEditActivity;
import de.symeda.sormas.app.BaseEditFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.backend.contact.Contact;
import de.symeda.sormas.app.backend.event.EventParticipant;
import de.symeda.sormas.app.backend.person.Person;
import de.symeda.sormas.app.caze.CaseSection;
import de.symeda.sormas.app.component.menu.PageMenuItem;
import de.symeda.sormas.app.component.validation.FragmentValidator;
import de.symeda.sormas.app.core.async.AsyncTaskResult;
import de.symeda.sormas.app.core.async.SavingAsyncTask;
import de.symeda.sormas.app.core.async.TaskResultHolder;
import de.symeda.sormas.app.core.notification.NotificationHelper;
import de.symeda.sormas.app.person.SelectOrCreatePersonDialog;
import de.symeda.sormas.app.util.Bundler;
import de.symeda.sormas.app.util.Consumer;

import static de.symeda.sormas.app.core.notification.NotificationType.ERROR;
import static de.symeda.sormas.app.core.notification.NotificationType.WARNING;

public class CaseNewActivity extends BaseEditActivity<Case> {

    public static final String TAG = CaseNewActivity.class.getSimpleName();

    private AsyncTask saveTask;

    private boolean emptyReportDate;
    private String contactUuid;
    private String eventParticipantUuid;

    public static void startActivity(Context fromActivity) {
        BaseEditActivity.startActivity(fromActivity, CaseNewActivity.class, buildBundle());
    }

    public static void startActivityWithEmptyReportDate(Context fromActivity) {
        BaseEditActivity.startActivity(fromActivity, CaseNewActivity.class, buildBundleWithEmptyReportDate());
    }

    public static void startActivityFromContact(Context fromActivity, String contactUuid) {
        BaseEditActivity.startActivity(fromActivity, CaseNewActivity.class, buildBundleWithContact(contactUuid));
    }

    public static void startActivityFromEventPerson(Context fromActivity, String eventParticipantUuid) {
        BaseEditActivity.startActivity(fromActivity, CaseNewActivity.class, buildBundleWithEventParticipant(eventParticipantUuid));
    }

    public static Bundler buildBundle() {
        return BaseEditActivity.buildBundle(null);
    }

    public static Bundler buildBundleWithEmptyReportDate() {
        return BaseEditActivity.buildBundle(null).setEmptyReportDate(true);
    }

    public static Bundler buildBundleWithContact(String contactUuid) {
        return BaseEditActivity.buildBundle(null).setContactUuid(contactUuid);
    }

    public static Bundler buildBundleWithEventParticipant(String eventParticipantUuid) {
        return BaseEditActivity.buildBundle(null).setEventParticipantUuid(eventParticipantUuid);
    }

    @Override
    protected void onCreateInner(Bundle savedInstanceState) {
        super.onCreateInner(savedInstanceState);
        Bundler bundler = new Bundler(savedInstanceState);
        contactUuid = bundler.getContactUuid();
        eventParticipantUuid = bundler.getEventParticipantUuid();
        emptyReportDate = bundler.getEmptyReportDate();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundler bundler = new Bundler(outState);
        bundler.setContactUuid(contactUuid);
        bundler.setEventParticipantUuid(eventParticipantUuid);
        bundler.setEmptyReportDate(emptyReportDate);
    }

    @Override
    public CaseClassification getPageStatus() {
        return null;
    }

    @Override
    protected Case queryRootEntity(String recordUuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Case buildRootEntity() {
        Person _person;
        Case _case;
        if (!DataHelper.isNullOrEmpty(contactUuid)) {
            Contact sourceContact = DatabaseHelper.getContactDao().queryUuid(contactUuid);
            _person = sourceContact.getPerson();
            _case = DatabaseHelper.getCaseDao().build(_person,
                    DatabaseHelper.getCaseDao().queryUuidBasic(sourceContact.getCaseUuid()));
        } else if (!DataHelper.isNullOrEmpty(eventParticipantUuid)) {
            EventParticipant eventParticipant = DatabaseHelper.getEventParticipantDao().queryUuid(eventParticipantUuid);
            _case = DatabaseHelper.getCaseDao().build(eventParticipant);
        } else {
            _person = DatabaseHelper.getPersonDao().build();
            _case = DatabaseHelper.getCaseDao().build(_person);
        }

        if (emptyReportDate) {
            _case.setReportDate(null);
        }

        return _case;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        getSaveMenu().setTitle(R.string.action_save_case);
        return result;
    }

    @Override
    protected BaseEditFragment buildEditFragment(PageMenuItem menuItem, Case activityRootData) {
        BaseEditFragment fragment;
        if (contactUuid != null && eventParticipantUuid == null) {
            fragment = CaseNewFragment.newInstanceFromContact(activityRootData, contactUuid);
        } else if (eventParticipantUuid != null && contactUuid == null) {
            fragment = CaseNewFragment.newInstanceFromEventParticipant(activityRootData, eventParticipantUuid);
        } else {
            fragment = CaseNewFragment.newInstance(activityRootData);
        }
        fragment.setLiveValidationDisabled(true);
        return fragment;
    }

    @Override
    protected int getActivityTitle() {
        return R.string.heading_case_new;
    }

    @Override
    public void replaceFragment(BaseEditFragment f, boolean allowBackNavigation) {
        super.replaceFragment(f, allowBackNavigation);
        getActiveFragment().setLiveValidationDisabled(true);
    }

    @Override
    public void saveData() {

        if (saveTask != null) {
            NotificationHelper.showNotification(this, WARNING, getString(R.string.message_already_saving));
            return; // don't save multiple times
        }

        final Case caze = getStoredRootEntity();
        CaseNewFragment fragment = (CaseNewFragment) getActiveFragment();

        fragment.setLiveValidationDisabled(false);

        try {
            FragmentValidator.validate(getContext(), fragment.getContentBinding());
        } catch (ValidationException e) {
            NotificationHelper.showNotification(this, ERROR, e.getMessage());
            return;
        }

        if (caze.getPerson().getId() == null) {
            SelectOrCreatePersonDialog.selectOrCreatePerson(caze.getPerson(), new Consumer<Person>() {
                @Override
                public void accept(Person person) {
                    caze.setPerson(person);
                    saveDataInner(caze);
                }
            });
        } else {
            saveDataInner(caze);
        }
    }

    private void saveDataInner(final Case caseToSave) {

        if (saveTask != null) {
            NotificationHelper.showNotification(this, WARNING, getString(R.string.message_already_saving));
            return; // don't save multiple times
        }

        saveTask = new SavingAsyncTask(getRootView(), caseToSave) {
            @Override
            protected void onPreExecute() {
                showPreloader();
            }

            @Override
            protected void doInBackground(TaskResultHolder resultHolder) throws Exception {
                DatabaseHelper.getPersonDao().saveAndSnapshot(caseToSave.getPerson());

                // epid number
                Calendar calendar = Calendar.getInstance();
                String year = String.valueOf(calendar.get(Calendar.YEAR)).substring(2);
                caseToSave.setEpidNumber(caseToSave.getRegion().getEpidCode() != null ? caseToSave.getRegion().getEpidCode() : ""
                        + "-" + caseToSave.getDistrict().getEpidCode() != null ? caseToSave.getDistrict().getEpidCode() : ""
                        + "-" + year + "-");

                DatabaseHelper.getCaseDao().saveAndSnapshot(caseToSave);

                if (!DataHelper.isNullOrEmpty(contactUuid)) {
                    Contact sourceContact = DatabaseHelper.getContactDao().queryUuid(contactUuid);
                    sourceContact.setResultingCaseUuid(caseToSave.getUuid());
                    sourceContact.setResultingCaseUser(ConfigProvider.getUser());
                    DatabaseHelper.getContactDao().saveAndSnapshot(sourceContact);
                }

                if (!DataHelper.isNullOrEmpty(eventParticipantUuid)) {
                    EventParticipant eventParticipant = DatabaseHelper.getEventParticipantDao().queryUuid(eventParticipantUuid);
                    eventParticipant.setResultingCaseUuid(caseToSave.getUuid());
                    DatabaseHelper.getEventParticipantDao().saveAndSnapshot(eventParticipant);
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<TaskResultHolder> taskResult) {
                hidePreloader();
                super.onPostExecute(taskResult);
                if (taskResult.getResultStatus().isSuccess()) {
                    finish();
                    CaseEditActivity.startActivity(getContext(), caseToSave.getUuid(), CaseSection.PERSON_INFO);
                }
                saveTask = null;
            }
        }.executeOnThreadPool();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (saveTask != null && !saveTask.isCancelled())
            saveTask.cancel(true);
    }

}