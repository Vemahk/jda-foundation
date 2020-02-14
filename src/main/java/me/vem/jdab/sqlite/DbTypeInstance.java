package me.vem.jdab.sqlite;

import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import me.vem.jdab.utils.Logger;

public class DbTypeInstance {

    private final DbType type;
    private final Object obj;
    
    DbTypeInstance(DbType type, Object obj){
        this.type = type;
        this.obj = obj;
        
        switch(type) {
        case NULL:
            if(obj != null)
                throw new IllegalArgumentException("For DbType 'NULL', wrapped object must be null");
            break;
        case INTEGER:
            if(!(obj instanceof Integer))
                throw new IllegalArgumentException("For DbType 'INTEGER', wrapped object must be of type Integer");
            break;
        case REAL:
            if(!(obj instanceof Float || obj instanceof Double))
                throw new IllegalArgumentException("For DbType 'REAL', wrapped object must either be of type Float or Double");
            break;
        case TEXT:
            if(!(obj instanceof String))
                throw new IllegalArgumentException("For DbType 'TEXT', wrapped object must be of type String");
            break;
        case BLOB:
            if(!(obj instanceof byte[]))
                throw new IllegalArgumentException("For DbType 'BLOB', wrapped object must either be of type byte[]. For converting a File to a byte[], see Files.readAllBytes().");
            break;
        case DYNAMIC:
        	throw new IllegalArgumentException("For DbType 'DYNAMIC', cannot wrap objects as a DbTypeInstance. DYNAMIC is used for describing table column.");
        }
    }
    
    public void applyTo(PreparedStatement stmt, int index) {
        try {
            switch(type) {
            case NULL:
                stmt.setNull(index, Types.NULL);
                break;
            case INTEGER:
                stmt.setInt(index, (Integer)obj);
                break;
            case REAL:
                if(obj instanceof Float)
                    stmt.setFloat(index, (Float)obj);
                else if(obj instanceof Double)
                    stmt.setDouble(index, (Double)obj);
                break;
            case TEXT:
                stmt.setString(index, (String)obj);
                break;
            case BLOB:
                stmt.setBlob(index, new ByteArrayInputStream((byte[])obj));
                break;
            default:
                throw new IllegalStateException("Cannot apply unknown type '" + type.toString() + "' to PreparedStatement in SQLite");
            }
        } catch (SQLException e) {
            Logger.err(e.getMessage());
            e.printStackTrace();
        }
    }
    
}
