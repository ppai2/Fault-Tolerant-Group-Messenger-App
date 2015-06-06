package edu.buffalo.cse.cse486586.groupmessenger2;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by prasan on 3/10/15.
 */

public class Message implements Serializable {
    ArrayList<Integer> arrayList = new ArrayList<Integer>();
    private String message;
    private int ownCounter;
    private int groupPriority;
    private final String sender;
    private String AckSender;
    private MessageType messageType;
    private MessageStatus messageStatus;
    //public int flag = -1;

    Message(String processInstance, String message, int ownCounter, int groupPriority) {
        this.message = message;
        this.ownCounter = ownCounter;
        this.groupPriority = groupPriority;
        sender = processInstance;
        this.messageStatus = MessageStatus.UNDELIVERABLE;
        this.messageType = MessageType.ORIGINAL;
    }

    public int getMsgCounter() {
        return ownCounter;
    }

    public int getGroupPriority() {
        return groupPriority;
    }

    public String getAcknowledgeSender() {
        return AckSender;
    }

    public String sender() {
        return sender;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getMessageText() {
        return message;
    }

    public ArrayList<Integer> getPriorityList() {
        return arrayList;
    }

    public void setAcknowledgeSender (String AckSender) {
        this.AckSender = AckSender;
    }

    public void setGroupPriority (int groupPriority) {
        this.groupPriority = groupPriority;
    }

    public void setMsgCounter (int ownPriority) {
        this.ownCounter = ownPriority;
    }

    public void setMessageStatus(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public void setPriorityList(ArrayList<Integer> arrayList) {
        this.arrayList = arrayList;
    }
}

enum ProcessInstance{
    ZERO("11108"), ONE("11112"), TWO("11116"), THREE("11120"), FOUR("11124");

    private String value = null;
    private ProcessInstance(String value) {
        value = value;
    }

    public String getValue() {
        return value;
    }

}

enum MessageStatus {
    DELIVERABLE, UNDELIVERABLE;
}

enum MessageType{
    ORIGINAL, ACKNOWLEDGE, FINAL;
}