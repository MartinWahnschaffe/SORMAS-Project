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
package de.symeda.sormas.backend.sample;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import de.symeda.auditlog.api.Audited;
import de.symeda.sormas.api.sample.SampleTestResultType;
import de.symeda.sormas.api.sample.SampleTestType;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.facility.Facility;
import de.symeda.sormas.backend.user.User;

@Entity(name="sampletest")
@Audited
public class SampleTest extends AbstractDomainObject {

	private static final long serialVersionUID = 2290351143518627813L;

	public static final String TABLE_NAME = "sampletest";
	
	public static final String SAMPLE = "sample";
	public static final String TEST_TYPE = "testType";
	public static final String TEST_TYPE_TEXT = "testTypeText";
	public static final String TEST_DATE_TIME = "testDateTime";
	public static final String LAB = "lab";
	public static final String LAB_DETAILS = "labDetails";
	public static final String LAB_USER = "labUser";
	public static final String TEST_RESULT = "testResult";
	public static final String TEST_RESULT_TEXT = "testResultText";
	public static final String TEST_RESULT_VERIFIED = "testResultVerified";
	public static final String FOUR_FOLD_INCREASE_ANTIBODY_TITER = "fourFoldIncreaseAntibodyTiter";
	
	private Sample sample;
	private SampleTestType testType;
	private String testTypeText;
	private Date testDateTime;
	private Facility lab;
	private String labDetails;
	private User labUser;
	private SampleTestResultType testResult;
	private String testResultText;
	private boolean testResultVerified;
	private boolean fourFoldIncreaseAntibodyTiter;
	
	@ManyToOne(cascade = {})
	@JoinColumn(nullable = false)
	public Sample getSample() {
		return sample;
	}
	public void setSample(Sample sample) {
		this.sample = sample;
	}
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public SampleTestType getTestType() {
		return testType;
	}
	public void setTestType(SampleTestType testType) {
		this.testType = testType;
	}
	
	@Column(length=512)
	public String getTestTypeText() {
		return testTypeText;
	}
	public void setTestTypeText(String testTypeText) {
		this.testTypeText = testTypeText;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	public Date getTestDateTime() {
		return testDateTime;
	}
	public void setTestDateTime(Date testDateTime) {
		this.testDateTime = testDateTime;
	}
	
	@ManyToOne(cascade = {})
	@JoinColumn(nullable = false)
	public Facility getLab() {
		return lab;
	}
	public void setLab(Facility lab) {
		this.lab = lab;
	}

	@Column(length=512)
	public String getLabDetails() {
		return labDetails;
	}
	public void setLabDetails(String labDetails) {
		this.labDetails = labDetails;
	}
	
	@ManyToOne(cascade = {})
	@JoinColumn(nullable = false)
	public User getLabUser() {
		return labUser;
	}
	public void setLabUser(User labUser) {
		this.labUser = labUser;
	}
	
	@Enumerated(EnumType.STRING)
	@JoinColumn(nullable = false)
	public SampleTestResultType getTestResult() {
		return testResult;
	}
	public void setTestResult(SampleTestResultType testResult) {
		this.testResult = testResult;
	}
	
	@Column(length=512, nullable = false)
	public String getTestResultText() {
		return testResultText;
	}
	public void setTestResultText(String testResultText) {
		this.testResultText = testResultText;
	}
	
	@Column(nullable = false)
	public boolean isTestResultVerified() {
		return testResultVerified;
	}
	public void setTestResultVerified(boolean testResultVerified) {
		this.testResultVerified = testResultVerified;
	}
	
	@Column
	public boolean isFourFoldIncreaseAntibodyTiter() {
		return fourFoldIncreaseAntibodyTiter;
	}
	public void setFourFoldIncreaseAntibodyTiter(boolean fourFoldIncreaseAntibodyTiter) {
		this.fourFoldIncreaseAntibodyTiter = fourFoldIncreaseAntibodyTiter;
	}
	
}
