package com.example.syncapp.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Date;

@DatabaseTable(tableName = "transactions")
public class Transaction {

    public boolean isCanRetry() {
        return canRetry;
    }

    public void setCanRetry(boolean canRetry) {
        this.canRetry = canRetry;
    }

    public enum SyncStatus {
        Success,
        Pending,
        Failed
    }

    @DatabaseField(generatedId = true, columnName = "id")
    private int id;

    @DatabaseField(columnName = "entity", canBeNull = false)
    private String entity;

    @DatabaseField(columnName = "action", canBeNull = false)
    private String action;

    @DatabaseField(columnName = "reference", canBeNull = false)
    private String reference;

    @DatabaseField(columnName = "performedOn", canBeNull = false)
    private Date performedOn;

    @DatabaseField(columnName = "syncStatus", canBeNull = false)
    private String syncStatus;

    @DatabaseField(columnName = "canRetry")
    private boolean canRetry;

    @DatabaseField(columnName = "errorMessage")
    private String errorMessage;


    public Transaction() {
    }



    public static Transaction newTransaction(String entity, String action, String reference, Date performedOn) {
        Transaction transaction = new Transaction();
        transaction.setEntity(entity);
        transaction.setAction(action);
        transaction.setReference(reference);
        transaction.setPerformedOn(performedOn);
        transaction.setSyncStatus(SyncStatus.Pending.name());
        transaction.setErrorMessage(null);
        return transaction;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Date getPerformedOn() {
        return performedOn;
    }

    public void setPerformedOn(Date performedOn) {
        this.performedOn = performedOn;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", entity='" + entity + '\'' +
                ", action='" + action + '\'' +
                ", reference='" + reference + '\'' +
                ", performedOn=" + performedOn +
                ", syncStatus='" + syncStatus + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
