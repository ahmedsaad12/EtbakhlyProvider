package com.etbakhly_provider.model;

import java.io.Serializable;
import java.util.List;

public class MessagesDataModel extends StatusResponse implements Serializable {
    private List<MessageModel> data;

    public List<MessageModel> getData() {
        return data;
    }
}
