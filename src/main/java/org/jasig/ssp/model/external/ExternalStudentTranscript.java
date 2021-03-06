/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.ssp.model.external;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Immutable;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@Immutable
@Table(name = "v_external_student_transcript")
public class ExternalStudentTranscript extends AbstractExternalData implements
		Serializable, ExternalData {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4538453565588973981L;

	@Column(nullable = false, length = 50)
	@NotNull
	@NotEmpty
	@Size(max = 50)
	private String schoolId;
	
	@Column(nullable = false)
	private BigDecimal creditHoursForGpa;
	
	@Column(nullable = false)
	private BigDecimal creditHoursAttempted;
	
	@Column(nullable = false)
	private BigDecimal totalQualityPoints;
	
	@Column(nullable = true)
	private BigDecimal gradePointAverage;
	
	@Column(nullable = true)
	private BigDecimal creditHoursEarned;
	
	@Column(nullable = true)
	private BigDecimal creditCompletionRate;
	
	@Column(nullable = true)
	private BigDecimal creditHoursNotCompleted;
	
	@Column(nullable = false, length = 100)
	@NotNull
	@NotEmpty
	@Size(max = 100)
	private String currentRestrictions;
	
	@Column(nullable = false, length = 50)
	@NotNull
	@NotEmpty
	@Size(max = 50)
	private String academicStanding;
	
	@Column(nullable = false, length = 25)
	@NotNull
	@NotEmpty
	@Size(max = 25)
	private String gpaTrendIndicator;

	@Column(nullable = true)
	private BigDecimal localGpa;

	@Column(nullable = true)
	private BigDecimal programGpa;



	/**
	 * @return the schoolId
	 */
	public String getSchoolId() {
		return schoolId;
	}

	/**
	 * @param schoolId the schoolId to set
	 */
	public void setSchoolId(final String schoolId) {
		this.schoolId = schoolId;
	}

	/**
	 * @return the creditHoursForGpa
	 */
	public BigDecimal getCreditHoursForGpa() {
		return creditHoursForGpa;
	}

	/**
	 * @param creditHoursForGpa the creditHoursForGpa to set
	 */
	public void setCreditHoursForGpa(final BigDecimal creditHoursForGpa) {
		this.creditHoursForGpa = creditHoursForGpa;
	}

	/**
	 * @return the creditHoursAttempted
	 */
	public BigDecimal getCreditHoursAttempted() {
		return creditHoursAttempted;
	}

	/**
	 * @param creditHoursAttempted the creditHoursAttempted to set
	 */
	public void setCreditHoursAttempted(final BigDecimal creditHoursAttempted) {
		this.creditHoursAttempted = creditHoursAttempted;
	}

	/**
	 * @return the totalQualityPoints
	 */
	public BigDecimal getTotalQualityPoints() {
		return totalQualityPoints;
	}

	/**
	 * @return the creditHoursEarned
	 */
	public BigDecimal getCreditHoursEarned() {
		return creditHoursEarned;
	}

	/**
	 * @param creditHoursEarned the creditHoursEarned to set
	 */
	public void setCreditHoursEarned(BigDecimal creditHoursEarned) {
		this.creditHoursEarned = creditHoursEarned;
	}

	/**
	 * @param totalQualityPoints the totalQualityPoints to set
	 */
	public void setTotalQualityPoints(final BigDecimal totalQualityPoints) {
		this.totalQualityPoints = totalQualityPoints;
	}

	/**
	 * @return the gradePointAverage
	 */
	public BigDecimal getGradePointAverage() {
		return gradePointAverage;
	}

	/**
	 * @param gradePointAverage the gradePointAverage to set
	 */
	public void setGradePointAverage(final BigDecimal gradePointAverage) {
		this.gradePointAverage = gradePointAverage;
	}

	/**
	 * @return the creditCompletionRate
	 */
	public BigDecimal getCreditCompletionRate() {
		return creditCompletionRate;
	}

	/**
	 * @param creditCompletionRate the creditCompletionRate to set
	 */
	public void setCreditCompletionRate(BigDecimal creditCompletionRate) {
		this.creditCompletionRate = creditCompletionRate;
	}

	/**
	 * @return the creditHoursNotCompleted
	 */
	public BigDecimal getCreditHoursNotCompleted() {
		return creditHoursNotCompleted;
	}

	/**
	 * @param creditHoursNotCompleted the creditHoursNotCompleted to set
	 */
	public void setCreditHoursNotCompleted(BigDecimal creditHoursNotCompleted) {
		this.creditHoursNotCompleted = creditHoursNotCompleted;
	}

	/**
	 * @return the currentRestrictions
	 */
	public String getCurrentRestrictions() {
		return currentRestrictions;
	}

	/**
	 * @param currentRestrictions the currentRestrictions to set
	 */
	public void setCurrentRestrictions(String currentRestrictions) {
		this.currentRestrictions = currentRestrictions;
	}

	/**
	 * @return the academicStanding
	 */
	public String getAcademicStanding() {
		return academicStanding;
	}

	/**
	 * @param academicStanding the academicStanding to set
	 */
	public void setAcademicStanding(String academicStanding) {
		this.academicStanding = academicStanding;
	}

	/**
	 * @return the gpaTrendIndicator
	 */
	public String getGpaTrendIndicator() {
		return gpaTrendIndicator;
	}

	/**
	 * @param gpaTrendIndicator the gpaTrendIndicator to set
	 */
	public void setGpaTrendIndicator(String gpaTrendIndicator) {
		this.gpaTrendIndicator = gpaTrendIndicator;
	}

	/**
	 * @return the localGpa
	 */
	public BigDecimal getLocalGpa() {
		return localGpa;
	}

	/**
	 * @param localGpa the localGpa to set
	 */
	public void setLocalGpa(final BigDecimal localGpa) {
		this.localGpa = localGpa;
	}

	/**
	 * @return the programGpa
	 */
	public BigDecimal getProgramGpa() {
		return programGpa;
	}

	/**
	 * @param programGpa the programGpa to set
	 */
	public void setProgramGpa(final BigDecimal programGpa) {
		this.programGpa = programGpa;
	}
}
