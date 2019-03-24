package com.zvecr.jmeter.smtp.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageBuilder;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;

/**
 * Connection handler that receives raw SMTP, converts, and stores on the message queue
 */
public class SinkMessageHandler implements MessageHandler {
    private static final Logger LOG = LoggerFactory.getLogger(SinkMessageHandler.class);

    private final BlockingQueue<Message> messages;

    /**
     * @param smtpSink
     */
    public SinkMessageHandler(BlockingQueue<Message> messages) {
        this.messages = messages;
    }

    private final MessageBuilder builder = new DefaultMessageBuilder();

    @Override
    public void from(String from) {
        // do we care about envelope sender?
    }

    @Override
    public void recipient(String recipient) {
        // do we care about envelope sender?
    }

    @Override
    public void data(InputStream data) throws IOException {
        try {
            Message message = builder.parseMessage(data);
            messages.add(message);

            LOG.trace("Received message:{}", message.getMessageId());
        } catch (MimeException e) {
            throw new RejectException(e.getMessage());
        }
    }

    @Override
    public void done() {
        // do nothing
    }
}