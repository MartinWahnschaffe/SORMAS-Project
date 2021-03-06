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
package de.symeda.sormas.backend.disease;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.caze.CaseCriteria;
import de.symeda.sormas.api.disease.DiseaseBurdenDto;
import de.symeda.sormas.api.disease.DiseaseFacade;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.caze.CaseFacadeEjb.CaseFacadeEjbLocal;
import de.symeda.sormas.backend.common.AbstractAdoService;
import de.symeda.sormas.backend.event.Event;
import de.symeda.sormas.backend.location.Location;
import de.symeda.sormas.backend.outbreak.Outbreak;
import de.symeda.sormas.backend.person.Person;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.util.ModelConstants;
//import de.symeda.sormas.ui.UserProvider;

/**
 * Provides the application configuration settings
 */
@Stateless(name="DiseaseFacade")
public class DiseaseFacadeEjb implements DiseaseFacade {
	
	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	protected EntityManager em;
	
	@EJB
	private CaseFacadeEjbLocal caseFacade;
	
	//@Override
	public List<DiseaseBurdenDto> getDiseaseBurdenForDashboard(
			RegionReferenceDto regionRef,
			DistrictReferenceDto districtRef, 
			Date from, 
			Date to,
			Date previousFrom,
			Date previousTo,
			String userUuid) {
		
		//diseases
		List<Disease> diseases = Stream.of(Disease.values()).collect(Collectors.toList());
				
		//new cases
		CaseCriteria caseCriteria = new CaseCriteria()
				.newCaseDateBetween(from, to, null)
				.region(regionRef)
				.district(districtRef);

		Map<Disease, Long> newCases = caseFacade.getCaseCountPerDisease(caseCriteria, userUuid);
		
		//previous cases
		caseCriteria.newCaseDateBetween(previousFrom, previousTo, null);
		
		Map<Disease, Long> previousCases = caseFacade.getCaseCountPerDisease(caseCriteria, userUuid);
				
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = null;
		Predicate filter = null;
		List<Object[]> results;
		
		//events
		cq = cb.createQuery(Object[].class);
		Root<Event> event = cq.from(Event.class);
		Join<Event, Location> eventLocation = event.join(Event.EVENT_LOCATION, JoinType.LEFT);
		cq.multiselect(event.get(Event.DISEASE), cb.count(event));
		cq.groupBy(event.get(Event.DISEASE));
		
		filter = null;
		if (from != null || to != null) {
			filter = AbstractAdoService.and(cb, filter, cb.between(event.get(Event.REPORT_DATE_TIME), from, to));
		}
		if (districtRef != null) {
			filter = AbstractAdoService.and(cb, filter, cb.equal(eventLocation.join(Location.DISTRICT, JoinType.LEFT).get(District.UUID), districtRef.getUuid()));
		}
		else if (regionRef != null) {
			filter = AbstractAdoService.and(cb, filter, cb.equal(eventLocation.join(Location.REGION, JoinType.LEFT).get(Region.UUID), regionRef.getUuid()));
		}
		if (filter != null) {
			cq.where(filter);
		}
		
		results = em.createQuery(cq).getResultList();
		
		Map<Disease, Long> events = results.stream().collect(
				Collectors.toMap(e -> (Disease) e[0], e -> (Long) e[1]));
					
		//outbreaks
		cq = cb.createQuery(Object[].class);
		Root<Outbreak> outbreak = cq.from(Outbreak.class);
		cq.multiselect(outbreak.get(Outbreak.DISEASE), cb.countDistinct(outbreak.get(Outbreak.DISTRICT)));
		cq.groupBy(outbreak.get(Outbreak.DISEASE));
		
		filter = null;
		if (from != null || to != null) {
			filter = AbstractAdoService.and(cb, filter, cb.between(outbreak.get(Outbreak.REPORT_DATE), from, to));
		}
		if (districtRef != null) {
			filter = AbstractAdoService.and(cb, filter, cb.equal(outbreak.join(Outbreak.DISTRICT, JoinType.LEFT).get(District.UUID), districtRef.getUuid()));
		}
		else if (regionRef != null) {
			filter = AbstractAdoService.and(cb, filter, cb.equal(outbreak.join(Outbreak.DISTRICT, JoinType.LEFT).join(District.REGION, JoinType.LEFT).get(Region.UUID), regionRef.getUuid()));
		}
		if (filter != null) {
			cq.where(filter);
		}
		
		results = em.createQuery(cq).getResultList();
		
		Map<Disease, Long> outbreaks = results.stream().collect(
				Collectors.toMap(e -> (Disease) e[0], e -> (Long) e[1]));
				
		//case fatalities
		cq = cb.createQuery(Object[].class);
		Root<Person> deceasedPerson = cq.from(Person.class);
		cq.multiselect(deceasedPerson.get(Person.CAUSE_OF_DEATH_DISEASE), cb.count(deceasedPerson));
		cq.groupBy(deceasedPerson.get(Person.CAUSE_OF_DEATH_DISEASE));

		filter = cb.isNotNull(deceasedPerson.get(Person.CAUSE_OF_DEATH_DISEASE));
		if (from != null || to != null) {
			filter = AbstractAdoService.and(cb, filter, cb.between(deceasedPerson.get(Person.DEATH_DATE), from, to));
		}
		if (districtRef != null) {
			filter = AbstractAdoService.and(cb, filter, cb.equal(deceasedPerson.join(Person.ADDRESS, JoinType.LEFT).join(Location.DISTRICT, JoinType.LEFT).get(District.UUID), districtRef.getUuid()));
		}
		else if (regionRef != null) {
			filter = AbstractAdoService.and(cb, filter, cb.equal(deceasedPerson.join(Person.ADDRESS, JoinType.LEFT).join(Location.DISTRICT, JoinType.LEFT).join(District.REGION, JoinType.LEFT).get(Region.UUID), regionRef.getUuid()));
		}
		if (filter != null) {
			cq.where(filter);
		}
		
		results = em.createQuery(cq).getResultList();
		
		Map<Disease, Long> caseFatalities = results.stream().collect(
				Collectors.toMap(e -> (Disease) e[0], e -> (Long) e[1]));
		
		//build diseasesBurden
		List<DiseaseBurdenDto> diseasesBurden = diseases.stream().map(disease -> {
			Long caseCount = newCases.getOrDefault(disease, 0L);
			Long previousCaseCount = previousCases.getOrDefault(disease, 0L);
			Long eventCount = events.getOrDefault(disease, 0L);
			Long outbreakDistrictCount = outbreaks.getOrDefault(disease, 0L);
			Long caseFatalityCount = caseFatalities.getOrDefault(disease, 0L);
			
			return new DiseaseBurdenDto(disease, caseCount, previousCaseCount, eventCount, outbreakDistrictCount, caseFatalityCount);
			
		}).collect(Collectors.toList());
		
		return diseasesBurden;
	}
	
	
	//@Override
	public List<DiseaseBurdenDto> getDiseaseBurdenForDashboard_old(
			RegionReferenceDto regionRef,
			DistrictReferenceDto districtRef, 
			Date from, 
			Date to,
			Date previousFrom,
			Date previousTo,
			String userUuid) {
		
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = null;
		
		//diseases
		List<Disease> diseases = Stream.of(Disease.values()).collect(Collectors.toList());
		
		
		//new cases
		cq = cb.createQuery(Object[].class);
		Root<Case> caze = cq.from(Case.class);
		cq.multiselect(caze.get(Case.DISEASE), cb.count(caze));
		cq.groupBy(caze.get(Case.DISEASE));
		
		Predicate filter = null;
		if (from != null || to != null) {
			filter = AbstractAdoService.and(cb, filter, cb.between(caze.get(Case.REPORT_DATE), from, to));
		}
		if (districtRef != null) {
			filter = AbstractAdoService.and(cb, filter, cb.equal(caze.join(Case.DISTRICT, JoinType.LEFT).get(District.UUID), districtRef.getUuid()));
		}
		else if (regionRef != null) {
			filter = AbstractAdoService.and(cb, filter, cb.equal(caze.join(Case.REGION, JoinType.LEFT).get(Region.UUID), regionRef.getUuid()));
		}
		if (filter != null) {
			cq.where(filter);
		}
		
		List<Object[]> cases = em.createQuery(cq).getResultList();
		
		//previous cases
		cq = cb.createQuery(Object[].class);
		caze = cq.from(Case.class);
		cq.multiselect(caze.get(Case.DISEASE), cb.count(caze));
		cq.groupBy(caze.get(Case.DISEASE));
		
		filter = null;
		if (previousFrom != null || to != null) {
			//filter = AbstractAdoService.and(cb, filter, createActiveCaseFilter(cb, caze, previousFrom, previousTo));
		}
		if (districtRef != null) {
			filter = AbstractAdoService.and(cb, filter, cb.equal(caze.join(Case.DISTRICT, JoinType.LEFT).get(District.UUID), districtRef.getUuid()));
		}
		else if (regionRef != null) {
			filter = AbstractAdoService.and(cb, filter, cb.equal(caze.join(Case.REGION, JoinType.LEFT).get(Region.UUID), regionRef.getUuid()));
		}
		if (filter != null) {
			cq.where(filter);
		}
		
		List<Object[]> previousCases = em.createQuery(cq).getResultList();
				
		//events
		cq = cb.createQuery(Object[].class);
		Root<Event> event = cq.from(Event.class);
		Join<Event, Location> eventLocation = event.join(Event.EVENT_LOCATION, JoinType.LEFT);
		cq.multiselect(event.get(Event.DISEASE), cb.count(event));
		cq.groupBy(event.get(Event.DISEASE));
		
		filter = null;
		if (from != null || to != null) {
			filter = AbstractAdoService.and(cb, filter, cb.between(event.get(Event.REPORT_DATE_TIME), from, to));
		}
		if (districtRef != null) {
			filter = AbstractAdoService.and(cb, filter, cb.equal(eventLocation.join(Location.DISTRICT, JoinType.LEFT).get(District.UUID), districtRef.getUuid()));
		}
		else if (regionRef != null) {
			filter = AbstractAdoService.and(cb, filter, cb.equal(eventLocation.join(Location.REGION, JoinType.LEFT).get(Region.UUID), regionRef.getUuid()));
		}
		if (filter != null) {
			cq.where(filter);
		}
		
		List<Object[]> events = em.createQuery(cq).getResultList();
				
		//outbreaks
		cq = cb.createQuery(Object[].class);
		Root<Outbreak> outbreak = cq.from(Outbreak.class);
		cq.multiselect(outbreak.get(Outbreak.DISEASE), cb.countDistinct(outbreak.get(Outbreak.DISTRICT)));
		cq.groupBy(outbreak.get(Outbreak.DISEASE));
		
		filter = null;
		if (from != null || to != null) {
			filter = AbstractAdoService.and(cb, filter, cb.between(outbreak.get(Outbreak.REPORT_DATE), from, to));
		}
		if (districtRef != null) {
			filter = AbstractAdoService.and(cb, filter, cb.equal(outbreak.join(Outbreak.DISTRICT, JoinType.LEFT).get(District.UUID), districtRef.getUuid()));
		}
		else if (regionRef != null) {
			filter = AbstractAdoService.and(cb, filter, cb.equal(outbreak.join(Outbreak.DISTRICT, JoinType.LEFT).join(District.REGION, JoinType.LEFT).get(Region.UUID), regionRef.getUuid()));
		}
		if (filter != null) {
			cq.where(filter);
		}
		
		List<Object[]> outbreaks = em.createQuery(cq).getResultList();
		
		//case fatalities
		cq = cb.createQuery(Object[].class);
		Root<Person> deceasedPerson = cq.from(Person.class);
		cq.multiselect(deceasedPerson.get(Person.CAUSE_OF_DEATH_DISEASE), cb.count(deceasedPerson));
		cq.groupBy(deceasedPerson.get(Person.CAUSE_OF_DEATH_DISEASE));

		filter = cb.isNotNull(deceasedPerson.get(Person.CAUSE_OF_DEATH_DISEASE));
		if (from != null || to != null) {
			filter = AbstractAdoService.and(cb, filter, cb.between(deceasedPerson.get(Person.DEATH_DATE), from, to));
		}
		if (districtRef != null) {
			filter = AbstractAdoService.and(cb, filter, cb.equal(deceasedPerson.join(Person.ADDRESS, JoinType.LEFT).join(Location.DISTRICT, JoinType.LEFT).get(District.UUID), districtRef.getUuid()));
		}
		else if (regionRef != null) {
			filter = AbstractAdoService.and(cb, filter, cb.equal(deceasedPerson.join(Person.ADDRESS, JoinType.LEFT).join(Location.DISTRICT, JoinType.LEFT).join(District.REGION, JoinType.LEFT).get(Region.UUID), regionRef.getUuid()));
		}
		if (filter != null) {
			cq.where(filter);
		}
		
		List<Object[]> caseFatalities = em.createQuery(cq).getResultList();
		
		//build diseases
		List<DiseaseBurdenDto> diseasesBurden = diseases.stream().map(disease -> {
			Long caseCount = cases.stream().filter(o -> o[0] == disease).map(o -> (Long)o[1]).findFirst().orElse(0L);
			Long previousCaseCount = previousCases.stream().filter(o -> o[0] == disease).map(o -> (Long)o[1]).findFirst().orElse(0L);
			Long eventCount = events.stream().filter(o -> o[0] == disease).map(o -> (Long)o[1]).findFirst().orElse(0L);
			Long outbreakDistrictCount = outbreaks.stream().filter(o -> o[0] == disease).map(o -> (Long)o[1]).findFirst().orElse(0L);
			Long caseFatalityCount = caseFatalities.stream().filter(o -> o[0] == disease).map(o -> (Long)o[1]).findFirst().orElse(0L);
			
			return new DiseaseBurdenDto(disease, caseCount, previousCaseCount, eventCount, outbreakDistrictCount, caseFatalityCount);
			
		}).collect(Collectors.toList());
		
		return diseasesBurden;
	}
	
	@LocalBean
	@Stateless
	public static class DiseaseFacadeEjbLocal extends DiseaseFacadeEjb {
	}
	
}