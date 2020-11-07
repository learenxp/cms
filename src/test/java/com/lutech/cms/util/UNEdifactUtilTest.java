package com.lutech.cms.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.milyn.edi.unedifact.d95b.D95BInterchangeFactory;
import org.milyn.edi.unedifact.d95b.IFTMIN.Iftmin;
import org.milyn.edi.unedifact.d99b.D99BInterchangeFactory;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.smooks.edi.unedifact.model.r41.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.StringWriter;
import java.io.Writer;


@RunWith(SpringRunner.class)
@SpringBootTest
public class UNEdifactUtilTest {

    @Test
    public void ediToXml(){
        String messageOut = UNEdifactUtil.ediToXml("d99b-mapping","iftmin.txt");
        System.out.println(messageOut);
    }

    @Test
    public void ediToObject() throws Exception{
        UNEdifactMessage41 result = UNEdifactUtil.edifactToObject(D99BInterchangeFactory.getInstance(),"iftmin.txt");
        Iftmin iftmin = (Iftmin)result.getMessage();
    }

    @Test
    public void javaToEdi() throws Exception{
        /**
         * 本次例子是先把EDI报文转换成java Object再转换成EDI，真是情况java Object应该是由数据库数据组装而成。
         */
        UNEdifactMessage41 result = UNEdifactUtil.edifactToObject(D95BInterchangeFactory.getInstance(),"/Users/mac/Projects/edi/COPARN empty release replacement.edi");
        Iftmin iftmin = (Iftmin)result.getMessage();

        Writer writer = new StringWriter();
        Delimiters delimiters = new Delimiters();
        delimiters.setSegment("'");
        delimiters.setField("+");
        delimiters.setComponent(":");
        delimiters.setDecimalSeparator(".");

        //只转换了消息主体
        iftmin.write(writer,delimiters);
        String msg = writer.toString();
        System.out.println(msg);

        System.out.println("======================================\n");
        //包含报文的头部尾部
        System.out.println(UNEdifactUtil.toUNEdidact("发送港","接受港",D99BInterchangeFactory.getInstance(),iftmin));
        System.out.println("\n======================================");
    }
}