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
package de.symeda.sormas.backend.caze;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.auth0.jwt.internal.org.apache.commons.lang3.StringUtils;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.CaseExportDto;
import de.symeda.sormas.api.caze.CaseIndexDto;
import de.symeda.sormas.api.caze.CaseOutcome;
import de.symeda.sormas.api.caze.DashboardCaseDto;
import de.symeda.sormas.api.caze.InvestigationStatus;
import de.symeda.sormas.api.caze.MapCaseDto;
import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.contact.FollowUpStatus;
import de.symeda.sormas.api.epidata.EpiDataTravelDto;
import de.symeda.sormas.api.facility.FacilityReferenceDto;
import de.symeda.sormas.api.hospitalization.PreviousHospitalizationDto;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.person.PresentCondition;
import de.symeda.sormas.api.region.CommunityReferenceDto;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.sample.SampleMaterial;
import de.symeda.sormas.api.sample.PathogenTestResultType;
import de.symeda.sormas.api.sample.PathogenTestType;
import de.symeda.sormas.api.symptoms.SymptomState;
import de.symeda.sormas.api.task.TaskContext;
import de.symeda.sormas.api.task.TaskDto;
import de.symeda.sormas.api.task.TaskStatus;
import de.symeda.sormas.api.task.TaskType;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.api.utils.OutdatedEntityException;
import de.symeda.sormas.backend.AbstractBeanTest;
import de.symeda.sormas.backend.TestDataCreator.RDCF;
import de.symeda.sormas.backend.util.DateHelper8;
import de.symeda.sormas.backend.util.DtoHelper;

public class CaseFacadeEjbTest extends AbstractBeanTest {

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testDiseaseChangeUpdatesContacts() {
		RDCF rdcf = creator.createRDCF("Region", "District", "Community", "Facility");
		UserDto user = creator.createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(),
				"Surv", "Sup", UserRole.SURVEILLANCE_SUPERVISOR);
		PersonDto cazePerson = creator.createPerson("Case", "Person");
		CaseDataDto caze = creator.createCase(user.toReference(), cazePerson.toReference(), Disease.EVD,
				CaseClassification.PROBABLE, InvestigationStatus.PENDING, new Date(), rdcf);
		PersonDto contactPerson = creator.createPerson("Contact", "Person");
		ContactDto contact = creator.createContact(user.toReference(), user.toReference(), contactPerson.toReference(),
				caze.toReference(), new Date(), new Date());

		// Follow-up status and duration should be set to the requirements for EVD
		assertEquals(FollowUpStatus.FOLLOW_UP, contact.getFollowUpStatus());
		assertEquals(LocalDate.now().plusDays(21), DateHelper8.toLocalDate(contact.getFollowUpUntil()));

		caze.setDisease(Disease.MEASLES);
		caze = getCaseFacade().saveCase(caze);

		// Follow-up status and duration should be set to no follow-up and null
		// respectively because
		// Measles does not require a follow-up
		contact = getContactFacade().getContactByUuid(contact.getUuid());
		assertEquals(FollowUpStatus.NO_FOLLOW_UP, contact.getFollowUpStatus());
		assertEquals(null, contact.getFollowUpUntil());
	}
	
	@Test
	public void testMovingCaseUpdatesTaskAssigneeAndCreatesPreviousHospitalization() {
		RDCF rdcf = creator.createRDCF("Region", "District", "Community", "Facility");
		UserDto user = creator.createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(),
				"Surv", "Sup", UserRole.SURVEILLANCE_SUPERVISOR);
		PersonDto cazePerson = creator.createPerson("Case", "Person");
		CaseDataDto caze = creator.createCase(user.toReference(), cazePerson.toReference(), Disease.EVD,
				CaseClassification.PROBABLE, InvestigationStatus.PENDING, new Date(), rdcf);
		UserDto caseOfficer = creator.createUser(rdcf.region.getUuid(), rdcf.district.getUuid(),
				rdcf.facility.getUuid(), "Case", "Officer", UserRole.CASE_OFFICER);
		TaskDto pendingTask = creator.createTask(TaskContext.CASE, TaskType.CASE_INVESTIGATION, TaskStatus.PENDING,
				caze.toReference(), null, null, new Date(), user.toReference());
		TaskDto doneTask = creator.createTask(TaskContext.CASE, TaskType.CASE_INVESTIGATION, TaskStatus.DONE,
				caze.toReference(), null, null, new Date(), user.toReference());

		RDCF newRDCF = creator.createRDCF("New Region", "New District", "New Community", "New Facility");
		
		caze.setRegion(new RegionReferenceDto(newRDCF.region.getUuid()));
		caze.setDistrict(new DistrictReferenceDto(newRDCF.district.getUuid()));
		caze.setCommunity(new CommunityReferenceDto(newRDCF.community.getUuid()));
		caze.setHealthFacility(new FacilityReferenceDto(newRDCF.facility.getUuid()));
		caze.setSurveillanceOfficer(caseOfficer.toReference());
		caze = getCaseFacade().saveAndTransferCase(caze);

		caze = getCaseFacade().getCaseDataByUuid(caze.getUuid());
		pendingTask = getTaskFacade().getByUuid(pendingTask.getUuid());
		doneTask = getTaskFacade().getByUuid(doneTask.getUuid());

		// Case should have the new region, district, community and facility set
		assertEquals(caze.getRegion().getUuid(), newRDCF.region.getUuid());
		assertEquals(caze.getDistrict().getUuid(), newRDCF.district.getUuid());
		assertEquals(caze.getCommunity().getUuid(), newRDCF.community.getUuid());
		assertEquals(caze.getHealthFacility().getUuid(), newRDCF.facility.getUuid());

		// Pending task should've been reassigned to the case officer, done task should
		// still be assigned to the surveillance supervisor
		assertEquals(pendingTask.getAssigneeUser().getUuid(), caseOfficer.getUuid());
		assertEquals(doneTask.getAssigneeUser().getUuid(), user.getUuid());

		// A previous hospitalization with the former facility should have been created
		List<PreviousHospitalizationDto> previousHospitalizations = caze.getHospitalization()
				.getPreviousHospitalizations();
		assertEquals(previousHospitalizations.size(), 1);
	}

	@Test
	public void testDashboardCaseListCreation() {

		RDCF rdcf = creator.createRDCF("Region", "District", "Community", "Facility");
		UserDto user = creator.createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(),
				"Surv", "Sup", UserRole.SURVEILLANCE_SUPERVISOR);
		PersonDto cazePerson = creator.createPerson("Case", "Person");
		CaseDataDto caze = creator.createCase(user.toReference(), cazePerson.toReference(), Disease.EVD,
				CaseClassification.PROBABLE, InvestigationStatus.PENDING, new Date(), rdcf);

		List<DashboardCaseDto> dashboardCaseDtos = getCaseFacade().getNewCasesForDashboard(caze.getRegion(),
				caze.getDistrict(), caze.getDisease(), DateHelper.subtractDays(new Date(), 1),
				DateHelper.addDays(new Date(), 1), user.getUuid());

		// List should have one entry
		assertEquals(1, dashboardCaseDtos.size());
	}

	@Test
	public void testMapCaseListCreation() {

		RDCF rdcf = creator.createRDCF("Region", "District", "Community", "Facility");
		UserDto user = creator.createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(),
				"Surv", "Sup", UserRole.SURVEILLANCE_SUPERVISOR);
		PersonDto cazePerson = creator.createPerson("Case", "Person");
		CaseDataDto caze = creator.createCase(user.toReference(), cazePerson.toReference(), Disease.EVD,
				CaseClassification.PROBABLE, InvestigationStatus.PENDING, new Date(), rdcf);

		List<MapCaseDto> mapCaseDtos = getCaseFacade().getCasesForMap(caze.getRegion(), caze.getDistrict(),
				caze.getDisease(), DateHelper.subtractDays(new Date(), 1), DateHelper.addDays(new Date(), 1),
				user.getUuid());

		// List should have one entry
		assertEquals(1, mapCaseDtos.size());
	}

	@Test
	public void testGetIndexList() {

		RDCF rdcf = creator.createRDCF("Region", "District", "Community", "Facility");
		UserDto user = creator.createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(),
				"Surv", "Sup", UserRole.SURVEILLANCE_SUPERVISOR);
		PersonDto cazePerson = creator.createPerson("Case", "Person");
		creator.createCase(user.toReference(), cazePerson.toReference(), Disease.EVD, CaseClassification.PROBABLE,
				InvestigationStatus.PENDING, new Date(), rdcf);

		List<CaseIndexDto> results = getCaseFacade().getIndexList(user.getUuid(), null);

		// List should have one entry
		assertEquals(1, results.size());
	}
	
	@Test
	public void testGetExportList() {

		RDCF rdcf = creator.createRDCF("Region", "District", "Community", "Facility");
		UserDto user = creator.createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(),
				"Surv", "Sup", UserRole.SURVEILLANCE_SUPERVISOR);
		PersonDto cazePerson = creator.createPerson("Case", "Person");
		CaseDataDto caze = creator.createCase(user.toReference(), cazePerson.toReference(), Disease.EVD, CaseClassification.PROBABLE,
				InvestigationStatus.PENDING, new Date(), rdcf);

		cazePerson.getAddress().setCity("City");
		getPersonFacade().savePerson(cazePerson);
		
		EpiDataTravelDto travel = new EpiDataTravelDto();
		travel.setUuid(DataHelper.createUuid());
		travel.setTravelDestination("Ghana");
		travel.setTravelDateFrom(new Date());
		travel.setTravelDateTo(new Date());
		caze.getEpiData().getTravels().add(travel);
		caze.getSymptoms().setAbdominalPain(SymptomState.YES);
		caze = getCaseFacade().saveCase(caze);
		
		creator.createSample(caze.toReference(), new Date(), new Date(), user.toReference(), SampleMaterial.BLOOD, rdcf.facility);
		creator.createSampleTest(caze, PathogenTestType.ANTIGEN_DETECTION, PathogenTestResultType.POSITIVE);
		
		List<CaseExportDto> results = getCaseFacade().getExportList(user.getUuid(), null, 0, 100);

		// List should have one entry
		assertEquals(1, results.size());
		
		// Make sure that everything that is added retrospectively (symptoms, sample dates, lab results, address, travel history) is present
		CaseExportDto exportDto = results.get(0);
		assertTrue(StringUtils.isNotEmpty(exportDto.getSymptoms()));
		assertTrue(StringUtils.isNotEmpty(exportDto.getSampleDates()));
		assertTrue(StringUtils.isNotEmpty(exportDto.getLabResults()));
		assertTrue(StringUtils.isNotEmpty(exportDto.getAddress()));
		assertTrue(StringUtils.isNotEmpty(exportDto.getTravelHistory()));
	}

	@Test
	public void testCaseDeletion() {

		RDCF rdcf = creator.createRDCF("Region", "District", "Community", "Facility");
		UserDto user = creator.createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(),
				"Surv", "Sup", UserRole.SURVEILLANCE_SUPERVISOR);
		UserDto admin = creator.createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(),
				"Ad", "Min", UserRole.ADMIN);
		String adminUuid = admin.getUuid();
		PersonDto cazePerson = creator.createPerson("Case", "Person");
		CaseDataDto caze = creator.createCase(user.toReference(), cazePerson.toReference(), Disease.EVD,
				CaseClassification.PROBABLE, InvestigationStatus.PENDING, new Date(), rdcf);
		PersonDto contactPerson = creator.createPerson("Contact", "Person");
		ContactDto contact = creator.createContact(user.toReference(), user.toReference(), contactPerson.toReference(), caze.toReference(),
				new Date(), new Date());
		TaskDto task = creator.createTask(TaskContext.CASE, TaskType.CASE_INVESTIGATION, TaskStatus.PENDING,
				caze.toReference(), null, null, new Date(), user.toReference());
		SampleDto sample = creator.createSample(caze.toReference(), new Date(), new Date(), user.toReference(), SampleMaterial.BLOOD,
				rdcf.facility);

		// Database should contain the created case, contact, task and sample
		assertNotNull(getCaseFacade().getCaseDataByUuid(caze.getUuid()));
		assertNotNull(getContactFacade().getContactByUuid(contact.getUuid()));
		assertNotNull(getTaskFacade().getByUuid(task.getUuid()));
		assertNotNull(getSampleFacade().getSampleByUuid(sample.getUuid()));

		getCaseFacade().deleteCase(caze.toReference(), adminUuid);

		// Database should not contain the deleted case, contact, task and sample
		assertNull(getCaseFacade().getCaseDataByUuid(caze.getUuid()));
		assertNull(getContactFacade().getContactByUuid(contact.getUuid()));
		assertNull(getTaskFacade().getByUuid(task.getUuid()));
		assertNull(getSampleFacade().getSampleByUuid(sample.getUuid()));
	}

	@Test
	public void testOutcomePersonConditionUpdate() {

		RDCF rdcf = creator.createRDCF("Region", "District", "Community", "Facility");
		UserDto user = creator.createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(),
				"Surv", "Sup", UserRole.SURVEILLANCE_SUPERVISOR);
		PersonDto cazePerson = creator.createPerson("Case", "Person");
		CaseDataDto firstCase = creator.createCase(user.toReference(), cazePerson.toReference(), Disease.EVD,
				CaseClassification.PROBABLE, InvestigationStatus.PENDING, new Date(), rdcf);

		// case deceased -> person has to set to dead
		firstCase.setOutcome(CaseOutcome.DECEASED);
		firstCase = getCaseFacade().saveCase(firstCase);
		assertNotNull(firstCase.getOutcomeDate());
		cazePerson = getPersonFacade().getPersonByUuid(cazePerson.getUuid());
		assertEquals(PresentCondition.DEAD, cazePerson.getPresentCondition());
		assertEquals(firstCase.getDisease(), cazePerson.getCauseOfDeathDisease());

		// case has no outcome again -> person should be alive
		firstCase.setOutcome(CaseOutcome.NO_OUTCOME);
		firstCase = getCaseFacade().saveCase(firstCase);
		assertNull(firstCase.getOutcomeDate());
		cazePerson = getPersonFacade().getPersonByUuid(cazePerson.getUuid());
		assertEquals(PresentCondition.ALIVE, cazePerson.getPresentCondition());

		// case deceased -> person has to set to dead
		firstCase.setOutcome(CaseOutcome.DECEASED);
		firstCase = getCaseFacade().saveCase(firstCase);
		cazePerson = getPersonFacade().getPersonByUuid(cazePerson.getUuid());
		assertEquals(PresentCondition.DEAD, cazePerson.getPresentCondition());

		// person alive again -> case has to be reset to no outcome
		cazePerson.setPresentCondition(PresentCondition.ALIVE);
		cazePerson = getPersonFacade().savePerson(cazePerson);

		firstCase = getCaseFacade().getCaseDataByUuid(firstCase.getUuid());
		assertEquals(CaseOutcome.NO_OUTCOME, firstCase.getOutcome());
		assertNull(firstCase.getOutcomeDate());

		// additional case for the the person. set to deceased -> person has to be dead
		// and other no outcome cases have to be set to deceased
		CaseDataDto secondCase = creator.createCase(user.toReference(), cazePerson.toReference(), Disease.EVD,
				CaseClassification.PROBABLE, InvestigationStatus.PENDING, new Date(), rdcf);
		secondCase.setOutcome(CaseOutcome.RECOVERED);
		secondCase = getCaseFacade().saveCase(secondCase);
		CaseDataDto thirdCase = creator.createCase(user.toReference(), cazePerson.toReference(), Disease.EVD,
				CaseClassification.PROBABLE, InvestigationStatus.PENDING, new Date(), rdcf);
		thirdCase.setOutcome(CaseOutcome.DECEASED);
		thirdCase = getCaseFacade().saveCase(thirdCase);

		cazePerson = getPersonFacade().getPersonByUuid(cazePerson.getUuid());
		assertEquals(PresentCondition.DEAD, cazePerson.getPresentCondition());
		firstCase = getCaseFacade().getCaseDataByUuid(firstCase.getUuid());
		assertEquals(CaseOutcome.DECEASED, firstCase.getOutcome());
		assertNotNull(firstCase.getOutcomeDate());
		secondCase = getCaseFacade().getCaseDataByUuid(secondCase.getUuid());
		assertEquals(CaseOutcome.RECOVERED, secondCase.getOutcome());

		// person alive again -> deceased cases have to be set to no outcome
		cazePerson.setPresentCondition(PresentCondition.ALIVE);
		cazePerson = getPersonFacade().savePerson(cazePerson);
		firstCase = getCaseFacade().getCaseDataByUuid(firstCase.getUuid());
		assertEquals(CaseOutcome.NO_OUTCOME, firstCase.getOutcome());
		secondCase = getCaseFacade().getCaseDataByUuid(secondCase.getUuid());
		assertEquals(CaseOutcome.RECOVERED, secondCase.getOutcome());
		thirdCase = getCaseFacade().getCaseDataByUuid(thirdCase.getUuid());
		assertEquals(CaseOutcome.NO_OUTCOME, thirdCase.getOutcome());
	}

	@Test
	public void testOutcomePersonConditionUpdateForAppSync() throws InterruptedException {
		RDCF rdcf = creator.createRDCF("Region", "District", "Community", "Facility");
		UserDto user = creator.createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(),
				"Surv", "Sup", UserRole.SURVEILLANCE_SUPERVISOR);
		PersonDto cazePerson = creator.createPerson("Case", "Person");
		CaseDataDto firstCase = creator.createCase(user.toReference(), cazePerson.toReference(), Disease.EVD,
				CaseClassification.PROBABLE, InvestigationStatus.PENDING, new Date(), rdcf);

		// simulate short delay between transmissions
		Thread.sleep(DtoHelper.CHANGE_DATE_TOLERANCE_MS + 1);

		// case deceased -> person has to set to dead
		firstCase.setOutcome(CaseOutcome.DECEASED);
		cazePerson.setPresentCondition(PresentCondition.DEAD);
		cazePerson = getPersonFacade().savePerson(cazePerson);

		// this should throw an exception
		exception.expect(OutdatedEntityException.class);
		firstCase = getCaseFacade().saveCase(firstCase);
	}
	
	@Test
	public void testArchiveAndDearchiveCase() {
		RDCF rdcf = creator.createRDCF("Region", "District", "Community", "Facility");
		UserDto user = creator.createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(),
				"Surv", "Sup", UserRole.SURVEILLANCE_SUPERVISOR);
		PersonDto cazePerson = creator.createPerson("Case", "Person");
		CaseDataDto caze = creator.createCase(user.toReference(), cazePerson.toReference(), Disease.EVD,
				CaseClassification.PROBABLE, InvestigationStatus.PENDING, new Date(), rdcf);
		Date testStartDate = new Date();
		
		// getAllActiveCases and getAllUuids should return length 1
		assertEquals(1, getCaseFacade().getAllActiveCasesAfter(null, user.getUuid()).size());
		assertEquals(1, getCaseFacade().getAllActiveUuids(user.getUuid()).size());
		
		getCaseFacade().archiveOrDearchiveCase(caze.getUuid(), true);
		
		// getAllActiveCases and getAllUuids should return length 0
		assertEquals(0, getCaseFacade().getAllActiveCasesAfter(null, user.getUuid()).size());
		assertEquals(0, getCaseFacade().getAllActiveUuids(user.getUuid()).size());
		
		// getArchivedUuidsSince should return length 1
		assertEquals(1, getCaseFacade().getArchivedUuidsSince(user.getUuid(), testStartDate).size());
		
		getCaseFacade().archiveOrDearchiveCase(caze.getUuid(), false);

		// getAllActiveCases and getAllUuids should return length 1
		assertEquals(1, getCaseFacade().getAllActiveCasesAfter(null, user.getUuid()).size());
		assertEquals(1, getCaseFacade().getAllActiveUuids(user.getUuid()).size());

		// getArchivedUuidsSince should return length 0
		assertEquals(0, getCaseFacade().getArchivedUuidsSince(user.getUuid(), testStartDate).size());
	}
}
