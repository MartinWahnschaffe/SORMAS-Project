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
package de.symeda.sormas.backend.region;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
import javax.validation.constraints.NotNull;

import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.region.DistrictCriteria;
import de.symeda.sormas.api.region.DistrictDto;
import de.symeda.sormas.api.region.DistrictFacade;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;

@Stateless(name = "DistrictFacade")
public class DistrictFacadeEjb implements DistrictFacade {

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	protected EntityManager em;

	@EJB
	private DistrictService districtService;
	@EJB
	private UserService userService;
	@EJB
	private RegionService regionService;

	@Override
	public List<DistrictReferenceDto> getAllAsReference() {
		return districtService.getAll(District.NAME, true).stream()
				.map(f -> toReferenceDto(f))
				.collect(Collectors.toList());
	}

	@Override
	public List<DistrictReferenceDto> getAllByRegion(String regionUuid) {

		Region region = regionService.getByUuid(regionUuid);

		return region.getDistricts().stream()
				.map(f -> toReferenceDto(f))
				.collect(Collectors.toList());
	}

	@Override
	public List<DistrictDto> getAllAfter(Date date) {
		return districtService.getAllAfter(date, null).stream()
				.map(c -> toDto(c))
				.collect(Collectors.toList());
	}

	@Override
	public List<DistrictDto> getIndexList(DistrictCriteria criteria) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<DistrictDto> cq = cb.createQuery(DistrictDto.class);
		Root<District> district = cq.from(District.class);
		Join<District, Region> region = district.join(District.REGION, JoinType.LEFT);

		if (criteria != null) {
			Predicate filter = districtService.buildCriteriaFilter(criteria, cb, district);
			if (filter != null) {
				cq.where(filter);
			}
		}
		
		cq.multiselect(district.get(District.CREATION_DATE), district.get(District.CHANGE_DATE), district.get(District.UUID), 
				district.get(District.NAME), district.get(District.EPID_CODE), district.get(District.POPULATION), district.get(District.GROWTH_RATE), 
				region.get(Region.UUID), region.get(Region.NAME));
		cq.orderBy(cb.asc(region.get(Region.NAME)), cb.asc(district.get(District.NAME)));

		List<DistrictDto> resultList = em.createQuery(cq).getResultList();
		return resultList;
	}

	@Override
	public List<String> getAllUuids(String userUuid) {

		User user = userService.getByUuid(userUuid);

		if (user == null) {
			return Collections.emptyList();
		}

		return districtService.getAllUuids(user);
	}

	@Override
	public List<String> getAllUuids() {
		return districtService.getAllUuids(null);
	}

	@Override	
	public int getCountByRegion(String regionUuid) {
		Region region = regionService.getByUuid(regionUuid);

		return districtService.getCountByRegion(region);
	}

	@Override
	public DistrictDto getDistrictByUuid(String uuid) {
		return toDto(districtService.getByUuid(uuid));
	}	

	@Override
	public List<DistrictDto> getByUuids(List<String> uuids) {
		return districtService.getByUuids(uuids)
				.stream()
				.map(c -> toDto(c))
				.collect(Collectors.toList());
	}


	@Override
	public DistrictReferenceDto getDistrictReferenceByUuid(String uuid) {
		return toReferenceDto(districtService.getByUuid(uuid));
	}

	@Override
	public DistrictReferenceDto getDistrictReferenceById(int id) {
		return toReferenceDto(districtService.getById(id));
	}
	
	@Override
	public void saveDistrict(DistrictDto dto) throws ValidationRuntimeException {
		District district = districtService.getByUuid(dto.getUuid());
		
		if (dto.getRegion() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validRegion));
		}

		district = fillOrBuildEntity(dto, district);
		districtService.ensurePersisted(district);
	}

	@Override
	public List<DistrictReferenceDto> getByName(String name, RegionReferenceDto regionRef) {
		return districtService.getByName(name, regionService.getByReferenceDto(regionRef)).stream().map(d -> toReferenceDto(d)).collect(Collectors.toList());
	}

	public static DistrictReferenceDto toReferenceDto(District entity) {
		if (entity == null) {
			return null;
		}
		DistrictReferenceDto dto = new DistrictReferenceDto(entity.getUuid(), entity.toString());
		return dto;
	}

	public static DistrictDto toDto(District entity) {
		if (entity == null) {
			return null;
		}
		DistrictDto dto = new DistrictDto();
		DtoHelper.fillDto(dto, entity);

		dto.setName(entity.getName());
		dto.setEpidCode(entity.getEpidCode());
		dto.setPopulation(entity.getPopulation());
		dto.setGrowthRate(entity.getGrowthRate());
		dto.setRegion(RegionFacadeEjb.toReferenceDto(entity.getRegion()));

		return dto;
	}	
	
	private District fillOrBuildEntity(@NotNull DistrictDto source, District target) {
		if (target == null) {
			target = new District();
			target.setUuid(source.getUuid());
		}
		
		DtoHelper.validateDto(source, target);
		
		target.setName(source.getName());
		target.setEpidCode(source.getEpidCode());
		target.setPopulation(source.getPopulation());
		target.setGrowthRate(source.getGrowthRate());
		target.setRegion(regionService.getByReferenceDto(source.getRegion()));
		
		return target;
	}

	@LocalBean
	@Stateless
	public static class DistrictFacadeEjbLocal extends DistrictFacadeEjb	 {
	}
}
