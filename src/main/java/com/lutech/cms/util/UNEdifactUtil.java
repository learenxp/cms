package com.lutech.cms.util;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.milyn.Smooks;
import org.milyn.smooks.edi.unedifact.UNEdifactReaderConfigurator;
import org.milyn.smooks.edi.unedifact.model.UNEdifactInterchange;
import org.milyn.smooks.edi.unedifact.model.UNEdifactInterchangeFactory;
import org.milyn.smooks.edi.unedifact.model.r41.*;
import org.milyn.smooks.edi.unedifact.model.r41.types.DateTime;
import org.milyn.smooks.edi.unedifact.model.r41.types.MessageIdentifier;
import org.milyn.smooks.edi.unedifact.model.r41.types.Party;
import org.milyn.smooks.edi.unedifact.model.r41.types.SyntaxIdentifier;
import org.springframework.util.StringUtils;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UNEdifactUtil {

 private static String dateformat = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());

 /**
  * 组装Interchange Header 报文交换头部
  * @param senderId
  * @param recipientId
  * @param yymmddStr
  * @param hhmmStr
  * @param agreementNumber
  * @return
  */
 public static UNB41 assembleUNB(String senderId, String recipientId, String yymmddStr, String hhmmStr, String agreementNumber){
  UNB41 interchangeHeader = new UNB41();
  SyntaxIdentifier syntaxIdentifier = new SyntaxIdentifier();
  syntaxIdentifier.setId("UNOA");
  syntaxIdentifier.setVersionNum("2");
  interchangeHeader.setSyntaxIdentifier(syntaxIdentifier);

  Party sender = new Party();
  sender.setId(senderId);
  sender.setCodeQualifier("ZZZ");
  interchangeHeader.setSender(sender);

  Party recipient = new Party();
  recipient.setId(recipientId);
  recipient.setCodeQualifier("ZZZ");
  interchangeHeader.setRecipient(recipient);

  DateTime dateTime = new DateTime();
  dateTime.setDate(yymmddStr);
  dateTime.setTime(hhmmStr);
  interchangeHeader.setDate(dateTime);

  interchangeHeader.setControlRef(agreementNumber);
  return interchangeHeader;
 }

 /**
  * 组装 Interchange Trailer 报文交换尾部
  * @param agreementNumber
  * @return
  */
 public static UNZ41 assembleUNZ(String agreementNumber){
  UNZ41 interchangeTrailer = new UNZ41();
  interchangeTrailer.setControlCount(1);
  interchangeTrailer.setControlRef(agreementNumber);
  return interchangeTrailer;
 }

 /**
  * 组装Message Header 报文头部
  * @param agreementNumber
  * @return
  */
 public static UNH41 assembleUNH(String agreementNumber){
  UNH41 messageHeader = new UNH41();
  messageHeader.setMessageRefNum(agreementNumber);
  MessageIdentifier messageIdentifier = new MessageIdentifier();
  messageIdentifier.setId("IFTMIN");
  messageIdentifier.setControllingAgencyCode("UN");
  messageIdentifier.setVersionNum("D");
  messageIdentifier.setReleaseNum("99B");
  messageHeader.setMessageIdentifier(messageIdentifier);
  return messageHeader;
 }

 /**
  * 组装Message Trailer 报文尾部
  * @param segmentCount
  * @param agreementNumber
  * @return
  */
 public static UNT41 assembleUNT(int segmentCount, String agreementNumber){
  UNT41 messageTrailer = new UNT41();
  messageTrailer.setSegmentCount(segmentCount);
  messageTrailer.setMessageRefNum(agreementNumber);
  return messageTrailer;
 }

 /**
  * java(message)转EDI报文
  *
  * @param factory 工厂类型
  * @param message 报文内容
  * @return
  * @throws Exception
  */
 public static String toUNEdidact(String senderIdentification, String receiverIdentification, UNEdifactInterchangeFactory factory, Object message) throws IOException{
  Date date = new Date();
  String yymmddStr = new SimpleDateFormat("yyMMdd").format(date);
  String hhmmStr = new SimpleDateFormat("HHmm").format(date);
  int randomNumber =(int)(Math.random() * 10000);
  //文件交换号
  String agreementNumber = yymmddStr + hhmmStr + randomNumber;
  UNB41 interchangeHeader = assembleUNB(senderIdentification, receiverIdentification, yymmddStr, hhmmStr, agreementNumber);
  UNZ41 interchangeTrailer = assembleUNZ(agreementNumber);
  Integer line = 1;
  UNH41 messageHeader = assembleUNH(agreementNumber);
  UNT41 beforeMessageTrailer = assembleUNT(line, agreementNumber);
  String beforeMessage = javaToUNEdifact(factory, message,null, null, messageHeader, beforeMessageTrailer);
//  line = StringUtil.messageLine(beforeMessage);

  UNT41 afterMessageTrailer = assembleUNT(line, agreementNumber);
  return javaToUNEdifact(factory,message,interchangeHeader,interchangeTrailer,messageHeader,afterMessageTrailer);
 }


 /**
  *
  * @param factory 工厂类型
  * @param message 报文内容
  * @param interchangeHeader 报文头
  * @param interchangeTrailer 报文尾
  * @param messageHeader 消息头
  * @param messageTrailer 消息尾
  * @return 国际标准EDI报文
  * @throws IOException
  */
 public static String javaToUNEdifact(UNEdifactInterchangeFactory factory, Object message,
                                      UNB41 interchangeHeader, UNZ41 interchangeTrailer,
                                      UNH41 messageHeader, UNT41 messageTrailer) throws IOException {
  StringWriter ediOutStream = new StringWriter();

  UNEdifactInterchange41 interchange = new UNEdifactInterchange41();
  interchange.setInterchangeHeader(interchangeHeader);
  interchange.setInterchangeTrailer(interchangeTrailer);

  UNEdifactMessage41 unEdifactMessage41 = new UNEdifactMessage41();
  unEdifactMessage41.setMessage(message);
  unEdifactMessage41.setInterchangeHeader(interchangeHeader);
  unEdifactMessage41.setMessageHeader(messageHeader);
  unEdifactMessage41.setMessageTrailer(messageTrailer);
  List<UNEdifactMessage41> unEdifactMessage41List = new ArrayList<>();
  unEdifactMessage41List.add(unEdifactMessage41);
  interchange.setMessages(unEdifactMessage41List);

  factory.toUNEdifact(interchange, ediOutStream);
  return ediOutStream.toString();
 }

 /**
  * 国际标准的UN/EDIFact 转 xml
  *
  * @param mappingType 报文标准类型 例如：d99b-mapping
  * @param filePath 报文文件路径
  * @return
  */
 public static String ediToXml(String mappingType, String filePath){
  Smooks smooks = new Smooks();
  StringWriter writer = new StringWriter();
  try{
   smooks.setReaderConfig(new UNEdifactReaderConfigurator("urn:org.milyn.edi.unedifact:"+mappingType+":*"));
   smooks.filterSource(new StreamSource(new FileInputStream(filePath)), new StreamResult(writer));
   return writer.toString();
  }catch (Exception e){
   e.printStackTrace();
   return null;
  }finally {
   smooks.close();
   IOUtils.closeQuietly(writer);
  }
 }


 /**
  * 国际标准的UN/EDIFact 转 Object
  *
  * @param factory 工厂类型 例如：D93AInterchangeFactory
  * @param filePath 报文路径
  * @return
  * @throws IOException
  */
 public static UNEdifactMessage41 edifactToObject(UNEdifactInterchangeFactory factory, String filePath) throws IOException {
  InputStream stream = new FileInputStream(filePath);
  UNEdifactInterchange interchange = factory.fromUNEdifact(stream);
  if (interchange instanceof UNEdifactInterchange41) {
   UNEdifactInterchange41 interchange41 = (UNEdifactInterchange41) interchange;
   List<UNEdifactMessage41> message41List = interchange41.getMessages();
   if(message41List.size()>0){
    return message41List.get(0);
   }
  }
  return null;
 }

 /**
  * 计算报文有多少行
  *
  * @param message 报文内容
  * @return
  */
 public static Integer messageLine(String message) {
  if (StringUtils.isEmpty(message)) {
   return 0;
  }
  message = message.trim();
  String[] array = message.split("'");
  String[] array2 = message.split("\\?'");
  if (array2.length > 0) {
   return array.length - array2.length + 1;
  } else {
   return array.length;
  }
 }
}