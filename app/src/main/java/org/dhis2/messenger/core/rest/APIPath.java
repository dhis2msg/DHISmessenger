package org.dhis2.messenger.core.rest;

/**
 * Created by iNick on 23.09.14.
 */
public class APIPath {
    public static final String USER_INFO = "api/me";
    public static final String FIRST_PAGE_MESSAGES = "api/messageConversations";
    public static final String FIRST_PAGE_INTERPRETATIONS = "api/interpretations";
    public static final String USERS = "api/users";
    public static final String ORG_UNITS = "api/organisationUnits";
    public static final String INBOX_FIELDS = "?fields=id,name,lastMessage,lastSenderFirstname,lastSenderSurname";
    public static final String INTERPRETATIONS_FIELDS = "?fields=id,lastUpdated,type,text,user[name,id],chart[id],map[id],reportTable[id],dataSet[id],comments[user[id,name],text,lastUpdated]";
    public static final String INTERPRETATIONS_COMMENT_FIELDS = "?fields=comments[lastUpdated,text,user[id,name]]";
    public static final String REST_CONVERSATION_FIELDS = "?fields=userMessages[user[name,id]],messages[text,lastUpdated,sender[name,id]]";
    public static final String NAME_AND_ID_FIELDS = "?fields=id,name&paging=false";
    public static final String ADD_GCM_ID = "/addgcm?id=";
    public static final String REMOVE_GCM_ID = "/removegcm?id=";
    public static final String GCMID = "697432136877";

    private APIPath() {
    }
}
