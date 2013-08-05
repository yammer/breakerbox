package com.yammer.homie.service.azure;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.yammer.dropwizard.util.Duration;
import com.yammer.homie.service.auth.User;
import com.yammer.homie.service.ldap.VirtualGroup;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.joda.time.DateTime.now;

public class RequestEntity extends TableType {
    private Integer messageId;
    private String requestState;
    private Date requestTime;
    private String username;
    private String groupname;
    private long duration;
    private Date approveTime;
    private String approverName;
    private String description;

    @Deprecated
    public RequestEntity() {
        super(AzureTableName.REQUEST);
    }

    private RequestEntity(UUID rowKey) {
        super(AzureTableName.REQUEST);
        this.partitionKey = AzureTableName.REQUEST.toString();
        this.rowKey = checkNotNull(rowKey, "rowKey cannot be null").toString();
    }
    private RequestEntity(RequestEntity copyFrom) {
        this(UUID.fromString(copyFrom.rowKey));
        requestState = copyFrom.requestState;
        requestTime = copyFrom.requestTime;
        username = copyFrom.username;
        groupname = copyFrom.groupname;
        duration = copyFrom.duration;
        approveTime = copyFrom.approveTime;
        approverName = copyFrom.approverName;
        description = copyFrom.description;
        setEtag(copyFrom.etag);
    }
    private RequestEntity(RequestEntity copyFrom, RequestState newRequestState, User approver, Date approveTime) {
        this(copyFrom);
        update(newRequestState);
        this.approveTime = checkNotNull(approveTime, "Must have affected date");
        this.approverName = checkNotNull(approver, "Change of state must have and user").getUsername();
    }

    private RequestEntity(UUID rowKey,
                          RequestState requestState,
                          DateTime requestTime,
                          User user,
                          VirtualGroup group,
                          Duration duration,
                          String description) {
        this(rowKey);
        update(requestState);
        this.requestTime = checkNotNull(requestTime, "requestTime cannot be null").withMillisOfSecond(0).toDate();
        this.username = checkNotNull(user, "username cannot be null").toString();
        this.groupname = checkNotNull(group, "groupname cannot be null").toString();
        this.duration = checkNotNull(duration, "duration cannot be null").toMilliseconds();
        this.description = checkNotNull(description, "description cannot be null");

    }

    public RequestEntity(RequestEntity requestEntity, Integer messageId) {
        this(requestEntity);
        this.messageId = checkNotNull(messageId, "Must have a messageId");
    }

    private void update(RequestState requestState) {
        this.requestState = checkNotNull(requestState, "requestState cannot be null").toString();
    }

    public static RequestEntityKey key(UUID rowKey) {
        return new RequestEntityKey(rowKey);
    }

    public RequestEntityKey key() {
        return new RequestEntityKey(UUID.fromString(rowKey));
    }

    public static RequestEntity pending(User user, VirtualGroup group, Duration duration, String description) {
        return new RequestEntity(
                UUID.randomUUID(),
                RequestState.PENDING,
                now(),
                user,
                group,
                duration,
                description);
    }

    public RequestEntity approved(User approver) {
        return with(RequestState.APPROVED, approver);
    }

    public RequestEntity declined(User approver) {
        return with(RequestState.DECLINED, approver);
    }

    private RequestEntity with(RequestState requestState, User approver) {
        return new RequestEntity(this, requestState, approver, now().toDate());
    }

    public String getRequestState() {
        return requestState;
    }

    @SuppressWarnings("UnusedDeclaration") //Azure lib requires
    public void setRequestState(String requestState) {
        this.requestState = requestState;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    @SuppressWarnings("UnusedDeclaration") //Azure lib requires
    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public String getUsername() {
        return username;
    }

    @SuppressWarnings("UnusedDeclaration") //Azure lib requires
    public void setUsername(String username) {
        this.username = username;
    }

    public String getGroupname() {
        return groupname;
    }

    @SuppressWarnings("UnusedDeclaration") //Azure lib requires
    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    @SuppressWarnings("UnusedDeclaration") //Azure lib requires
    public long getDuration() {
        return duration;
    }

    @SuppressWarnings("UnusedDeclaration") //Azure lib requires
    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Optional<Date> getApproveTimeOptional() {
        return Optional.fromNullable(approveTime);
    }

    @SuppressWarnings("UnusedDeclaration") //Azure lib requires
    public void setApproveTime(Date approveTime) {
        this.approveTime = approveTime;
    }

    public Optional<String> getApproverNameOptional() {
        return Optional.fromNullable(approverName);
    }

    @SuppressWarnings("UnusedDeclaration") //Azure lib requires
    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getDescription() {
        return description;
    }

    @SuppressWarnings("UnusedDeclaration") //Azure lib requires
    public void setDescription(String description) {
        this.description = description;
    }

    public Date getApproveTime() {
        return approveTime;
    }

    public String getApproverName() {
        return approverName;
    }

    public UUID getUUID() {
        return UUID.fromString(rowKey);
    }

    public User getUser() {
        return User.from(username);
    }

    public VirtualGroup getGroup() {
        return VirtualGroup.from(groupname);
    }

    public DateTime getExpireAtTime() {
        return new DateTime(requestTime).plus(duration);
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequestEntity)) return false;
        if (!super.equals(o)) return false;

        RequestEntity that = (RequestEntity) o;

        if (duration != that.duration) return false;
        if (approveTime != null ? !approveTime.equals(that.approveTime) : that.approveTime != null) return false;
        if (approverName != null ? !approverName.equals(that.approverName) : that.approverName != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (groupname != null ? !groupname.equals(that.groupname) : that.groupname != null) return false;
        if (messageId != null ? !messageId.equals(that.messageId) : that.messageId != null) return false;
        if (requestState != null ? !requestState.equals(that.requestState) : that.requestState != null) return false;
        if (requestTime != null ? !requestTime.equals(that.requestTime) : that.requestTime != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (messageId != null ? messageId.hashCode() : 0);
        result = 31 * result + (requestState != null ? requestState.hashCode() : 0);
        result = 31 * result + (requestTime != null ? requestTime.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (groupname != null ? groupname.hashCode() : 0);
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        result = 31 * result + (approveTime != null ? approveTime.hashCode() : 0);
        result = 31 * result + (approverName != null ? approverName.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("partitionKey", partitionKey)
                .add("rowKey", rowKey)
                .add("requestState", requestState)
                .add("requestTime", requestTime)
                .add("username", username)
                .add("groupname", groupname)
                .add("duration", duration)
                .add("approveTime", approveTime)
                .add("approverName", approverName)
                .add("description", description)
                .add("messageId", messageId)
                .toString();
    }

    public RequestEntity updateMessageId(Integer messageId) {
        return new RequestEntity(this, messageId);
    }

    public Integer getMessageId() {
        return messageId;
    }

    public Optional<Integer> getMessageIdOptional() {
        return Optional.fromNullable(messageId);
    }

    @SuppressWarnings("UnusedDeclaration") //Azure lib requires
    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }
}
