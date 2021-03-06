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
package de.symeda.sormas.ui.configuration.infrastructure;

import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionModel.HasUserSelectionAllowed;
import com.vaadin.ui.renderers.HtmlRenderer;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.region.CommunityCriteria;
import de.symeda.sormas.api.region.CommunityDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.utils.AbstractGrid;

public class CommunitiesGrid extends Grid implements AbstractGrid<CommunityCriteria> {

	private static final long serialVersionUID = 3355810665696318673L;

	public static final String EDIT_BTN_ID = "edit";
	
	private CommunityCriteria communityCriteria = new CommunityCriteria();
	
	public CommunitiesGrid() {
		setSizeFull();
		setSelectionMode(SelectionMode.NONE);
		
		BeanItemContainer<CommunityDto> container = new BeanItemContainer<CommunityDto>(CommunityDto.class);
		GeneratedPropertyContainer generatedContainer = new GeneratedPropertyContainer(container);
		setContainerDataSource(generatedContainer);
		
		if (UserProvider.getCurrent().hasUserRight(UserRight.INFRASTRUCTURE_EDIT)) {
			generatedContainer.addGeneratedProperty(EDIT_BTN_ID, new PropertyValueGenerator<String>() {
				private static final long serialVersionUID = -7255691609662228895L;

				@Override
				public String getValue(Item item, Object itemId, Object propertyId) {
					return FontAwesome.PENCIL_SQUARE.getHtml();
				}

				@Override
				public Class<String> getType() {
					return String.class;
				}
			});
		}
		
		setColumns(CommunityDto.NAME, CommunityDto.REGION, CommunityDto.DISTRICT);
		
        for (Column column : getColumns()) {
        	column.setHeaderCaption(I18nProperties.getPrefixCaption(
        			CommunityDto.I18N_PREFIX, column.getPropertyId().toString(), column.getHeaderCaption()));
        }
		
		if (UserProvider.getCurrent().hasUserRight(UserRight.INFRASTRUCTURE_EDIT)) {
			addColumn(EDIT_BTN_ID);
			getColumn(EDIT_BTN_ID).setRenderer(new HtmlRenderer());
			getColumn(EDIT_BTN_ID).setWidth(40);
			getColumn(EDIT_BTN_ID).setHeaderCaption("");

			addItemClickListener(e -> {
				if (e.getPropertyId() != null && (e.getPropertyId().equals(EDIT_BTN_ID) || e.isDoubleClick())) {
					ControllerProvider.getInfrastructureController().editCommunity(((CommunityDto) e.getItemId()).getUuid());
				}
			});
		}
	}
	
	@SuppressWarnings("unchecked")
	public BeanItemContainer<CommunityDto> getContainer() {
		GeneratedPropertyContainer container = (GeneratedPropertyContainer) super.getContainerDataSource();
		return (BeanItemContainer<CommunityDto>) container.getWrappedContainer();
	}
	
	public void reload() {
		if (getSelectionModel() instanceof HasUserSelectionAllowed) {
			deselectAll();
		}
		
		List<CommunityDto> districts = FacadeProvider.getCommunityFacade().getIndexList(communityCriteria);
		getContainer().removeAllItems();
		getContainer().addAll(districts);
	}

	@Override
	public CommunityCriteria getCriteria() {
		return communityCriteria;
	}
	
	@Override
	public void setCriteria(CommunityCriteria communityCriteria) {
		this.communityCriteria = communityCriteria;
	}	
	
}
