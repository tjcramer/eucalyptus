/*************************************************************************
 * Copyright 2009-2013 Eucalyptus Systems, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Please contact Eucalyptus Systems, Inc., 6755 Hollister Ave., Goleta
 * CA 93117, USA or visit http://www.eucalyptus.com/licenses/ if you need
 * additional information or have any questions.
 ************************************************************************/
package com.eucalyptus.reporting.domain;

import org.apache.log4j.Logger;

import com.eucalyptus.entities.Entities;
import com.eucalyptus.entities.TransactionResource;

/**
 * <p>ReportingUserCrud is an object for CReating, Updating, and Deleting users. This class should
 * be used instead of creating User hibernate objects and storing them directly.
 */
public class ReportingUserCrud
{
	private static Logger LOG = Logger.getLogger( ReportingUserCrud.class );

	private static ReportingUserCrud instance = null;

	public static synchronized ReportingUserCrud getInstance()
	{
		if (instance == null) {
			instance = new ReportingUserCrud();
		}
		return instance;
	}

	protected ReportingUserCrud()
	{
	}

	/**
 	 * Create or update a user. This method can be called repeatedly every time an event is
 	 * received that has user data. This method will update the user with a new name if
 	 * the name has changed, or will do nothing if the user already exists with the supplied
 	 * name.
 	 */
	public void createOrUpdateUser(String id, String accountId, String name)
	{
		if (id==null || accountId==null || name==null) throw new IllegalArgumentException("args cant be null");

		ReportingUser oldUser = ReportingUserDao.getInstance().getReportingUser(id);
		if (oldUser!=null && oldUser.getName().equals(name)) {
			return;
		} else if (oldUser!=null) {
			updateInDb(id, name);
		} else {
			try {
				addToDb(id, accountId, name);
			} catch (RuntimeException e) {
				LOG.error(e);
			}
		}

	}

	private void updateInDb(String id, String name)
	{
		LOG.debug("Update reporting user in db, id:" + id + " name:" + name);

		try ( final TransactionResource db = Entities.transactionFor( ReportingUser.class ) ) {
			final ReportingUser searchUser = new ReportingUser( );
			searchUser.setId( id );
			Entities.uniqueResult( searchUser ).setName( name );
			db.commit();
		} catch (Exception ex) {
			LOG.error(ex);
			throw new RuntimeException(ex);
		}			
	}

	private void addToDb(String id, String accountId, String name)
	{
		LOG.debug("Add reporting user to db, id:" + id + " accountId:" + accountId + " name:" + name);

		try ( final TransactionResource db = Entities.transactionFor( ReportingUser.class ) ) {
			Entities.persist( new ReportingUser( id, accountId, name ) );
			db.commit();
		} catch (Exception ex) {
			LOG.error(ex);
			throw new RuntimeException(ex);
		}					
	}

}
