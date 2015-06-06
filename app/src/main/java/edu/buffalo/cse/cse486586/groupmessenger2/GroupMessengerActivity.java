package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    int total_nodes = 5;
    static final String finalQueue = "QUEUE";
    static final String TAG="tag";

    static String REMOTE_PORT0 = "11108";
    static String REMOTE_PORT1 = "11112";
    static String REMOTE_PORT2 = "11116";
    static String REMOTE_PORT3 = "11120";
    static String REMOTE_PORT4 = "11124";
    static final int SERVER_PORT = 10000;
    int msgCounter = 0;
    public final Uri mUri = null;
    String port_myPort;
    String process;
    HashMap<String,Integer> tempMap = new HashMap<String,Integer>();

    Socket socket = null;

    public int myMessageCounter = 0;
    public int groupMessagePriorityCounter = 0;

    public PriorityBlockingQueue<Message> finalPriorityQueue;
    //HashMap<ProcessInstance, Integer> timeProc;
    HashMap<ProcessInstance, Integer> timeFailProc;

    ArrayList<Message> queueOriginalAcknowledge = new ArrayList<Message>();

    Timer timer;
    TimerTask timerTask;

    static int agreedPriority = -1;
    static int proposedPriority = -1;

    Map<Integer, Integer[]> messageMap = new HashMap<Integer, Integer[]>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);

        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
            Log.e(TAG, "ServerSocket");
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        port_myPort = myPort;
        System.out.println("Local Port:"+myPort);
        int port = Integer.parseInt(port_myPort);
        int port_index = ((port-11108)/4);
        switch (port_index) {
            case 0: process = "11108"; break;
            case 1: process = "11112"; break;
            case 2: process = "11116"; break;
            case 3: process = "11120"; break;
            case 4: process = "11124"; break;
        }

        /*if (port_index == 0) {
            process = ProcessInstance.ZERO;
        } else if (port_index == 1) {
            process = ProcessInstance.ONE;
        } else if (port_index == 2) {
            process = ProcessInstance.TWO;
        } else if (port_index == 3) {
            process = ProcessInstance.THREE;
        } else if (port_index == 4) {
            process = ProcessInstance.FOUR;
        }*/

        finalPriorityQueue = new PriorityBlockingQueue<Message>(500, new MessageComparator());
        Log.e (TAG, "FinalPriorityQueue after the comparator");
        /*
        timeProc = new HashMap<ProcessInstance, Integer>();
        timeProc.put(ProcessInstance.ZERO, 0);
        timeProc.put(ProcessInstance.ONE, 0);
        timeProc.put(ProcessInstance.TWO, 0);
        timeProc.put(ProcessInstance.THREE, 0);
        timeProc.put(ProcessInstance.FOUR, 0);
        */

        timeFailProc = new HashMap<ProcessInstance, Integer>();
        //Log.e(finalQueue, "Before the timerTask block for checking the message stats");
        /*timer = new Timer();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.e(TAG, "Inside the timerTask block for checking the message stats");
                while (finalPriorityQueue != null && !(finalPriorityQueue.isEmpty()) && finalPriorityQueue.peek() != null && finalPriorityQueue.peek().getMessageStatus().equals(MessageStatus.UNDELIVERABLE)) {
                    while (finalPriorityQueue.peek().sender().equals(ProcessInstance.ZERO)) {
                        Message processZero = new Message(ProcessInstance.ZERO, "", 0, 0);
                        //processZero.flag = 0;
                        processZero.setMessageType(MessageType.ACKNOWLEDGE);
                        System.out.println("Message content : " + processZero.getMessageText());
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, processZero, null);
                    }
                    while (finalPriorityQueue.peek().sender().equals(ProcessInstance.ONE)) {
                        Message processOne = new Message(ProcessInstance.ONE, "", 0, 0);
                        //processOne.flag = 0;
                        processOne.setMessageType(MessageType.ACKNOWLEDGE);
                        System.out.println("Message content : " + processOne.getMessageText());
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, processOne, null);
                    }
                    while (finalPriorityQueue.peek().sender().equals(ProcessInstance.TWO)) {
                        Message processTwo = new Message(ProcessInstance.TWO, "", 0, 0);
                        //processTwo.flag = 0;
                        processTwo.setMessageType(MessageType.ACKNOWLEDGE);
                        System.out.println("Message content : " + processTwo.getMessageText());
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, processTwo, null);
                    }
                    while (finalPriorityQueue.peek().sender().equals(ProcessInstance.THREE)) {
                        Message processThree = new Message(ProcessInstance.THREE, "", 0, 0);
                        processThree.setMessageType(MessageType.ACKNOWLEDGE);
                        processThree.setAcknowledgeSender(ProcessInstance.THREE);
                        Log.e(TAG, "Message Type of processThree " + processThree.getMessageType() + " " + processThree.getAcknowledgeSender());
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, processThree, null);
                    }
                    while (finalPriorityQueue.peek().sender().equals(ProcessInstance.FOUR)) {
                        Message processFour = new Message(ProcessInstance.FOUR, "", 0, 0);
                        processFour.setMessageType(MessageType.ACKNOWLEDGE);
                        processFour.setAcknowledgeSender(ProcessInstance.FOUR);
                        Log.e(TAG, "Message Type of processFour " + processFour.getMessageType() + " " + processFour.getAcknowledgeSender());
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, processFour, null);
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 1000, 1000);
        */

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */

        findViewById(R.id.button1).setOnClickListener(new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

        final EditText editText = (EditText) findViewById(R.id.editText1);

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString();
                editText.setText(""); // This is one way to reset the input box.

                myMessageCounter++;
                Message messageInstance = new Message(process, msg, myMessageCounter, groupMessagePriorityCounter++);
                Log.e(TAG, "Message content and status: " + messageInstance.getMessageText() + " " + messageInstance.getMessageStatus());
                messageInstance.setMessageType(MessageType.ORIGINAL);
                messageMap.put(myMessageCounter, new Integer[] {0,0});
                //List<String> remotePorts = new ArrayList<String>();                                       // Creating an array list of remote ports for iteration
                //remotePorts.add("11108");
                //remotePorts.add("11112");
                //remotePorts.add("11116");
                //remotePorts.add("11120");
                //remotePorts.add("11124");
               // for (int j = 0; j< remotePorts.size(); j++) {
                 //   messageInstance.setAcknowledgeSender(remotePorts.get(j));
                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, messageInstance);
                //}
            }
        });
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if(!socket.isClosed())
                socket.close();
            if(!socket.isClosed())
                socket.close();
        } catch (IOException e) {
            System.out.println("Error closing the socket");
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(!socket.isClosed())
                socket.close();
            if(!socket.isClosed())
                socket.close();
        } catch (IOException e) {
            System.out.println("Error closing the socket");
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if(!socket.isClosed())
                socket.close();
            if(!socket.isClosed())
                socket.close();
        } catch (IOException e) {
            System.out.println("Error closing the socket");
            e.printStackTrace();
        }
    }
    */



    private class ServerTask extends AsyncTask<ServerSocket, Message, Void> {
        // AsyncTask<doInBackground

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            ObjectInputStream objectInputStream=null;
            while (!serverSocket.isClosed()) {
                try{
                    socket = serverSocket.accept();
                    objectInputStream = new ObjectInputStream(socket.getInputStream());
                    Message m = (Message)objectInputStream.readObject();
                    Log.e(TAG, "Server Message : " + m.getMessageText() + " " + m.getGroupPriority() +  " " + m.getMsgCounter() + " " + m.getMessageType() + " " + m.getMessageStatus());
                    publishProgress(m);
                } catch(IOException e) {
                    Log.e(TAG, "Socket IOException at server end");
                } catch(ClassNotFoundException e) {
                    Log.e(TAG, "ClassNotFoundException at server end");
                } finally {
                    /*try {
                        if (!socket.isClosed()) {
                            socket.close();
                        }
                    } catch (IOException e) {
                        System.out.println(e);
                        e.printStackTrace();
                    }
                    try {
                        objectInputStream.close();
                    } catch (IOException e) {
                        System.out.println(e);
                        e.printStackTrace();

                    }
                    try {
                        if (!serverSocket.isClosed()) {
                            serverSocket.close();
                        }
                    } catch (IOException e) {
                        System.out.println(e);
                        e.printStackTrace();
                    }
*/
                }
            }
            return null;
        }

        protected void onProgressUpdate(Message...messages) {
            /*
             * The following code displays what is received in doInBackground().
             */

            Message m1 = messages[0];
            Log.e(TAG, "OnProgressUpdate block: " + m1.getMessageText());
            //while (m1.flag != 0) {
                /*if (m1.getMessageType().equals(MessageType.ACKNOWLEDGE)) {
                    if (timeProc.containsKey(m1.getAcknowledgeSender())) {
                        timeProc.put(m1.getAcknowledgeSender(), 1);
                    }
                }
                else {
                    if (timeProc.containsKey(m1.sender())) {///////////////////////////////////////
                        timeProc.put(m1.sender(),1);
                    }

                }
                */

                /*
                    This block handles all the ORIGINAL messages and change them to ACKNOWLEDGE status before sending them back to their senders.
                */
            if (m1.getMessageType().equals(MessageType.ORIGINAL)) {
                    Log.e(TAG, "In ORIGINAL block" + m1.getMessageType());
                   /* int priority = m1.getGroupPriority();
                    Log.e(TAG, "Priority of the message : " + m1.getGroupPriority());

                    int newPriority = 0;
                    if (priority > groupMessagePriorityCounter) {
                        newPriority = priority + 1;
                    } else {
                        newPriority = groupMessagePriorityCounter + 1;
                    }
                    Log.e(TAG, "Group Message Priority of the message : " + groupMessagePriorityCounter);

                    groupMessagePriorityCounter = newPriority;
                    m1.setProcessInstancePriority(groupMessagePriorityCounter);*/
                if(!(tempMap.containsKey(m1.sender()) && tempMap.get(m1.sender())==m1.getMsgCounter())) {
                    tempMap.put(m1.sender(),m1.getMsgCounter());
                    m1.setMessageType(MessageType.ACKNOWLEDGE);
                    m1.setAcknowledgeSender(port_myPort);
                    proposedPriority = Math.max(proposedPriority, agreedPriority) + 1;
                    //m1.setProcessInstancePriority(proposedPriority);
                    m1.setGroupPriority(proposedPriority);
                    finalPriorityQueue.put(m1);
                    Log.e(finalQueue, "UD: " + m1.sender() + " " + m1.getMsgCounter()+" "+proposedPriority);
                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, m1);
                }
                    /*for (Message mFinal : finalPriorityQueue) {
                        Log.e(TAG, "Final priority queue in original : " + mFinal.getMessageText() + " " + mFinal.getMessageType() + " " + mFinal.getAcknowledgeSender() + " " + "Group priority: " + mFinal.getGroupPriority() + " " + "ProcessInstance Priority :" + mFinal.getMsgCounter());
                    }*/

            } else if (m1.getMessageType().equals(MessageType.ACKNOWLEDGE)) {
                    /*
                    This block handles the messages which have been ACKNOWLEDGE and change them to FINAL status before enqueueing them in the waiting queue
                    and taking their MAXIMUM priority
                     */
                   /* ArrayList<Integer> list1 = queueOriginalAcknowledge.get(m1.getGroupPriority()).getPriorityList();
                    list1.set((Integer.parseInt(m1.getAcknowledgeSender())-11108)/4,m1.getGroupPriority());

                        while (!timeFailProc.isEmpty()) {
                        for (Map.Entry<ProcessInstance, Integer> dataSet : timeFailProc.entrySet()) {
                            while((list1.get((Integer.parseInt(dataSet.getKey().getValue())-11108)/4)!= -1)) {
                                list1.set((Integer.parseInt(dataSet.getKey().getValue())-11108)/4, 0);
                            }
                        }
                    }
                    System.out.println("Proposed Priority:" + list1.get((Integer.parseInt(m1.getAcknowledgeSender()) - 11108) / 4));
                    */
                    //m1.setPriorityList(list1);
                    int currentPriority = m1.getGroupPriority();
                    int msgID = m1.getMsgCounter();
                    Integer[] intArr = messageMap.get(msgID);
                    messageMap.remove(msgID);
                    intArr[0]++;                                                // Response count
                    intArr[1] = Math.max(intArr[1],currentPriority);            // Highest priority
                    messageMap.put(msgID,intArr);
                    if(intArr[0]==total_nodes){
                        //List<String> remotePorts = new ArrayList<String>();                                       // Creating an array list of remote ports for iteration
                        //remotePorts.add("11108");
                        //remotePorts.add("11112");
                        //remotePorts.add("11116");
                        //remotePorts.add("11120");
                        //remotePorts.add("11124");
                        agreedPriority = Math.max(agreedPriority, intArr[1]);

                        //for (int j = 0; j< remotePorts.size(); j++) {
                            m1.setGroupPriority(intArr[1]);
                            m1.setMessageType(MessageType.FINAL);
                          //  m1.setAcknowledgeSender(remotePorts.get(j));
                            //Log.e(TAG, "Message in acknowledge block : " + m1.getMessageText() + " " + m1.getMessageType() + " " + m1.getMsgCounter() + " " + m1.getGroupPriority() + " " + m1.getAcknowledgeSender());
                            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, m1);
                        //}

                    }

                    /*for (Message mFinal : finalPriorityQueue) {
                        Log.e(TAG, "Final priority queue in acknowledge : " + mFinal.getMessageText() + " " + mFinal.getMessageType() + " " + mFinal.getAcknowledgeSender() + " " + "Group priority: " + mFinal.getGroupPriority() + " " + "ProcessInstance Priority :" + mFinal.getMsgCounter());
                    }
                    */

                    /* while(Collections.min(list1) != -1) {
                        queueOriginalAcknowledge.set(m1.getProcessInstancePriority(), null);
                        int maxPriority = Collections.max(list1);
                        m1.setMessageType(MessageType.FINAL);
                        m1.setProcessInstancePriority(maxPriority);
                        groupMessagePriorityCounter = maxPriority + 1;
                        //Log.e(TAG, finalPriorityQueue.toString());
                        Log.e(TAG, process + ": Sending FINAL:" + m1.getMessageText() + " priority: " + m1.getGroupPriority());
                        ///////////////////////// Acknowledge Process details

                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, m1);
                    }*/

            } else if (m1.getMessageType().equals(MessageType.FINAL)){
                    /*
                    This block handles the messages which are FINAL and deliverable messages by sending them to the AVDs and inserting them in the database at the same time
                     *
                     * /
                    /*if (groupMessagePriorityCounter > m1.getGroupPriority()) {
                        groupMessagePriorityCounter = groupMessagePriorityCounter + 1;
                    } else {
                        groupMessagePriorityCounter = m1.getGroupPriority() + 1;
                    }
                    */
                    Iterator<Message> iterator = finalPriorityQueue.iterator();
                    while(iterator.hasNext()) {
                        String str1 = iterator.next().getMessageText();
                        if(str1.equals(m1.getMessageText())) {
                            //System.out.println("Message content : " + m1.getMessageText());
                            Log.e(finalQueue,"UD rm "+m1.sender()+" "+m1.getMsgCounter());
                            iterator.remove();
                            break;
                        }
                    }
                    agreedPriority = Math.max(agreedPriority, m1.getGroupPriority());
                    //m1.setGroupPriority(agreedPriority);
                    m1.setMessageStatus(MessageStatus.DELIVERABLE);
                    //Log.e(TAG, "Final message status : " + m1.getGroupPriority() + " " + m1.getMessageType());
                    finalPriorityQueue.add(m1);
                    Log.e(finalQueue,"D ins "+m1.sender()+" "+m1.getMsgCounter()+ " "+m1.getGroupPriority());

                        //System.out.println("Priority Queue:"+finalPriorityQueue.toString());

                    Message finalMessage = null;
                    while(finalPriorityQueue.size() > 0 && finalPriorityQueue.peek().getMessageStatus().equals(MessageStatus.DELIVERABLE)) {
                        //if () { //&& timeFailProc.containsKey(finalMessage.sender()//)) {
                       finalMessage = finalPriorityQueue.poll();
                       Log.e(finalQueue,"D pop "+finalMessage.sender()+" "+finalMessage.getMsgCounter());

                       insert(finalMessage);
                    }
//                Log.e(finalQueue,"D ")

                   /* for (Message mFinal : finalPriorityQueue) {
                        Log.e(finalQueue, "Final priority queue in final : " + mFinal.getMessageText() + " " + mFinal.getMessageType() + " " + mFinal.getAcknowledgeSender() + " " + "Group priority: " + mFinal.getGroupPriority() + " " + "ProcessInstance Priority :" + mFinal.getMsgCounter());
                   }*/

                    //Log.e(TAG, "Final queue messages: " + m1.getMessageType() + " " + m1.getGroupPriority() + " " + m1.getMsgCounter() + " " + m1.getMessageStatus());

            }// for MessageType.FINAL status messages
            //}

            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */
        }

        private void insert (final Message messageRead) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("key", msgCounter++);
            contentValues.put("value", messageRead.getMessageText());
            getContentResolver().insert(buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider"), contentValues);
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView localTextView = (TextView) findViewById(R.id.textView1);
                    localTextView.append(messageRead.getMessageText() + " " + Integer.toString(messageRead.getGroupPriority()) + "\n");
                }
            });*/
        }

        private Uri buildUri(String scheme, String authority) {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }

        /*
        Insert method for inserting records in the form of keys and values in the SQLite database and displaying the messages on the AVDs
         */

    }

    private class ClientTask extends AsyncTask<Message, Void, Void> {

        @Override
        protected Void doInBackground(Message... msgs) {

            Message msgToSend = msgs[0];
            Socket socket;
            String remotePort;
            String senderPort;
            ObjectOutputStream objectOutputStream;

            List<String> remotePorts = new ArrayList<String>();                                       // Creating an array list of remote ports for iteration
            remotePorts.add("11108");
            remotePorts.add("11112");
            remotePorts.add("11116");
            remotePorts.add("11120");
            remotePorts.add("11124");

            /*
            For messages with message type as ORIGINAL or FINAL to be handled by this block and check for socket timeout exception in which case
            the control will be handled by timeOutPort function
             */
            if (msgToSend.getMessageType().equals(MessageType.ORIGINAL) || msgToSend.getMessageType().equals(MessageType.FINAL)) {
                /*while (msgToSend.getMessageType().equals(MessageType.ORIGINAL)) {
                    System.out.println(process.toString() + ": Client sending ORIGINAL:" + msgToSend.getMessageText() + " to ALL");
                    queueOriginalAcknowledge.add(msgToSend.getProcessInstancePriority(), msgToSend);
                }
                */
                //queueOriginalAcknowledge.add(msgToSend.getMsgCounter(), msgToSend);

                for (int i = 0; i < remotePorts.size(); i++) {
                    try {
                        remotePort = remotePorts.get(i);// msgToSend.getAcknowledgeSender();
                        socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(remotePort));
                        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                        objectOutputStream.writeObject(msgToSend);
                        objectOutputStream.flush();
                        objectOutputStream.close();
                        socket.close();
                        Log.e(TAG, "Message sent from the client");
                    } catch (UnknownHostException e) {
                        Log.e(TAG, "ClientTask UnknownHostException");
                    } catch (IOException e) {
                        Log.e(TAG, "ClientTask socket IOException");
                        //timeOutPort(remotePorts.get(i));
                        //timer.cancel();
                        //timer.cancel();
                    }

                }
            }
            else if (msgToSend.getMessageType().equals(MessageType.ACKNOWLEDGE)){
                /*
                For messages with message type ACKNOWLEDGE will be handled by this block and checked for socket timeout in which case
                the control will be handled by timeOutProc function
                 */
                try {

                    senderPort = msgToSend.sender();

                    //Log.e(TAG,"Msg Type:"+msgToSend.getMessageType());
                    //Log.e(TAG, "Sender port : " + msgToSend.sender());
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(senderPort));
                    objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject(msgToSend);
                    objectOutputStream.flush();
                    objectOutputStream.close();
                    Log.e(TAG, "Message sent from the client's acknowledge block");
                    socket.close();
                } catch (UnknownHostException e) {
                    Log.e(TAG, "ClientTask UnknownHostException");
                } catch (IOException e) {
                    Log.e(TAG, "ClientTask socket IOException");
                    //timeOutProcess(msgToSend.sender());
                    //timer.cancel();
                    //timer.cancel();
                }
            }
            return null;
        }
    }
}
