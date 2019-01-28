package com.github.groovymail

import com.github.groovymail.protocols.ClasspathHandler
import com.github.groovymail.protocols.ConfigurableStreamHandlerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

public class Emailer {

    static {
        URL.setURLStreamHandlerFactory( new ConfigurableStreamHandlerFactory("classpath", new ClasspathHandler() ))
    }

    private static final Logger logger = LoggerFactory.getLogger(Emailer.class)

    MarkupTemplateEngine engine

    private Properties mailProperties
    private String username
    private String password
    private ExecutorService backgroundService = Executors.newCachedThreadPool()

    public Emailer(String propertyFilename = "emailer.properties") {
        if( propertyFilename ) {
            this.mailProperties = loadProperties(propertyFilename)
        }
    }

    public Emailer(Map config) {
        this("")
        this.mailProperties = new Properties( config )
    }

    public Email email(String to, String subject) {
        return new Email(to, subject)
    }

    public Email email(String to, String subject, String htmlTemplate) {
        return new Email(to, subject).html(htmlTemplate)
    }

    private Properties loadProperties(String name) {
        return getClass().getResourceAsStream("/" + name)?.withStream { InputStream stream ->
            Properties props = new Properties()
            props.load( stream )
            return props
        }
    }

    private init() {
        String protocol = this.mailProperties["mail.transport.protocol"] ?: "smtp"
        this.username = this.mailProperties["mail.${protocol}.user"]
        this.password = this.mailProperties["mail.${protocol}.password"]

        if( !this.engine ) {
            TemplateConfiguration config = new TemplateConfiguration();
            config.setAutoIndent(true);
            config.setAutoNewLine(true);
            //config.setExpandEmptyElements(true);
            //config.setLocale( locale );
            config.setUseDoubleQuotes(true);

            this.engine = new MarkupTemplateEngine(config)
        }
    }

    public void stop() {
        this.backgroundService.shutdown()
    }

    public void setMailProperties() {
        this.mailProperties = mailProperties
    }

    public class Email {
        private String to
        private String[] bcc
        private String[] cc
        private String subject
        private Map<String, Object> params = [:]
        private List<File> attachments = []
        Map<String,TemplateSource> mimeTypeToTemplate =[:]

        private Email() {
        }

        public Email(String to, String subject) {
            this()
            this.to = to
            this.subject = subject
        }

        public Email bind(Map params) {
            this.params.putAll( params )
            return this
        }

        public Email bind(String key, Object obj) {
            this.params[key] = obj
            return this
        }

        public Email to(String to) {
            this.to = to
            return this
        }

        public Email cc(String... cc) {
            this.cc = cc
            return this
        }

        public Email bcc(String... bcc) {
            this.bcc = bcc
            return this
        }

        public Email attach(File... files) {
            this.attachments.addAll(Arrays.asList(files))
            return this
        }

        public Email html(String template) {
            return mimeType("text/html", template)
        }

        public Email html(File template) {
            return mimeType("text/html", template)
        }

        public Email html(Reader reader, String sourceName = null) {
            return mimeType("text/html", reader, sourceName)
        }

        public Email text(String template) {
            return mimeType("text/plain", template)
        }

        public Email text(File file) {
            return mimeType("text/plain", file)
        }

        public Email text(Reader reader, String sourceName = null) {
            return mimeType("text/plain", reader, sourceName)
        }

        public Email mimeType(String mimeType, String template) {
            this.mimeTypeToTemplate[mimeType] = new UrlTemplate(template)
            return this
        }

        public Email mimeType(String mimeType, File file) {
            this.mimeTypeToTemplate[mimeType] = new FileTemplate(file)
            return this
        }

        public Email mimeType(String mimeType, Reader reader, String sourceName = null) {
            this.mimeTypeToTemplate[mimeType] = new ReaderTemplate(reader, sourceName)
            return this
        }

        private InternetAddress[] convertToAddress(String... recipients) throws AddressException {
            InternetAddress[] addresses = new InternetAddress[recipients.length]
            String[] arr = recipients
            int len = recipients.length

            for(int i = 0; i < len; i++) {
                String address = arr[i]
                addresses[i] = new InternetAddress(address)
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

                MimeMultipart part = new MimeMultipart("alternative")

                this.mimeTypeToTemplate.each { entry ->
                    StringWriter out = new StringWriter();
                    entry.value.locate(engine).make(this.params).writeTo(out)
                    part.addBodyPart( this.createBody( out.toString(), entry.key ) )
                }

                this.attachments.each { File f ->
                    part.addBodyPart( this.createAttachment(f) )
                }

                mimeMessage.setContent(part)

                mimeMessage.saveChanges()
                Transport transport = session.getTransport()
                logger.debug("Connecting to mail server...")

                transport.connect(username, password)

                logger.debug("Connection made with mail server.")

                transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients())
                logger.debug("Message {} sent to {}", this.subject, this.to)

                transport.close()
                logger.debug("Transport closed.")
            } catch (Exception var9) {
                logger.error("There was an problem emailing: ${this.subject} to: ${this.to}", var9)
            }
        }

        public void send() {
            String protocol = Emailer.this.mailProperties["mail.transport.protocol"] ?: "smtp"
            String from = Emailer.this.mailProperties["mail.${protocol}.from"] as String
            send( from )
        }

        public void send(String from) {
            this.send(Session.getInstance(Emailer.this.mailProperties), from)
        }

        public void sendAsync() {
            Emailer.this.backgroundService.submit(new Runnable() {
                public void run() {
                    Email.this.send()
                }
            })
        }

        public void sendAsync(String from) {
            Emailer.this.backgroundService.submit(new Runnable() {
                public void run() {
                    Email.this.send(from)
                }
            })
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
