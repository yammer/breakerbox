package com.yammer.homie.service.azure;

import com.google.common.base.Objects;
import com.yammer.homie.service.auth.User;
import com.yammer.homie.service.ldap.VirtualGroup;
import org.joda.time.DateTime;

import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

public class AccessEntity extends TableType {
    private Date expireAtTime;
    private String description;

    @Deprecated
    public AccessEntity() {
        super(AzureTableName.ACCESS);
    }

    public AccessEntity(User user, VirtualGroup group, DateTime expireAtTime, String description) {
        super(AzureTableName.ACCESS);
        this.partitionKey = checkNotNull(user, "username cannot be null").getUsername();
        this.rowKey = checkNotNull(group, "groupname cannot be null").getName();
        this.expireAtTime = checkNotNull(expireAtTime, "expireAtTime cannot be null").withMillisOfSecond(0).toDate();
        this.description = checkNotNull(description, "description cannot be null");
    }

    public AccessEntityKey key() {
        return key(User.from(partitionKey), VirtualGroup.from(rowKey));
    }

    public static AccessEntityKey key(User user, VirtualGroup group) {
        return new AccessEntityKey(user, group);
    }

    public static AccessEntity from(RequestEntity requestEntity) {
        if (RequestState.valueOf(requestEntity.getRequestState()) == RequestState.APPROVED) {
            return new AccessEntity(
                    requestEntity.getUser(),
                    requestEntity.getGroup(),
                    requestEntity.getExpireAtTime(),
                    requestEntity.getDescription());
        } else {
            throw new IllegalArgumentException("RequestEntity in an unknown state: " + requestEntity);
        }
    }

    public Date getExpireAtTime() {
        return expireAtTime;
    }

    public void setExpireAtTime(Date expireAtTime) {
        this.expireAtTime = expireAtTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DateTime getExpireTime() {
        return new DateTime(expireAtTime);
    }

    public User getUser() {
        return User.from(partitionKey);
    }

    public VirtualGroup getGroup() {
        return VirtualGroup.from(rowKey);
    }

    public boolean expired() {
        return new DateTime(expireAtTime).isBefore(DateTime.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccessEntity)) return false;
        if (!super.equals(o)) return false;

        AccessEntity that = (AccessEntity) o;

        if (partitionKey != null ? !partitionKey.equals(that.partitionKey) : that.partitionKey != null) return false;
        if (rowKey != null ? !rowKey.equals(that.rowKey) : that.rowKey != null) return false;
        if (expireAtTime != null ? !expireAtTime.equals(that.expireAtTime) : that.expireAtTime != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (partitionKey != null ? partitionKey.hashCode() : 0);
        result = 31 * result + (rowKey != null ? rowKey.hashCode() : 0);
        result = 31 * result + (expireAtTime != null ? expireAtTime.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("partitionKey", partitionKey)
                .add("rowKey", rowKey)
                .add("expireAtTime", expireAtTime)
                .add("description", description)
                .toString();
    }
}
