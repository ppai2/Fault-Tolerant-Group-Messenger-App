package edu.buffalo.cse.cse486586.groupmessenger2;

import java.util.Comparator;

/**
* Created by prasan on 3/13/15.
*/
    public class MessageComparator implements Comparator<Message> {
    public int compare(Message m1, Message m2) {

        if (m1.getGroupPriority() > m2.getGroupPriority()) return 1;
        else if (m1.getGroupPriority() < m2.getGroupPriority()) return -1;
        else {

            // Tie Breaker if final priority of two messages is same.

            // If one is undeliverable and one is deliverable then the undeliverable will get higher priority.
            // (i.e. low priority number)

            MessageStatus messageStatus1 = m1.getMessageStatus();
            MessageStatus messageStatus2 = m2.getMessageStatus();
            if (!messageStatus1.equals(messageStatus2)) {
                if (!messageStatus1.equals(MessageStatus.UNDELIVERABLE)) {
                    return 1;
                } else {
                    return -1;
                }
            } else {
                if (messageStatus1.equals(MessageStatus.DELIVERABLE)) {
                    int sender1_index = Integer.parseInt(m1.sender());
                    int sender2_index = Integer.parseInt(m2.sender());
                    if (sender1_index > sender2_index) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
            return 0;
        }
    }
}
