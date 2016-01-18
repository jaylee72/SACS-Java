package com.sabre.api.sacs.soap.callback;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;

import com.sabre.api.sacs.contract.soap.MessageHeader;
import com.sabre.api.sacs.contract.soap.Security;
import com.sabre.api.sacs.soap.pool.SessionPool;
import com.sabre.api.sacs.workflow.SharedContext;

/**
 * This is a callback class for working with Spring Web Services. It plays a
 * crucial role in consuming Sabre Web Services using Spring WS framework. By
 * default, Spring WS has no mechanism that would allow to compose custom
 * headers into the message. This callback inserts wsse:Security and
 * eb:MessageHeader headers into processed message. The doWithMessage() method
 * marshals proper header objects into the message.
 */
public class HeaderComposingCallback implements HeaderCallback {

    private String actionString;

    private MessageHeader header;
    private SharedContext workflowContext;
    private Security security;

    @Autowired
    private MessageHeaderFactory messageHeaderFactory;

    @Autowired
    private SessionPool sessionPool;

    @Autowired
    private Jaxb2Marshaller marshaller;

    public HeaderComposingCallback(String actionString) {
        this.actionString = actionString;
    }

    @Override
    public void doWithMessage(WebServiceMessage webServiceMessage) throws IOException, TransformerException {

        boolean creatingSession = actionString.equalsIgnoreCase("SessionCreateRQ");

        header = messageHeaderFactory.getMessageHeader(this.actionString);
        header.setConversationId(workflowContext.getConversationId());

        if (creatingSession) {
            throw new UnsupportedOperationException("Legal for calls other than Session Create and Session Close.");
        } else {
            security = sessionPool.getFromPool(workflowContext);
        }

        SoapHeader soapHeaderElement = ((SoapMessage) webServiceMessage).getSoapHeader();

        marshaller.marshal(header, soapHeaderElement.getResult());
        marshaller.marshal(security, soapHeaderElement.getResult());

    }

    public void setWorkflowContext(SharedContext workflowContext) {
        this.workflowContext = workflowContext;
    }
}