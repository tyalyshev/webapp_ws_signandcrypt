package ru.rashgild.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import ru.rashgild.generated.v1.fileoperationsln.ws.FileOperationsLn;
import ru.rashgild.generated.v1.fileoperationsln.ws.FileOperationsLnImplService;
import ru.rashgild.generated.v1.fileoperationsln.ws.FileOperationsLnUserGetNewLNNumRangeOut;
import ru.rashgild.generated.v1.fileoperationsln.ws.SOAPException_Exception;
import ru.rashgild.utils.GlobalVariables;

import static ru.rashgild.utils.SQL.sqlUpdIns;
import static ru.rashgild.utils.StoredQuery.SaveNumber;

@WebServlet("/sNewLnNumRange")
public class sNewLnNumRange extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Logger logger = Logger.getLogger("simple");
        logger.info("1) NewLnNumRange");
        response.setContentType("text/html ;charset=UTF-8");

        String ogrn = request.getParameter("ogrn");
        Integer count = Integer.valueOf(request.getParameter("count"));

        GlobalVariables.requestParam = ogrn;
        GlobalVariables.requestParam2 = count;

        PrintWriter out = response.getWriter();

        out.println("<html>");
        out.println("<head>" +
                "  <meta charset=\"UTF-8\" />\n" +
                "  <title>SignAndCrypt</title>\n" +
                "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"> </head>");

        out.println("<body>");
        out.print("<H1> ogrn=" + ogrn + "</H1>");
        out.print("<H1> count=" + count + "</H1>");

        System.setProperty("javax.net.ssl.trustStore", GlobalVariables.pathandnameSSL);//КОНФ
        System.setProperty("javax.net.ssl.trustStorePassword", GlobalVariables.passwordSSL);

        FileOperationsLnImplService service = new FileOperationsLnImplService();
        FileOperationsLn start = service.getFileOperationsLnPort();
        try {
            FileOperationsLnUserGetNewLNNumRangeOut Num = start.getNewLNNumRange(ogrn, count);

            out.println("<H1>" + Num.getMESS() + "</H1>");
            List<String> data = Num.getDATA().getLNNum();
            out.println("<H1> Получены номера: </H1>");
            for (int i = 0; i < data.size(); i++) {
                out.println("<H1>" + i + ") " + data.get(i) + "</H1>");
                sqlUpdIns(SaveNumber(data.get(i)));
            }

        } catch (SOAPException_Exception e) {
            e.printStackTrace();
        }
        out.println("</body>");
        out.println("</html>");
    }
}
