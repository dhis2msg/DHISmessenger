package org.dhis2.messenger.core.rest.callback;

import org.dhis2.messenger.model.ChatModel;
import org.dhis2.messenger.model.NameAndIDModel;

import java.util.List;

/**
 * Created by iNick on 20.10.14.
 */
public interface RESTConversationCallback {
    void updateMessages(List<ChatModel> list);

    void updateUsers(List<NameAndIDModel> list);

    void messageSent(boolean sent);
}
