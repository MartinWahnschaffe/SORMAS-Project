package de.symeda.sormas.backend.clinicalcourse;

import java.sql.Timestamp;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;

import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.clinicalcourse.ClinicalVisitCriteria;
import de.symeda.sormas.api.clinicalcourse.ClinicalVisitDto;
import de.symeda.sormas.api.clinicalcourse.ClinicalVisitFacade;
import de.symeda.sormas.api.clinicalcourse.ClinicalVisitIndexDto;
import de.symeda.sormas.api.symptoms.SymptomsDto;
import de.symeda.sormas.api.symptoms.SymptomsHelper;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.backend.caze.CaseFacadeEjb.CaseFacadeEjbLocal;
import de.symeda.sormas.backend.person.PersonFacadeEjb;
import de.symeda.sormas.backend.person.PersonService;
import de.symeda.sormas.backend.symptoms.Symptoms;
import de.symeda.sormas.backend.symptoms.SymptomsFacadeEjb;
import de.symeda.sormas.backend.symptoms.SymptomsFacadeEjb.SymptomsFacadeEjbLocal;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;

@Stateless(name = "ClinicalVisitFacade")
public class ClinicalVisitFacadeEjb implements ClinicalVisitFacade {

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	protected EntityManager em;
	
	@EJB
	private ClinicalVisitService service;
	@EJB
	private UserService userService;
	@EJB
	private CaseFacadeEjbLocal caseFacade;
	@EJB
	private ClinicalCourseService clinicalCourseService;
	@EJB
	SymptomsFacadeEjbLocal symptomsFacade;
	@EJB
	PersonService personService;

	//	private String countPositiveSymptomsQuery;
	
	@Override
	public List<ClinicalVisitIndexDto> getIndexList(ClinicalVisitCriteria criteria) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<ClinicalVisitIndexDto> cq = cb.createQuery(ClinicalVisitIndexDto.class);
		Root<ClinicalVisit> visit = cq.from(ClinicalVisit.class);
		Join<ClinicalVisit, Symptoms> symptoms = visit.join(ClinicalVisit.SYMPTOMS, JoinType.LEFT);

		cq.multiselect(
				visit.get(ClinicalVisit.UUID),
				visit.get(ClinicalVisit.VISIT_DATE_TIME),
				visit.get(ClinicalVisit.VISITING_PERSON),
				visit.get(ClinicalVisit.VISIT_REMARKS),
				symptoms.get(Symptoms.TEMPERATURE),
				symptoms.get(Symptoms.TEMPERATURE_SOURCE),
				symptoms.get(Symptoms.BLOOD_PRESSURE_SYSTOLIC),
				symptoms.get(Symptoms.BLOOD_PRESSURE_DIASTOLIC),
				symptoms.get(Symptoms.HEART_RATE),
				symptoms.get(Symptoms.ID));

		if (criteria != null) {
			cq.where(service.buildCriteriaFilter(criteria, cb, visit));
		}

		cq.orderBy(cb.desc(visit.get(ClinicalVisit.VISIT_DATE_TIME)));

		List<ClinicalVisitIndexDto> results = em.createQuery(cq).getResultList();

		// Build the query to count positive symptoms
		// TODO: Re-activate when issue #964 (replace EclipseLink with Hibernate) has been done
		//		if (countPositiveSymptomsQuery == null) {
		//			String listColumnsQuery = "SELECT column_name FROM information_schema.columns "
		//					+ "WHERE table_schema = 'public' AND data_type IN ('character varying') "
		//					+ "AND TABLE_NAME = '" + Symptoms.TABLE_NAME + "' "
		//					+ "ORDER BY column_name";
		//			List<String> columns = em.createNativeQuery(listColumnsQuery).getResultList();
		//
		//			StringBuilder queryBuilder = new StringBuilder();
		//			queryBuilder.append("SELECT id, ");
		//			for (int i = 0; i < columns.size(); i++) {
		//				queryBuilder.append("CASE " + columns.get(i) + " WHEN 'YES' THEN 1 ELSE 0 END");
		//				if (i < columns.size() - 1) {
		//					queryBuilder.append(" + ");
		//				}
		//			}
		//			queryBuilder.append(" AS count FROM symptoms WHERE id IN (");
		//			countPositiveSymptomsQuery = queryBuilder.toString();
		//		}
		//
		//		if (!results.isEmpty()) {
		//			// Add number of positive symptoms
		//			HashMap<Long, ClinicalVisitIndexDto> symptomVisits = new HashMap<>();
		//			StringBuilder symptomIdsBuilder = new StringBuilder();
		//			for (int i = 0; i < results.size(); i++) {
		//				ClinicalVisitIndexDto result = results.get(i);
		//				symptomIdsBuilder.append(result.getSymptomsId());
		//				if (i < results.size() - 1) {
		//					symptomIdsBuilder.append(", ");
		//				}
		//				symptomVisits.put(result.getSymptomsId(), result);
		//			}
		//			symptomIdsBuilder.append(");");
		//
		//			List<Object[]> symptomCounts = em.createNativeQuery(countPositiveSymptomsQuery + symptomIdsBuilder.toString()).getResultList();
		//
		//			symptomCounts.stream().forEach(c -> {
		//				symptomVisits.get(c[0]).setSignsAndSymptomsCount((Integer) c[1]);
		//			});
		//		}

		return results;
	}
	
	@Override
	public ClinicalVisitDto getClinicalVisitByUuid(String uuid) {
		return toDto(service.getByUuid(uuid));
	}

	@Override
	public ClinicalVisitDto saveClinicalVisit(ClinicalVisitDto clinicalVisit, String caseUuid) {
		SymptomsHelper.updateIsSymptomatic(clinicalVisit.getSymptoms());
		ClinicalVisit entity = fromDto(clinicalVisit);

		service.ensurePersisted(entity);

		// Update case symptoms
		CaseDataDto caze = caseFacade.getCaseDataByUuid(caseUuid);
		SymptomsDto caseSymptoms = caze.getSymptoms();
		SymptomsHelper.updateSymptoms(clinicalVisit.getSymptoms(), caseSymptoms);
		caseFacade.saveCase(caze);

		return toDto(entity);
	}

	@Override
	public void deleteClinicalVisit(String clinicalVisitUuid, String userUuid) {
		User user = userService.getByUuid(userUuid);
		// TODO replace this with a proper right call #944
		if (!user.getUserRoles().contains(UserRole.ADMIN)) {
			throw new UnsupportedOperationException("Only admins are allowed to delete entities");
		}

		ClinicalVisit clinicalVisit = service.getByUuid(clinicalVisitUuid);
		service.delete(clinicalVisit);
	}
	
	public static ClinicalVisitDto toDto(ClinicalVisit source) {
		if (source == null) {
			return null;
		}

		ClinicalVisitDto target = new ClinicalVisitDto();
		DtoHelper.fillDto(target, source);

		target.setClinicalCourse(ClinicalCourseFacadeEjb.toReferenceDto(source.getClinicalCourse()));
		target.setSymptoms(SymptomsFacadeEjb.toDto(source.getSymptoms()));
		target.setDisease(source.getDisease());
		target.setPerson(PersonFacadeEjb.toReferenceDto(source.getPerson()));
		target.setVisitDateTime(source.getVisitDateTime());
		target.setVisitRemarks(source.getVisitRemarks());
		target.setVisitingPerson(source.getVisitingPerson());

		return target;
	}
	
	public ClinicalVisit fromDto(@NotNull ClinicalVisitDto source) {
		ClinicalVisit target = service.getByUuid(source.getUuid());

		if (target == null) {
			target = new ClinicalVisit();
			target.setUuid(source.getUuid());
			if (source.getCreationDate() != null) {
				target.setCreationDate(new Timestamp(source.getCreationDate().getTime()));
			}
		}

		DtoHelper.validateDto(source, target);

		target.setClinicalCourse(clinicalCourseService.getByReferenceDto(source.getClinicalCourse()));
		target.setSymptoms(symptomsFacade.fromDto(source.getSymptoms()));
		target.setDisease(source.getDisease());
		target.setPerson(personService.getByReferenceDto(source.getPerson()));
		target.setVisitDateTime(source.getVisitDateTime());
		target.setVisitRemarks(source.getVisitRemarks());
		target.setVisitingPerson(source.getVisitingPerson());

		return target;
	}

	@LocalBean
	@Stateless
	public static class ClinicalVisitFacadeEjbLocal extends ClinicalVisitFacadeEjb {
	}
	
}
