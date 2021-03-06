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

import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;

import com.google.android.gms.common.util.Strings;

import java.util.List;

import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.CaseOutcome;
import de.symeda.sormas.api.caze.DengueFeverType;
import de.symeda.sormas.api.caze.PlagueType;
import de.symeda.sormas.api.caze.Vaccination;
import de.symeda.sormas.api.caze.VaccinationInfoSource;
import de.symeda.sormas.api.person.Sex;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.app.BaseActivity;
import de.symeda.sormas.app.BaseEditFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.classification.DiseaseClassificationAppHelper;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.component.Item;
import de.symeda.sormas.app.component.controls.ControlPropertyField;
import de.symeda.sormas.app.component.controls.ValueChangeListener;
import de.symeda.sormas.app.component.dialog.InfoDialog;
import de.symeda.sormas.app.databinding.DialogClassificationRulesLayoutBinding;
import de.symeda.sormas.app.databinding.FragmentCaseEditLayoutBinding;
import de.symeda.sormas.app.util.Callback;
import de.symeda.sormas.app.util.Consumer;
import de.symeda.sormas.app.util.DataUtils;
import de.symeda.sormas.app.util.InfrastructureHelper;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class CaseEditFragment extends BaseEditFragment<FragmentCaseEditLayoutBinding, Case, Case> {

    public static final String TAG = CaseEditFragment.class.getSimpleName();

    private Case record;

    // Enum lists

    private List<Item> caseClassificationList;
    private List<Item> caseOutcomeList;
    private List<Item> vaccinationInfoSourceList;
    private List<Item> plagueTypeList;
    private List<Item> dengueFeverTypeList;

    // Static methods

    public static CaseEditFragment newInstance(Case activityRootData) {
        return newInstance(CaseEditFragment.class, null, activityRootData);
    }

    // Instance methods

    private void setUpFieldVisibilities(final FragmentCaseEditLayoutBinding contentBinding) {
        setVisibilityByDisease(CaseDataDto.class, contentBinding.getData().getDisease(), contentBinding.mainContent);
        InfrastructureHelper.initializeHealthFacilityDetailsFieldVisibility(contentBinding.caseDataHealthFacility, contentBinding.caseDataHealthFacilityDetails);

        // Vaccination date
        if (isVisibleAllowed(CaseDataDto.class, contentBinding.getData().getDisease(), contentBinding.caseDataVaccination)) {
            setVisibleWhen(contentBinding.caseDataVaccinationDate, contentBinding.caseDataVaccination, Vaccination.VACCINATED);
        }
        if (isVisibleAllowed(CaseDataDto.class, contentBinding.getData().getDisease(), contentBinding.caseDataSmallpoxVaccinationReceived)) {
            setVisibleWhen(contentBinding.caseDataVaccinationDate, contentBinding.caseDataSmallpoxVaccinationReceived, YesNoUnknown.YES);
        }

        // Pregnancy
        if (record.getPerson().getSex() != Sex.FEMALE) {
            contentBinding.caseDataPregnant.setVisibility(View.GONE);
        }

        // Smallpox vaccination scar image
        contentBinding.caseDataSmallpoxVaccinationScar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                contentBinding.smallpoxVaccinationScarImg.setVisibility(contentBinding.caseDataSmallpoxVaccinationScar.getVisibility());
            }
        });

        // Button panel
        if (!DatabaseHelper.getDiseaseClassificationCriteriaDao().getByDisease(record.getDisease()).hasAnyCriteria()) {
            contentBinding.showClassificationRules.setVisibility(GONE);
        }
        if (!ConfigProvider.hasUserRight(UserRight.CASE_TRANSFER)) {
            contentBinding.transferCase.setVisibility(GONE);
        }
        if (contentBinding.showClassificationRules.getVisibility() == GONE && contentBinding.transferCase.getVisibility() == GONE) {
            contentBinding.caseButtonsPanel.setVisibility(GONE);
        }
    }

    private void setUpButtonListeners(FragmentCaseEditLayoutBinding contentBinding) {
        contentBinding.transferCase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CaseEditActivity activity = (CaseEditActivity) CaseEditFragment.this.getActivity();
                activity.saveData(new Consumer<Case>() {
                    @Override
                    public void accept(final Case caze) {
                        final Case caseClone = (Case) caze.clone();
                        final MoveCaseDialog moveCaseDialog = new MoveCaseDialog(BaseActivity.getActiveActivity(), caze);
                        moveCaseDialog.setPositiveCallback(new Callback() {
                            @Override
                            public void call() {
                                record = caseClone;
                                requestLayoutRebind();
                                moveCaseDialog.dismiss();
                            }
                        });
                        moveCaseDialog.show();
                    }
                });
            }
        });

        contentBinding.showClassificationRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final InfoDialog classificationDialog = new InfoDialog(CaseEditFragment.this.getContext(), R.layout.dialog_classification_rules_layout, null);
                WebView classificationView = ((DialogClassificationRulesLayoutBinding) classificationDialog.getBinding()).content;
                classificationView.loadData(DiseaseClassificationAppHelper.buildDiseaseClassificationHtml(record.getDisease()), "text/html", "utf-8");
                classificationDialog.show();
            }
        });
    }

    // Overrides

    @Override
    protected String getSubHeadingTitle() {
        return getResources().getString(R.string.caption_case_information);
    }

    @Override
    public Case getPrimaryData() {
        return record;
    }

    @Override
    protected void prepareFragmentData() {
        record = getActivityRootData();

        caseClassificationList = DataUtils.getEnumItems(CaseClassification.class, true);
        caseOutcomeList = DataUtils.getEnumItems(CaseOutcome.class, true);
        vaccinationInfoSourceList = DataUtils.getEnumItems(VaccinationInfoSource.class, true);
        plagueTypeList = DataUtils.getEnumItems(PlagueType.class, true);
        dengueFeverTypeList = DataUtils.getEnumItems(DengueFeverType.class, true);
    }

    @Override
    public void onLayoutBinding(FragmentCaseEditLayoutBinding contentBinding) {
        setUpButtonListeners(contentBinding);

        // Epid number warning state
        if (ConfigProvider.hasUserRight(UserRight.CASE_CHANGE_EPID_NUMBER)) {
            contentBinding.caseDataEpidNumber.addValueChangedListener(new ValueChangeListener() {
                @Override
                public void onChange(ControlPropertyField field) {
                    String value = (String) field.getValue();
                    if (value.trim().isEmpty()) {
                        getContentBinding().caseDataEpidNumber.enableWarningState(
                                R.string.validation_soft_case_epid_number_empty);
                    } else if (value.matches(DataHelper.getEpidNumberRegexp())) {
                        getContentBinding().caseDataEpidNumber.disableWarningState();
                    } else {
                        getContentBinding().caseDataEpidNumber.enableWarningState(
                                R.string.validation_soft_case_epid_number);
                    }
                }
            });
        }

        // Case classification warning state
        if (ConfigProvider.hasUserRight(UserRight.CASE_CLASSIFY)) {
            contentBinding.caseDataCaseClassification.addValueChangedListener(new ValueChangeListener() {
                @Override
                public void onChange(ControlPropertyField field) {
                    CaseClassification caseClassification = (CaseClassification) field.getValue();
                    if (caseClassification == CaseClassification.NOT_CLASSIFIED) {
                        getContentBinding().caseDataCaseClassification.enableWarningState(
                                R.string.validation_soft_case_classification);
                    } else {
                        getContentBinding().caseDataCaseClassification.disableWarningState();
                    }
                }
            });
        }

        contentBinding.setData(record);
        contentBinding.setYesNoUnknownClass(YesNoUnknown.class);
        contentBinding.setVaccinationClass(Vaccination.class);
    }

    @Override
    public void onAfterLayoutBinding(final FragmentCaseEditLayoutBinding contentBinding) {
        setUpFieldVisibilities(contentBinding);

        // Initialize ControlSpinnerFields
        contentBinding.caseDataCaseClassification.initializeSpinner(caseClassificationList);
        contentBinding.caseDataOutcome.initializeSpinner(caseOutcomeList);
        contentBinding.caseDataPlagueType.initializeSpinner(plagueTypeList);
        contentBinding.caseDataDengueFeverType.initializeSpinner(dengueFeverTypeList);
        contentBinding.caseDataVaccinationInfoSource.initializeSpinner(vaccinationInfoSourceList);

        // Initialize ControlDateFields
        contentBinding.caseDataReportDate.initializeDateField(getFragmentManager());
        contentBinding.caseDataOutcomeDate.initializeDateField(getFragmentManager());
        contentBinding.caseDataVaccinationDate.initializeDateField(getFragmentManager());

        // Replace classification user field with classified by field when case has been classified automatically
        if (contentBinding.getData().getClassificationDate() != null && contentBinding.getData().getClassificationUser() == null) {
            contentBinding.caseDataClassificationUser.setVisibility(GONE);
            contentBinding.caseDataClassifiedBy.setVisibility(VISIBLE);
            contentBinding.caseDataClassifiedBy.setValue(getResources().getString(R.string.system));
        }
    }

    @Override
    public int getEditLayout() {
        return R.layout.fragment_case_edit_layout;
    }

    @Override
    public boolean isShowSaveAction() {
        return true;
    }

    @Override
    public boolean isShowNewAction() {
        return false;
    }
}
