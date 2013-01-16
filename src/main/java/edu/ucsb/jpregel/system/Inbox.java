package edu.ucsb.jpregel.system;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Pete Cappello
 */
public final class Inbox 
{  
    private Map<Object, Message> targetVertexIdToAddEdgeMessageMap;
    private Map<Object, Message> targetVertexIdToRemoveEdgeMessageMap;
    private Message addVertexMessage;
    private Message removeVertexMessage;
    private MessageQ messageQ;
    private Combiner messageCombiner;
    
    Inbox( Combiner messageCombiner )
    {
        targetVertexIdToAddEdgeMessageMap    = new HashMap<Object, Message>();
        targetVertexIdToRemoveEdgeMessageMap = new HashMap<Object, Message>();
        this.messageCombiner = messageCombiner;
        messageQ = new MessageQ( messageCombiner );
    }
    
    Map<Object, Message> getTargetVertexIdToAddEdgeMessageMap()    { return targetVertexIdToAddEdgeMessageMap; }
    
    Map<Object, Message> getTargetVertexIdToRemoveEdgeMessageMap() { return targetVertexIdToRemoveEdgeMessageMap; }
    
    Message getAddVertexMessage()    { return addVertexMessage; }
    
    Message getRemoveVertexMessage() { return removeVertexMessage; }
    
    MessageQ getMessageQ() { return messageQ; }
}
