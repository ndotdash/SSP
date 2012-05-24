package org.jasig.ssp.model.tool;

import org.jasig.ssp.model.Person;
import org.jasig.ssp.model.PersonAssoc;

/**
 * The model for the Intake Form tool.
 * 
 * Currently only a simple wrapper around a fully-normalized {@link Person}.
 */
public class IntakeForm implements PersonAssoc {

	/**
	 * Person with the full tree of data, down to only using identifiers
	 * (non-full objects) when a circular dependency (usually a reference back
	 * to a Person instance) or reference data (system-level lookups like
	 * Challenges, etc.).
	 */
	private Person person;

	/**
	 * Gets the full Person instance.
	 * 
	 * @return the full Person instance
	 */
	@Override
	public Person getPerson() {
		return person;
	}

	/**
	 * Sets the full Person instance.
	 * 
	 * @param person
	 *            Person instance
	 */
	@Override
	public void setPerson(final Person person) {
		this.person = person;
	}
}
