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
package de.symeda.sormas.api.caze.classification;

import java.util.Arrays;
import java.util.List;

import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.sample.PathogenTestDto;
import de.symeda.sormas.api.sample.PathogenTestResultType;
import de.symeda.sormas.api.sample.PathogenTestType;

public class ClassificationSampleTestPositiveResultCriteriaDto extends ClassificationCriteriaDto {

	private static final long serialVersionUID = 3811127784970509183L;
	
	protected List<PathogenTestType> pathogenTestTypes;

	public ClassificationSampleTestPositiveResultCriteriaDto() {
		
	}
	
	public ClassificationSampleTestPositiveResultCriteriaDto(PathogenTestType... sampleTestTypes) {
		this.pathogenTestTypes = Arrays.asList(sampleTestTypes);
	}

	@Override
	public boolean eval(CaseDataDto caze, PersonDto person, List<PathogenTestDto> sampleTests) {
		for (PathogenTestDto sampleTest : sampleTests) {
			if (sampleTest.getTestResult() == PathogenTestResultType.POSITIVE
					&& pathogenTestTypes.contains(sampleTest.getTestType())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String buildDescription() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(I18nProperties.getString(Strings.classificationOnePositiveTestResult)).append(" ");
		for (int i = 0; i < pathogenTestTypes.size(); i++) {
			if (i > 0) {
				if (i < pathogenTestTypes.size() - 1) {
					stringBuilder.append(", ");
				} else {
					stringBuilder.append(" <b>").append(I18nProperties.getString(Strings.sOr).toUpperCase()).append("</b> ");
				}
			}

			stringBuilder.append(pathogenTestTypes.get(i).toString());	
		}

		return stringBuilder.toString();
	}

	public List<PathogenTestType> getSampleTestTypes() {
		return pathogenTestTypes;
	}

	public void setSampleTestTypes(List<PathogenTestType> pathogenTestTypes) {
		this.pathogenTestTypes = pathogenTestTypes;
	}

}