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
package de.symeda.sormas.ui.samples;

import java.util.Collection;

import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.sample.SampleIndexDto;
import de.symeda.sormas.api.sample.SampleReferenceDto;
import de.symeda.sormas.api.sample.SpecimenCondition;
import de.symeda.sormas.api.task.TaskContext;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.SormasUI;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent.CommitListener;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent.DeleteListener;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent.DiscardListener;
import de.symeda.sormas.ui.utils.ConfirmationComponent;
import de.symeda.sormas.ui.utils.VaadinUiUtil;

public class SampleController {

	public SampleController() { }

	public void registerViews(Navigator navigator) {
		navigator.addView(SamplesView.VIEW_NAME, SamplesView.class);
		navigator.addView(SampleDataView.VIEW_NAME, SampleDataView.class);
	}

	public void navigateToData(String sampleUuid) {
		String navigationState = SampleDataView.VIEW_NAME + "/" + sampleUuid;
		SormasUI.get().getNavigator().navigateTo(navigationState);
	}

	public void create(CaseReferenceDto caseRef, Runnable callback) {
		SampleCreateForm createForm = new SampleCreateForm(UserRight.SAMPLE_CREATE);
		createForm.setValue(SampleDto.buildSample(UserProvider.getCurrent().getUserReference(), caseRef));
		final CommitDiscardWrapperComponent<SampleCreateForm> editView = new CommitDiscardWrapperComponent<SampleCreateForm>(createForm, createForm.getFieldGroup());

		editView.addCommitListener(new CommitListener() {
			@Override
			public void onCommit() {
				if( !createForm.getFieldGroup().isModified()) {
					SampleDto dto = createForm.getValue();
					FacadeProvider.getSampleFacade().saveSample(dto);
					callback.run();
				}
			}
		});

		VaadinUiUtil.showModalPopupWindow(editView, I18nProperties.getString(Strings.headingCreateNewSample));
	}

	public void createReferral(SampleDto sample) {
		SampleCreateForm createForm = new SampleCreateForm(UserRight.SAMPLE_CREATE);
		SampleDto referralSample = SampleDto.buildReferralSample(UserProvider.getCurrent().getUserReference(), sample);
		createForm.setValue(referralSample);
		final CommitDiscardWrapperComponent<SampleCreateForm> createView = new CommitDiscardWrapperComponent<SampleCreateForm>(createForm, createForm.getFieldGroup());

		createView.addCommitListener(new CommitListener() {
			@Override
			public void onCommit() {
				if (!createForm.getFieldGroup().isModified()) {
					SampleDto newSample = createForm.getValue();
					FacadeProvider.getSampleFacade().saveSample(newSample);
					sample.setReferredTo(FacadeProvider.getSampleFacade().getReferenceByUuid(newSample.getUuid()));
					FacadeProvider.getSampleFacade().saveSample(sample);
					navigateToData(newSample.getUuid());
				}
			}
		});
		
		// Reload the page when the form is discarded because the sample has been saved before
		createView.addDiscardListener(new DiscardListener() {
			@Override
			public void onDiscard() {
				navigateToData(sample.getUuid());
			}
		});

		VaadinUiUtil.showModalPopupWindow(createView, I18nProperties.getString(Strings.headingReferSample));
	}

	public CommitDiscardWrapperComponent<SampleEditForm> getSampleEditComponent(final String sampleUuid) {
		SampleEditForm form = new SampleEditForm(UserRight.SAMPLE_EDIT);
		form.setWidth(form.getWidth() * 10/12, Unit.PIXELS);
		SampleDto dto = FacadeProvider.getSampleFacade().getSampleByUuid(sampleUuid);
		form.setValue(dto);
		final CommitDiscardWrapperComponent<SampleEditForm> editView = new CommitDiscardWrapperComponent<SampleEditForm>(form, form.getFieldGroup());

		editView.addCommitListener(new CommitListener() {
			@Override
			public void onCommit() {
				if (!form.getFieldGroup().isModified()) {
					SampleDto dto = form.getValue();
					SampleDto originalDto = FacadeProvider.getSampleFacade().getSampleByUuid(dto.getUuid());
					FacadeProvider.getSampleFacade().saveSample(dto);
					SormasUI.refreshView();

					if (dto.getSpecimenCondition() != originalDto.getSpecimenCondition() &&
							dto.getSpecimenCondition() == SpecimenCondition.NOT_ADEQUATE &&
							UserProvider.getCurrent().hasUserRight(UserRight.TASK_CREATE)) {
						requestSampleCollectionTaskCreation(dto, form);
					} else {
						Notification.show(I18nProperties.getString(Strings.messageSampleSaved), Type.WARNING_MESSAGE);
					}
				}
			}
		});

		if (UserProvider.getCurrent().hasUserRole(UserRole.ADMIN)) {
			editView.addDeleteListener(new DeleteListener() {
				@Override
				public void onDelete() {
					FacadeProvider.getSampleFacade().deleteSample(dto.toReference(), UserProvider.getCurrent().getUserReference().getUuid());
					UI.getCurrent().getNavigator().navigateTo(SamplesView.VIEW_NAME);
				}
			}, I18nProperties.getString(Strings.entitySample));
		}

		// Initialize 'Refer to another laboratory' button or link to referred sample
		Button referOrLinkToOtherLabButton = new Button();
		referOrLinkToOtherLabButton.addStyleName(ValoTheme.BUTTON_LINK);
		if (dto.getReferredTo() == null) {
			if (UserProvider.getCurrent().hasUserRight(UserRight.SAMPLE_TRANSFER)) {
				referOrLinkToOtherLabButton.setCaption(I18nProperties.getCaption(Captions.sampleRefer));
				referOrLinkToOtherLabButton.addClickListener(new ClickListener() {
					private static final long serialVersionUID = 1L;
					@Override
					public void buttonClick(ClickEvent event) {
						try {
							form.commit();
							SampleDto sampleDto = form.getValue();
							sampleDto = FacadeProvider.getSampleFacade().saveSample(sampleDto);
							createReferral(sampleDto);
						} catch (SourceException | InvalidValueException e) {
							Notification.show(I18nProperties.getString(Strings.messageSampleErrors), Type.ERROR_MESSAGE);
						}
					}
				});

				editView.getButtonsPanel().addComponentAsFirst(referOrLinkToOtherLabButton);
				editView.getButtonsPanel().setComponentAlignment(referOrLinkToOtherLabButton, Alignment.BOTTOM_LEFT);
			}
		} else {
			SampleDto referredDto = FacadeProvider.getSampleFacade().getSampleByUuid(dto.getReferredTo().getUuid());
			referOrLinkToOtherLabButton.setCaption(I18nProperties.getCaption(Captions.sampleReferredTo) + " " + referredDto.getLab().toString());
			referOrLinkToOtherLabButton.addClickListener(new ClickListener() {
				private static final long serialVersionUID = 1L;
				@Override
				public void buttonClick(ClickEvent event) {
					navigateToData(dto.getReferredTo().getUuid());
				}
			});

			editView.getButtonsPanel().addComponentAsFirst(referOrLinkToOtherLabButton);
			editView.getButtonsPanel().setComponentAlignment(referOrLinkToOtherLabButton, Alignment.BOTTOM_LEFT);
		}

		return editView;
	}

	private void requestSampleCollectionTaskCreation(SampleDto dto, SampleEditForm form) {
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);

		ConfirmationComponent requestTaskComponent = buildRequestTaskComponent();

		Label description = new Label(I18nProperties.getString(Strings.messageCreateCollectionTask));
		description.setContentMode(ContentMode.HTML);
		description.setWidth(100, Unit.PERCENTAGE);
		layout.addComponent(description);
		layout.addComponent(requestTaskComponent);
		layout.setComponentAlignment(requestTaskComponent, Alignment.BOTTOM_RIGHT);
		layout.setSizeUndefined();
		layout.setSpacing(true);

		Window popupWindow = VaadinUiUtil.showPopupWindow(layout);
		popupWindow.setSizeUndefined();
		popupWindow.setCaption(I18nProperties.getString(Strings.headingCreateNewTaskQuestion));
		requestTaskComponent.getConfirmButton().addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				popupWindow.close();
				ControllerProvider.getTaskController().createSampleCollectionTask(TaskContext.CASE, dto.getAssociatedCase(), dto);
			}
		});
		requestTaskComponent.getCancelButton().addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				popupWindow.close();
			}
		});
	}

	private ConfirmationComponent buildRequestTaskComponent() {
		ConfirmationComponent requestTaskComponent = new ConfirmationComponent(false) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onConfirm() {
			}
			@Override
			protected void onCancel() {
			}
		};
		requestTaskComponent.getConfirmButton().setCaption(I18nProperties.getString(Strings.yes));
		requestTaskComponent.getCancelButton().setCaption(I18nProperties.getString(Strings.no));
		return requestTaskComponent;
	}

	public void deleteAllSelectedItems(Collection<Object> selectedRows, Runnable callback) {
		if (selectedRows.size() == 0) {
			new Notification(I18nProperties.getString(Strings.headingNoSamplesSelected), 
					I18nProperties.getString(Strings.messageNoSamplesSelected), Type.WARNING_MESSAGE, false).show(Page.getCurrent());
		} else {
			VaadinUiUtil.showDeleteConfirmationWindow(String.format(I18nProperties.getString(Strings.confirmationDeleteSamples), selectedRows.size()), new Runnable() {
				public void run() {
					for (Object selectedRow : selectedRows) {
						FacadeProvider.getSampleFacade().deleteSample(new SampleReferenceDto(((SampleIndexDto) selectedRow).getUuid()), UserProvider.getCurrent().getUuid());
					}
					callback.run();
					new Notification(I18nProperties.getString(Strings.headingSamplesDeleted), 
							I18nProperties.getString(Strings.messageSamplesDeleted), Type.HUMANIZED_MESSAGE, false).show(Page.getCurrent());
				}
			});
		}
	}

}
