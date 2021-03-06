/*******************************************************************************
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
 *******************************************************************************/
package de.symeda.sormas.ui.configuration;

import java.util.Optional;

import com.vaadin.navigator.Navigator;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.SubMenu;
import de.symeda.sormas.ui.configuration.infrastructure.CommunitiesView;
import de.symeda.sormas.ui.configuration.infrastructure.DistrictsView;
import de.symeda.sormas.ui.configuration.infrastructure.HealthFacilitiesView;
import de.symeda.sormas.ui.configuration.infrastructure.LaboratoriesView;
import de.symeda.sormas.ui.configuration.infrastructure.RegionsView;
import de.symeda.sormas.ui.configuration.outbreak.OutbreaksView;
import de.symeda.sormas.ui.utils.AbstractSubNavigationView;

public abstract class AbstractConfigurationView extends AbstractSubNavigationView {

	private static final long serialVersionUID = 3193505016439327054L;

	public static final String ROOT_VIEW_NAME = "configuration";
	
	protected AbstractConfigurationView(String viewName) {
		super(viewName);
	}

	@Override
	public void refreshMenu(SubMenu menu, Label infoLabel, Label infoLabelSub, String params) {
		menu.removeAllViews();
		if (UserProvider.getCurrent().hasUserRight(UserRight.INFRASTRUCTURE_VIEW)) {
			menu.addView(RegionsView.VIEW_NAME, I18nProperties.getPrefixCaption("View",
					RegionsView.VIEW_NAME.replaceAll("/", ".") + ".short", ""), null, false);
			menu.addView(DistrictsView.VIEW_NAME, I18nProperties.getPrefixCaption("View",
					DistrictsView.VIEW_NAME.replaceAll("/", ".") + ".short", ""), null, false);
			menu.addView(CommunitiesView.VIEW_NAME, I18nProperties.getPrefixCaption("View",
					CommunitiesView.VIEW_NAME.replaceAll("/", ".") + ".short", ""), null, false);
			menu.addView(HealthFacilitiesView.VIEW_NAME, I18nProperties.getPrefixCaption("View",
					HealthFacilitiesView.VIEW_NAME.replaceAll("/", ".") + ".short", ""), null, false);
			menu.addView(LaboratoriesView.VIEW_NAME, I18nProperties.getPrefixCaption("View",
					LaboratoriesView.VIEW_NAME.replaceAll("/", ".") + ".short", ""), null, false);
		}
//		if (LoginHelper.hasUserRight(UserRight.USER_RIGHTS_MANAGE)) {
//			menu.addView(UserRightsView.VIEW_NAME, I18nProperties.getPrefixFragment("View", 
//					UserRightsView.VIEW_NAME.replaceAll("/", ".") + ".short", ""), params);
//		}
		menu.addView(OutbreaksView.VIEW_NAME,
				I18nProperties.getPrefixCaption("View", OutbreaksView.VIEW_NAME.replaceAll("/", ".") + ".short", ""),
				params);
	}

	public static void registerViews(Navigator navigator) {
		if (UserProvider.getCurrent().hasUserRight(UserRight.INFRASTRUCTURE_VIEW)) {
			navigator.addView(RegionsView.VIEW_NAME, RegionsView.class);
			navigator.addView(DistrictsView.VIEW_NAME, DistrictsView.class);
			navigator.addView(CommunitiesView.VIEW_NAME, CommunitiesView.class);
			navigator.addView(HealthFacilitiesView.VIEW_NAME, HealthFacilitiesView.class);
			navigator.addView(LaboratoriesView.VIEW_NAME, LaboratoriesView.class);
		}
//		if (LoginHelper.hasUserRight(UserRight.USER_RIGHTS_MANAGE)) {
//			navigator.addView(UserRightsView.VIEW_NAME, UserRightsView.class);
//		}
		navigator.addView(OutbreaksView.VIEW_NAME, OutbreaksView.class);
	}

	@Override
	protected Optional<VerticalLayout> createInfoLayout() {
		return Optional.empty();
	}
}
