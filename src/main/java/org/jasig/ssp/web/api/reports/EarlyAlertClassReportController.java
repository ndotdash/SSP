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
package org.jasig.ssp.web.api.reports; // NOPMD

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;

import org.jasig.ssp.model.reference.Campus;
import org.jasig.ssp.security.permissions.Permission;
import org.jasig.ssp.service.EarlyAlertResponseService;
import org.jasig.ssp.service.EarlyAlertService;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.PersonService;
import org.jasig.ssp.service.external.ExternalPersonService;
import org.jasig.ssp.service.external.RegistrationStatusByTermService;
import org.jasig.ssp.service.external.TermService;
import org.jasig.ssp.service.reference.CampusService;
import org.jasig.ssp.util.DateTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Maps;

/**
 * Service methods for Reporting on Early Alert Case Counts
 * <p>
 * Mapped to URI path <code>/1/report/earlyalertclass</code>
 */
@Controller
@RequestMapping("/1/report/earlyalertclass")
public class EarlyAlertClassReportController extends ReportBaseController<Object> {
	
	private static final String REPORT_URL = "/reports/earlyAlertClassReport.jasper";
	private static final String REPORT_FILE_TITLE = "Early_Alert_Class_Report";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(EarlyAlertClassReportController.class);
	
	@Autowired
	protected transient TermService termService;
	
	@Autowired
	protected transient ExternalPersonService externalPersonService;
	
	@Autowired
	protected transient PersonService personService;
	
	@Autowired
	protected transient CampusService campusService;
	
	@Autowired
	protected transient RegistrationStatusByTermService registrationStatusByTermService;

	@Autowired
	protected transient EarlyAlertService earlyAlertService;
	@Autowired
	protected transient EarlyAlertResponseService earlyAlertResponseService;

	
	@InitBinder
	public void initBinder(final WebDataBinder binder) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT,
				Locale.US);
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(
				dateFormat, true));
	}
	
	
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize(Permission.SECURITY_REPORT_READ)
	public @ResponseBody
	void getEarlyAlertClassReport(
			final HttpServletResponse response,		
			final @RequestParam(required = false) UUID campusId,
			final @RequestParam(required = false) String rosterStatus,
			final @RequestParam(required = false) String termCode,
			final @RequestParam(required = false) Date createDateFrom,
			final @RequestParam(required = false) Date createDateTo,			
			final @RequestParam(required = false, defaultValue = DEFAULT_REPORT_TYPE) String reportType)
			throws ObjectNotFoundException, IOException {
		
		final DateTerm dateTerm = new DateTerm(createDateFrom, createDateTo, termCode, termService);		
		final Map<String, Object> parameters = Maps.newHashMap();
		final Campus campus = SearchParameters.getCampus(campusId, campusService);
		
		SearchParameters.addCampusToParameters(campus, parameters);
		SearchParameters.addDateTermToMap(dateTerm, parameters);
		SearchParameters.addRosterStatusToMap(rosterStatus, parameters);
		
		renderReport( response,  parameters, null,  REPORT_URL, reportType, REPORT_FILE_TITLE);
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
	
}