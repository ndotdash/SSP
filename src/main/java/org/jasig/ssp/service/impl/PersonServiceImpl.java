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
package org.jasig.ssp.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.hibernate.exception.ConstraintViolationException;
import org.jasig.ssp.dao.ObjectExistsException;
import org.jasig.ssp.dao.PersonDao;
import org.jasig.ssp.dao.PersonExistsException;
import org.jasig.ssp.model.ObjectStatus;
import org.jasig.ssp.model.Person;
import org.jasig.ssp.model.external.ExternalPerson;
import org.jasig.ssp.security.PersonAttributesResult;
import org.jasig.ssp.security.exception.UnableToCreateAccountException;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.PersonAttributesService;
import org.jasig.ssp.service.PersonService;
import org.jasig.ssp.service.external.ExternalPersonService;
import org.jasig.ssp.service.tool.IntakeService;
import org.jasig.ssp.transferobject.CoachPersonLiteTO;
import org.jasig.ssp.transferobject.PersonTO;
import org.jasig.ssp.transferobject.reports.BaseStudentReportTO;
import org.jasig.ssp.transferobject.reports.DisabilityServicesReportTO;
import org.jasig.ssp.transferobject.reports.PersonSearchFormTO;
import org.jasig.ssp.util.sort.PagingWrapper;
import org.jasig.ssp.util.sort.SortingAndPaging;
import org.jasig.ssp.util.transaction.WithTransaction;
import org.jasig.ssp.web.api.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.portlet.PortletRequest;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Person service implementation
 * 
 * @author jon.adams
 */ 
@Service
@Transactional
public class PersonServiceImpl implements PersonService {

	public static final boolean ALL_AUTHENTICATED_USERS_CAN_CREATE_ACCOUNT = true;

	public static final String PERMISSION_TO_CREATE_ACCOUNT = "ROLE_CAN_CREATE";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PersonServiceImpl.class);

	private static final Logger TIMING_LOGGER = LoggerFactory
			.getLogger("timing." + PersonServiceImpl.class.getName());

	@Autowired
	private transient PersonDao dao;

	@Autowired
	private transient PersonAttributesService personAttributesService;

	@Autowired
	private transient ExternalPersonService externalPersonService;

	@Autowired
	private transient WithTransaction withTransaction;

	/**
	 * If <code>true</code>, each individual coach synchronized by
	 * {@link #syncCoaches()} will be written in its own transaction. If false,
	 * the entire synchronization across all coaches will be a single
	 * transaction. Defaults to <code>true</code>. Usually only set to
	 * <code>false</code> for tests.
	 *
	 */
	@Value("#{configProperties.per_coach_sync_transactions}")
	private boolean perCoachSyncTransactions = true;


	private static interface PersonAttributesLookup {
		public PersonAttributesResult lookupPersonAttributes(String username)
				throws ObjectNotFoundException;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Person createUserAccount(final String username,
			final Collection<GrantedAuthority> authorities) {

		return createUserAccount(username, authorities, new PersonAttributesLookup() {
			@Override
			public PersonAttributesResult lookupPersonAttributes(String username)
			throws ObjectNotFoundException {
				return personAttributesService.getAttributes(username);
			}
		});

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Person createUserAccountForCurrentPortletUser(String username,
			final PortletRequest portletRequest)
			throws UnableToCreateAccountException {

		if ( !(StringUtils.hasText(username)) ) {
			@SuppressWarnings("unchecked") Map<String,String> userInfo =
					(Map<String,String>) portletRequest.getAttribute(PortletRequest.USER_INFO);
			username = userInfo
					.get(PortletRequest.P3PUserInfos.USER_LOGIN_ID.toString());
		}

		if ( !(StringUtils.hasText(username)) ) {
			throw new UnableToCreateAccountException(
					"Cannot find a username to assign to new account.");
		}

		Collection<GrantedAuthority> authorities = Sets.newHashSet();
		return createUserAccount(username, authorities, new PersonAttributesLookup() {
			@Override
			public PersonAttributesResult lookupPersonAttributes(String username)
			throws ObjectNotFoundException {
				return personAttributesService.getAttributes(username, portletRequest);
			}
		});

	}

	private void ensureRequiredFieldsForDirectoryPerson(Person person) {
		if ( person.getUsername() == null ) {
			// This method is currently focused solely on serving the needs of
			// createUserAccount(), in which case a username is always known. So
			// if you pass a Person with no username, we can treat it as a programmer
			// error and don't need to worry about writing more username/schoolId
			// external person lookup fallback logic
			throw new IllegalArgumentException("Person has no username");
		}

		ExternalPerson byUsername = null;
		final String username = person.getUsername();
		if ( person.getSchoolId() == null || person.getFirstName() == null || person.getLastName() == null || person.getPrimaryEmailAddress() == null ) {
			if ( person.getSchoolId() != null ) {
				try {
					byUsername = externalPersonService.getBySchoolId(person.getSchoolId());
				} catch ( ObjectNotFoundException e ) {
					// handled below in the null check
				}
			}
			if ( byUsername == null ) {
				try {
					byUsername = externalPersonService.getByUsername(username);
				} catch ( ObjectNotFoundException e ) {
					// handled below in the null check
				}
			}
		}

		if ( person.getSchoolId() == null ) {
			if ( byUsername == null ) {
				// Again, the idea here is that we trust that the person in question
				// is fundamentally "valid", but just might be missing required
				// fields in the directory. We don't just want to set schoolId=username, though
				// because whatever we do, we don't want to accidentally create collisions
				// with "real" accounts.
				final String generatedSchoolId = UUID.randomUUID().toString();
				LOGGER.info("Person with username {} had no schoolId and also couldn't be found in external data"
						+ " by username. Assigning a generated schoolId {}.", username, generatedSchoolId);
				person.setSchoolId(generatedSchoolId);
			} else {
				person.setSchoolId(byUsername.getSchoolId());
			}
		}
		if ( person.getFirstName() == null ) {
			if ( byUsername == null || byUsername.getFirstName() == null ) {
				person.setFirstName("Unknown");
			} else {
				person.setFirstName(byUsername.getFirstName());
			}
		}
		if ( person.getLastName() == null ) {
			if ( byUsername == null || byUsername.getLastName() == null ) {
				person.setLastName("Unknown");
			} else {
				person.setLastName(byUsername.getLastName());
			}
		}
		if ( person.getPrimaryEmailAddress() == null ) {
			if ( byUsername == null || byUsername.getPrimaryEmailAddress() == null ) {
				person.setPrimaryEmailAddress(person.getSchoolId() + "@unknown.domain");
			} else {
				person.setPrimaryEmailAddress(byUsername.getPrimaryEmailAddress());
			}
		}
	}

	private Person createUserAccount(String username,
			Collection<GrantedAuthority> authorities,
			PersonAttributesLookup personAttributesLookup) {

		Person person = null;

		if (hasAccountCreationPermission(authorities)) {
			person = new Person();
			person.setEnabled(true);
			person.setUsername(username);

			try {
				// Get the Person Attributes to create the person
				final PersonAttributesResult attr =
						personAttributesLookup.lookupPersonAttributes(username);
				person.setSchoolId(attr.getSchoolId());
				person.setFirstName(attr.getFirstName());
				person.setLastName(attr.getLastName());
				person.setPrimaryEmailAddress(attr.getPrimaryEmailAddress());

				ensureRequiredFieldsForDirectoryPerson(person);
				person = create(person);
				externalPersonService.updatePersonFromExternalPerson(person);
				LOGGER.info("Successfully Created Account for {}", username);

			} catch (final ObjectNotFoundException onfe) {
				// personAttributesService may throw this exception, if so,
				// we can't create the user.
				throw new UnableToCreateAccountException(// NOPMD
						"Unable to pull required attributes", onfe);
			} catch (final PersonExistsException pee ) {
				LOGGER.info("Tried to add a user that was already present ({})",
						username, pee);
				throw pee;
			} catch (final ConstraintViolationException sqlException) {
				// if we received a constraintViolationException of
				// unique_person_username, then the user might have been
				// added since we started. (If using SQLServer this will only
				// work if you're running with ExtendedSQLServer*Dialect. Else
				// getConstraintName() will always be null.)
				if (sqlException.getConstraintName().equalsIgnoreCase(

                       "uq_person_school_id")) {
                     LOGGER.info("Tried to add a user that was already present");
 
                     throw new ObjectExistsException("Account with school_id "
                             + person.getSchoolId() + " already exists.");
                 }
				if (sqlException.getConstraintName().equalsIgnoreCase(						
						"unique_person_username")) {
					LOGGER.info("Tried to add a user that was already present ({})",
							username, sqlException);

					// SSP-397. Have to throw something to rollback the
					// transaction, else Spring/Hib will attempt a commit when
					// this method returns, which Postgres will refuse with a
					// "current transaction is aborted" message and the caller
					// will get an opaque HibernateJdbcException. With an
					// PersonExistsException the client has at least some
					// clue as to a reasonable recovery path.
					throw new PersonExistsException("Account with user name "
							+ username + " already exists.");
				}
				// Also SSP-397
				throw sqlException;

			} catch (final Exception genException) {
				// This exception seems to get swallowed... trying to reveal
				// it.
				throw new UnableToCreateAccountException( // NOPMD
						"Unable to Create Account for login.", genException);
			}

		} else {
			throw new UnableToCreateAccountException( // NOPMD
					// already know the account was not found
					"Insufficient Permissions to create Account");
		}

		return person;
	}

	private boolean hasAccountCreationPermission(
			final Collection<GrantedAuthority> authorities) {
		boolean permission = ALL_AUTHENTICATED_USERS_CAN_CREATE_ACCOUNT;

		// if already true, skip permission check
		if (permission) {
			return true;
		}

		for (final GrantedAuthority auth : authorities) {
			if (auth.getAuthority().equals(PERMISSION_TO_CREATE_ACCOUNT)) {
				permission = true;
				break;
			}
		}

		return permission;
	}

	@Override
	public PagingWrapper<Person> getAll(final SortingAndPaging sAndP) {
		final PagingWrapper<Person> people = dao.getAll(sAndP);
		return people;
	}

	/**
	 * Retrieves the specified Person.
	 * 
	 * @param id
	 *            Required identifier for the Person to retrieve. Can not be
	 *            null.
	 * @exception ObjectNotFoundException
	 *                If the supplied identifier does not exist in the database
	 *                or is not {@link ObjectStatus#ACTIVE}.
	 * @return The specified Person instance.
	 */
	@Override
	public Person get(final UUID id) throws ObjectNotFoundException {
		final Person person = dao.get(id);
		return person;
	}

	@Override
	public Person load(final UUID id) {
		return dao.load(id);
	}

	@Override
	public Person getBySchoolId(final String schoolId,boolean commitPerson)
			throws ObjectNotFoundException {
		try { 
			return dao.getBySchoolId(schoolId);
		} catch (final ObjectNotFoundException e) {
			final ExternalPerson externalPerson = externalPersonService
					.getBySchoolId(schoolId);
			if (externalPerson == null) {
				throw new ObjectNotFoundException( // NOPMD
						"Unable to find person by schoolId: " + schoolId,
						"Person");
			}

			final Person person = new Person();
			evict(person);
			externalPersonService.updatePersonFromExternalPerson(person,
					externalPerson,commitPerson);
			return person;
		}
	}
	
	@Override
	public Person getByUsername(final String username, final Boolean commitPerson)
			throws ObjectNotFoundException {

		final Person obj = dao.fromUsername(username);
		if(obj != null)
			return obj;


		final ExternalPerson externalPerson = externalPersonService
				.getByUsername(username);
		if (externalPerson == null) {
			throw new ObjectNotFoundException( // NOPMD
					"Unable to find person by username: " + username,
					"Person");
		}

		final Person person = new Person();
		evict(person);
		externalPersonService.updatePersonFromExternalPerson(person,
				externalPerson, commitPerson);
		return person;
	}

	@Override
	public Person personFromUsername(final String username)
			throws ObjectNotFoundException {

		final Person obj = dao.fromUsername(username);

		if (null == obj) {
			throw new ObjectNotFoundException(
					"Could not find person with username: " + username,
					"Person");
		}
		return obj;
	}

	/**
	 * Creates a new Person instance based on the supplied model.
	 * 
	 * @param obj
	 *            Model instance
	 * @throws PersonExistsException
	 *             Thrown if any of the specified data has a unique key
	 *             violation.
	 */
	@Override
	public Person create(final Person obj) throws PersonExistsException {

		LOGGER.debug("Creating User {}", obj);

		// Best to check for schoolId collisions 1st b/c proposed usernames
		// are sometimes just calculated values from the UI. Conflicts with
		// those calculated values are perfectly legitimate, but the UI will
		// then attempt to load *that* person record to help the user work
		// through the conflict. But what the user actually entered was a
		// schoolId, not a username, and the username conflict might have
		// been on a person record that doesn't have the schoolId they originally
		// requested. So if the UI isn't careful, it will end up potentially
		// reloading the screen with the wrong person record or, worse,
		// eventually overwriting the wrong person record.
		if (obj.getSchoolId() != null) {
			Person existing = null;
			try {
				existing = dao.getBySchoolId(obj.getSchoolId());
			} catch ( ObjectNotFoundException e ) {}
			if (null != existing) {
				throw new PersonExistsException(PersonExistsException.ERROR_SCHOOL_ID_EXISTING,existing.getId(), existing.getUsername(), existing.getSchoolId(),  obj.getUsername(), obj.getSchoolId(), existing.getFullName());
			}
		}

		if (obj.getUsername() != null) {
			final Person existing = dao.fromUsername(obj.getUsername());
			if (null != existing) {
				throw new PersonExistsException(PersonExistsException.ERROR_USERNAME_EXISTING,existing.getId(), existing.getUsername(), existing.getSchoolId(),  obj.getUsername(), obj.getSchoolId(), existing.getFullName());
			}
		}

		Person person = null;
		try {
			person = dao.create(obj);
		} catch ( ConstraintViolationException e ) {
			final String constraintName = e.getConstraintName();
			if ( "uq_person_school_id".equals(constraintName) ) {
				person = withTransaction.withNewTransactionAndUncheckedExceptionsReadOnly(new Callable<Person>() {

					@Override
					public Person call() throws Exception {
						return dao.getBySchoolId(obj.getSchoolId());
					}
				});
				throw new PersonExistsException(PersonExistsException.ERROR_CONSTRAINT_VIOLATION_USERNAME,person.getId(), person.getUsername(), person.getSchoolId(),  obj.getUsername(), obj.getSchoolId(), person.getFullName());

			}
			
			if ( "unique_person_username".equals(constraintName) ) {
				person = withTransaction.withNewTransactionAndUncheckedExceptionsReadOnly(new Callable<Person>() {
					@Override
					public Person call() throws Exception {
						return dao.getByUsername(obj.getUsername());
					}
				});
				throw new PersonExistsException(PersonExistsException.ERROR_CONSTRAINT_VIOLATION_SCHOOL_ID,person.getId(), person.getUsername(), person.getSchoolId(),  obj.getUsername(), obj.getSchoolId(), person.getFullName());
			}
			throw e;
		}

		if (LOGGER.isDebugEnabled()) {
			if (person == null) {
				LOGGER.debug("Failed to create user");
			} else {
				LOGGER.debug("User successfully created");
			}
		}

		return person;
	}

	/**
	 * @param obj
	 *            Model instance to save
	 * @see IntakeService
	 */
	@Override
	public Person save(final Person obj) throws ObjectNotFoundException {
		return dao.save(obj);
	}

	@Override
	public UUID getCoachIdForStudent(PersonTO obj) {
		return dao.getCoachIdForStudent(obj);
	}

	/**
	 * Mark a Person as deleted.
	 * 
	 * Does not remove them from persistent storage, but marks their status flag
	 * to {@link ObjectStatus#INACTIVE}.
	 */
	@Override
	public void delete(final UUID id) throws ObjectNotFoundException {
		final Person current = get(id);

		if (null != current) {
			current.setObjectStatus(ObjectStatus.INACTIVE);
			save(current);
		}
	}

	protected void setDao(final PersonDao dao) {
		this.dao = dao;
	}

	@Override
	public List<Person> peopleFromListOfIds(final List<UUID> personIds,
			final SortingAndPaging sAndP) {
		try {
			final List<Person> people = dao.getPeopleInList(personIds, sAndP);
			return people;
		} catch (final ValidationException exc) {
			return Lists.newArrayList();
		}
	}

	/**
	 * Used for Specific Report "Address Labels"
	 */
	@Override
	public List<Person> peopleFromCriteria(
			final PersonSearchFormTO personSearchFormTO,
			final SortingAndPaging sAndP) throws ObjectNotFoundException {

		final List<Person> people = dao.getPeopleByCriteria(
				personSearchFormTO,
				sAndP);
		return people;
	}

	@Override
	public PagingWrapper<CoachPersonLiteTO> getAllCoachesLite(final SortingAndPaging sAndP) {
		long methodStart = new Date().getTime();
		final Collection<String> coachUsernames =
				getAllCoachUsernamesFromDirectory();
		long localPersonsLookupStart = new Date().getTime();
		PagingWrapper<CoachPersonLiteTO> coaches =
				dao.getCoachPersonsLiteByUsernames(coachUsernames, sAndP);
		long localPersonsLookupEnd = new Date().getTime();
		TIMING_LOGGER.info("Read {} local coaches in {} ms",
				coaches.getResults(),
				localPersonsLookupEnd - localPersonsLookupStart);
		TIMING_LOGGER.info("Read {} PersonAttributesService coaches and"
				+ " correlated them with {} local coaches in {} ms",
				new Object[] { coachUsernames.size(), coaches.getResults(),
						localPersonsLookupEnd - methodStart } );
		return coaches;
	}
	
	@Override
	public PagingWrapper<CoachPersonLiteTO> getAllCoachesLite(final SortingAndPaging sAndP, String HomeDepartment) {
		long methodStart = new Date().getTime();
		final Collection<String> coachUsernames =
				getAllCoachUsernamesFromDirectory();
		long localPersonsLookupStart = new Date().getTime();
		PagingWrapper<CoachPersonLiteTO> coaches =
				dao.getCoachPersonsLiteByUsernames(coachUsernames, sAndP, HomeDepartment);
		long localPersonsLookupEnd = new Date().getTime();
		TIMING_LOGGER.info("Read {} local coaches in {} ms",
				coaches.getResults(),
				localPersonsLookupEnd - localPersonsLookupStart);
		TIMING_LOGGER.info("Read {} PersonAttributesService coaches and"
				+ " correlated them with {} local coaches in {} ms",
				new Object[] { coachUsernames.size(), coaches.getResults(),
						localPersonsLookupEnd - methodStart } );
		
		return coaches;
	}

	@Override
	public PagingWrapper<Person> getAllCoaches(final SortingAndPaging sAndP) {
		return syncCoaches();
	}

	private Collection<String> getAllCoachUsernamesFromDirectory() {
		long pasLookupStart = new Date().getTime();
		final Collection<String> coachUsernames = personAttributesService
				.getCoaches();
		long pasLookupEnd = new Date().getTime();
		TIMING_LOGGER.info("Read {} coaches from PersonAttributesService in {} ms",
				coachUsernames.size(), pasLookupEnd - pasLookupStart);
		return coachUsernames;
	}

	@Override
	public PagingWrapper<Person> getAllAssignedCoaches(SortingAndPaging sAndP) {
		return dao.getAllAssignedCoaches(sAndP);
	}

	@Override
	public PagingWrapper<CoachPersonLiteTO> getAllAssignedCoachesLite(SortingAndPaging sAndP) {
		return dao.getAllAssignedCoachesLite(sAndP);
	}
	
	@Override
	public PagingWrapper<CoachPersonLiteTO> getAllAssignedCoachesLite(SortingAndPaging sAndP, String homeDepartment) {
		return dao.getAllAssignedCoachesLite(sAndP, homeDepartment);
	}

	@Override
	public SortedSet<Person> getAllCurrentCoaches(Comparator<Person> sortBy) {
		final Collection<Person> officialCoaches = getAllCoaches(null).getRows();
		SortedSet<Person> currentCoachesSet =
				Sets.newTreeSet(sortBy == null ? Person.PERSON_NAME_AND_ID_COMPARATOR : sortBy);
		currentCoachesSet.addAll(officialCoaches);
		final Collection<Person> assignedCoaches =
				getAllAssignedCoaches(null).getRows();
		currentCoachesSet.addAll(assignedCoaches);
		return currentCoachesSet;
	}

	@Override
	public SortedSet<CoachPersonLiteTO> getAllCurrentCoachesLite(Comparator<CoachPersonLiteTO> sortBy) {
		final Collection<CoachPersonLiteTO> officialCoaches = getAllCoachesLite(null).getRows();
		SortedSet<CoachPersonLiteTO> currentCoachesSet =
				Sets.newTreeSet(sortBy == null ? CoachPersonLiteTO.COACH_PERSON_LITE_TO_NAME_AND_ID_COMPARATOR : sortBy);
		currentCoachesSet.addAll(officialCoaches);
		final Collection<CoachPersonLiteTO> assignedCoaches =
				getAllAssignedCoachesLite(null).getRows();
		currentCoachesSet.addAll(assignedCoaches);
		return currentCoachesSet;
	}
	
	@Override
	public SortedSet<CoachPersonLiteTO> getAllCurrentCoachesLite(Comparator<CoachPersonLiteTO> sortBy, String homeDepartment) {
		final Collection<CoachPersonLiteTO> officialCoaches = getAllCoachesLite(null, homeDepartment).getRows();
		SortedSet<CoachPersonLiteTO> currentCoachesSet =
				Sets.newTreeSet(sortBy == null ? CoachPersonLiteTO.COACH_PERSON_LITE_TO_NAME_AND_ID_COMPARATOR : sortBy);
		currentCoachesSet.addAll(officialCoaches);
		final Collection<CoachPersonLiteTO> assignedCoaches =
				getAllAssignedCoachesLite(null, homeDepartment).getRows();
		currentCoachesSet.addAll(assignedCoaches);
		return currentCoachesSet;
	}

	@Override
	public PagingWrapper<Person> syncCoaches() {
		long methodStart = new Date().getTime();
		final Collection<Person> coaches = Lists.newArrayList();

		if ( Thread.currentThread().isInterrupted() ) {
			LOGGER.info("Abandoning syncCoaches because of thread interruption");
			return new PagingWrapper<Person>(coaches);
		}

		final Collection<String> coachUsernames = getAllCoachUsernamesFromDirectory();

		long mergeLoopStart = new Date().getTime();
		final AtomicLong timeInExternalReads = new AtomicLong();
		final AtomicLong timeInExternalWrites = new AtomicLong();
		for (final String coachUsername : coachUsernames) {

			if ( Thread.currentThread().isInterrupted() ) {
				LOGGER.info("Abandoning syncCoaches on username {} because of thread interruption", coachUsername);
				break;
			}

			long singlePersonStart = new Date().getTime();

			final AtomicReference<Person> coach = new AtomicReference<Person>();

			try {
				withCoachSyncTransaction(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						long localPersonLookupStart = new Date().getTime();
						try {
							coach.set(personFromUsername(coachUsername));
						} catch (final ObjectNotFoundException e) {
							LOGGER.debug("Coach {} not found", coachUsername);
						}
						long localPersonLookupEnd = new Date().getTime();
						TIMING_LOGGER.info("Read local coach by username {} in {} ms",
								coachUsername, localPersonLookupEnd - localPersonLookupStart);

						// Does coach exist in local SSP.person table?

						if (coach.get() == null) {

							// Attempt to find coach in external data
							try {
								long externalPersonLookupStart = new Date().getTime();

								final ExternalPerson externalPerson = externalPersonService
										.getByUsername(coachUsername);

								long externalPersonLookupEnd = new Date().getTime();
								long externalPersonLookupElapsed = externalPersonLookupEnd -
										externalPersonLookupStart;
								timeInExternalReads.set(timeInExternalReads.get()
										+ externalPersonLookupElapsed);
								TIMING_LOGGER.info("Read external coach by username {} in {} ms",
										coachUsername, externalPersonLookupElapsed);

								long externalPersonSyncStart = new Date().getTime();

								coach.set(new Person()); // NOPMD
								externalPersonService.updatePersonFromExternalPerson(
										coach.get(), externalPerson,true);

								long externalPersonSyncEnd = new Date().getTime();
								long externalPersonSyncElapsed = externalPersonSyncEnd -
										externalPersonSyncStart;
								timeInExternalWrites.set(timeInExternalWrites.get()
										+ externalPersonSyncElapsed);
								TIMING_LOGGER.info("Synced external coach by username {} in {} ms",
										coachUsername, externalPersonSyncElapsed);

							} catch (final ObjectNotFoundException e) {
								LOGGER.debug("Coach {} not found in external data",
										coachUsername);
							}
						}
						return coach.get();
					}
				});
			} catch ( ConstraintViolationException e ) {
				if ( "uq_person_school_id".equals(e.getConstraintName()) ) {
					LOGGER.warn("Skipping coach with non-unique schoolId '{}' (username '{}')",
							new Object[] { coach.get().getSchoolId(), coachUsername, e });
					coach.set(null);
				} else if ( "unique_person_username".equals(e.getConstraintName()) ) {
					LOGGER.warn("Skipping coach with non-unique username '{}' (schoolId '{}')",
							new Object[] { coachUsername, coach.get().getSchoolId(), e });
					coach.set(null);
				} else {
					throw e;
				}
			}


			if (coach.get() != null) {
				coaches.add(coach.get());
			}
			long singlePersonEnd = new Date().getTime();
			TIMING_LOGGER.info("SSP coach merge for username {} completed in {} ms",
					coachUsername, singlePersonEnd - singlePersonStart);
		}
		Long mergeLoopEnd = new Date().getTime();
		TIMING_LOGGER.info("All SSP merges for {} coaches completed in {} ms. Reading: {} ms. Writing: {} ms",
				new Object[] { coachUsernames.size(), mergeLoopEnd - mergeLoopStart,
						timeInExternalReads.get(), timeInExternalWrites.get() });

		PagingWrapper pw = new PagingWrapper<Person>(coaches);
		long methodEnd = new Date().getTime();
		TIMING_LOGGER.info("Read and merged PersonAttributesService {} coaches in {} ms",
				coaches.size(), methodEnd - methodStart);
		return pw;
	}

	@Override
	public String getSchoolIdForPersonId(UUID personId) throws ObjectNotFoundException {
		return dao.getSchoolIdForPersonId(personId);
	}

	private <V> V withCoachSyncTransaction(Callable<V> callable) {
		if ( perCoachSyncTransactions ) {
			return withTransaction.withNewTransactionAndUncheckedExceptions(callable);
		}
		return withTransaction.withTransactionAndUncheckedExceptions(callable);
	}

	

	@Override
	public void setPersonAttributesService(
			final PersonAttributesService personAttributesService) {
		this.personAttributesService = personAttributesService;
	}
	
	@Override
	public PagingWrapper<DisabilityServicesReportTO> getDisabilityReport(PersonSearchFormTO form,
			final SortingAndPaging sAndP) throws ObjectNotFoundException{
		return dao.getDisabilityReport(form, sAndP);
	}
	
	/**
	 * Used for Specific Report "Address Labels"
	 */
	@Override
	public PagingWrapper<BaseStudentReportTO> getStudentReportTOsFromCriteria(
			final PersonSearchFormTO personSearchFormTO,
			final SortingAndPaging sAndP) throws ObjectNotFoundException {

		final PagingWrapper<BaseStudentReportTO> people = dao.getStudentReportTOs(
				personSearchFormTO,
				sAndP);
		return people;
	}

	@Override
	public void evict(Person model) {
		dao.removeFromSession(model);	
	}

}