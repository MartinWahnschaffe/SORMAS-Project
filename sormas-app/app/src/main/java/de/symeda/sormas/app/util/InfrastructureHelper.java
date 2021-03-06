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

package de.symeda.sormas.app.util;

import java.util.ArrayList;
import java.util.List;

import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.facility.FacilityDto;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.facility.Facility;
import de.symeda.sormas.app.backend.region.Community;
import de.symeda.sormas.app.backend.region.District;
import de.symeda.sormas.app.backend.region.Region;
import de.symeda.sormas.app.component.Item;
import de.symeda.sormas.app.component.controls.ControlPropertyEditField;
import de.symeda.sormas.app.component.controls.ControlPropertyField;
import de.symeda.sormas.app.component.controls.ControlSpinnerField;
import de.symeda.sormas.app.component.controls.ValueChangeListener;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.symeda.sormas.app.util.DataUtils.toItems;

public final class InfrastructureHelper {

    public static List<Item> loadRegions() {
        return toItems(DatabaseHelper.getRegionDao().queryForAll(Region.NAME, true));
    }

    public static List<Item> loadDistricts(Region region) {
        return DataUtils.toItems(region != null
                ? DatabaseHelper.getDistrictDao().getByRegion(region)
                : new ArrayList<>(), true);
    }

    public static List<Item> loadCommunities(District district) {
        return toItems(district != null
                ? DatabaseHelper.getCommunityDao().getByDistrict(district)
                : new ArrayList<>(), true);
    }

    public static List<Item> loadFacilities(District district, Community community) {
        return toItems(community != null
                ? DatabaseHelper.getFacilityDao().getHealthFacilitiesByCommunity(community, true, true)
                : district != null
                ? DatabaseHelper.getFacilityDao().getHealthFacilitiesByDistrict(district, true, true)
                : new ArrayList<>(), true);
    }

    public static void initializeRegionFields(final ControlSpinnerField regionField, List<Item> initialRegions,
                                              final ControlSpinnerField districtField, List<Item> initialDistricts,
                                              final ControlSpinnerField communityField, List<Item> initialCommunities) {
        regionField.initializeSpinner(initialRegions, new ValueChangeListener() {
            @Override
            public void onChange(ControlPropertyField field) {
                Region selectedRegion = (Region) field.getValue();
                if (selectedRegion != null) {
                    districtField.setSpinnerData(toItems(DatabaseHelper.getDistrictDao().getByRegion(selectedRegion)), districtField.getValue());
                } else {
                    districtField.setSpinnerData(null);
                }
            }
        });

        districtField.initializeSpinner(initialDistricts, new ValueChangeListener() {
            @Override
            public void onChange(ControlPropertyField field) {
                District selectedDistrict = (District) field.getValue();
                if (selectedDistrict != null) {
                    communityField.setSpinnerData(toItems(DatabaseHelper.getCommunityDao().getByDistrict(selectedDistrict)), communityField.getValue());
                } else {
                    communityField.setSpinnerData(null);
                }
            }
        });

        communityField.initializeSpinner(initialCommunities);
    }

    public static void initializeFacilityFields(final ControlSpinnerField regionField, List<Item> regions,
                                                final ControlSpinnerField districtField, List<Item> districts,
                                                final ControlSpinnerField communityField, List<Item> communities,
                                                final ControlSpinnerField facilityField, List<Item> facilities) {

        regionField.initializeSpinner(regions, new ValueChangeListener() {
            @Override
            public void onChange(ControlPropertyField field) {
                Region selectedRegion = (Region) field.getValue();
                if (selectedRegion != null) {
                    districtField.setSpinnerData(loadDistricts(selectedRegion), districtField.getValue());
                } else {
                    districtField.setSpinnerData(null);
                }
            }
        });

        districtField.initializeSpinner(districts, new ValueChangeListener() {
            @Override
            public void onChange(ControlPropertyField field) {
                District selectedDistrict = (District) field.getValue();
                if (selectedDistrict != null) {
                    communityField.setSpinnerData(loadCommunities(selectedDistrict), communityField.getValue());
                    facilityField.setSpinnerData(loadFacilities(selectedDistrict, null), facilityField.getValue());
                } else {
                    communityField.setSpinnerData(null);
                    facilityField.setSpinnerData(null);
                }
            }
        });

        communityField.initializeSpinner(communities, new ValueChangeListener() {
            @Override
            public void onChange(ControlPropertyField field) {
                Community selectedCommunity = (Community) field.getValue();
                if (selectedCommunity != null) {
                    facilityField.setSpinnerData(loadFacilities(null, selectedCommunity));
                } else if (districtField.getValue() != null) {
                    facilityField.setSpinnerData(loadFacilities((District) districtField.getValue(), null));
                } else {
                    facilityField.setSpinnerData(null);
                }
            }
        });

        facilityField.initializeSpinner(facilities);
    }

    /**
     * Hide facilityDetails when no static health facility is selected and adjust the caption based on
     * the selected static health facility.
     */
    public static void initializeHealthFacilityDetailsFieldVisibility(final ControlPropertyField healthFacilityField, final ControlPropertyField healthFacilityDetailsField) {
        setHealthFacilityDetailsFieldVisibility(healthFacilityField, healthFacilityDetailsField);
        healthFacilityField.addValueChangedListener(new ValueChangeListener() {
            @Override
            public void onChange(ControlPropertyField field) {
                setHealthFacilityDetailsFieldVisibility(healthFacilityField, healthFacilityDetailsField);
            }
        });
    }

    private static void setHealthFacilityDetailsFieldVisibility(ControlPropertyField healthFacilityField, ControlPropertyField healthFacilityDetailsField) {
        Facility selectedFacility = (Facility) healthFacilityField.getValue();

        if (selectedFacility != null) {
            boolean otherHealthFacility = selectedFacility.getUuid().equals(FacilityDto.OTHER_FACILITY_UUID);
            boolean noneHealthFacility = selectedFacility.getUuid().equals(FacilityDto.NONE_FACILITY_UUID);

            if (otherHealthFacility) {
                healthFacilityDetailsField.setVisibility(VISIBLE);
                String caption = I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.HEALTH_FACILITY_DETAILS);
                healthFacilityDetailsField.setCaption(caption);
                if (healthFacilityDetailsField instanceof ControlPropertyEditField) {
                    ((ControlPropertyEditField) healthFacilityDetailsField).setHint(caption);
                }
            } else if (noneHealthFacility) {
                healthFacilityDetailsField.setVisibility(VISIBLE);
                String caption = I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.NONE_HEALTH_FACILITY_DETAILS);
                healthFacilityDetailsField.setCaption(caption);
                if (healthFacilityDetailsField instanceof ControlPropertyEditField) {
                    ((ControlPropertyEditField) healthFacilityDetailsField).setHint(caption);
                }
            } else {
                healthFacilityDetailsField.setVisibility(GONE);
            }
        } else {
            healthFacilityDetailsField.setVisibility(GONE);
        }
    }
}
