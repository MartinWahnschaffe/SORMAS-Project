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

package de.symeda.sormas.app.caze;

import android.content.Context;

import de.symeda.sormas.app.R;
import de.symeda.sormas.app.core.enumeration.StatusElaborator;

public enum CaseSection implements StatusElaborator {

    CASE_INFO(R.string.caption_case_information, R.drawable.ic_drawer_case_blue_24dp),
    PERSON_INFO(R.string.caption_person_information, R.drawable.ic_person_black_24dp),
    HOSPITALIZATION(R.string.caption_case_hospitalization, R.drawable.ic_local_hospital_black_24dp),
    SYMPTOMS(R.string.caption_symptoms, R.drawable.ic_healing_black_24dp),
    EPIDEMIOLOGICAL_DATA(R.string.caption_case_epidemiological_data, R.drawable.ic_pets_black_24dp),
    CONTACTS(R.string.caption_case_contacts, R.drawable.ic_drawer_contact_blue_24dp),
    SAMPLES(R.string.caption_case_samples, R.drawable.ic_drawer_sample_blue_24dp),
    TASKS(R.string.caption_case_tasks, R.drawable.ic_drawer_user_task_blue_24dp);

    private int friendlyNameResourceId;
    private int iconResourceId;

    CaseSection(int friendlyNameResourceId, int iconResourceId) {
        this.friendlyNameResourceId = friendlyNameResourceId;
        this.iconResourceId = iconResourceId;
    }

    public static CaseSection fromOrdinal(int ordinal) {
        return CaseSection.values()[ordinal];
    }

    @Override
    public String getFriendlyName(Context context) {
        return context.getResources().getString(friendlyNameResourceId);
    }

    @Override
    public int getColorIndicatorResource() {
        return 0;
    }

    @Override
    public Enum getValue() {
        return this;
    }

    @Override
    public int getIconResourceId() {
        return iconResourceId;
    }
}
