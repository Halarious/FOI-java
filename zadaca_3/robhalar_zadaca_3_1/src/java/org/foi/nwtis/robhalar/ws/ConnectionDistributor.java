package org.foi.nwtis.robhalar.ws;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.foi.nwtis.robhalar.konfiguracije.bp.BP_Konfiguracija;

public class ConnectionDistributor
{
    private final BP_Konfiguracija bp_konfiguracija;
    private       Connection       connection;
    
    public ConnectionDistributor(ServletContext context)
    {
        this.bp_konfiguracija = (BP_Konfiguracija)context.getAttribute("BP_Konfiguracija");
        this.connection       =  null;
    }
    
    public void connect() throws SQLException, ClassNotFoundException
    {
        if(connection != null && connection.isValid(6))
        {
            connection.close();
            connection = null;
        }
        Class.forName(bp_konfiguracija.getDriverDatabase());
        connection = DriverManager.getConnection(
                            bp_konfiguracija.getServerDatabase() +
                                    bp_konfiguracija.getUserDatabase(),
                            bp_konfiguracija.getUserUsername(),
                            bp_konfiguracija.getUserPassword());
    }
    
    public PreparedStatement prepareStatement(String statementString)
    {
        PreparedStatement preparedStatement = null;
        
        try
        {
            if(connection != null)
            {
                preparedStatement = connection.prepareStatement(statementString);
            } 
        }
        catch (SQLException ex)
        {
            Logger.getLogger(ConnectionDistributor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return preparedStatement;
    }
    
    public ResultSet simpleQuery(String query)
    {
        ResultSet resultSet = null;
        try
        {
            if(connection != null)
            {
                PreparedStatement queryStatement = connection.prepareStatement(query);
                resultSet = queryStatement.executeQuery(query);
            }
        } catch (SQLException ex)
        {
            Logger.getLogger(ConnectionDistributor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return resultSet;
    }
    
    public ResultSet query(PreparedStatement statement)
    {
        ResultSet resultSet = null;
        try
        {
            if(connection.isValid(6))
            {
                resultSet = statement.executeQuery();
            }
        } catch (SQLException ex)
        {
            Logger.getLogger(ConnectionDistributor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return resultSet;
    }
    
    public boolean runStatement(PreparedStatement preparedStatement)
    {
        try
        {
            if(connection.isValid(6))
            {
                preparedStatement.execute();
                return true;
            }
        } catch (SQLException ex)
        {
            Logger.getLogger(ConnectionDistributor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }
}
