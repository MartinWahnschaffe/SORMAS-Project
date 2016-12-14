package de.symeda.sormas.backend.util;

import de.symeda.sormas.api.DataTransferObject;
import de.symeda.sormas.api.ReferenceDto;
import de.symeda.sormas.backend.common.AbstractDomainObject;

public final class DtoHelper {

	public static void fillDto(DataTransferObject dto, AbstractDomainObject entity) {
		dto.setCreationDate(entity.getCreationDate());
		dto.setChangeDate(entity.getChangeDate());
		dto.setUuid(entity.getUuid());
	}
	
	public static void fillReferenceDto(ReferenceDto dto, AbstractDomainObject entity) {
		fillDto(dto, entity);
		dto.setCaption(entity.toString());
	}
}
