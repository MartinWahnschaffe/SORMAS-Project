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

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import de.symeda.auditlog.api.Audited;
import de.symeda.sormas.api.sample.AdditionalTestType;
import de.symeda.sormas.api.sample.PathogenTestType;
import de.symeda.sormas.api.sample.SampleMaterial;
import de.symeda.sormas.api.sample.SampleReferenceDto;
import de.symeda.sormas.api.sample.SampleSource;
import de.symeda.sormas.api.sample.SpecimenCondition;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.facility.Facility;
import de.symeda.sormas.backend.user.User;

@Entity(name="samples")
@Audited
public class Sample extends AbstractDomainObject {

	private static final long serialVersionUID = -7196712070188634978L;

	public static final String TABLE_NAME = "samples";

	public static final String ASSOCIATED_CASE = "associatedCase";
	public static final String SAMPLE_CODE = "sampleCode";
	public static final String LAB_SAMPLE_ID = "labSampleID";
	public static final String SAMPLE_DATE_TIME = "sampleDateTime";
	public static final String REPORT_DATE_TIME = "reportDateTime";
	public static final String REPORTING_USER = "reportingUser";
	public static final String SAMPLE_MATERIAL = "sampleMaterial";
	public static final String SAMPLE_MATERIAL_TEXT = "sampleMaterialText";
	public static final String LAB = "lab";
	public static final String LAB_DETAILS = "labDetails";
	public static final String SHIPMENT_DATE = "shipmentDate";
	public static final String SHIPMENT_DETAILS = "shipmentDetails";
	public static final String RECEIVED_DATE = "receivedDate";
	public static final String NO_TEST_POSSIBLE_REASON = "noTestPossibleReason";
	public static final String COMMENT = "comment";
	public static final String SAMPLE_SOURCE = "sampleSource";
	public static final String REFERRED_TO = "referredTo";
	public static final String SHIPPED = "shipped";
	public static final String RECEIVED = "received";
	public static final String SPECIMEN_CONDITION = "specimenCondition";
	public static final String MAIN_SAMPLE_TEST = "mainSampleTest";

	private Case associatedCase;
	private String sampleCode;
	private String labSampleID;
	private Date sampleDateTime;

	private Date reportDateTime;
	private User reportingUser;
	private Double reportLat;
	private Double reportLon;
	private Float reportLatLonAccuracy;

	private SampleMaterial sampleMaterial;
	private String sampleMaterialText;
	private Facility lab;
	private String labDetails;
	private Date shipmentDate;
	private String shipmentDetails;
	private Date receivedDate;
	private SpecimenCondition specimenCondition;
	private String noTestPossibleReason;
	private String comment;
	private SampleSource sampleSource;
	private Sample referredTo;
	private boolean shipped;
	private boolean received;

	private Boolean pathogenTestingRequested;
	private Boolean additionalTestingRequested;
	private Set<PathogenTestType> requestedPathogenTests;
	private Set<AdditionalTestType> requestedAdditionalTests;
	private String requestedPathogenTestsString;
	private String requestedAdditionalTestsString;

	private List<PathogenTest> pathogenTests;
	private PathogenTest mainSampleTest; 

	@ManyToOne(cascade = {})
	@JoinColumn(nullable = false)
	public Case getAssociatedCase() {
		return associatedCase;
	}
	public void setAssociatedCase(Case associatedCase) {
		this.associatedCase = associatedCase;
	}

	@Column(length=512)
	public String getSampleCode() {
		return sampleCode;
	}
	public void setSampleCode(String sampleCode) {
		this.sampleCode = sampleCode;
	}

	@Column(length=512)
	public String getLabSampleID() {
		return labSampleID;
	}
	public void setLabSampleID(String labSampleID) {
		this.labSampleID = labSampleID;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable=false)
	public Date getSampleDateTime() {
		return sampleDateTime;
	}
	public void setSampleDateTime(Date sampleDateTime) {
		this.sampleDateTime = sampleDateTime;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable=false)
	public Date getReportDateTime() {
		return reportDateTime;
	}
	public void setReportDateTime(Date reportDateTime) {
		this.reportDateTime = reportDateTime;
	}

	@ManyToOne(cascade = {})
	@JoinColumn(nullable=false)
	public User getReportingUser() {
		return reportingUser;
	}
	public void setReportingUser(User reportingUser) {
		this.reportingUser = reportingUser;
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable=false)
	public SampleMaterial getSampleMaterial() {
		return sampleMaterial;
	}
	public void setSampleMaterial(SampleMaterial sampleMaterial) {
		this.sampleMaterial = sampleMaterial;
	}

	@Column(length=512)
	public String getSampleMaterialText() {
		return sampleMaterialText;
	}
	public void setSampleMaterialText(String sampleMaterialText) {
		this.sampleMaterialText = sampleMaterialText;
	}

	@ManyToOne(cascade = {})
	@JoinColumn(nullable=false)
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

	@Temporal(TemporalType.TIMESTAMP)
	public Date getShipmentDate() {
		return shipmentDate;
	}
	public void setShipmentDate(Date shipmentDate) {
		this.shipmentDate = shipmentDate;
	}

	@Column(length=512)
	public String getShipmentDetails() {
		return shipmentDetails;
	}
	public void setShipmentDetails(String shipmentDetails) {
		this.shipmentDetails = shipmentDetails;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getReceivedDate() {
		return receivedDate;
	}
	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}

	@Enumerated(EnumType.STRING)
	public SpecimenCondition getSpecimenCondition() {
		return specimenCondition;
	}
	public void setSpecimenCondition(SpecimenCondition specimenCondition) {
		this.specimenCondition = specimenCondition;
	}

	@Column(length=512)
	public String getNoTestPossibleReason() {
		return noTestPossibleReason;
	}
	public void setNoTestPossibleReason(String noTestPossibleReason) {
		this.noTestPossibleReason = noTestPossibleReason;
	}

	@OneToMany(cascade = {}, mappedBy = PathogenTest.SAMPLE)
	public List<PathogenTest> getSampleTests() {
		return pathogenTests;
	}
	public void setSampleTests(List<PathogenTest> pathogenTests) {
		this.pathogenTests = pathogenTests;
	}

	@Column(length=512)
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Enumerated(EnumType.STRING)
	public SampleSource getSampleSource() {
		return sampleSource;
	}
	public void setSampleSource(SampleSource sampleSource) {
		this.sampleSource = sampleSource;
	}

	@OneToOne(cascade = {})
	@JoinColumn(nullable = true)
	public Sample getReferredTo() {
		return referredTo;
	}
	public void setReferredTo(Sample referredTo) {
		this.referredTo = referredTo;
	}

	@Column
	public boolean isShipped() {
		return shipped;
	}
	public void setShipped(boolean shipped) {
		this.shipped = shipped;
	}

	@Column
	public boolean isReceived() {
		return received;
	}
	public void setReceived(boolean received) {
		this.received = received;
	}

	@Column
	public Boolean getPathogenTestingRequested() {
		return pathogenTestingRequested;
	}
	public void setPathogenTestingRequested(Boolean pathogenTestingRequested) {
		this.pathogenTestingRequested = pathogenTestingRequested;
	}

	@Column
	public Boolean getAdditionalTestingRequested() {
		return additionalTestingRequested;
	}
	public void setAdditionalTestingRequested(Boolean additionalTestingRequested) {
		this.additionalTestingRequested = additionalTestingRequested;
	}

	@Transient
	public Set<PathogenTestType> getRequestedPathogenTests() {
		if (requestedPathogenTests == null) {
			if (StringUtils.isEmpty(requestedPathogenTestsString)) {
				requestedPathogenTests = new HashSet<>();
			} else {
				requestedPathogenTests = Arrays.stream(requestedPathogenTestsString.split(";"))
						.map(PathogenTestType::valueOf)
						.collect(Collectors.toSet());
			}
		}
		return requestedPathogenTests;
	}
	public void setRequestedPathogenTests(Set<PathogenTestType> requestedPathogenTests) {
		this.requestedPathogenTests = requestedPathogenTests;
		StringBuilder sb = new StringBuilder();
		requestedPathogenTests.stream().forEach(t -> sb.append(t.name()));
		requestedPathogenTestsString = sb.toString();
	}	

	@Transient
	public Set<AdditionalTestType> getRequestedAdditionalTests() {
		if (requestedAdditionalTests == null) {
			if (StringUtils.isEmpty(requestedAdditionalTestsString)) {
				requestedAdditionalTests = new HashSet<>();
			} else {
				requestedAdditionalTests = Arrays.stream(requestedAdditionalTestsString.split(";"))
						.map(AdditionalTestType::valueOf)
						.collect(Collectors.toSet());
			}
		}
		return requestedAdditionalTests;
	}
	public void setRequestedAdditionalTests(Set<AdditionalTestType> requestedAdditionalTests) {
		this.requestedAdditionalTests = requestedAdditionalTests;
		StringBuilder sb = new StringBuilder();
		requestedAdditionalTests.stream().forEach(t -> sb.append(t.name()));
		requestedAdditionalTestsString = sb.toString();
	}

	public String getRequestedPathogenTestsString() {
		return requestedPathogenTestsString;
	}
	public void setRequestedPathogenTestsString(String requestedPathogenTestsString) {
		this.requestedPathogenTestsString = requestedPathogenTestsString;
		requestedPathogenTests = null;
	}

	public String getRequestedAdditionalTestsString() {
		return requestedAdditionalTestsString;
	}
	public void setRequestedAdditionalTestsString(String requestedAdditionalTestsString) {
		this.requestedAdditionalTestsString = requestedAdditionalTestsString;
		requestedAdditionalTests = null;
	}

	@Override
	public String toString() {
		return SampleReferenceDto.buildCaption(getSampleMaterial(), 
				getAssociatedCase() != null ? getAssociatedCase().getUuid() : null);
	}

	public SampleReferenceDto toReference() {
		return new SampleReferenceDto(getUuid(), getSampleMaterial(), 
				getAssociatedCase() != null ? getAssociatedCase().getUuid() : null);
	}

	public Double getReportLat() {
		return reportLat;
	}
	public void setReportLat(Double reportLat) {
		this.reportLat = reportLat;
	}
	public Double getReportLon() {
		return reportLon;
	}
	public void setReportLon(Double reportLon) {
		this.reportLon = reportLon;
	}
	public Float getReportLatLonAccuracy() {
		return reportLatLonAccuracy;
	}
	public void setReportLatLonAccuracy(Float reportLatLonAccuracy) {
		this.reportLatLonAccuracy = reportLatLonAccuracy;
	}

	/**
	 * The representative test.
	 * Be default this should be set to the last done test.
	 * @see SampleService#updateMainSampleTest
	 */
	@OneToOne(cascade = {})
	@JoinColumn(nullable = true)
	public PathogenTest getMainSampleTest() {
		return mainSampleTest;
	}

	public void setMainSampleTest(PathogenTest mainSampleTest) {
		this.mainSampleTest = mainSampleTest;
	}

}
