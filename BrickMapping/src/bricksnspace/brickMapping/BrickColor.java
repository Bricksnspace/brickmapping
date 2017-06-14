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

import java.awt.Color;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.StartElement;


/* 
 * A brick "color" in LDD, BL and LDraw catalogs, with helpers for display 
 * color sample and conversions between catalogs 
 */



public class BrickColor {
	private int mapid;
	private int ldd;		// master ID for color
	private int bl;
	private int ldraw;
	private Color color;
	private boolean inProduction;
	private boolean metallic;
	private boolean transparent;
	private boolean glitter;
	private String lddName;
	private int colorGroup;	// index in ColorGroup class
	private String notes;
	private Timestamp lastmod;

	public static final String fieldsOrder = "ldd,bl,ldraw,r,g,b,a,inuse,metal,transparent,glitter,lddname,colgrp,notes,lastmod";
	public static final String table = "colors";
	private static PreparedStatement insertPS = null;
	private static PreparedStatement updatePS = null;
	//private static PreparedStatement deletePS = null;
	private static HashMap<Integer,BrickColor>colorMap;
	
	
	@Override
	public String toString() {
		return "BrickColor [mapid=" + getMapid() +", ldd=" + getLdd() + ", color=" + getColor()
				+ ", inProduction=" + isInProduction() + ", metallic=" + isMetallic() + ", transparent=" + isTransparent() 
				+ ", glitter=" + isGlitter() + ", lddName=" + getLddName()
				+ ", lastmod=" + getLastmod() + "]";
	}

	
	public BrickColor() {

		setColor(Color.BLACK);
		setInProduction(false);
		setMetallic(false);
		setTransparent(false);
		setGlitter(false);
		setLddName("");
		setNotes("");
	}
	
	
	
	@Override
	public BrickColor clone() {
		
		BrickColor bc = new BrickColor();
		bc.setMapid(mapid);
		bc.setLdd(ldd);
		bc.setBl(bl);
		bc.setLdraw(ldraw);
		bc.setColor(new Color(getColor().getRed(),
				getColor().getGreen(),
				getColor().getBlue(),
				getColor().getAlpha()));
		bc.setInProduction(inProduction);
		bc.setMetallic(metallic);
		bc.setTransparent(transparent);
		bc.setGlitter(glitter);
		bc.setLddName(lddName);
		bc.setColorGroup(colorGroup);
		bc.setNotes(notes);
		bc.setLastmod(lastmod);
		return bc;
	}

	
	
	public BrickColor(StartElement xsr) throws IOException {
		
		try {
			setMapid(Integer.parseInt(xsr.getAttributeByName(new QName("id")).getValue()));
			setLdd(Integer.parseInt(xsr.getAttributeByName(new QName("ldd")).getValue()));
			setBl(Integer.parseInt(xsr.getAttributeByName(new QName("bl")).getValue()));
			setLdraw(Integer.parseInt(xsr.getAttributeByName(new QName("ldraw")).getValue()));
			setColor(new Color(Integer.parseInt(xsr.getAttributeByName(new QName("r")).getValue()),
					Integer.parseInt(xsr.getAttributeByName(new QName("g")).getValue()),
					Integer.parseInt(xsr.getAttributeByName(new QName("b")).getValue()),
					Integer.parseInt(xsr.getAttributeByName(new QName("a")).getValue())));
			setInProduction(xsr.getAttributeByName(new QName("inuse")).getValue().equals("1"));
			setMetallic(xsr.getAttributeByName(new QName("metallic")).getValue().equals("1"));
			setTransparent(xsr.getAttributeByName(new QName("transparent")).getValue().equals("1"));
			setGlitter(xsr.getAttributeByName(new QName("glitter")).getValue().equals("1"));
			setLddName(xsr.getAttributeByName(new QName("lddname")).getValue());
			setColorGroup(Integer.parseInt(xsr.getAttributeByName(new QName("group")).getValue()));
			setNotes(xsr.getAttributeByName(new QName("notes")).getValue());
			setLastmod(Timestamp.valueOf(xsr.getAttributeByName(new QName("lastmod")).getValue()));
		} catch (NumberFormatException | NullPointerException e) {
			throw new IOException("Error in file format. Opening tag:\n"+xsr,e);
		}
	}

	
	
	protected static void init() throws SQLException {

		Statement st = BrickMapping.db.createStatement();
		// creates indexes to speedup search
		st.executeUpdate("CREATE INDEX IF NOT EXISTS color_ldd ON "+table+"(ldd)");
		st.executeUpdate("CREATE INDEX IF NOT EXISTS color_bl ON "+table+"(bl)");
		st.executeUpdate("CREATE INDEX IF NOT EXISTS color_ldr ON "+table+"(ldraw)");

		// prepared statements
		insertPS = BrickMapping.db.prepareStatement("INSERT INTO "+table+" " +
				"("+fieldsOrder+") VALUES " +
				"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW())" +
				";",Statement.RETURN_GENERATED_KEYS);
		updatePS = BrickMapping.db.prepareStatement("UPDATE "+table+" SET " +
				"ldd=?," +
				"bl=?," +
				"ldraw=?," +
				"r=?," +
				"g=?," +
				"b=?," +
				"a=?," +
				"inuse=?," +
				"metal=?," +
				"transparent=?," +
				"glitter=?," +
				"lddname=?," +
				"colgrp=?," +
				"notes=?," +
				"lastmod=NOW() " +
				"WHERE mapid=?");
		colorMap = getAllColor();
		
	}
	
	
	
	protected static void createTable() throws SQLException {
		
		Statement st = BrickMapping.db.createStatement();
		st.execute("DROP TABLE IF EXISTS "+table+"; " +
				"CREATE TABLE IF NOT EXISTS "+table+" (" +
				"mapid INT PRIMARY KEY AUTO_INCREMENT," +
				"ldd INT UNIQUE, " +
				"bl INT," +
				"ldraw INT," +
				"r INT," +
				"g INT," +
				"b INT," +
				"a INT," +
				"inuse BOOL," +
				"metal BOOL," +
				"transparent BOOL," +
				"glitter BOOL," +
				"lddname VARCHAR(255)," +
				"colgrp VARCHAR(64)," +
				"notes VARCHAR(255)," +
				"lastmod TIMESTAMP" +
				"); COMMIT ");
	}
	
	
	public int insert() throws SQLException {

		ResultSet rs;
		
		insertPS.setInt(1, getLdd());
		insertPS.setInt(2, getBl());
		insertPS.setInt(3, getLdraw());
		insertPS.setInt(4, getColor().getRed());
		insertPS.setInt(5, getColor().getGreen());
		insertPS.setInt(6, getColor().getBlue());
		insertPS.setInt(7, getColor().getAlpha());
		insertPS.setBoolean(8, isInProduction());
		insertPS.setBoolean(9, isMetallic());
		insertPS.setBoolean(10, isTransparent());
		insertPS.setBoolean(11, isGlitter());
		insertPS.setString(12, getLddName());
		insertPS.setInt(13, getColorGroup());
		insertPS.setString(14, getNotes());
		
		insertPS.executeUpdate();
		
		rs = insertPS.getGeneratedKeys();
		rs.next();
		setMapid(rs.getInt(1));
		return getMapid();
		
	}

	
	public void update() throws SQLException {

		updatePS.setInt(1, getLdd());
		updatePS.setInt(2, getBl());
		updatePS.setInt(3, getLdraw());
		updatePS.setInt(4, getColor().getRed());
		updatePS.setInt(5, getColor().getGreen());
		updatePS.setInt(6, getColor().getBlue());
		updatePS.setInt(7, getColor().getAlpha());
		updatePS.setBoolean(8, isInProduction());
		updatePS.setBoolean(9, isMetallic());
		updatePS.setBoolean(10, isTransparent());
		updatePS.setBoolean(11, isGlitter());
		updatePS.setString(12, getLddName());
		updatePS.setInt(13, getColorGroup());
		updatePS.setString(14, getNotes());
		updatePS.setInt(15, getMapid());
		
		updatePS.executeUpdate();
		
	}
	
	
	
	public static ArrayList<BrickColor> get(String filterExpr) throws SQLException {
		
		ArrayList<BrickColor> brc = new ArrayList<BrickColor>();
		BrickColor bc;
		Statement st;
		ResultSet rs;
		
		if (filterExpr == null) {
			st = BrickMapping.db.createStatement();
			rs = st.executeQuery("SELECT mapid,"+fieldsOrder+" FROM "+table+"");
		}
		else {
			st = BrickMapping.db.createStatement();
			rs = st.executeQuery("SELECT mapid," + fieldsOrder +
				" FROM "+table+" WHERE " + filterExpr);
		}
		while (rs.next()) {
			// fetch and assign rows to an Array list
			//ldd,bl,ldraw,r,g,b,a,inuse,metal,glitter,lddname,colgrp,notes
			bc = new BrickColor();
			bc.setMapid(rs.getInt("mapid"));
			bc.setLdd(rs.getInt("ldd"));
			bc.setBl(rs.getInt("bl"));
			bc.setLdraw(rs.getInt("ldraw"));
			bc.setColor(new Color(rs.getInt("r"),rs.getInt("g"),rs.getInt("b"),rs.getInt("a")));
			bc.setInProduction(rs.getBoolean("inuse"));
			bc.setMetallic(rs.getBoolean("metal"));
			bc.setTransparent(rs.getBoolean("transparent"));
			bc.setGlitter(rs.getBoolean("glitter"));
			bc.setLddName(rs.getString("lddname"));
			bc.setColorGroup(rs.getInt("colgrp"));
			bc.setNotes(rs.getString("notes"));
			bc.setLastmod(rs.getTimestamp("lastmod"));
			brc.add(bc);
		}
		return brc;
		
	}
	
	
	
	public static HashMap<Integer,BrickColor> getAllColor() throws SQLException {
		
		HashMap<Integer,BrickColor> allColor = new HashMap<Integer,BrickColor>();
		ArrayList<BrickColor> bc;
		
		bc = get(null);
		for (BrickColor b : bc) {
			allColor.put(b.getMapid(), b);
		}
		BrickColor b = new BrickColor();
		b.setMapid(0);
		b.setLddName("Unknown");
		allColor.put(0, b);
		return allColor;
	}
	
	
	public static ArrayList<BrickColor> getPS(PreparedStatement ps) throws SQLException {
		
		ArrayList<BrickColor> brc = new ArrayList<BrickColor>();
		BrickColor bc;
		ResultSet rs;
		
		rs = ps.executeQuery();
		while (rs.next()) {
			// fetch and assign rows to an Array list
			//ldd,bl,ldraw,r,g,b,a,inuse,metal,glitter,lddname,colgrp,notes
			bc = new BrickColor();
			bc.setMapid(rs.getInt("mapid"));
			bc.setLdd(rs.getInt("ldd"));
			bc.setBl(rs.getInt("bl"));
			bc.setLdraw(rs.getInt("ldraw"));
			bc.setColor(new Color(rs.getInt("r"),rs.getInt("g"),rs.getInt("b"),rs.getInt("a")));
			bc.setInProduction(rs.getBoolean("inuse"));
			bc.setMetallic(rs.getBoolean("metal"));
			bc.setTransparent(rs.getBoolean("transparent"));
			bc.setGlitter(rs.getBoolean("glitter"));
			bc.setLddName(rs.getString("lddname"));
			bc.setColorGroup(rs.getInt("colgrp"));
			bc.setNotes(rs.getString("notes"));
			bc.setLastmod(rs.getTimestamp("lastmod"));
			brc.add(bc);
		}
		return brc;
		
	}
	
	

	public void check() throws SQLException {
		
		Statement st;
		ResultSet rs;
		
		st = BrickMapping.db.createStatement();
		rs = st.executeQuery("SELECT mapid,"+fieldsOrder+" FROM "+table+" " +
				"WHERE ((ldd = "+getLdd()+" AND "+getLdd()+"!=0) " +
				"OR " +
				"(bl = "+getBl()+" AND "+getBl() +"!=0) " +
				"OR " +
				"(ldraw = "+getLdraw()+" AND "+getLdraw()+"!=-1)) " +
				"AND " +
				"mapid !="+getMapid());
		if (rs.next()) {
			int lddid = rs.getInt("ldd");
			int blid = rs.getInt("bl");
			int ldrawid = rs.getInt("ldraw");
			throw new SQLException(
					"Duplicated color definition. Color:\n" +
					"Ldd="+getLdd()+" Bl="+getBl()+" LDraw="+getLdraw() +"\n" +
					"is already defined as:\n" +
					"Ldd="+lddid+" Bl="+blid+" LDraw="+ldrawid);
		}
	}
	
	


	public String getLddName() {
		return lddName;
	}


	public static Timestamp[] getLastModifyTime() throws SQLException {
		
		PreparedStatement ps;
		ResultSet rs;
		Timestamp last[] = new Timestamp[5];
		
		ps = BrickMapping.db.prepareStatement("SELECT FORMATDATETIME(lastmod,'yyyy-MM-dd') as datemod "+
				" FROM "+table+" GROUP BY datemod ORDER BY datemod DESC LIMIT 5");
		rs = ps.executeQuery();
		int i = 0;
		while (rs.next()) {
			last[i] = Timestamp.valueOf(rs.getString("datemod")+" 00:00:01");
			i++;
			if (i >= 5)
				break;
		}
		return last;
	}

	
	public static ArrayList<BrickColor> getModifiedAfter(Timestamp lastmodified) throws SQLException {
		
		PreparedStatement ps;
		ps = BrickMapping.db.prepareStatement("SELECT mapid,"+fieldsOrder+" FROM "+table+
				" where lastmod>=?");
		ps.setTimestamp(1, lastmodified);
		return getPS(ps);
	}
    

	public static int getMapByLdd(int lddid) {
		
		ArrayList<BrickColor> bc;
		
		try {
			bc = get("ldd="+lddid);
			//System.out.println(id + " " + bc);
			if (bc.size() >= 1) {
				return bc.get(0).getMapid();
			}
			else return -1;
		} catch (SQLException e) {
			return -1;
		}
	}

	
	
	public static int getMapByBl(int blcolor) throws SQLException {
		
		PreparedStatement ps;
		ArrayList<BrickColor> bc;
		
		ps = BrickMapping.db.prepareStatement("SELECT mapid,"+fieldsOrder+" FROM "+table+" WHERE bl=?");
		ps.setInt(1,blcolor);
		bc = getPS(ps);
		if (bc.size() != 1) {
			return 0;
		}
		else {
			return bc.get(0).getMapid();
		}
	}
	
	
	public static int getMapByLdr(int ldrcolor) throws SQLException {
		
		PreparedStatement ps;
		ArrayList<BrickColor> bc;
		
		ps = BrickMapping.db.prepareStatement("SELECT mapid,"+fieldsOrder+" FROM "+table+" WHERE ldraw=?");
		ps.setInt(1,ldrcolor);
		bc = getPS(ps);
		if (bc.size() != 1) {
			return 0;
		}
		else {
			return bc.get(0).getMapid();
		}
	}
	
	
	public static BrickColor getColor(int mapid) {
		
		BrickColor bc;
		
		bc = colorMap.get(mapid);
		if (bc == null) 
			return colorMap.get(0);
		else 
			return bc;
	}
	
	
	public static Set<Integer> getColorList() {
		
		return colorMap.keySet();
	}
	

	
	///////////////////////////////////////////
	// statistics
	///////////////////////////////////////////
	
	
	public static int countRules() {

		Statement st;
		ResultSet rs;
		
		try {
			st = BrickMapping.db.createStatement();
			rs = st.executeQuery("SELECT COUNT(*) FROM " + table);
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
		
		xsw.writeStartElement("colormap");
		xsw.writeAttribute("id",Integer.toString(getMapid()));
		xsw.writeAttribute("ldd",Integer.toString(getLdd())); 
		xsw.writeAttribute("bl",Integer.toString(getBl()));
		xsw.writeAttribute("ldraw",Integer.toString(getLdraw()));
		xsw.writeAttribute("r",Integer.toString(getColor().getRed()));
		xsw.writeAttribute("g",Integer.toString(getColor().getGreen()));
		xsw.writeAttribute("b",Integer.toString(getColor().getBlue()));	
		xsw.writeAttribute("a",Integer.toString(getColor().getAlpha()));
		xsw.writeAttribute("inuse",isInProduction()?"1":"0");
		xsw.writeAttribute("metallic",isMetallic()?"1":"0");
		xsw.writeAttribute("transparent",isTransparent()?"1":"0");
		xsw.writeAttribute("glitter",isTransparent()?"1":"0");
		xsw.writeAttribute("lddname",getLddName());
		xsw.writeAttribute("group",Integer.toString(getColorGroup()));
		xsw.writeAttribute("notes",getNotes());
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
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}


	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
	}


	/**
	 * @return the ldd
	 */
	public int getLdd() {
		return ldd;
	}


	/**
	 * @param ldd the ldd to set
	 */
	public void setLdd(int ldd) {
		this.ldd = ldd;
	}


	/**
	 * @return the bl
	 */
	public int getBl() {
		return bl;
	}


	/**
	 * @param bl the bl to set
	 */
	public void setBl(int bl) {
		this.bl = bl;
	}


	/**
	 * @return the ldraw
	 */
	public int getLdraw() {
		return ldraw;
	}


	/**
	 * @param ldraw the ldraw to set
	 */
	public void setLdraw(int ldraw) {
		this.ldraw = ldraw;
	}


	/**
	 * @return the colorGroup
	 */
	public int getColorGroup() {
		return colorGroup;
	}


	/**
	 * @param colorGroup the colorGroup to set
	 */
	public void setColorGroup(int colorGroup) {
		this.colorGroup = colorGroup;
	}


	/**
	 * @return the inProduction
	 */
	public boolean isInProduction() {
		return inProduction;
	}


	/**
	 * @param inProduction the inProduction to set
	 */
	public void setInProduction(boolean inProduction) {
		this.inProduction = inProduction;
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
	 * @return the metallic
	 */
	public boolean isMetallic() {
		return metallic;
	}


	/**
	 * @param metallic the metallic to set
	 */
	public void setMetallic(boolean metallic) {
		this.metallic = metallic;
	}


	/**
	 * @return the transparent
	 */
	public boolean isTransparent() {
		return transparent;
	}


	/**
	 * @param transparent the transparent to set
	 */
	public void setTransparent(boolean transparent) {
		this.transparent = transparent;
	}


	/**
	 * @return the glitter
	 */
	public boolean isGlitter() {
		return glitter;
	}


	/**
	 * @param glitter the glitter to set
	 */
	public void setGlitter(boolean glitter) {
		this.glitter = glitter;
	}


	/**
	 * @param lddName the lddName to set
	 */
	public void setLddName(String lddName) {
		this.lddName = lddName;
	}


	/**
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}


	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
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
	


	
	
}
