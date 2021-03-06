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
package de.symeda.sormas.ui.dashboard.surveillance;

import java.util.List;

import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.api.caze.DashboardCaseDto;
import de.symeda.sormas.api.event.DashboardEventDto;
import de.symeda.sormas.api.event.EventStatus;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.outbreak.DashboardOutbreakDto;
import de.symeda.sormas.api.sample.DashboardTestResultDto;
import de.symeda.sormas.api.sample.SampleTestResultType;
//import de.symeda.sormas.ui.CurrentUser;
import de.symeda.sormas.ui.dashboard.DashboardDataProvider;
import de.symeda.sormas.ui.dashboard.statistics.CountElementStyle;
import de.symeda.sormas.ui.dashboard.statistics.DashboardStatisticsCountElement;
import de.symeda.sormas.ui.dashboard.statistics.DashboardStatisticsSubComponent;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.LayoutUtil;

public class DiseaseStatisticsComponent extends CustomLayout {

	private static final long serialVersionUID = 6582975657305031105L;

	private DashboardDataProvider dashboardDataProvider;

	// "New Cases" elements
	private Label caseCountLabel;
//	private Label caseDiseaseLabel;
	private DashboardStatisticsCountElement caseClassificationConfirmed;
	private DashboardStatisticsCountElement caseClassificationProbable;
	private DashboardStatisticsCountElement caseClassificationSuspect;
	private DashboardStatisticsCountElement caseClassificationNotACase;
	private DashboardStatisticsCountElement caseClassificationNotYetClassified;

	// "Outbreak Districts" elements
	private Label outbreakDistrictCountLabel;
	
	// "Case Fatality" elements
	private Label caseFatalityRateValue;
	private Label caseFatalityCountValue;
	private Label caseFatalityCountGrowth;

	// "New Events" elements
	private Label eventCountLabel;
	private DashboardStatisticsCountElement eventStatusConfirmed;
	private DashboardStatisticsCountElement eventStatusPossible;
	private DashboardStatisticsCountElement eventStatusNotAnEvent;

	// "New Test Results" elements
	private Label testResultCountLabel;
	private DashboardStatisticsCountElement testResultPositive;
	private DashboardStatisticsCountElement testResultNegative;
	private DashboardStatisticsCountElement testResultPending;
	private DashboardStatisticsCountElement testResultIndeterminate;
	
	private static final String CASE_LOC = "case";
	private static final String OUTBREAK_LOC = "outbreak";
	private static final String EVENT_LOC = "event";
	private static final String SAMPLE_LOC = "sample";

	public DiseaseStatisticsComponent(DashboardDataProvider dashboardDataProvider) {
		this.dashboardDataProvider = dashboardDataProvider;
		
		setWidth(100, Unit.PERCENTAGE);

		setTemplateContents(
				LayoutUtil.fluidRow(
						LayoutUtil.fluidColumn(6, 0, 12, 0, LayoutUtil.fluidRowLocs(CASE_LOC, OUTBREAK_LOC)), 
						LayoutUtil.fluidColumn(6, 0, 12, 0, LayoutUtil.fluidRowLocs(EVENT_LOC, SAMPLE_LOC))
						) 
				);

		addComponent(createCaseComponent(), CASE_LOC);
		addComponent(createOutbreakDistrictAndCaseFatalityLayout(), OUTBREAK_LOC);
		addComponent(createEventComponent(), EVENT_LOC);
		addComponent(createTestResultComponent(), SAMPLE_LOC);
	}
	
	private DashboardStatisticsSubComponent createCaseComponent() {
		DashboardStatisticsSubComponent caseComponent = new DashboardStatisticsSubComponent();

		// Header
		HorizontalLayout headerLayout = new HorizontalLayout();
		// count
		caseCountLabel = new Label();
		CssStyles.style(caseCountLabel, CssStyles.LABEL_PRIMARY, CssStyles.LABEL_XXXLARGE, CssStyles.LABEL_BOLD,
				CssStyles.VSPACE_4, CssStyles.VSPACE_TOP_NONE);
		headerLayout.addComponent(caseCountLabel);
		// title
		Label caseComponentTitle = new Label(I18nProperties.getCaption(Captions.dashboardNewCases));
		CssStyles.style(caseComponentTitle, CssStyles.H2, CssStyles.HSPACE_LEFT_4);
		headerLayout.addComponent(caseComponentTitle);
//		caseDiseaseLabel = new Label();
//		CssStyles.style(caseDiseaseLabel, CssStyles.H2, CssStyles.HSPACE_LEFT_4);
//		headerLayout.addComponent(caseDiseaseLabel);

		caseComponent.addComponent(headerLayout);

		// Count layout
		CssLayout countLayout = caseComponent.createCountLayout(true);
		caseClassificationConfirmed = new DashboardStatisticsCountElement(I18nProperties.getCaption(Captions.dashboardConfirmed), CountElementStyle.CRITICAL);
		caseComponent.addComponentToCountLayout(countLayout, caseClassificationConfirmed);
		caseClassificationProbable = new DashboardStatisticsCountElement(I18nProperties.getCaption(Captions.dashboardProbable), CountElementStyle.IMPORTANT);
		caseComponent.addComponentToCountLayout(countLayout, caseClassificationProbable);
		caseClassificationSuspect = new DashboardStatisticsCountElement(I18nProperties.getCaption(Captions.dashboardSuspect), CountElementStyle.RELEVANT);
		caseComponent.addComponentToCountLayout(countLayout, caseClassificationSuspect);
		caseClassificationNotACase = new DashboardStatisticsCountElement(I18nProperties.getCaption(Captions.dashboardNotACase), CountElementStyle.POSITIVE);
		caseComponent.addComponentToCountLayout(countLayout, caseClassificationNotACase);
		caseClassificationNotYetClassified = new DashboardStatisticsCountElement(I18nProperties.getCaption(Captions.dashboardNotYetClassified), CountElementStyle.MINOR);
		caseComponent.addComponentToCountLayout(countLayout, caseClassificationNotYetClassified);
		caseComponent.addComponent(countLayout);
		
		return caseComponent;
	}
	
	private DashboardStatisticsSubComponent createOutbreakDistrictAndCaseFatalityLayout() {
		DashboardStatisticsSubComponent layout = new DashboardStatisticsSubComponent();
		
		layout.addComponent(createCaseFatalityComponent());
		
		layout.addComponent(createOutbreakDistrictComponent());
		
		layout.addStyleName(CssStyles.VSPACE_TOP_4);
		
		return layout;
	}

	private VerticalLayout createOutbreakDistrictComponent() {
		VerticalLayout outbreakDistrictComponent = new VerticalLayout();
		outbreakDistrictComponent.setMargin(false);

		// Header
		HorizontalLayout headerLayout = new HorizontalLayout();
		// count
		outbreakDistrictCountLabel = new Label();
		CssStyles.style(outbreakDistrictCountLabel, CssStyles.LABEL_PRIMARY, CssStyles.LABEL_XXXLARGE,
				CssStyles.LABEL_BOLD, CssStyles.VSPACE_4, CssStyles.VSPACE_TOP_NONE);
		headerLayout.addComponent(outbreakDistrictCountLabel);
		// title
		Label titleLabel = new Label(I18nProperties.getCaption(Captions.dashboardOutbreakDistricts));
		CssStyles.style(titleLabel, CssStyles.H2, CssStyles.HSPACE_LEFT_4);
		headerLayout.addComponent(titleLabel);

		outbreakDistrictComponent.addComponent(headerLayout);	
		
		return outbreakDistrictComponent;
	}

	private HorizontalLayout createCaseFatalityComponent() {
		HorizontalLayout layout = new HorizontalLayout();
		
		// rate
		VerticalLayout cfrRateLayout = new VerticalLayout();
		// value
		HorizontalLayout cfrShortLabelAndValueLayout = new HorizontalLayout();
		Label cfrShortLabel = new Label(I18nProperties.getCaption(Captions.dashboardCaseFatalityRateShort));
		CssStyles.style(cfrShortLabel, CssStyles.LABEL_PRIMARY, CssStyles.LABEL_BOLD, CssStyles.LABEL_LARGE, CssStyles.HSPACE_RIGHT_4);
		cfrShortLabelAndValueLayout.addComponent(cfrShortLabel);
		caseFatalityRateValue = new Label("00.0%");
		CssStyles.style(caseFatalityRateValue, CssStyles.LABEL_PRIMARY, CssStyles.LABEL_BOLD, CssStyles.LABEL_LARGE);
		cfrShortLabelAndValueLayout.addComponent(caseFatalityRateValue);
		cfrRateLayout.addComponent(cfrShortLabelAndValueLayout);
		// caption
		Label caseFatalityRateCaption = new Label(I18nProperties.getCaption(Captions.dashboardCaseFatalityRate));
		cfrRateLayout.addComponent(caseFatalityRateCaption);
		CssStyles.style(caseFatalityRateCaption, CssStyles.LABEL_CRITICAL);

		layout.addComponent(cfrRateLayout);
		// ~rate

		// count
		VerticalLayout cfrCountLayout = new VerticalLayout();
		// value
		HorizontalLayout cfrCountValuesLayout = new HorizontalLayout();
		// current
		caseFatalityCountValue = new Label("0");
		CssStyles.style(caseFatalityCountValue, CssStyles.LABEL_PRIMARY, CssStyles.LABEL_BOLD, CssStyles.LABEL_LARGE);
		cfrCountValuesLayout.addComponent(caseFatalityCountValue);
		// growth
		caseFatalityCountGrowth = new Label("", ContentMode.HTML);
		cfrCountValuesLayout.addComponent(caseFatalityCountGrowth);

		cfrCountLayout.addComponent(cfrCountValuesLayout);
		cfrCountLayout.setComponentAlignment(cfrCountValuesLayout, Alignment.MIDDLE_RIGHT);
		cfrCountValuesLayout.setWidthUndefined();
		// caption
		Label caseFatalityCountCaption = new Label(I18nProperties.getCaption(Captions.dashboardFatalities));
		cfrCountLayout.addComponent(caseFatalityCountCaption);
		cfrCountLayout.setComponentAlignment(caseFatalityCountCaption, Alignment.MIDDLE_RIGHT);
		caseFatalityCountCaption.setWidthUndefined();
		CssStyles.style(caseFatalityCountCaption, CssStyles.LABEL_CRITICAL);

		layout.addComponent(cfrCountLayout);
		// ~count

		layout.setWidth(100, Unit.PERCENTAGE);
		
		return layout;
	}

	private DashboardStatisticsSubComponent createEventComponent() {
		DashboardStatisticsSubComponent eventComponent = new DashboardStatisticsSubComponent();

		// Header
		HorizontalLayout headerLayout = new HorizontalLayout();
		// count
		eventCountLabel = new Label();
		CssStyles.style(eventCountLabel, CssStyles.LABEL_PRIMARY, CssStyles.LABEL_XXXLARGE, CssStyles.LABEL_BOLD,
				CssStyles.VSPACE_4, CssStyles.VSPACE_TOP_NONE);
		headerLayout.addComponent(eventCountLabel);
		// title
		Label titleLabel = new Label(I18nProperties.getCaption(Captions.dashboardNewEvents));
		CssStyles.style(titleLabel, CssStyles.H2, CssStyles.HSPACE_LEFT_4);
		headerLayout.addComponent(titleLabel);

		eventComponent.addComponent(headerLayout);

		// Count layout
		CssLayout countLayout = eventComponent.createCountLayout(true);
		eventStatusConfirmed = new DashboardStatisticsCountElement(I18nProperties.getCaption(Captions.dashboardConfirmed), CountElementStyle.CRITICAL);
		eventComponent.addComponentToCountLayout(countLayout, eventStatusConfirmed);
		eventStatusPossible = new DashboardStatisticsCountElement(I18nProperties.getCaption(Captions.dashboardPossible), CountElementStyle.IMPORTANT);
		eventComponent.addComponentToCountLayout(countLayout, eventStatusPossible);
		eventStatusNotAnEvent = new DashboardStatisticsCountElement(I18nProperties.getCaption(Captions.dashboardNotAnEvent), CountElementStyle.POSITIVE);
		eventComponent.addComponentToCountLayout(countLayout, eventStatusNotAnEvent);
		eventComponent.addComponent(countLayout);
		
		return eventComponent;
	}

	private DashboardStatisticsSubComponent createTestResultComponent() {
		DashboardStatisticsSubComponent testResultComponent = new DashboardStatisticsSubComponent();

		// Header
		HorizontalLayout headerLayout = new HorizontalLayout();
		// count
		testResultCountLabel = new Label();
		CssStyles.style(testResultCountLabel, CssStyles.LABEL_PRIMARY, CssStyles.LABEL_XXXLARGE, CssStyles.LABEL_BOLD,
				CssStyles.VSPACE_4, CssStyles.VSPACE_TOP_NONE);
		headerLayout.addComponent(testResultCountLabel);
		// title
		Label titleLabel = new Label(I18nProperties.getCaption(Captions.dashboardNewTestResults));
		CssStyles.style(titleLabel, CssStyles.H2, CssStyles.HSPACE_LEFT_4);
		headerLayout.addComponent(titleLabel);

		testResultComponent.addComponent(headerLayout);

		// Count layout
		CssLayout countLayout = testResultComponent.createCountLayout(true);
		testResultPositive = new DashboardStatisticsCountElement(I18nProperties.getCaption(Captions.dashboardPositive), CountElementStyle.CRITICAL);
		testResultComponent.addComponentToCountLayout(countLayout, testResultPositive);
		testResultNegative = new DashboardStatisticsCountElement(I18nProperties.getCaption(Captions.dashboardNegative), CountElementStyle.POSITIVE);
		testResultComponent.addComponentToCountLayout(countLayout, testResultNegative);
		testResultPending = new DashboardStatisticsCountElement(I18nProperties.getCaption(Captions.dashboardPending), CountElementStyle.IMPORTANT);
		testResultComponent.addComponentToCountLayout(countLayout, testResultPending);
		testResultIndeterminate = new DashboardStatisticsCountElement(I18nProperties.getCaption(Captions.dashboardIndeterminate), CountElementStyle.MINOR);
		testResultComponent.addComponentToCountLayout(countLayout, testResultIndeterminate);
		testResultComponent.addComponent(countLayout);
		
		return testResultComponent;
	}

	public void refresh() {
		Disease disease = this.dashboardDataProvider.getDisease();
		
		updateCaseComponent(disease);
		updateOutbreakDistrictComponent(disease);
		updateCaseFatalityComponent(disease);
		updateEventComponent(disease);
		updateTestResultComponent(disease);
	}

	private void updateCaseComponent(Disease disease) {
		List<DashboardCaseDto> cases = dashboardDataProvider.getCases();

		//caseDiseaseLabel.setValue("(" + disease.toString() + ")");
		caseCountLabel.setValue(Integer.toString(cases.size()).toString());

		int confirmedCasesCount = (int) cases.stream()
				.filter(c -> c.getCaseClassification() == CaseClassification.CONFIRMED).count();
		caseClassificationConfirmed.updateCountLabel(confirmedCasesCount);
		int probableCasesCount = (int) cases.stream()
				.filter(c -> c.getCaseClassification() == CaseClassification.PROBABLE).count();
		caseClassificationProbable.updateCountLabel(probableCasesCount);
		int suspectCasesCount = (int) cases.stream()
				.filter(c -> c.getCaseClassification() == CaseClassification.SUSPECT).count();
		caseClassificationSuspect.updateCountLabel(suspectCasesCount);
		int notACaseCasesCount = (int) cases.stream()
				.filter(c -> c.getCaseClassification() == CaseClassification.NO_CASE).count();
		caseClassificationNotACase.updateCountLabel(notACaseCasesCount);
		int notYetClassifiedCasesCount = (int) cases.stream()
				.filter(c -> c.getCaseClassification() == CaseClassification.NOT_CLASSIFIED).count();
		caseClassificationNotYetClassified.updateCountLabel(notYetClassifiedCasesCount);
	}

	private void updateOutbreakDistrictComponent(Disease disease) {
		List<DashboardOutbreakDto> outbreaks = dashboardDataProvider.getOutbreaks();
		
		Long districtsCount = outbreaks.stream()
									   .map(o -> o.getDistrict())
									   .distinct()
									   .count();

		outbreakDistrictCountLabel.setValue(districtsCount.toString());
	}

	private void updateCaseFatalityComponent(Disease disease) {
		List<DashboardCaseDto> newCases = dashboardDataProvider.getCases();
		List<DashboardCaseDto> previousCases = dashboardDataProvider.getPreviousCases();

		int casesCount = newCases.size();
		Long fatalCasesCount = newCases.stream().filter((c) -> c.wasFatal()).count();
		long previousFatalCasesCount = previousCases.stream().filter((c) -> c.wasFatal()).count();
		long fatalCasesGrowth = fatalCasesCount - previousFatalCasesCount;
		float fatalityRate = 100 * ((float) fatalCasesCount / (float) (casesCount == 0 ? 1 : casesCount));
		fatalityRate = Math.round(fatalityRate * 100) / 100f;

		// count
		// current
		caseFatalityCountValue.setValue(fatalCasesCount.toString());
		// growth
		String chevronType = "";
		String criticalLevel = "";

		if (fatalCasesGrowth > 0) {
			chevronType = FontAwesome.CHEVRON_UP.getHtml();
			criticalLevel = CssStyles.LABEL_CRITICAL;
		} else if (fatalCasesGrowth < 0) {
			chevronType = FontAwesome.CHEVRON_DOWN.getHtml();
			criticalLevel = CssStyles.LABEL_POSITIVE;
		} else {
			chevronType = FontAwesome.CHEVRON_RIGHT.getHtml();
			criticalLevel = CssStyles.LABEL_IMPORTANT;
		}

		caseFatalityCountGrowth.setValue("<div class=\"v-label v-widget " + criticalLevel + " v-label-" + criticalLevel
				+ " align-center v-label-align-center bold v-label-bold v-has-width\" "
				+ "	  style=\"margin-top: 4px;margin-left: 5px;\">"
				+ "		<span class=\"v-icon\" style=\"font-family: FontAwesome;\">" + chevronType + "		</span>"
				+ "</div>");

		// rate
		caseFatalityRateValue.setValue(fatalityRate + "%");
	}
	
	private void updateEventComponent(Disease disease) {
		List<DashboardEventDto> events = dashboardDataProvider.getEvents();

		eventCountLabel.setValue(Integer.toString(events.size()).toString());

		int confirmedEventsCount = (int) events.stream().filter(e -> e.getEventStatus() == EventStatus.CONFIRMED)
				.count();
		eventStatusConfirmed.updateCountLabel(confirmedEventsCount);
		int possibleEventsCount = (int) events.stream().filter(e -> e.getEventStatus() == EventStatus.POSSIBLE).count();
		eventStatusPossible.updateCountLabel(possibleEventsCount);
		int notAnEventEventsCount = (int) events.stream().filter(e -> e.getEventStatus() == EventStatus.NO_EVENT)
				.count();
		eventStatusNotAnEvent.updateCountLabel(notAnEventEventsCount);
	}

	private void updateTestResultComponent(Disease disease) {
		List<DashboardTestResultDto> testResults = dashboardDataProvider.getTestResults();

		testResultCountLabel.setValue(Integer.toString(testResults.size()).toString());

		int positiveTestResultsCount = (int) testResults.stream()
				.filter(r -> r.getTestResult() == SampleTestResultType.POSITIVE).count();
		testResultPositive.updateCountLabel(positiveTestResultsCount);
		int negativeTestResultsCount = (int) testResults.stream()
				.filter(r -> r.getTestResult() == SampleTestResultType.NEGATIVE).count();
		testResultNegative.updateCountLabel(negativeTestResultsCount);
		int pendingTestResultsCount = (int) testResults.stream()
				.filter(r -> r.getTestResult() == SampleTestResultType.PENDING).count();
		testResultPending.updateCountLabel(pendingTestResultsCount);
		int indeterminateTestResultsCount = (int) testResults.stream()
				.filter(r -> r.getTestResult() == SampleTestResultType.INDETERMINATE).count();
		testResultIndeterminate.updateCountLabel(indeterminateTestResultsCount);
	}
}
