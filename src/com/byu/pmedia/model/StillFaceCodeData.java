package com.byu.pmedia.model;

import com.googlecode.cqengine.attribute.Attribute;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import static com.googlecode.cqengine.query.QueryFactory.attribute;

public class StillFaceCodeData {

    /**
     * These variables are for the JavaFX Table object in the GUI. The table cells are populated using a CellFactory,
     * and in order to do this we need to have special variable types
     */
    private SimpleIntegerProperty dataID = new SimpleIntegerProperty();
    private SimpleIntegerProperty importID = new SimpleIntegerProperty();
    private SimpleIntegerProperty time = new SimpleIntegerProperty();
    private SimpleIntegerProperty duration = new SimpleIntegerProperty();
    private SimpleObjectProperty<StillFaceCode> code = new SimpleObjectProperty<>();
    private SimpleStringProperty comment = new SimpleStringProperty();


    /**
     * The following variables are defined for use with the CQEngine IndexedCollections. This allows us to
     * create extremely fast indexing capabilities and cache data in memory for use. Data is only cached if
     * the model.cache configuration option is set to 'true'
     */
    public static final Attribute<StillFaceCodeData, Integer> DATA_ID =
            attribute("dataID", StillFaceCodeData::getDataID);
    public static final Attribute<StillFaceCodeData, Integer> IMPORT_ID =
            attribute("importID", StillFaceCodeData::getImportID);
    public static final Attribute<StillFaceCodeData, Integer> TIME =
            attribute("time", StillFaceCodeData::getTime);
    public static final Attribute<StillFaceCodeData, Integer> DURATION =
            attribute("duration", StillFaceCodeData::getDuration);
    public static final Attribute<StillFaceCodeData, StillFaceCode> CODE =
            attribute("code", StillFaceCodeData::getCode);
    public static final Attribute<StillFaceCodeData, String> COMMENT =
            attribute("comment", StillFaceCodeData::getComment);

    public StillFaceCodeData(int importID, int time, int duration, StillFaceCode code, String comment){
        this.importID = new SimpleIntegerProperty(importID);
        this.setTime(time);
        this.setDuration(duration);
        this.setCode(code);
        this.setComment(comment);
    }

    public StillFaceCodeData(int dataID, int importID, int time, int duration, StillFaceCode code, String comment){
        this.dataID = new SimpleIntegerProperty(dataID);
        this.importID = new SimpleIntegerProperty(importID);
        this.setTime(time);
        this.setDuration(duration);
        this.setCode(code);
        this.setComment(comment);
    }

    public int getTime() {
        return time.get();
    }

    public void setTime(int time) {
        this.time.set(time);
    }

    public int getDuration() {
        return duration.get();
    }

    public void setDuration(int duration) {
        this.duration.set(duration);
    }

    public StillFaceCode getCode() {
        return code.get();
    }

    public void setCode(StillFaceCode code){
        this.code.set(code);
    }

    public String getComment() { return comment.get(); }

    public void setComment(String comment) {
        this.comment.set(comment);
    }

    public int getDataID() {
        return dataID.get();
    }

    public void setImportID(int importID) { this.importID.set(importID); }

    public int getImportID() {
        return importID.get();
    }

    @Override
    public int hashCode(){
        int result = code.hashCode();
        result = result * 24 + dataID.get();
        result = result >> (time.get() % 4);
        result = result * 13 + time.get();
        result += comment.get().hashCode();
        return result;
    }

}
