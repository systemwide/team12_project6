package edu.uga.cs.rentaride.persistence.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mysql.jdbc.PreparedStatement;

import edu.uga.cs.rentaride.RARException;
import edu.uga.cs.rentaride.entity.*;
import edu.uga.cs.rentaride.object.ObjectLayer;


class HourlyPriceManager
{
    private ObjectLayer objectLayer = null;
    private Connection   conn = null;
    
    public HourlyPriceManager( Connection conn, ObjectLayer objectLayer )
    {
        this.conn = conn;
        this.objectLayer = objectLayer;
    }
    
    public void store( HourlyPrice hPrice ) 
            throws RARException
    {
    	String               insertHourlyPriceSql = "insert into HourlyPrice ( maxHours, hourlyprice, hourlyPriceID ) values ( ?, ?, ? )";
        String               updateHourlyPriceSql = "update HourlyPrice set maxHours = ?, hourlyprice = ? where id = ?";
        PreparedStatement    stmt = null;
        int                  inscnt;
        long                 hourlyPriceId;
       
        try {

            if( !hPrice.isPersistent() )
                stmt = (PreparedStatement) conn.prepareStatement( insertHourlyPriceSql );
            else
                stmt = (PreparedStatement) conn.prepareStatement( updateHourlyPriceSql );

            if( hPrice.getMaxHours() >= 0 ) // name is unique unique and non null
                stmt.setInt( 1, hPrice.getMaxHours() );
            else 
                throw new RARException( "HourlyPriceManager.save: max hours not set or is not persistent" );

            if( hPrice.getPrice() >= 0 )
                stmt.setInt( 2, hPrice.getPrice() );
            else
            	throw new RARException( "HourlyPriceManager.save: price not set or is not persistent" );
            
            if( hPrice.isPersistent() )
                stmt.setLong( 3, hPrice.getId() );

            inscnt = stmt.executeUpdate();

            if( !hPrice.isPersistent() ) {
                if( inscnt >= 1 ) {
                    String sql = "select last_insert_id()";
                    if( stmt.execute( sql ) ) { // statement returned a result

                        // retrieve the result
                        ResultSet r = stmt.getResultSet();

                        // we will use only the first row!
                        //
                        while( r.next() ) {

                            // retrieve the last insert auto_increment value
                        	hourlyPriceId = r.getLong( 1 );
                            if( hourlyPriceId > 0 )
                                hPrice.setId( hourlyPriceId ); // set this hourly price's db id (proxy object)
                        }
                    }
                }
                else
                    throw new RARException( "HourlyPriceManager.save: failed to save a Hourly Price" );
            }
            else {
                if( inscnt < 1 )
                    throw new RARException( "HourlyPriceManager.save: failed to save a Hourly Price" ); 
            }
        }
        catch( SQLException e ) {
            e.printStackTrace();
            throw new RARException( "HourlyPriceManager.save: failed to save a Hourly Price: " + e );
        }
    }

    public List<HourlyPrice> restore( HourlyPrice modelHourlyPrice ) 
            throws RARException
    {
        String       selectHourlyPriceSql = "select maxHours and price from Hourly Price";
        Statement    stmt = null;
        StringBuffer query = new StringBuffer( 100 );
        StringBuffer condition = new StringBuffer( 100 );
        List<HourlyPrice>   hourlyP = new ArrayList<HourlyPrice>();

        condition.setLength( 0 );
        
        // form the query based on the given Hourly Price object instance
        query.append( selectHourlyPriceSql );
        
        if( modelHourlyPrice != null ) {
            if( modelHourlyPrice.getId() >= 0 ) // id is unique, so it is sufficient to get a hourly price
                query.append( " where id = " + modelHourlyPrice.getId() );
            else{
            	if( modelHourlyPrice.getMaxHours() > 0 )
                    condition.append( " where maxHours = '" + modelHourlyPrice.getMaxHours() + "'" );
            	
            	if( modelHourlyPrice.getPrice() > 0){
    				if(condition.length() > 0 )
    					condition.append(" and ");
    				condition.append( "price = '" + modelHourlyPrice.getPrice() + "'" );
    			}  
            }
        }
        
        try {

            stmt = conn.createStatement();

            // retrieve the persistent Hourly Price objects
            //
            if( stmt.execute( query.toString() ) ) { // statement returned a result
                
                long   id;
                int maxHours;
                int price;
                HourlyPrice   nextHourlyP = null;
                
                ResultSet rs = stmt.getResultSet();
                
                // retrieve the retrieved hourlyPrices
                while( rs.next() ) {
                    
                    id = rs.getLong( 1 );
                    maxHours = rs.getInt( 2 );
                    price = rs.getInt( 3 );
                    
                    nextHourlyP = objectLayer.createHourlyPrice(); // create a proxy Hourly Price object
                    // and now set its retrieved attributes
                    nextHourlyP.setId( id );
                    nextHourlyP.setMaxHours( maxHours );
                    nextHourlyP.setPrice( price );
                    // set this to null for the "lazy" association traversal
                    nextHourlyP.setVehicleType(null);
                    
                    hourlyP.add( nextHourlyP );
                }
                
                return hourlyP;
            }
        }
        catch( Exception e ) {      // just in case...
            throw new RARException( "HourlyPriceManager.restore: Could not restore persistent Hourly Price objects; Root cause: " + e );
        }

        throw new RARException( "HourlyPriceManager.restore: Could not restore persistent Hourly Price objects" );
    }

    public List<VehicleType> restoreVehicleTypeHourlyPrice( HourlyPrice hourlyPrice ) throws RARException
    {
        String       selectHourlyPriceSql = "select v.name, h.maxHours, hp.price from VehicleType v and HourlyPrice h where h.hourlyPriceID = v.vehicleTypeId ";;              
        Statement    stmt = null;
        StringBuffer query = new StringBuffer( 100 );
        StringBuffer condition = new StringBuffer( 100 );
		List<VehicleType> vType = new ArrayList<VehicleType>(); 

        condition.setLength( 0 );
        
        // form the query based on the given Hourly Price object instance
        query.append( selectHourlyPriceSql );
        
        if( hourlyPrice != null ) {
            if( hourlyPrice.getId() >= 0 ) // id is unique, so it is sufficient to get a hourlyPrice
                query.append( " hourlyPriceid = " + hourlyPrice.getId() );
            else {

                if( hourlyPrice.getMaxHours() != 0 )
                	condition.append( "maxHours = '" + hourlyPrice.getMaxHours() + "'" );   

                if( hourlyPrice.getPrice() != 0 ) {
                    if( condition.length() > 0 )
                    	condition.append( " and" );
                    condition.append( " price = '" + hourlyPrice.getPrice() + "'" );
                }
            }
        }
                
        try {

            stmt = conn.createStatement();

            // retrieve the persistent vehicle type object
            //
            if( stmt.execute( query.toString() ) ) { // statement returned a result
                ResultSet rs = stmt.getResultSet();
                
                long   id;
                String name;
                VehicleType nextVType = null;
                
                while( rs.next() ) {

                    id = rs.getLong( 1 );
                    name = rs.getString( 2 );

                    nextVType = objectLayer.createVehicleType(); // create a proxy vehicle type object
                    // and now set its retrieved attributes
                    nextVType.setId( id );
                    nextVType.setName(name);
    				// set this to null for the "lazy" association traversal
    				
                    vType.add( nextVType );
                }
                
                return vType;
            }
            else
                return vType;
        }
        catch( Exception e ) {      // just in case...
            throw new RARException( "HourlyPriceManager.restore: Could not restore persistent Hourly Price object; Root cause: " + e );
        }
        
    }
    
    public void delete( HourlyPrice hPrice ) 
            throws RARException
    {
        String               deleteHourlyPriceSql = "delete from hourly price where id = ?";              
        PreparedStatement    stmt = null;
        int                  inscnt;
             
        if( !hPrice.isPersistent() ) // is the Hourly Price object persistent?  If not, nothing to actually delete
            return;
        
        try {
            stmt = (PreparedStatement) conn.prepareStatement( deleteHourlyPriceSql );         
            stmt.setLong( 1, hPrice.getId() );
            inscnt = stmt.executeUpdate();          
            if( inscnt == 1 ) {
                return;
            }
            else
                throw new RARException( "HourlyPriceManager.delete: failed to delete a Hourly Price" );
        }
        catch( SQLException e ) {
            e.printStackTrace();
            throw new RARException( "HourlyPriceManager.delete: failed to delete a Hourly Price: " + e );        }
    }
}