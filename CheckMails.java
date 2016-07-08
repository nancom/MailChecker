import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.*;
import javax.mail.search.*;
import java.text.*;
import javax.activation.DataHandler;

public class CheckingMails {

public static void check(String host, String storeType, String user,String password,int mailCount,String filter,String subjectFilter){

      final String username = user;
      final String pass = password;
      try {
      
      System.out.println("-=== filter :"+subjectFilter+" ===-");
      //create properties field
      Properties properties = new Properties();

      properties.put("mail.pop3.host", host);
      properties.put("mail.pop3.port", "110");
      properties.put("mail.pop3.starttls.enable", "true");
    
      Session emailSession = Session.getInstance(properties,
        new javax.mail.Authenticator() {
          protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(username, pass);
        }
      });

  
      //create the POP3 store object and connect with the pop server
      Store store = emailSession.getStore("pop3");

      store.connect(host, user, password);

      //create the folder object and open it
      Folder emailFolder = store.getFolder("INBOX");
      emailFolder.open(Folder.READ_ONLY);

      //retrieve the messages from the folder in an array and print it
      
      Message[] messages = emailFolder.getMessages();

      if(messages.length>0){
         //System.out.println("!! You have new [" + messages.length +"] messages");

        int length = messages.length-1;

        for (int i = length, n = (length-mailCount); i > n; i--) {
          Message message = messages[i];
          SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy-kk:mm:ss");

          if((""+message.getFrom()[0]).indexOf(filter)>=0){
            //System.out.println("Email Number " + (i - 1));
            System.out.println("Subject: " + message.getSubject());
            System.out.println("From: " + message.getFrom()[0]);
            System.out.println("Sent Date: " + formatter.format(message.getSentDate()));
            System.out.println("-----------------------\n");

            if((""+formatter.format(message.getSentDate())).indexOf(subjectFilter)>=0){
              try{
                Multipart mp = (Multipart) message.getContent();
                //BodyPart bp = mp.getBodyPart(0);
                System.out.println("=====================");
                //System.out.println("Text: " + bp.getContent());


                for (int j = 0; j < mp.getCount(); j++) {
                  BodyPart bodyPart = mp.getBodyPart(j);
                  String disposition = bodyPart.getDisposition();
                  if (disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) { 
                    System.out.println("Mail have some attachment");
                    DataHandler handler = bodyPart.getDataHandler();
                    System.out.println("file name : " + handler.getName());                                 
                  }else { 
                    //System.out.println("Text: " + bodyPart.getContent());
                    String result = getText(bodyPart);
                    result = result.replaceAll("<[^>]+>", "");
                    result = result.split("From")[0];
                    System.out.println("Text: " + result);
                  }
                }
                System.out.println("=====================");
              }catch(Exception e){
                System.out.println("!! Cannot convert multipart !!");
              }
            }
          }
        }
      }else{
         System.out.println("!! No new Message !!");
      }
      
        //close the store and folder objects
        emailFolder.close(false);
        store.close();

      } catch (NoSuchProviderException e) {
         e.printStackTrace();
      } catch (MessagingException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }


private static String getText(Part p) throws Exception{
        if (p.isMimeType("text/*")) {
            String s = (String)p.getContent();
            //textIsHtml = p.isMimeType("text/html");
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart)p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }


   public static void main(String[] args) {

      String host = "";// change accordingly
      String mailStoreType = "pop3";
      String username = "";// change accordingly
      String password = "";// change accordingly

      check(host, mailStoreType, username, password,Integer.parseInt(args[0]),args[1],args[2]);

   }

}
