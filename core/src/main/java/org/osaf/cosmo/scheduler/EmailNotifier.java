/*
 * Copyright 2008 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo.scheduler;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolManager;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * Notifier implementation that emails results to user. Email text is generated
 * using Velocity templates.
 */
public class EmailNotifier implements Notifier {

    private static final Log log = LogFactory.getLog(EmailNotifier.class);

    private ToolManager toolManager;
    private VelocityEngine velocityEngine;
    private JavaMailSender mailSender;
    private HashMap<String, String> properties = new HashMap<String, String>();

    public static final String TEMPLATE_PREFIX = "/org/osaf/cosmo/scheduler/";
    public static final String PROPERTY_FROM_ADDRESS = "notifier.email.fromAddress";
    public static final String PROPERTY_FROM_HANDLE = "notifier.email.fromHandle";

    public void init() {
        // initialize velocity toolbox
        toolManager = new ToolManager(false);
        toolManager.configure(TEMPLATE_PREFIX + "tools.xml");
    }

    public void sendNotificationReport(Report report,
            Map<String, String> properties) {
        boolean isHtml = "true".equalsIgnoreCase(properties.get("html"));
        boolean sendEmptyMail = "true".equalsIgnoreCase(properties.get("sendEmptyMail"));
        String[] emailAddresses = new String[] { report.getUser().getEmail() };

        if (report instanceof ForwardLookingReport)
            handleForwardLookingReport((ForwardLookingReport) report,
                    emailAddresses, isHtml, sendEmptyMail);
        else if (report instanceof DummyReport)
            handleDummyReport((DummyReport) report, emailAddresses, isHtml);
    }

    private void handleDummyReport(DummyReport report, String[] emailAddresses,
            boolean isHtml) {
        sendEmail(emailAddresses, getText(report, TEMPLATE_PREFIX
                + "dummy-subject.vm"), getText(report, TEMPLATE_PREFIX
                + (isHtml ? "dummy-body-html.vm" : "dummy-body-plain.vm")),
                isHtml);
    }

    private void handleForwardLookingReport(ForwardLookingReport report,
            String[] emailAddresses, boolean isHtml, boolean sendEmptyMail) {
        
        // Don't send email if there are no items and notifier is configured to not
        // send an empty email (saying that there are no results)
        if(!sendEmptyMail && report.getNowItems().size()==0 && report.getUpcomingItems().size()==0)
            return;
        
        sendEmail(emailAddresses, getText(report, TEMPLATE_PREFIX
                + "forward-subject.vm"), getText(report, TEMPLATE_PREFIX
                + (isHtml ? "forward-body-html.vm" : "forward-body-plain.vm")),
                isHtml);
    }

    protected String getText(Report report, String template) {
        Context context = toolManager.createContext();
        context.put("report", report);
        return evaluateVelocityTemplate(template, context);
    }

    protected void sendEmail(final String[] emailAddresses,
            final String subject, final String text, final boolean isHtml) {

        if (log.isDebugEnabled()) {
            log.debug("sending email to:");
            for(String s: emailAddresses)
                log.debug(s);
            log.debug(subject);
            log.debug(text);
        }

        for (final String address : emailAddresses) {
            try {
                mailSender.send(new MimeMessagePreparator() {
                    public void prepare(MimeMessage mimeMessage)
                            throws MessagingException {

                        String fromAddr = properties.get(PROPERTY_FROM_ADDRESS);
                        String fromHandle = properties
                                .get(PROPERTY_FROM_HANDLE);

                        MimeMessageHelper message = new MimeMessageHelper(
                                mimeMessage);
                        message.setFrom("\"" + fromHandle + "\" <" + fromAddr
                                + ">");
                        message.setTo(address.trim());
                        message.setSubject(subject);
                        message.setText(text, isHtml);
                    }
                });
            } catch (MailAuthenticationException e) {
                log.error(e.getMessage());
            } catch (MailException e) {
                log.info("failed to send email to " + address + ": "
                        + e.getMessage());
            }
        }
    }

    private String evaluateVelocityTemplate(String template, Context context) {
        StringWriter result = new StringWriter();
        try {
            velocityEngine.mergeTemplate(template, context, result);
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }
        return result.toString();
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

    public VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

}
