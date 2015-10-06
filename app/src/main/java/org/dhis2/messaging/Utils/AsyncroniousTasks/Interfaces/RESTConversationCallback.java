package org.dhis2.messaging.Utils.AsyncroniousTasks.Interfaces;

import org.dhis2.messaging.Models.ChatModel;
import org.dhis2.messaging.Models.NameAndIDModel;

import java.util.List;

/**
 * Created by iNick on 20.10.14.
 */
public interface RESTConversationCallback {
    void updateMessages(List<ChatModel> list);

    void updateUsers(List<NameAndIDModel> list);

    void messageSent(boolean sent);
}
