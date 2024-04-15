// <editor-fold desc="The MIT License" defaultstate="collapsed">
/*
 * The MIT License
 * 
 * Copyright 2022 Studio 42 GmbH ( https://www.s42m.de ).
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
//</editor-fold>
package de.s42.dl.services.email;

import de.s42.base.files.FilesHelper;
import de.s42.dl.DLAttribute.AttributeDL;
import de.s42.dl.services.AbstractService;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.Address;
import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Benjamin Schiller
 */
public class SMTPEmailService extends AbstractService implements EmailService
{

	private static final Logger log = LogManager.getLogger(SMTPEmailService.class.getName());

	protected static final AtomicInteger mailsSent = new AtomicInteger(0);

	@AttributeDL(required = true)
	private String host;

	@AttributeDL(required = true, defaultValue = "25")
	private int port = 25;

	@AttributeDL(required = true, defaultValue = "smtp")
	private String protocol = "smtp";

	@AttributeDL(required = true)
	private String user;

	@AttributeDL(required = true)
	private String password;

	@AttributeDL(required = true, defaultValue = "UTF-8")
	private String encoding = "UTF-8";

	@AttributeDL(required = true)
	private String senderName;

	@AttributeDL(required = true)
	private String senderEmail;

	@AttributeDL(required = false, defaultValue = "false")
	private boolean ssl = false;

	@AttributeDL(required = false, defaultValue = "false")
	private boolean sslRequired = false;

	@AttributeDL(required = false, defaultValue = "false")
	private boolean auth = false;

	@AttributeDL(required = false, defaultValue = "false")
	protected boolean startTLS = false;

	@Override
	public void init()
	{
		log.info("protocol :", getProtocol());
		log.info("host :", getHost());
		log.info("port :", getPort());
		log.info("ssl :", isSsl());
		log.info("sslRequired :", isSslRequired());
		log.info("auth :", isAuth());
		log.info("startTLS :", isStartTLS());		
		log.info("encoding :", getEncoding());
		log.info("senderName :", getSenderName());
		log.info("senderEmail :", getSenderEmail());
	}

	@Override
	public void exit()
	{
	}

	@Override
	public void sendEmail(
		String receiversMails,
		String ccMails,
		String bccMails,
		String subject,
		String htmlBody,
		String plainTextBody,
		Path... attachmentPaths
	) throws Exception
	{
		sendEmail(receiversMails, ccMails, bccMails, subject, senderName, senderEmail, htmlBody, plainTextBody, attachmentPaths);
	}
	
	@Override
	public void sendEmail(
		String receiversMails,
		String ccMails,
		String bccMails,
		String subject,
		String senderName,
		String senderEmail,
		String htmlBody,
		String plainTextBody,
		Path... attachmentPaths
	) throws Exception
	{
		sendEmail(receiversMails, ccMails, bccMails, subject, senderName, senderEmail, htmlBody, plainTextBody, null, attachmentPaths);
	}
	
	@Override
	public void sendEmail(
		String receiversMails,
		String ccMails,
		String bccMails,
		String subject,
		String senderName,
		String senderEmail,
		String htmlBody,
		String plainTextBody,
		String replyTo,
		Path... attachmentPaths
	) throws Exception
	{
		log.start("sendingMail");

		log.debug("Sending email");

		Properties properties = System.getProperties();

		properties.put("mail.debug", true);
		properties.setProperty("mail.transport.protocol", getProtocol());
		properties.put("mail.smtp.ssl.enable", isSsl());
		properties.put("mail.smtp.ssl.required", isSslRequired());
		properties.setProperty("mail.smtp.host", getHost());
		properties.put("mail.smtp.auth", isAuth());
		properties.put("mail.smtp.port", getPort());
		properties.put("mail.smtp.starttls.enable", isStartTLS());

		if (isSsl()) {
			properties.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		}

		Session session = Session.getDefaultInstance(properties,
			new Authenticator()
		{
			@Override
			protected PasswordAuthentication getPasswordAuthentication()
			{
				return new PasswordAuthentication(getUser(), getPassword());
			}

		});

		//Session session = Session.getDefaultInstance(properties);
		MimeMessage message = new MimeMessage(session);
		message.setSentDate(new Date());
		message.setHeader("charset", getEncoding());
		message.setFrom(new InternetAddress(senderEmail, senderName));

		if (receiversMails != null) {
			String[] recipients = receiversMails.split(";");
			for (int i = 0; i < recipients.length; ++i) {
				if (!recipients[i].isBlank()) {
					message.addRecipient(Message.RecipientType.TO,
						new InternetAddress(recipients[i].trim()));
				}
			}
		}

		if (ccMails != null) {
			String[] ccRecipients = ccMails.split(";");
			for (int i = 0; i < ccRecipients.length; ++i) {
				if (!ccRecipients[i].isBlank()) {
					message.addRecipient(Message.RecipientType.CC,
						new InternetAddress(ccRecipients[i].trim()));
				}
			}
		}

		if (bccMails != null) {
			String[] bccRecipients = bccMails.split(";");
			for (int i = 0; i < bccRecipients.length; ++i) {
				if (!bccRecipients[i].isBlank()) {
					message.addRecipient(Message.RecipientType.BCC,
						new InternetAddress(bccRecipients[i].trim()));
				}
			}
		}
		
		// Optional reply to
		if (replyTo != null) {
			message.setReplyTo(new Address[]{new InternetAddress(replyTo)});
		}

		message.setSubject(subject, getEncoding());

		MimeMultipart multipart = new MimeMultipart("alternative");
		Multipart rootBodyPart = new MimeMultipart();

		//Plaintext Body
		BodyPart messageBodyPlainPart = new MimeBodyPart();
		messageBodyPlainPart.setContent(plainTextBody, "text/plain; charset=\"" + getEncoding() + "\"");
		multipart.addBodyPart(messageBodyPlainPart);

		//HTML Body
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(htmlBody, "text/html; charset=\"" + getEncoding() + "\"");
		multipart.addBodyPart(messageBodyPart);

		BodyPart contentWrapper = new MimeBodyPart();
		contentWrapper.setContent(multipart);
		rootBodyPart.addBodyPart(contentWrapper);

		//Add attachments
		for (Path path : attachmentPaths) {

			log.debug("Attaching file", path);

			BodyPart attachmentBodyPart = new MimeBodyPart();

			Path filePath = path;
			File file = filePath.toFile();

			if (!file.exists()) {
				throw new RuntimeException("Attachment does not exist as file - " + path);
			}

			if (!file.isFile()) {
				throw new RuntimeException("Attachment is not a file - " + path);
			}

			byte[] sendBytes = Files.readAllBytes(filePath);
			String mimeType = determineMimeType(file);

			//calendar attachment - optimized for outlook
			if ("text/calendar".equals(mimeType)) {
				DataSource source = new ByteArrayDataSource(sendBytes, mimeType + ";method=REQUEST");
				attachmentBodyPart.setDataHandler(new DataHandler(source));
				attachmentBodyPart.addHeader("Content-Class", "urn:content-classes:calendarmessage");
			} //normal attachment entry
			else {
				DataSource source = new ByteArrayDataSource(sendBytes, mimeType);
				attachmentBodyPart.setDataHandler(new DataHandler(source));
				attachmentBodyPart.setFileName(file.getName());
				attachmentBodyPart.setHeader("Content-ID", file.getName());
				attachmentBodyPart.setDisposition("attachment");
			}

			rootBodyPart.addBodyPart(attachmentBodyPart);
		}

		// Send the complete message parts
		message.setContent(rootBodyPart);

		// Send message
		Transport.send(message);

		mailsSent.incrementAndGet();

		log.stopDebug("sendingMail");
	}

	protected String determineMimeType(File file) throws IOException
	{
		return FilesHelper.getMimeType(file.toPath());
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public String getProtocol()
	{
		return protocol;
	}

	public void setProtocol(String protocol)
	{
		this.protocol = protocol;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	public String getSenderName()
	{
		return senderName;
	}

	public void setSenderName(String senderName)
	{
		this.senderName = senderName;
	}

	public String getSenderEmail()
	{
		return senderEmail;
	}

	public void setSenderEmail(String senderEmail)
	{
		this.senderEmail = senderEmail;
	}

	public boolean isSsl()
	{
		return ssl;
	}

	public void setSsl(boolean ssl)
	{
		this.ssl = ssl;
	}

	public boolean isSslRequired()
	{
		return sslRequired;
	}

	public void setSslRequired(boolean sslRequired)
	{
		this.sslRequired = sslRequired;
	}

	public static int getMailsSent()
	{
		return mailsSent.get();
	}

	public boolean isAuth()
	{
		return auth;
	}

	public void setAuth(boolean auth)
	{
		this.auth = auth;
	}

	public boolean isStartTLS()
	{
		return startTLS;
	}

	public void setStartTLS(boolean startTLS)
	{
		this.startTLS = startTLS;
	}
}
