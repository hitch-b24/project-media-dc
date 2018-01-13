package com.byu.pmedia.database;

import com.byu.pmedia.log.PMLogger;
import com.byu.pmedia.model.StillFaceCode;
import com.byu.pmedia.model.StillFaceCodeData;
import com.byu.pmedia.model.StillFaceImportData;
import com.byu.pmedia.model.StillFaceTag;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class StillFaceDAO {

    private IDatabaseConnection databaseConnection;
    private StillFaceQueryBuilder queryBuilder = new StillFaceQueryBuilder();
    private boolean connectionLocked = false;

    public StillFaceDAO(IDatabaseConnection databaseConnection){
        this.databaseConnection = databaseConnection;
    }


    /**
     * Insert new import data into the database
     *
     * @param data A populated StillFaceImportData object
     *
     * @return The generated key of the successfully inserted data if successful. Otherwise -1.
     */
    public int insertImportData(StillFaceImportData data){
        // Create the query
        String query = this.queryBuilder.buildInsertImport(data);

        // Execute it
        try{
            this.openConnection();
            PreparedStatement statement = this.databaseConnection.getConnection()
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.execute();
            ResultSet resultSet = statement.getGeneratedKeys();
            int generatedKey = -1;
            if(resultSet.next()){
                generatedKey = resultSet.getInt(1);
            }
            this.closeConnection();
            return generatedKey;
        }
        catch(SQLException e){
            PMLogger.getInstance().error("Unable to insert import data: " + e.getMessage() + "\n" + query);
            return -1;
        }
    }

    /**
     * Retrieves import data from the database and returns it as a map containing StillFaceImportData
     *
     * @param importID The integer ID of the desired data. If 0, all data will be returned.
     *
     * @return A map of StillFaceImportData objects with their respective import IDs as the key
     */
    public Map<Integer, StillFaceImportData> getImportData(int importID){
        // Create the query
        String query = this.queryBuilder.buildSelectImportData(importID);

        // Initialize the map
        Map<Integer, StillFaceImportData> importData;

        // Get the data
        try {
            //Execute the query
            this.openConnection();
            Statement statement = this.databaseConnection.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            importData = new HashMap<>();
            // Iterate over the results and populate the map with StillFaceImportData objects
            while(resultSet.next()){
                int iid = resultSet.getInt("iid");
                String filename = resultSet.getString("filename");
                int year = resultSet.getInt("syear");
                int familyID = resultSet.getInt("fid");
                int participantNumber = resultSet.getInt("pid");
                String alias = resultSet.getString("alias");
                Date date = resultSet.getDate("date");
                importData.put(iid, new StillFaceImportData(iid, filename, year, familyID, participantNumber, alias, date));
            }
            this.closeConnection();
            return importData;
        }
        catch (SQLException e){
            PMLogger.getInstance().error("Could not get import data: " + e.getMessage());
            return null;
        }


    }

    /**
     * Updates existing import data information in the database
     *
     * @param data Populated StillFaceImportData object with the new values
     *
     * @return True if the update is successful. False otherwise.
     */
    public boolean updateImportData(StillFaceImportData data){
        // Create the query
        String query = this.queryBuilder.buildUpdateImport(data);

        // Execute the query
        try{
            this.openConnection();
            PreparedStatement statement = this.databaseConnection.getConnection().prepareStatement(query);
            statement.executeUpdate();
            this.closeConnection();
            return true;
        }
        catch(SQLException e){
            PMLogger.getInstance().error("Unable to update import data: " + e.getMessage());
            return false;
        }
    }


    /**
     * Inserts new code data into the database from a populated StillFaceCodeData object
     *
     * @param data The populated data object
     *
     * @return The newly generated key if successful. -1 otherwise.
     */
    public int insertCodeData(StillFaceCodeData data){
        // Create the query
        String query = this.queryBuilder.buildInsertCodeData(data);

        // Execute the query
        try{
            this.openConnection();
            PreparedStatement statement = this.databaseConnection.getConnection()
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.execute();
            ResultSet resultSet = statement.getGeneratedKeys();
            int generatedKey = -1;
            while(resultSet.next()){
                generatedKey = resultSet.getInt(1);
            }
            this.closeConnection();
            return generatedKey;
        }
        catch(SQLException e){
            PMLogger.getInstance().error("Unable to insert code data: " + e.getMessage());
            return -1;
        }
    }


    /**
     * Retrieves coded video data for Still face based on the import associated with the data. If 0 is provided, all
     * available code data is returned.
     *
     * @param importID The id of the import whose data we want to retrieve
     *
     * @return A map of integer data ID, StillFaceCodeData object pairs if the query succeeds. Null otherwise.
     */
    public Map<Integer, StillFaceCodeData> getCodeDataFromImport(int importID){
        // Create the query
        String query = this.queryBuilder.buildSelectCodeDataFromImport(importID);

        // Prepare a map of StillFaceCodeData
        Map<Integer, StillFaceCodeData> codeDataMap;

        // Execute the query
        try{
            this.openConnection();
            Statement statement = this.databaseConnection.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            codeDataMap = new HashMap<>();
            while(resultSet.next()){
                int dataID = resultSet.getInt("did");
                int iid = resultSet.getInt("iid");
                int time = resultSet.getInt("time");
                int duration = resultSet.getInt("duration");
                int codeID = resultSet.getInt("cid");
                String comment = resultSet.getString("comment");
                String codeName = resultSet.getString("name");
                codeDataMap.put(dataID, new StillFaceCodeData(dataID, iid, time, duration,
                        new StillFaceCode(codeID, codeName), comment));
            }
            this.closeConnection();
            return codeDataMap;
        }
        catch(SQLException e){
            PMLogger.getInstance().error("Unable to retrieve code data: " + e.getMessage());
            return null;
        }
    }


    /**
     * Retrieves coded video data for entries that share a common family id. If 0 is provided, all available code data
     * is returned.
     *
     * @param familyID The family ID
     *
     * @return A Map of integer data ID, StillFaceCodeData object pairs if successful. Null otherwise.
     */
    public Map<Integer, StillFaceCodeData> getCodeDataFromFamilyID(int familyID){
        // Create the query
        String query = this.queryBuilder.buildSelectCodeDataFromFamilyID(familyID);

        // Prepare a map of StillFaceCodeData
        Map<Integer, StillFaceCodeData> codeDataMap;

        // Execute the query
        try{
            this.openConnection();
            Statement statement = this.databaseConnection.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            codeDataMap = new HashMap<>();
            while(resultSet.next()){
                int dataID = resultSet.getInt("did");
                int iid = resultSet.getInt("iid");
                int time = resultSet.getInt("time");
                int duration = resultSet.getInt("duration");
                int codeID = resultSet.getInt("cid");
                String comment = resultSet.getString("comment");
                String codeName = resultSet.getString("name");
                codeDataMap.put(dataID, new StillFaceCodeData(dataID, iid, time, duration,
                        new StillFaceCode(codeID, codeName), comment));
            }
            this.closeConnection();
            return codeDataMap;
        }
        catch(SQLException e){
            PMLogger.getInstance().error("Unable to retrieve code data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Updates existing coded video data in the database
     *
     * @param data The data entry to update in the database
     *
     * @return True if the update is successful. False otherwise.
     */
    public boolean updateCodeData(StillFaceCodeData data){
        // Create the query
        String query = this.queryBuilder.buildUpdateCodeData(data);

        // Execute the query
        try{
            this.openConnection();
            PreparedStatement statement = this.databaseConnection.getConnection().prepareStatement(query);
            statement.executeUpdate();
            this.closeConnection();
            return true;
        }
        catch(SQLException e){
            PMLogger.getInstance().error("Unable to update code data: " + e.getMessage());
            return false;
        }
    }


    /**
     * Creates a new entry for a code type in the database
     *
     * @param code The new code to insert
     *
     * @return The generated key of the new entry if successful. -1 otherwise.
     */
    public int insertNewCode(StillFaceCode code){
        // Create the query
        String query = this.queryBuilder.buildInsertNewCode(code);

        // Execute the query
        try{
            this.openConnection();
            PreparedStatement statement = this.databaseConnection.getConnection()
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.execute();
            ResultSet resultSet = statement.getGeneratedKeys();
            int generatedKey = -1;
            while(resultSet.next()){
                generatedKey = resultSet.getInt(1);
            }
            this.closeConnection();
            return generatedKey;
        }
        catch(SQLException e){
            PMLogger.getInstance().error("Unable to insert new code: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Retrieves information about the code with the provided codeID.
     *
     * @param codeID The id of the code to retrieve. If 0 is provided, all available codes are returned.
     *
     * @return A populated map of integer code ID, StillFaceCode object pairs if successful. Null otherwise.
     */
    public Map<Integer, StillFaceCode> getCode(int codeID){
        // Create the query
        String query = this.queryBuilder.buildSelectCode(codeID);

        // Prepare the map
        Map<Integer, StillFaceCode> codeMap;

        // Execute the query
        try{
            this.openConnection();
            Statement statement = this.databaseConnection.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            codeMap = new HashMap<>();
            while(resultSet.next()){
                int cid = resultSet.getInt("cid");
                String name = resultSet.getString("name");
                codeMap.put(cid, new StillFaceCode(cid, name));
            }
            this.closeConnection();
            return codeMap;
        }
        catch(SQLException e){
            PMLogger.getInstance().error("Unable to retrieve code: " + e.getMessage());
            return null;
        }
    }

    /**
     * Updates existing information about an available code for StillFace
     *
     * @param code A StillFaceCode object populated with the new information
     *
     * @return True if successful, false otherwise.
     */
    public boolean updateExistingCode(StillFaceCode code){
        // Create the query
        String query = this.queryBuilder.buildUpdateExistingCode(code);

        // Execute the query
        try{
            this.openConnection();
            PreparedStatement statement = this.databaseConnection.getConnection().prepareStatement(query);
            statement.executeUpdate();
            this.closeConnection();
            return true;
        }
        catch(SQLException e){
            PMLogger.getInstance().error("Unable to update code: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes an existing code entry in the database
     *
     * @param code The code entry to delete
     *
     * @return True if successful. False otherwise.
     */
    public boolean deleteExistingCode(StillFaceCode code){
        // Create the query
        String query = this.queryBuilder.buildDeleteCode(code);

        // Execute the query
        try{
            this.openConnection();
            PreparedStatement statement = this.databaseConnection.getConnection().prepareStatement(query);
            statement.execute();
            this.closeConnection();
            return true;
        }
        catch(SQLException e){
            PMLogger.getInstance().error("Unable to delete code: " + e.getMessage());
            return false;
        }
    }


    /**
     * Creates a new entry for tag information in the database
     *
     * @param tag The tag information to insert. If 0, all tags will be returned.
     *
     * @return The new entry's generated key if successful. -1 otherwise.
     */
    public int insertNewTag(StillFaceTag tag){
        // Create the query
        String query = this.queryBuilder.buildInsertNewTag(tag);

        // Execute the query
        try{
            this.openConnection();
            PreparedStatement statement = this.databaseConnection.getConnection()
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.execute();
            ResultSet resultSet = statement.getGeneratedKeys();
            int generatedKey = -1;
            while(resultSet.next()){
                generatedKey = resultSet.getInt(1);
            }
            this.closeConnection();
            return generatedKey;
        }
        catch(SQLException e){
            PMLogger.getInstance().error("Unable to insert new tag: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Retrieves tag information from the database
     *
     * @param tagID The id of the tag to get information about. If 0, all tag information will be returned.
     *
     * @return A map of integer tag ID, StillFaceTag object pairs if successful. Null otherwise.
     */
    public Map<Integer, StillFaceTag> getTag(int tagID){
        // Create the query
        String query = this.queryBuilder.buildSelectTag(tagID);

        // Prepare the map
        Map<Integer, StillFaceTag> tagMap;

        // Execute the query
        try{
            this.openConnection();
            Statement statement = this.databaseConnection.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            tagMap = new HashMap<>();
            while(resultSet.next()){
                int tid = resultSet.getInt("tid");
                String value = resultSet.getString("value");
                tagMap.put(tid, new StillFaceTag(tid, value));
            }
            this.closeConnection();
            return tagMap;
        }
        catch(SQLException e){
            PMLogger.getInstance().error("Unable to retrieve tag information: " + e.getMessage());
            return null;
        }
    }

    /**
     * Updates existing tag information in the database
     *
     * @param tag The populated tag object to provide updates
     *
     * @return True if successful, False otherwise.
     */
    public boolean updateExistingTag(StillFaceTag tag){
        // Create the query
        String query = this.queryBuilder.buildUpdateExistingTag(tag);

        // Execute the query
        try{
            this.openConnection();
            PreparedStatement statement = this.databaseConnection.getConnection().prepareStatement(query);
            statement.executeUpdate();
            this.closeConnection();
            return true;
        }
        catch(SQLException e){
            PMLogger.getInstance().error("Unable to update tag: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes an existing tag entry in the database
     *
     * @param tag The tag entry to delete
     *
     * @return True if successful. False otherwise.
     */
    public boolean deleteExistingTag(StillFaceTag tag){
        // Create the query
        String query = this.queryBuilder.buildDeleteTag(tag);

        // Execute the query
        try{
            this.openConnection();
            PreparedStatement statement = this.databaseConnection.getConnection().prepareStatement(query);
            statement.execute();
            this.closeConnection();
            return true;
        }
        catch(SQLException e){
            PMLogger.getInstance().error("Unable to delete tag: " + e.getMessage());
            return false;
        }
    }

    public boolean createTables(DatabaseMode mode){
        // Initialize all the queries
        String createImportTableQuery = this.queryBuilder.buildCreateSFImportTable(mode);
        String createDataTableQuery = this.queryBuilder.buildCreateSFDataTable(mode);
        String createCodeTableQuery = this.queryBuilder.buildCreateSFCodesTable(mode);
        String createTagTableQuery = this.queryBuilder.buildCreateSFTagsTable(mode);

        // Execute the queries
        try{
            this.openConnection();
            Statement statement = this.databaseConnection.getConnection().createStatement();
            statement.executeUpdate(createImportTableQuery);
            statement.executeUpdate(createDataTableQuery);
            statement.executeUpdate(createCodeTableQuery);
            statement.executeUpdate(createTagTableQuery);
            this.closeConnection();
            return true;
        }
        catch(SQLException e){
            PMLogger.getInstance().error("Unable to create database table: " + e.getMessage());
            return false;
        }
    }

    public boolean dropTables(){
        // Initialize all the queries
        String dropImportTableQuery = this.queryBuilder.buildDropSFImportTable();
        String dropDataTableQuery = this.queryBuilder.buildDropSFDataTable();
        String dropCodeTableQuery = this.queryBuilder.buildDropSFCodesTable();
        String dropTagTableQuery = this.queryBuilder.buildDropSFTagsTable();

        // Execute the queries
        try{
            this.openConnection();
            Statement statement = this.databaseConnection.getConnection().createStatement();
            statement.executeUpdate(dropImportTableQuery);
            statement.executeUpdate(dropDataTableQuery);
            statement.executeUpdate(dropCodeTableQuery);
            statement.executeUpdate(dropTagTableQuery);
            this.closeConnection();
            return true;
        }
        catch(SQLException e){
            PMLogger.getInstance().error("Unable to drop database table: " + e.getMessage());
            return false;
        }
    }










    /**
     * Opens a database connection to this DAO's associated database. If unsuccessful, it will throw an SQLException
     *
     * @throws SQLException
     */
    public void openConnection() throws SQLException{
        if(!this.databaseConnection.connectionIsEstablished()){
            try{
                this.databaseConnection.establish();
            }
            catch(SQLException e){
                PMLogger.getInstance().error("DAO unable to open database connection");
                throw e;
            }
        }
    }

    /**
     * Closes a database connection to this DAO's associated database. If unsuccessful, it will throw an SQLException
     *
     * @throws SQLException
     */
    public void closeConnection() throws SQLException{
        if(this.databaseConnection.connectionIsEstablished() && !this.connectionLocked){
            try{
                this.databaseConnection.close();
            }
            catch(SQLException e){
                PMLogger.getInstance().error("DAO unable to close database connection");
                throw e;
            }
        }
    }

    /**
     * Prevents the database connection for this DAO to be closed once established. This means that calls to this
     * object's closeConnection() method will do nothing.
     */
    public void lockConnection(){
        this.connectionLocked = true;
    }

    /**
     * Unlocks the database connection and allows it to be closed after being established. This means that calls to
     * this object's closeConnection() method will attempt to close the database connection.
     */
    public void unlockConnection(){
        this.connectionLocked = false;
    }

    public boolean isDatabaseInitialized(){
        // Set up the queries
        String checkImportTable = "SELECT COUNT(*) FROM sf_imports";
        String checkDataTable = "SELECT COUNT(*) FROM sf_data";
        String checkCodesTable = "SELECT COUNT(*) FROM sf_codes";
        String checkTagsTable = "SELECT COUNT(*) FROM sf_tags";

        try{
            this.openConnection();
            this.databaseConnection.getConnection().createStatement().executeQuery(checkImportTable);
            this.databaseConnection.getConnection().createStatement().executeQuery(checkDataTable);
            this.databaseConnection.getConnection().createStatement().executeQuery(checkCodesTable);
            this.databaseConnection.getConnection().createStatement().executeQuery(checkTagsTable);
            this.closeConnection();
            return true;
        }
        catch (SQLException e){
            PMLogger.getInstance().error("Caught exception checking database status: " + e.getMessage());
            return false;
        }
    }


}
