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
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;

import de.symeda.sormas.api.region.RegionCriteria;
import de.symeda.sormas.api.region.RegionDto;
import de.symeda.sormas.api.region.RegionFacade;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;

@Stateless(name = "RegionFacade")
public class RegionFacadeEjb implements RegionFacade {
	
	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	protected EntityManager em;
	
	@EJB
	protected RegionService regionService;
	@EJB
	protected UserService userService;
	@EJB
	protected DistrictService districtService;
	@EJB
	protected CommunityService communityService;

	@Override
	public List<RegionReferenceDto> getAllAsReference() {
		return regionService.getAll(Region.NAME, true).stream()
				.map(f -> toReferenceDto(f))
				.collect(Collectors.toList());
	}
	
	@Override
	public List<RegionDto> getAllAfter(Date date) {
		return regionService.getAllAfter(date, null).stream()
			.map(c -> toDto(c))
			.collect(Collectors.toList());
	}
	
	@Override
	public List<RegionDto> getIndexList(RegionCriteria criteria) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Region> cq = cb.createQuery(Region.class);
		Root<Region> region = cq.from(Region.class);
		
		if (criteria != null) {
			Predicate filter = regionService.buildCriteriaFilter(criteria, cb, region);
			if (filter != null) {
				cq.where(filter);
			}
		}
		
		cq.select(region);
		cq.orderBy(cb.asc(region.get(Region.NAME)));
		cq.distinct(true);
		
		List<Region> regions = em.createQuery(cq).getResultList();
		return regions.stream().map(r -> toDto(r)).collect(Collectors.toList());
	}
	
	@Override
	public List<String> getAllUuids(String userUuid) {
		
		User user = userService.getByUuid(userUuid);
		
		if (user == null) {
			return Collections.emptyList();
		}
		
		return regionService.getAllUuids(user);
	}
	
	@Override
	public List<String> getAllUuids() {
		return regionService.getAllUuids(null);
	}
	
	@Override
	public RegionDto getRegionByUuid(String uuid) {
		return toDto(regionService.getByUuid(uuid));
	}

	@Override
	public List<RegionDto> getByUuids(List<String> uuids) {
		return regionService.getByUuids(uuids)
				.stream()
				.map(c -> toDto(c))
				.collect(Collectors.toList());
	}
	
	@Override
	public RegionReferenceDto getRegionReferenceByUuid(String uuid) {
		return toReferenceDto(regionService.getByUuid(uuid));
	}
	
	@Override
	public RegionReferenceDto getRegionReferenceById(int id) {
		return toReferenceDto(regionService.getById(id));
	}
	
	public static RegionReferenceDto toReferenceDto(Region entity) {
		if (entity == null) {
			return null;
		}
		RegionReferenceDto dto = new RegionReferenceDto(entity.getUuid(), entity.toString());
		return dto;
	}
	
	public static RegionDto toDto(Region entity) {
		if (entity == null) {
			return null;
		}
		RegionDto dto = new RegionDto();
		DtoHelper.fillDto(dto, entity);
		
		dto.setName(entity.getName());
		dto.setEpidCode(entity.getEpidCode());
		dto.setPopulation(entity.getPopulation());
		dto.setGrowthRate(entity.getGrowthRate());

		return dto;
	}
	
	@Override
	public void saveRegion(RegionDto dto) {
		Region region = regionService.getByUuid(dto.getUuid());
		region = fillOrBuildEntity(dto, region);
		regionService.ensurePersisted(region);
	}
	
	@Override
	public List<RegionReferenceDto> getByName(String name) {
		return regionService.getByName(name).stream().map(r -> toReferenceDto(r)).collect(Collectors.toList());
	}
	
	private Region fillOrBuildEntity(@NotNull RegionDto source, Region target) {
		if (target == null) {
			target = new Region();
			target.setUuid(source.getUuid());
		}
		
		DtoHelper.validateDto(source, target);
		
		target.setName(source.getName());
		target.setEpidCode(source.getEpidCode());
		target.setPopulation(source.getPopulation());
		target.setGrowthRate(source.getGrowthRate());
		
		return target;
	}
	
	@LocalBean
	@Stateless
	public static class RegionFacadeEjbLocal extends RegionFacadeEjb	 {
	}
}
