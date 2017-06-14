/*
	Copyright 2013-2017 Mario Pascucci <mpascucci@gmail.com>
	This file is part of BrickUtils.

	BrickUtils is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	BrickUtils is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with BrickUtils.  If not, see <http://www.gnu.org/licenses/>.

*/


package bricksnspace.brickMapping;



import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.StartElement;



/**
 * Mapping part IDs between catalogs (LDD, Bricklink, LDraw)
 * 
 * @author Mario Pascucci
 *
 */
public class PartMapping {
	private int mapid;			// mapping id in table (primary key)
    private String masterid;	// the brick "family" ID 
    private String designid;	// LDD design ID
    private String decorid;	// brick decoration id from LDD
    private String name;		// brick name from BL
    private String blid;		// BrickLink ID
    private String ldrawid;		// LDraw catalog ID	
    private boolean ldd2bl;		// converts LDD to BL?
    private boolean bl2ldd;		// converts BL toLDD?
    private boolean ldd2dat;	// converts LDD to LDraw?
    private boolean dat2ldd;	// converts LDraw to LDD?
    private boolean bl2dat;		// converts BL to LDraw?
    private boolean dat2bl;		// converts LDraw to BL?
    private Timestamp lastmod;	// last modified
	public final static String fieldsOrder = "masterid,designid,blid,ldrawid,name,ldd2bl,bl2ldd,ldd2dat,dat2ldd,bl2dat,dat2bl,decorid,lastmod";
	public final static String table = "partmapping";
	public final static String FTSfields = "name,masterid,designid,blid,ldrawid,decorid";
	private static PreparedStatement updatePS = null;
	private static PreparedStatement insertPS = null;

    
	
	/**
	 * Create an empty mapping
	 */
    public PartMapping() {
    	setMasterid("");
    	setDesignid("");
    	setDecorid("");
    	setName("");
    	setBlid("");
    	setLdrawid("");
    	setLdd2bl(false);
    	setBl2ldd(false);
    	setLdd2dat(false);
    	setDat2ldd(false);
    	setBl2dat(false);
    	setDat2bl(false);
    }

    
//    /**
//     * Create a clone mapping
//     * @param pm mapping to clone
//     */
//    public PartMapping(PartMapping pm) {
//    	mapid = pm.mapid;
//    	masterid = pm.masterid;
//    	designid = pm.designid;
//    	decorid = pm.decorid;
//    	name = pm.name;
//    	blid = pm.blid;
//    	ldrawid = pm.ldrawid;
//    	ldd2bl = pm.ldd2bl;
//    	bl2ldd = pm.bl2ldd;
//    	ldd2dat = pm.ldd2dat;
//    	dat2ldd = pm.dat2ldd;
//    	bl2dat = pm.bl2dat;
//    	dat2bl = pm.dat2bl;
//    	lastmod = pm.lastmod;
//    }
    
    
    @Override
    public PartMapping clone() {
    	PartMapping pm = new PartMapping();
    	pm.setMapid(mapid);
    	pm.setMasterid(masterid);
    	pm.setDesignid(designid);
    	pm.setDecorid(decorid);
    	pm.setName(name);
    	pm.setBlid(blid);
    	pm.setLdrawid(ldrawid);
    	pm.setLdd2bl(ldd2bl);
    	pm.setBl2ldd(bl2ldd);
    	pm.setLdd2dat(ldd2dat);
    	pm.setDat2ldd(dat2ldd);
    	pm.setBl2dat(bl2dat);
    	pm.setDat2bl(dat2bl);
    	pm.setLastmod(lastmod);
    	return pm;
    }
    
    
    
	/**
     * Create a part mapping from an XML file 
     * @param xsr opening tag for mapping in XML format
	 * @throws IOException 
     */
    public PartMapping(StartElement xsr) throws IOException {

    	try {
	    	setMapid(Integer.parseInt(xsr.getAttributeByName(new QName("id")).getValue()));
	    	setMasterid(xsr.getAttributeByName(new QName("masterid")).getValue());
	    	if (getMasterid() == null) 
	    		setMasterid("");
	    	setDesignid(xsr.getAttributeByName(new QName("designid")).getValue());
	    	if (getDesignid() == null) 
	    		setDesignid("");
	    	setDecorid(xsr.getAttributeByName(new QName("decorid")).getValue());
	    	if (getDecorid() == null) 
	    		setDecorid("");
	    	setName(xsr.getAttributeByName(new QName("name")).getValue());
	    	if (getName() == null) 
	    		setName("");
	    	setBlid(xsr.getAttributeByName(new QName("blid")).getValue());
	    	if (getBlid() == null) 
	    		setBlid("");
	    	setLdrawid(xsr.getAttributeByName(new QName("ldrawid")).getValue());
	    	if (getLdrawid() == null) 
	    		setLdrawid("");
	    	setLdd2bl(xsr.getAttributeByName(new QName("ldd2bl")).getValue().equals("1"));
	    	setBl2ldd(xsr.getAttributeByName(new QName("bl2ldd")).getValue().equals("1"));
	    	setLdd2dat(xsr.getAttributeByName(new QName("ldd2dat")).getValue().equals("1"));
	    	setDat2ldd(xsr.getAttributeByName(new QName("dat2ldd")).getValue().equals("1"));
	    	setBl2dat(xsr.getAttributeByName(new QName("bl2dat")).getValue().equals("1"));
	    	setDat2bl(xsr.getAttributeByName(new QName("dat2bl")).getValue().equals("1"));
	    	setLastmod(Timestamp.valueOf(xsr.getAttributeByName(new QName("lastmod")).getValue()));
    	} catch (NumberFormatException | NullPointerException e) {
    		throw new IOException("Error in update file format. Opening tag:\n"+xsr, e);
    	}
    }

    
    
    @Override
    public String toString() {
    	return getMapid()+"-"+getDesignid()+"|"+getBlid()+"|"+getLdrawid()+" - "+getName();
    }

    
    
    /**
     * Init database and utility
     * @param bdb DBConnector for database
     * @throws SQLException
     */
	protected static void init() throws SQLException {

		Statement st = BrickMapping.db.createStatement();
		st.executeUpdate("CREATE INDEX IF NOT EXISTS pm_mapid ON "+table+"(mapid)");
		st.executeUpdate("CREATE INDEX IF NOT EXISTS pm_lddid ON "+table+"(designid)");
		st.executeUpdate("CREATE INDEX IF NOT EXISTS pm_masterid ON "+table+"(masterid)");
		st.executeUpdate("CREATE INDEX IF NOT EXISTS pm_blid ON "+table+"(blid)");
		st.executeUpdate("CREATE INDEX IF NOT EXISTS pm_ldrawid ON "+table+"(ldrawid)");
		if (!BrickMapping.db.checkFTS(table,FTSfields)) {
			BrickMapping.db.createFTS(table, FTSfields);
			Logger.getGlobal().log(Level.INFO, "Create Full Text index");
		}
		insertPS = BrickMapping.db.prepareStatement("INSERT INTO "+table +
				" ("+fieldsOrder+") " +
				"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,NOW())",Statement.RETURN_GENERATED_KEYS);
		updatePS = BrickMapping.db.prepareStatement("UPDATE "+table+" SET " +
				"masterid=?," +
				"designid=?," +
				"blid=?," +
				"ldrawid=?," +
				"name=?," +
				"ldd2bl=?," +
				"bl2ldd=?," +
				"ldd2dat=?," +
				"dat2ldd=?," +
				"bl2dat=?," +
				"dat2bl=?," +
				"decorid=?," +
				"lastmod=NOW() " +
				"WHERE mapid=?");
//		selectPS  = BrickMapping.db.prepareStatement(
//				"SELECT mapid," + fieldsOrder +
//				" FROM "+table);
	}
	
	

	
	protected static void createTable() throws SQLException {
		
		Statement st;
		
		st = BrickMapping.db.createStatement();
		st.execute("DROP TABLE IF EXISTS "+table+"; " +
				"CREATE TABLE IF NOT EXISTS "+table+" (" +
				"mapid INT PRIMARY KEY AUTO_INCREMENT, " +
				"masterid VARCHAR(64)," +
				"designid VARCHAR(64)," +
				"decorid VARCHAR(64)," +
				"blid VARCHAR(64)," +
				"ldrawid VARCHAR(64)," +
				"name VARCHAR(255)," +
				"ldd2bl BOOL," +
				"bl2ldd BOOL," +
				"ldd2dat BOOL," +
				"dat2ldd BOOL," +
				"bl2dat BOOL," +
				"dat2bl BOOL," +
				"lastmod TIMESTAMP); COMMIT ");
	}
	
	
	/**
	 * Prepare for update. Uses transaction to handle update failures
	 * @throws SQLException
	 */
	public void prepareUpdate() throws SQLException {
		
		BrickMapping.db.deleteFTS(table);
//		insertPS = BrickMapping.db.prepareStatement("INSERT INTO "+table +
//				" ("+fieldsOrder+") " +
//				"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,NOW())",Statement.RETURN_GENERATED_KEYS);
//		updatePS = BrickMapping.db.prepareStatement("UPDATE "+table+" SET " +
//				"masterid=?," +
//				"designid=?," +
//				"blid=?," +
//				"ldrawid=?," +
//				"name=?," +
//				"ldd2bl=?," +
//				"bl2ldd=?," +
//				"ldd2dat=?," +
//				"dat2ldd=?," +
//				"bl2dat=?," +
//				"dat2bl=?," +
//				"decorid=?," +
//				"lastmod=NOW() " +
//				"WHERE mapid=?");
		// NOTE! drop "something" commits an open transaction
		BrickMapping.db.autocommitDisable();
	}
	
	
	
	/**
	 * Successfully ends an update
	 * @throws SQLException
	 */
	public void endUpdate() throws SQLException {
		
		BrickMapping.db.commit();
		BrickMapping.db.autocommitEnable();
		BrickMapping.db.createFTS(table, FTSfields);
//		insertPS.close();
//		updatePS.close();
	}
	
	
	
	/**
	 * Rollback on failed update
	 * @throws SQLException
	 */
	public void abortUpdate() throws SQLException {
		
		BrickMapping.db.rollback();
		BrickMapping.db.autocommitEnable();
//		insertPS.close();
//		updatePS.close();
		BrickMapping.db.createFTS(table, FTSfields);
	}
	
	
	

	
	// Special method to execute debug/diagnostic/repair query
	// that needs to execute once at startup
	// remove or comment code after use 
	public static void special() throws SQLException {
		
		@SuppressWarnings("unused")
		Statement st;
		
		st = BrickMapping.db.createStatement();
		// put code here ----v----
//		st.execute("INSERT INTO "+table + " (mapid) values (5596)");
		// end code      ----^----
		
	}
	
	
	public int insert() throws SQLException {
		
		ResultSet rs;
		
		insertPS.setString(1, getMasterid());
		insertPS.setString(2, getDesignid());
		insertPS.setString(3, getBlid());
		insertPS.setString(4, getLdrawid());
		insertPS.setString(5, getName());
		insertPS.setBoolean(6, isLdd2bl());
		insertPS.setBoolean(7, isBl2ldd());
		insertPS.setBoolean(8, isLdd2dat());
		insertPS.setBoolean(9, isDat2ldd());
		insertPS.setBoolean(10, isBl2dat());
		insertPS.setBoolean(11, isDat2bl());
		insertPS.setString(12, getDecorid());
		
		insertPS.executeUpdate();
		rs = insertPS.getGeneratedKeys();
		rs.next();
		setMapid(rs.getInt(1));
		return getMapid();
	}
	
	
	public void update() throws SQLException {
		
		updatePS.setString(1, getMasterid());
		updatePS.setString(2, getDesignid());
		updatePS.setString(3, getBlid());
		updatePS.setString(4, getLdrawid());
		updatePS.setString(5, getName());
		updatePS.setBoolean(6, isLdd2bl());
		updatePS.setBoolean(7, isBl2ldd());
		updatePS.setBoolean(8, isLdd2dat());
		updatePS.setBoolean(9, isDat2ldd());
		updatePS.setBoolean(10, isBl2dat());
		updatePS.setBoolean(11, isDat2bl());
		updatePS.setString(12, getDecorid());
		updatePS.setInt(13, getMapid());
		
		updatePS.executeUpdate();
	}
	
	
	public static ArrayList<PartMapping> get(String filterExpr) throws SQLException {
		
		ArrayList<PartMapping> pml = new ArrayList<PartMapping>();
		PartMapping pm;
		ResultSet rs;
		
		Statement st = BrickMapping.db.createStatement();
		if (filterExpr == null)
			rs = st.executeQuery("SELECT mapid,"+fieldsOrder+" FROM "+table);
		else {
			rs = st.executeQuery("SELECT " +
					"mapid," + fieldsOrder + 
					" FROM "+table+" where "+filterExpr);
		}
		while (rs.next()) {
			// fetch and assign rows to a PartMapping Array list
			pm = new PartMapping();
			pm.setMapid(rs.getInt("mapid"));
			pm.setMasterid(rs.getString("masterid"));
			pm.setDesignid(rs.getString("designid"));
			pm.setBlid(rs.getString("blid"));
			pm.setLdrawid(rs.getString("ldrawid"));
			pm.setDecorid(rs.getString("decorid"));
			pm.setName(rs.getString("name"));
			pm.setLdd2bl(rs.getBoolean("ldd2bl"));
			pm.setLdd2dat(rs.getBoolean("ldd2dat"));
			pm.setBl2ldd(rs.getBoolean("bl2ldd"));
			pm.setBl2dat(rs.getBoolean("bl2dat"));
			pm.setDat2bl(rs.getBoolean("dat2bl"));
			pm.setDat2ldd(rs.getBoolean("dat2ldd"));
			pm.setLastmod(rs.getTimestamp("lastmod"));
			pml.add(pm);
		}
		return pml;
	}
	
	
	
	public static ArrayList<PartMapping> getPS(PreparedStatement ps) throws SQLException {
		
		ArrayList<PartMapping> pml = new ArrayList<PartMapping>();
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			// fetch and assign rows to a PartMapping Array list
			PartMapping pm = new PartMapping();
			pm.setMapid(rs.getInt("mapid"));
			pm.setMasterid(rs.getString("masterid"));
			pm.setDesignid(rs.getString("designid"));
			pm.setBlid(rs.getString("blid"));
			pm.setLdrawid(rs.getString("ldrawid"));
			pm.setDecorid(rs.getString("decorid"));
			pm.setName(rs.getString("name"));
			pm.setLdd2bl(rs.getBoolean("ldd2bl"));
			pm.setLdd2dat(rs.getBoolean("ldd2dat"));
			pm.setBl2ldd(rs.getBoolean("bl2ldd"));
			pm.setBl2dat(rs.getBoolean("bl2dat"));
			pm.setDat2bl(rs.getBoolean("dat2bl"));
			pm.setDat2ldd(rs.getBoolean("dat2ldd"));
			pm.setLastmod(rs.getTimestamp("lastmod"));
			pml.add(pm);
		}
		return pml;
	}

	
	
	
	public static ArrayList<PartMapping> getFTS(String filterExpr,String filter) throws SQLException {
		
		PreparedStatement ps;

		//select b.*,f.score from FTL_SEARCH_DATA('words', 0, 0) f left join blparts b on(f.keys[0]=b.id) 
		//                 where f.table='BLPARTS';

		if (filterExpr != null) {
			if (filter == null) {
				ps = BrickMapping.db.prepareStatement("SELECT mapid,"+fieldsOrder+" FROM FTL_SEARCH_DATA('"+filterExpr+"',0,0) f " +
						"LEFT JOIN "+table+" b on (f.keys[0]=b.mapid) WHERE f.table='PARTMAPPING'");
			}
			else {
				ps = BrickMapping.db.prepareStatement("SELECT mapid,"+fieldsOrder+" FROM FTL_SEARCH_DATA('"+filterExpr+"',0,0) f " +
						"LEFT JOIN "+table+" b on (f.keys[0]=b.mapid) WHERE f.table='PARTMAPPING' AND "+filter);
			}
			return getPS(ps);
		}
		return new ArrayList<PartMapping>(1);
	}
	
	
	

	
	
	public static ArrayList<PartMapping> getNew() throws SQLException {
		
		PreparedStatement ps = BrickMapping.db.prepareStatement("SELECT mapid,"+fieldsOrder+
				" FROM "+table+" where lastmod > TIMESTAMPADD(MINUTE,-15,SELECT MAX(lastmod) from "+table+")");
		return getPS(ps);
	}
	
	
	
	public static Timestamp[] getLastModifyTime() throws SQLException {
		
		PreparedStatement ps;
		ResultSet rs;
		final int LIMIT = 20;
		Timestamp last[] = new Timestamp[LIMIT];
		
		ps = BrickMapping.db.prepareStatement("SELECT FORMATDATETIME(lastmod,'yyyy-MM-dd') as datemod "+
				" FROM "+table+" GROUP BY datemod ORDER BY datemod DESC LIMIT "+LIMIT);
		rs = ps.executeQuery();
		int i = 0;
		while (rs.next()) {
			last[i] = Timestamp.valueOf(rs.getString("datemod")+" 00:00:00");
			i++;
			if (i >= LIMIT)
				break;
		}
		return last;
	}

	
	public static ArrayList<PartMapping> getModifiedAfter(Timestamp lastmodified) throws SQLException {
		
		PreparedStatement ps;
		ps = BrickMapping.db.prepareStatement("SELECT mapid,"+fieldsOrder+" FROM "+table+
				" where lastmod>=?");
		ps.setTimestamp(1, lastmodified);
		return getPS(ps);
	}
    

	
	
	

	public static PartMapping lddToLDraw(String designid, String decorid) throws SQLException {
		
		PreparedStatement ps;
		
		if (decorid == null || decorid.length() == 0) {
			ps = BrickMapping.db.prepareStatement("SELECT mapid,"+fieldsOrder+" FROM "+table+" WHERE designid=? and ldd2dat and decorid=''");
			ps.setString(1, designid);
		}
		else {
			ps = BrickMapping.db.prepareStatement("SELECT mapid,"+fieldsOrder+" FROM "+table+" WHERE designid=? and ldd2dat and decorid=?");
			ps.setString(1, designid);
			ps.setString(2, decorid);
		}
		ArrayList<PartMapping> pm = getPS(ps);
		if (pm.size() > 1) {
			throw new SQLException("Internal error: duplicated part mapping in database\n"+pm.get(0));
		}
		else if (pm.size() == 1) {
			return pm.get(0);
		}
		return new PartMapping();
	}
	
	
	
	public static PartMapping lddToBlink(String designid, String decorid) throws SQLException {
		
		PreparedStatement ps;
		
		if (decorid == null || decorid.length() == 0) {
			ps = BrickMapping.db.prepareStatement("SELECT mapid,"+fieldsOrder+" FROM "+table+" WHERE designid=? and ldd2bl and decorid=''");
			ps.setString(1, designid);
		}
		else {
			ps = BrickMapping.db.prepareStatement("SELECT mapid,"+fieldsOrder+" FROM "+table+" WHERE designid=? and ldd2bl and decorid=?");
			ps.setString(1, designid);
			ps.setString(2, decorid);
		}
		ArrayList<PartMapping> pm = getPS(ps);
		if (pm.size() > 1) {
			throw new SQLException("Internal error: duplicated part mapping in database\n"+pm.get(0));
		}
		else if (pm.size() == 1) {
			return pm.get(0);
		}
		return new PartMapping();
	}
	
	
	
	public static PartMapping blinkToLdd(String blid) throws SQLException {
		
		// gets ldd part equivalence
		PreparedStatement ps = BrickMapping.db.prepareStatement("SELECT mapid,"+fieldsOrder+" FROM "+table+" WHERE blid=? and bl2ldd");
		ps.setString(1, blid);
		ArrayList<PartMapping> pm = getPS(ps);
		if (pm.size() > 1) {
			throw new SQLException("Internal error: duplicated part mapping in database\n"+pm.get(0));
		}
		else if (pm.size() == 1) {
			return pm.get(0);
		}
		return new PartMapping();
	}
	
	
	
	public static PartMapping blinkToLDraw(String blid) throws SQLException {
		
		// get ldraw part equivalence
		PreparedStatement ps = BrickMapping.db.prepareStatement("SELECT mapid,"+fieldsOrder+" FROM "+table+" WHERE blid=? and bl2dat");
		ps.setString(1, blid);
		ArrayList<PartMapping> pm = getPS(ps);
		if (pm.size() > 1) {
			throw new SQLException("Internal error: duplicated part mapping in database\n"+pm.get(0));
		}
		else if (pm.size() == 1) {
			return pm.get(0);
		}
		return new PartMapping();
	}

	

	public static PartMapping ldrawToLdd(String ldr) throws SQLException {
		
		// gets ldd part equivalence
		PreparedStatement ps = BrickMapping.db.prepareStatement("SELECT mapid,"+fieldsOrder+" FROM "+table+" WHERE ldrawid=? and dat2ldd");
		ps.setString(1, ldr);
		ArrayList<PartMapping> pm = getPS(ps);
		if (pm.size() > 1) {
			throw new SQLException("Internal error: duplicated part mapping in database\n"+pm.get(0));
		}
		else if (pm.size() == 1) {
			return pm.get(0);
		}
		return new PartMapping();
	}
	
	
	public static PartMapping ldrawToBlink(String ldr) throws SQLException {
	
		// get BL part equivalence
		PreparedStatement ps = BrickMapping.db.prepareStatement("SELECT mapid,"+fieldsOrder+" FROM "+table+" WHERE ldrawid=? and dat2bl");
		ps.setString(1, ldr);
		ArrayList<PartMapping> pm = getPS(ps);
		if (pm.size() > 1) {
			throw new SQLException("Internal error: duplicated part mapping in database\n"+pm.get(0));
		}
		else if (pm.size() == 1) {
			return pm.get(0);
		}
		return new PartMapping();
	}

	
	
//	// checks part mappings for part delete/change in BLink/LDraw part lists
//	// tags parts with:
//	// ##bldelete
//	// ##ldrdelete
//	
//	public static void checkDeletedId() throws SQLException {
//		
//		Statement st;
//		
//		st = BrickMapping.db.createStatement();
//		st.execute("UPDATE "+table+" set name=CONCAT(name,' ##bldelete') " +
//				"WHERE blid != '' and blid NOT IN (SELECT blid from blparts WHERE NOT deleted)");
//		st.execute("UPDATE "+table+" set name=CONCAT(name,' ##ldrdelete') " +
//				"WHERE ldrawid != '' and ldrawid NOT IN (SELECT ldrid from ldrparts WHERE NOT deleted)");
//		
//	}
	
	
	
	
	public static void cleanup() throws SQLException {
		
		Statement st;
		
		st = BrickMapping.db.createStatement();
		// cleanup ##ldrawnew tag
		//st.execute("UPDATE "+table+" set name=REPLACE(name,'##ldrawnew')");
		//st.execute("UPDATE "+table+" set name=REPLACE(name,'##ldrdelete')");
		//st.execute("UPDATE "+table+" set name=REPLACE(name,'##bldelete')");
		st.execute("UPDATE "+table+" set masterid=TRIM(masterid),designid=TRIM(designid),name=TRIM(name)");
		st.execute("UPDATE "+table+" set ldrawid=TRIM(ldrawid),blid=TRIM(blid),decorid=TRIM(decorid)");
	}
	
	
	
	public void check() throws SQLException {
		
		Statement st;
		ResultSet rs;
		
		if (isLdd2bl()) {
			// it is a ldd to Bricklink mapping, must be unique
			st = BrickMapping.db.createStatement();
			rs = st.executeQuery("SELECT * FROM "+table+
					" WHERE designid = '"+getDesignid()+"' " +
							"AND " +
					"decorid = '"+getDecorid()+"' " +
							"AND " +
					"ldd2bl " +
					"AND " +
					"mapid != "+Integer.toString(getMapid()));
			if (rs.next()) {
				String lddid = rs.getString("designid");
				String bl = rs.getString("blid");
				throw new SQLException("Duplicated part\n"+
						"Design ID: "+getDesignid()+"->"+
						"BLink ID:  "+getBlid()+"\n"+
						"is already mapped as\n"+
						"Design ID: "+lddid+"->"+
						"BLink ID:  "+bl);
			}
		}
		if (isLdd2dat()) {
			// it is a ldd to LDraw mapping, must be unique
			st = BrickMapping.db.createStatement();
			rs = st.executeQuery("SELECT * FROM "+table+
					" WHERE designid = '"+getDesignid()+"' " +
							"AND " +
					"decorid = '"+getDecorid()+"' " +
							"AND " +
					"ldd2dat " +
					"AND " +
					"mapid != "+Integer.toString(getMapid()));
			if (rs.next()) {
				String lddid = rs.getString("designid");
				String ldraw = rs.getString("ldrawid");
				throw new SQLException("Duplicated part\n"+
						"Design ID: "+getDesignid()+"->"+
						"LDraw ID:  "+getLdrawid()+"\n"+
						"is already mapped as\n"+
						"Design ID: "+lddid+"->"+
						"LDraw ID:  "+ldraw);
			}
		}
		if (isBl2ldd()) {
			// it is a BL to ldd mapping
			st = BrickMapping.db.createStatement();
			rs = st.executeQuery("SELECT * FROM "+table+
					" WHERE blid = '"+getBlid()+"' " +
							"AND " +
					"decorid = '"+getDecorid()+"' " +
							"AND " +
					"bl2ldd " +
					"AND " +
					"mapid != "+Integer.toString(getMapid()));
			if (rs.next()) {
				String lddid = rs.getString("designid");
				String bl = rs.getString("blid");
				throw new SQLException("Duplicated part\n"+
						"BLink ID: "+getBlid()+"->"+
						"Design ID:  "+getDesignid()+"\n"+
						"is already mapped as\n"+
						"BLink ID: "+bl+"\n"+
						"Design ID:  "+lddid);
			}
		}
		if (isDat2ldd()) {
			// it is a LDraw to ldd mapping
			st = BrickMapping.db.createStatement();
			rs = st.executeQuery("SELECT * FROM "+table+
					" WHERE ldrawid = '"+getLdrawid()+"' " +
							"AND " +
					"decorid = '"+getDecorid()+"' " +
							"AND " +
					"dat2ldd " +
					"AND " +
					"mapid != "+Integer.toString(getMapid()));
			if (rs.next()) {
				String lddid = rs.getString("designid");
				String ldraw = rs.getString("ldrawid");
				throw new SQLException("Duplicated part\n"+
						"LDRaw ID: "+getLdrawid()+"->"+
						"Design ID:  "+getDesignid()+"\n"+
						"is already mapped as\n"+
						"LDraw ID: "+ldraw+"->"+
						"Design ID:  "+lddid);
			}
		}
		if (isBl2dat()) {
			// it is a BL to ldraw mapping
			st = BrickMapping.db.createStatement();
			rs = st.executeQuery("SELECT * FROM "+table+
					" WHERE blid = '"+getBlid()+"' " +
							"AND " +
					"bl2dat " +
					"AND " +
					"mapid != "+Integer.toString(getMapid()));
			if (rs.next()) {
				String ldraw = rs.getString("ldrawid");
				String bl = rs.getString("blid");
				throw new SQLException("Duplicated part\n"+
						"BLink ID: "+getBlid()+"->"+
						"LDraw ID:  "+getLdrawid()+"\n"+
						"is already mapped as\n"+
						"BLink ID: "+bl+"\n"+
						"LDraw ID:  "+ldraw);
			}
		}
		if (isDat2bl()) {
			// it is a LDraw to bl mapping
			st = BrickMapping.db.createStatement();
			rs = st.executeQuery("SELECT * FROM "+table+
					" WHERE ldrawid = '"+getLdrawid()+"' " +
							"AND " +
					"dat2bl " +
					"AND " +
					"mapid != "+Integer.toString(getMapid()));
			if (rs.next()) {
				String bl = rs.getString("blid");
				String ldraw = rs.getString("ldrawid");
				throw new SQLException("Duplicated part\n"+
						"LDRaw ID: "+getLdrawid()+"->"+
						"BLink ID:  "+getBlid()+"\n"+
						"is already mapped as\n"+
						"LDraw ID: "+ldraw+"->"+
						"BLink ID:  "+bl);
			}
		}
		
	}
	
	///////////////////////////////////////////
	// statistics
	///////////////////////////////////////////
	
	
	public static int countRules() {

		Statement st;
		ResultSet rs;
		
		try {
			st = BrickMapping.db.createStatement();
			rs = st.executeQuery("SELECT COUNT(*) FROM " + table +
					" WHERE ldd2bl OR ldd2dat OR bl2dat OR bl2ldd OR dat2bl OR dat2ldd");
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			return -1;
		}
	}

	
	
	///////////////////////////////////////////
	// XML I/O & update helper methods
	///////////////////////////////////////////

	public void XMLWrite(XMLStreamWriter xsw) throws XMLStreamException {
		
		xsw.writeStartElement("partmap");
		xsw.writeAttribute("id",Integer.toString(getMapid()));
		xsw.writeAttribute("masterid",getMasterid()); 
		xsw.writeAttribute("designid",getDesignid());
		xsw.writeAttribute("decorid",getDecorid());
		xsw.writeAttribute("name",getName());
		xsw.writeAttribute("blid",getBlid());
		xsw.writeAttribute("ldrawid",getLdrawid());	
		xsw.writeAttribute("ldd2bl",isLdd2bl()?"1":"0");
		xsw.writeAttribute("bl2ldd",isBl2ldd()?"1":"0");
		xsw.writeAttribute("ldd2dat",isLdd2dat()?"1":"0");
		xsw.writeAttribute("dat2ldd",isDat2ldd()?"1":"0");
		xsw.writeAttribute("bl2dat",isBl2dat()?"1":"0");
		xsw.writeAttribute("dat2bl",isDat2bl()?"1":"0");
		xsw.writeAttribute("lastmod",getLastmod().toString());
		xsw.writeEndElement();
		xsw.writeCharacters("\n");
	}

	
	
	public void updateMapping() throws SQLException {
		
		PreparedStatement ps;
		ResultSet rs;
		
		ps = BrickMapping.db.prepareStatement("SELECT * FROM "+table+" where mapid=?");
		ps.setInt(1, getMapid());
		rs = ps.executeQuery();
		if (rs.next()) {
			update();
		}
		else {
			insert();
		}
	}


	/**
	 * @return the designid
	 */
	public String getDesignid() {
		return designid;
	}


	/**
	 * @param designid the designid to set
	 */
	public void setDesignid(String designid) {
		this.designid = designid;
	}


	/**
	 * @return the decorid
	 */
	public String getDecorid() {
		return decorid;
	}


	/**
	 * @param decorid the decorid to set
	 */
	public void setDecorid(String decorid) {
		this.decorid = decorid;
	}


	/**
	 * @return the masterid
	 */
	public String getMasterid() {
		return masterid;
	}


	/**
	 * @param masterid the masterid to set
	 */
	public void setMasterid(String masterid) {
		this.masterid = masterid;
	}


	/**
	 * @return the ldrawid
	 */
	public String getLdrawid() {
		return ldrawid;
	}


	/**
	 * @param ldrawid the ldrawid to set
	 */
	public void setLdrawid(String ldrawid) {
		this.ldrawid = ldrawid;
	}


	/**
	 * @return the blid
	 */
	public String getBlid() {
		return blid;
	}


	/**
	 * @param blid the blid to set
	 */
	public void setBlid(String blid) {
		this.blid = blid;
	}


	/**
	 * @return the mapid
	 */
	public int getMapid() {
		return mapid;
	}


	/**
	 * @param mapid the mapid to set
	 */
	public void setMapid(int mapid) {
		this.mapid = mapid;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return the lastmod
	 */
	public Timestamp getLastmod() {
		return lastmod;
	}


	/**
	 * @param lastmod the lastmod to set
	 */
	public void setLastmod(Timestamp lastmod) {
		this.lastmod = lastmod;
	}


	/**
	 * @return the ldd2bl
	 */
	public boolean isLdd2bl() {
		return ldd2bl;
	}


	/**
	 * @param ldd2bl the ldd2bl to set
	 */
	public boolean setLdd2bl(boolean ldd2bl) {
		this.ldd2bl = ldd2bl;
		return ldd2bl;
	}


	/**
	 * @return the ldd2dat
	 */
	public boolean isLdd2dat() {
		return ldd2dat;
	}


	/**
	 * @param ldd2dat the ldd2dat to set
	 */
	public boolean setLdd2dat(boolean ldd2dat) {
		this.ldd2dat = ldd2dat;
		return ldd2dat;
	}


	/**
	 * @return the bl2ldd
	 */
	public boolean isBl2ldd() {
		return bl2ldd;
	}


	/**
	 * @param bl2ldd the bl2ldd to set
	 */
	public boolean setBl2ldd(boolean bl2ldd) {
		this.bl2ldd = bl2ldd;
		return bl2ldd;
	}


	/**
	 * @return the bl2dat
	 */
	public boolean isBl2dat() {
		return bl2dat;
	}


	/**
	 * @param bl2dat the bl2dat to set
	 */
	public void setBl2dat(boolean bl2dat) {
		this.bl2dat = bl2dat;
	}


	/**
	 * @return the dat2bl
	 */
	public boolean isDat2bl() {
		return dat2bl;
	}


	/**
	 * @param dat2bl the dat2bl to set
	 */
	public boolean setDat2bl(boolean dat2bl) {
		this.dat2bl = dat2bl;
		return dat2bl;
	}


	/**
	 * @return the dat2ldd
	 */
	public boolean isDat2ldd() {
		return dat2ldd;
	}


	/**
	 * @param dat2ldd the dat2ldd to set
	 */
	public boolean setDat2ldd(boolean dat2ldd) {
		this.dat2ldd = dat2ldd;
		return dat2ldd;
	}
	

}
