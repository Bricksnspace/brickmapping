/**
	Copyright 2017 Mario Pascucci <mpascucci@gmail.com>
	This file is part of BrickMapping

	BrickMapping is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	BrickMapping is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with BrickMapping.  If not, see <http://www.gnu.org/licenses/>.
 
 */


package bricksnspace.brickMapping;


import java.sql.SQLException;
import bricksnspace.dbconnector.DBConnector;

/**
 * Init functions
 * 
 * @author Mario Pascucci
 *
 */
public class BrickMapping {
	
	protected static DBConnector db;
	private static final String DBVERCONSTANT = "MPPMVERSION";
	private static final int DBVERSION = 1;

	
	private BrickMapping() {
		
	}
	
	
	public static void Init(DBConnector dbc) throws SQLException {

		if (dbc == null)
			throw new IllegalArgumentException("[BrickMapping] undefined DBConnector");
		db = dbc;
		// checks for new or already populated database
		if (!db.checkTable(PartMapping.table)) {
			// is a new database
			PartMapping.createTable();
			BrickColor.createTable();
			db.setDbVersion(DBVERCONSTANT, DBVERSION);
		}
		else { 
			// checks for database upgrade
			if (db.needsUpgrade(DBVERCONSTANT, DBVERSION)) {
				switch (db.getDbVersion(DBVERCONSTANT)) {
				case -1:
					upgradeFromMinus1();
					break;
				}
			}
		}
		PartMapping.init();
		BrickColor.init();
	}
	
	
	
	/**
	 * Contain all operation and queries to upgrade database tables and schema 
	 * to current version
	 * @throws SQLException
	 */
	private static void upgradeFromMinus1() throws SQLException {
		
		db.setDbVersion(DBVERCONSTANT, DBVERSION);
	}

	
}
