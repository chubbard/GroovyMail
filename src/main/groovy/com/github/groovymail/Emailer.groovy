package com.github.groovymail

import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.Iterator
import java.util.List
import java.util.Map
import java.util.Properties
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.activation.FileDataSource
import javax.mail.BodyPart
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimeMessage.RecipientType
import org.apache.log4j.Logger

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

public class Emailer {

    private static final Logger logger = Logger.getLogger(Emailer.class)

		MarkupTemplateEngine engine

    private String from
    private String username
    private String password
    private Properties mailProperties
    private ExecutorService backgroundService = Executors.newCachedThreadPool()

    protected Emailer() {
    }

    public Emailer(Properties mailProperties, String from, String username, String password) throws IOException {
    		this();
        this.mailProperties = mailProperties
        this.from = from
        this.username = username
        this.password = password
    }

    public Emailer.Email email(String subject, String mailTemplate, String htmlTemplate) {
        return new Emailer.Email(subject, mailTemplate, htmlTemplate)
    }

    public Emailer.Email email(String to, String subject, String mailTemplate, String htmlTemplate) {
        return new Emailer.Email(to, subject, mailTemplate, htmlTemplate)
    }

    public void stop() {
        this.backgroundService.shutdown()
    }

    private init() {
    	if( !this.engine ) {
		    TemplateConfiguration config = new TemplateConfiguration();
		    config.setAutoIndent(true);
		    config.setAutoNewLine(true);
		    config.setExpandEmptyElements(true);
		    //config.setLocale( locale );
		    config.setUseDoubleQuotes(true);

		    this.engine = new MarkupTemplateEngine(config)    		
    	}
    }

    public class Email {
        private String to
        private String[] bcc
        private String[] cc
        private String subject
        private String textTemplate
        private String htmlTemplate
        private Map<String, Object> params
        private List<File> attachments

        private Email() {
            this.params = new HashMap()
            this.attachments = new ArrayList()
        }

        public Email(String subject, String textTemplate, String htmlTemplate) {
            this()
            this.subject = subject
            this.textTemplate = textTemplate
            this.htmlTemplate = htmlTemplate
        }

        public Email(String to, String subject, String textTemplate, String htmlTemplate) {
            this()
            this.to = to
            this.subject = subject
            this.textTemplate = textTemplate
            this.htmlTemplate = htmlTemplate
        }

        public Emailer.Email bind(String key, Object obj) {
            this.params[key] = obj
            return this
        }

        public Emailer.Email to(String to) {
            this.to = to
            return this
        }

        public Emailer.Email cc(String... cc) {
            this.cc = cc
            return this
        }

        public Emailer.Email bcc(String... bcc) {
            this.bcc = bcc
            return this
        }

        public Emailer.Email attach(File... files) {
            this.attachments.addAll(Arrays.asList(files))
            return this
        }

        private InternetAddress[] convertToAddress(String... recipients) throws AddressException {
            InternetAddress[] addresses = new InternetAddress[recipients.length]
            int i = 0
            String[] arr = recipients
            int len = recipients.length

            for(int i = 0; i < len; ++i) {
                String address = arr[i]
                addresses[i++] = new InternetAddress(address)
            }

            return addresses
        }

        public void send(Session session, String from) {
        		Emailer.this.init();        	
            try {
                MimeMessage mimeMessage = new MimeMessage(session)
                mimeMessage.setRecipient(RecipientType.TO, new InternetAddress(this.to))
                if (this.cc != null) {
                    mimeMessage.setRecipients(RecipientType.CC, this.convertToAddress(this.cc))
                }

                if (this.bcc != null) {
                    mimeMessage.setRecipients(RecipientType.BCC, this.convertToAddress(this.bcc))
                }

                mimeMessage.setFrom(new InternetAddress(from))
                mimeMessage.setSubject(this.subject)
            //     if (this.htmlTemplate == null) {
            //         if (this.attachments.isEmpty()) {
            //         		StringWriter out = new StringWriter();
												// Emailer.this.engine.createTemplate(new URL(this.template)).make(this.params).writeTo(out)
            //             mimeMessage.setText( out.toString() )
            //         } else {
            //             MimeMultipart partx = new MimeMultipart()
            //         		StringWriter out = new StringWriter();
												// Emailer.this.engine.createTemplate(new URL(this.template)).make(this.params).writeTo(out)

            //             partx.addBodyPart(this.createBody(out.toString(), "text/plain"))
            //             Iterator i$ = this.attachments.iterator()

            //             while(i$.hasNext()) {
            //                 File f = (File)i$.next()
            //                 partx.addBodyPart(this.createAttachment(f))
            //             }

            //             mimeMessage.setContent(partx)
            //         }
            //     } else {

		        		StringWriter out = new StringWriter();
								Emailer.this.engine.createTemplate(new URL(this.template)).make(this.params).writeTo(out)

		            MimeMultipart part = new MimeMultipart("alternative")
		            
		            part.addBodyPart(this.createBody(text, "text/plain"))
		            part.addBodyPart(this.createBody(html, "text/html"))

		            Iterator i$x = this.attachments.iterator()

		            while(i$x.hasNext()) {
		                File fx = (File)i$x.next()
		                part.addBodyPart(this.createAttachment(fx))
		            }

		            mimeMessage.setContent(part)
          	// }

                mimeMessage.saveChanges()
                Transport transport = session.getTransport()
                if (Emailer.logger.isDebugEnabled()) {
                    Emailer.logger.debug("Connecting to mail server...")
                }

                if (Emailer.this.username && Emailer.this.password ) {
                    transport.connect(Emailer.this.username, Emailer.this.password)
                } else {
                    transport.connect()
                }

                if (Emailer.logger.isDebugEnabled()) {
                    Emailer.logger.debug("Connection made with mail server.")
                }

                transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients())
                if (Emailer.logger.isDebugEnabled()) {
                    Emailer.logger.debug("Message " + this.subject + " sent to " + this.to)
                }

                transport.close()
                if (Emailer.logger.isDebugEnabled()) {
                    Emailer.logger.debug("Transport closed.")
                }
            } catch (Exception var9) {
                Emailer.logger.error("There was an problem emailing: " + this.subject + " to: " + this.to, var9)
            }

        }

        public void send() {
            this.send(Session.getInstance(Emailer.this.mailProperties), Emailer.this.from)
        }

        public void sendAsync() {
            Emailer.this.backgroundService.submit(new Runnable() {
                public void run() {
                    Email.this.send()
                }
            })
        }

        public void send(String from) {
            this.send(Session.getInstance(Emailer.this.mailProperties), from)
        }

        private BodyPart createBody(String text, String mimetype) throws MessagingException {
            MimeBodyPart body = new MimeBodyPart()
            body.setContent(text, mimetype)
            return body
        }

        private BodyPart createAttachment(File f) throws MessagingException {
            DataSource source = new FileDataSource(f)
            MimeBodyPart attachment = new MimeBodyPart()
            attachment.setDataHandler(new DataHandler(source))
            attachment.setFileName(f.getName())
            return attachment
        }
    }
}
