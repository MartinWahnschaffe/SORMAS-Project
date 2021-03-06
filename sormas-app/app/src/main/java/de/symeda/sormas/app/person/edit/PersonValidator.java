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

package de.symeda.sormas.app.person.edit;

import org.joda.time.DateTimeComparator;

import java.util.Date;

import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.symptoms.SymptomsDto;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.component.controls.ControlPropertyField;
import de.symeda.sormas.app.component.controls.ValueChangeListener;
import de.symeda.sormas.app.databinding.DialogCaseEpidBurialEditLayoutBinding;
import de.symeda.sormas.app.databinding.DialogCaseEpidTravelEditLayoutBinding;
import de.symeda.sormas.app.databinding.DialogPreviousHospitalizationLayoutBinding;
import de.symeda.sormas.app.databinding.FragmentCaseEditHospitalizationLayoutBinding;
import de.symeda.sormas.app.databinding.FragmentPersonEditLayoutBinding;
import de.symeda.sormas.app.util.Callback;

final class PersonValidator {

    static void initializePersonValidation(final FragmentPersonEditLayoutBinding contentBinding) {
        Callback deathDateCallback = new Callback() {
            public void call() {
                Date birthDate = PersonEditFragment.calculateBirthDateValue(contentBinding);
                if (contentBinding.personDeathDate.getValue() != null && birthDate != null) {
                    if (contentBinding.personDeathDate.getValue().before(birthDate)) {
                        contentBinding.personDeathDate.enableErrorState(
                                I18nProperties.getValidationError(Validations.afterDate,
                                        contentBinding.personDeathDate.getCaption(),
                                        contentBinding.personBirthdateLabel.getText()));
                    }
                }
            }
        };

        Callback burialDateCallback = new Callback() {
            public void call() {
                if (contentBinding.personBurialDate.getValue() != null && contentBinding.personDeathDate.getValue() != null) {
                    if (contentBinding.personBurialDate.getValue().before(contentBinding.personDeathDate.getValue())) {
                        contentBinding.personBurialDate.enableErrorState(
                                I18nProperties.getValidationError(Validations.afterDate,
                                        contentBinding.personBurialDate.getCaption(),
                                        contentBinding.personDeathDate.getCaption()));
                    }
                }
            }
        };

        contentBinding.personDeathDate.setValidationCallback(deathDateCallback);
        contentBinding.personBurialDate.setValidationCallback(burialDateCallback);
    }
}
