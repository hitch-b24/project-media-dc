package com.byu.pmedia.model;

import com.byu.pmedia.config.StillFaceConfig;
import com.byu.pmedia.database.StillFaceDAO;
import com.byu.pmedia.log.PMLogger;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.query.Query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.cqengine.query.QueryFactory.*;

public class StillFaceModel {

    private StillFaceDAO dao;               // Database Access Object for the model
    private boolean cached;                 // If true, the sf_data table is cached in memory to this object at initialization
                                            // and retrieving data from the model will not refer to the database server
    private boolean initialized = false;    // Flag to check if the model has been initialized

    private IndexedCollection<StillFaceImport> importDataCollection = new ConcurrentIndexedCollection<>();
    private IndexedCollection<StillFaceData> dataCollection = new ConcurrentIndexedCollection<>();
    private IndexedCollection<StillFaceCode> codeCollection = new ConcurrentIndexedCollection<>();
    private IndexedCollection<StillFaceTag> tagCollection = new ConcurrentIndexedCollection<>();

    private static List<StillFaceCode> codeList = new ArrayList<>();
    private static List<StillFaceTag> tagList = new ArrayList<>();

    private static StillFaceModel singleton;

    public static StillFaceModel getInstance(){
        if(singleton == null){
            singleton = new StillFaceModel();
        }
        return singleton;
    }

    public boolean initialize(StillFaceDAO dao){
        try {
            // TODO: correctly implement caching
            this.dao = dao;
            this.dao.lockConnection();
            this.cached = StillFaceConfig.getInstance().getAsBoolean("model.cache");
            this.importDataCollection = this.dao.getImportData(0);
            this.codeCollection = this.dao.getCode(0);
            this.tagCollection = this.dao.getTag(0);
            this.dataCollection = this.dao.getCodeDataFromImport(0);
            this.dao.unlockConnection();
            this.dao.closeConnection();
            this.initialized = true;
            populateCodeList();
            populateTagList();
            return true;
        }
        catch (SQLException e){
            PMLogger.getInstance().error("Error initializing model: " + e.getMessage());
            return false;
        }
    }

    public boolean isCached() {
        return cached;
    }

    public IndexedCollection<StillFaceImport> getImportDataCollection() {
        return importDataCollection;
    }

    public IndexedCollection<StillFaceData> getDataCollection() {
        return dataCollection;
    }

    public IndexedCollection<StillFaceCode> getCodeCollection() {
        return codeCollection;
    }

    public IndexedCollection<StillFaceTag> getTagCollection() {
        return tagCollection;
    }

    public static List<StillFaceCode> getCodeList(){ return codeList; }

    public static List<StillFaceTag> getTagList() { return tagList; }

    public boolean refreshImportData(){
        if(this.initialized){
            IndexedCollection<StillFaceImport> tmpCollection = this.dao.getImportData(0);
            if(tmpCollection == null){
                PMLogger.getInstance().warn("Failed to refresh import data");
                return false;
            }
            this.importDataCollection = tmpCollection;
            return true;
        }
        return false;
    }

    public boolean refreshCodeData(){
        if(this.initialized && this.cached){
            IndexedCollection<StillFaceData> tmpCollection = this.dao.getCodeDataFromImport(0);
            if(tmpCollection == null){
                PMLogger.getInstance().warn("Failed to refresh coded video data data");
                return false;
            }
            this.dataCollection = tmpCollection;
            return true;
        }
        return !this.cached;
    }

    public boolean refreshCodes(){
        if(this.initialized){
            IndexedCollection<StillFaceCode> tmpCollection = this.dao.getCode(0);
            if(tmpCollection != null) {
                this.codeCollection = tmpCollection;
                populateCodeList();
                return true;
            }
        }
        PMLogger.getInstance().warn("Failed to refresh codes");
        return false;
    }

    public boolean refreshTags(){
        if(this.initialized){
            IndexedCollection<StillFaceTag> tmpCollection = this.dao.getTag(0);
            if(tmpCollection != null){
                this.tagCollection = tmpCollection;
                populateTagList();
                return true;
            }
        }
        PMLogger.getInstance().warn("Failed to refresh tags");
        return false;
    }

    public boolean refresh(){
        return refreshImportData() && refreshCodeData() && refreshCodes() && refreshTags();
    }

    private void populateCodeList(){
        codeList.clear();
        Query<StillFaceCode> query = not(equal(StillFaceCode.CODE_ID, 0));
        for(StillFaceCode c : this.codeCollection.retrieve(query,
                queryOptions(orderBy(ascending(StillFaceCode.NAME))))){
            codeList.add(c);
        }
    }

    private void populateTagList(){
        tagList.clear();
        Query<StillFaceTag> query = not(equal(StillFaceTag.TAG_ID, 0));
        for(StillFaceTag t : this.tagCollection.retrieve(query,
                queryOptions(orderBy(ascending(StillFaceTag.TAG_VALUE))))){
            tagList.add(t);
        }
    }
}
