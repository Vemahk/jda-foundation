package me.vem.jdab.sqlite;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import me.vem.jdab.utils.Logger;

public enum DbType { 
    NULL, 
    INTEGER, 
    REAL, 
    TEXT, 
    BLOB;
    
    public static DbTypeInstance wrap(Object o) {
        if(o == null)
            return NULL.instance();
        
        if(o instanceof Integer)
            return INTEGER.instance(o);
        
        if(o instanceof Float || o instanceof Double)
            return REAL.instance(o);
        
        if(o instanceof String)
            return TEXT.instance(o);
        
        if(o instanceof byte[])
            return BLOB.instance(o);
        
        if(o instanceof File) {
            try{
                byte[] bytes = Files.readAllBytes(((File)o).toPath());
                return BLOB.instance(bytes);                
            }catch(IOException e) {
                Logger.err("Could not read file into byte[]");
                e.printStackTrace();
            }
        }
        
        throw new IllegalArgumentException("Could not convert '" + o.getClass().getName() + "' to a DbType.");
    }
    
    private DbType() {}
    
    public DbTypeInstance instance() {
        switch(this) {
        case NULL: return instance(null);
        case INTEGER: return instance(0);
        case REAL: return instance(0.0);
        case TEXT: return instance("");
        case BLOB: return instance(new byte[0]);
        }
        
        throw new IllegalStateException("No current default mapping exists for DbType '" + this.toString() + "'.");
    }
    
    public DbTypeInstance instance(Object o) {
        return new DbTypeInstance(this, o);
    }
}