package ru.rashgild.interceptor;

import org.apache.log4j.Logger;

import ru.rashgild.utils.GlobalVariables;
import ru.rashgild.utils.SQL;
import ru.rashgild.utils.XmlUtils;
import ru.rashgild.service.LnDate_start;
import ru.rashgild.service.DisableLn;
import ru.rashgild.service.ExistingLNNumRange;
import ru.rashgild.service.NewLNNum;
import ru.rashgild.service.NewLnNumRange;
import ru.rashgild.service.PrParseFileLnLpu;
import ru.rashgild.signAndCrypt.VerifyAndDecrypt;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

import static ru.rashgild.utils.XmlUtils.stringToSoap;

public class Injecter implements SOAPHandler<SOAPMessageContext> {

    @Override
    public boolean handleMessage(SOAPMessageContext context) {

        Logger logger = Logger.getLogger(Injecter.class);
        Boolean isRequest = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        try {
            if (isRequest) {
                logger.info("2) Intercept request!");
                SOAPMessage soapMsg = context.getMessage();

                if (whatTheFunc(soapMsg) == 1) {
                    logger.info("2.1) initialized PrParseFileLnLpu_start!");
                    soapMsg = PrParseFileLnLpu.start(GlobalVariables.requestParam);
                }
                if (whatTheFunc(soapMsg) == 2) {
                    logger.info("2.1) initialized NewLNNumRange_start!");
                    soapMsg = NewLnNumRange.Start(soapMsg);
                }

                if (whatTheFunc(soapMsg) == 3) {
                    logger.info("2.1) initialized NewLNNum_start!");
                    soapMsg = NewLNNum.Start(soapMsg);
                }

                if (whatTheFunc(soapMsg) == 4) {
                    logger.info("2.1) initialized getLNDate!");
                    soapMsg = LnDate_start.Start(soapMsg);
                }

                if (whatTheFunc(soapMsg) == 5) {
                    logger.info("2.1) initialized disableLn!");
                    soapMsg = DisableLn.Start(soapMsg);
                }

                if (whatTheFunc(soapMsg) == 6) {
                    logger.info("2.1) initialized ExistingLNNumRange!");
                    soapMsg = ExistingLNNumRange.Start(soapMsg);
                }

                if (whatTheFunc(soapMsg) == 7) {
                    logger.info("2.1) INITIALIZETED NEW SEND!");
                    soapMsg = stringToSoap(GlobalVariables.Request);
                }

                logger.info("Send Request!");
                context.setMessage(soapMsg);
            }

            if (!isRequest) {
                logger.info("Get Response");
                try {
                    SOAPMessage msg = context.getMessage();
                    msg = VerifyAndDecrypt.Start(msg);
                    GlobalVariables.Response = XmlUtils.soapMessageToString(msg);
                    GlobalVariables.returningXml = XmlUtils.soapMessageToString(msg);
                    context.setMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        } catch (Exception e) {
            SQL.saveInBaseDate("ErrorInSending", 0);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        SOAPMessage msg = context.getMessage();
        Logger logger = Logger.getLogger("simple");
        logger.error("fail send");
        logger.info(XmlUtils.soapMessageToString(msg));
        return true;
    }

    @Override
    public void close(MessageContext context) {
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    private static int whatTheFunc(SOAPMessage msg) {

        try {
            String strDoc = XmlUtils.docToString(msg.getSOAPPart().getEnvelope().getOwnerDocument());
            if (strDoc.contains("prParseFilelnlpu")) {
                GlobalVariables.Type = "prParseFilelnlpu";

                if (strDoc.contains("ThisIsNewSender")) {
                    return 7;
                }
                return 1;
            }
            if (strDoc.contains("getNewLNNumRange")) {
                GlobalVariables.Type = "getNewLNNumRange";
                return 2;
            }
            if (strDoc.contains("getNewLNNum")) {
                GlobalVariables.Type = "getNewLNNum";
                return 3;
            }
            if (strDoc.contains("getLNData")) {
                GlobalVariables.Type = "getLNData";
                return 4;
            }
            if (strDoc.contains("disableLn")) {
                GlobalVariables.Type = "disableLn";
                return 5;
            }
            if (strDoc.contains("ExistingLNNumRange")) {
                GlobalVariables.Type = "ExistingLNNumRange";
                return 6;
            }

        } catch (SOAPException e) {
            e.printStackTrace();
        }
        return 0;
    }
}