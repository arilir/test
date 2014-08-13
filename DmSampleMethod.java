import java.io.OutputStream;
import java.util.*;
import java.lang.*;
import com.documentum.mthdservlet.IDmMethod;
import com.documentum.fc.common.*;
import com.documentum.fc.client.*;

/**
 * This class is a simple sample that demonstrates how to implement 
 * the IDmMethod interface.
 *
 * This class expects to be called with a parameter Map containing
 * parameter names as keys of type String, and parameter values
 * of type String array.
 *
 * KEY          VALUE
 * docbase      <docbase_name>
 * user         <user_name>
 * ticket       <login_ticket>
 * id           <r_object_id>
 *
 *
 * The user credentials and r_object_id will be used to obtain the attributes
 * or meta-data of the docbase object specified by the r_object_id.  These
 * attributes are returned as an XML file.
 *
 * If the DO_METHOD apply method was launched asynchronously, no data
 * can be returned.  The OutputStream provided to the execute() method
 * will be null.  No attributes will be returned in this case.
 */
public class DmSampleMethod implements IDmMethod
{
    private static final String XML_PROLOG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
    private static final String XML_ELEMENT_START = "<dctm-object>\r\n";
    private static final String XML_ELEMENT_END = "</dctm-object>\r\n";

    public void execute(Map parameters, OutputStream output) throws Exception
    {
        // initialize parameters
        String docbase = null;
        String user = null;
        String ticket = null;
        String id = null;
        IDfSession session = null;

        // extract parameter values
        Iterator i = parameters.keySet().iterator();
        while ( i.hasNext() )
        {
            String key = (String) i.next();
            String[] values = (String[]) parameters.get(key);

            if((key == null) || (key.length() == 0) ||
               (values == null) || (values.length < 1))
            {
                continue;
            }

            if (key.equalsIgnoreCase("docbase")) {
                docbase = values[0];
            }
            else if (key.equalsIgnoreCase("user")) {
                user = values[0];
            }
            else if (key.equalsIgnoreCase("ticket")) {
                ticket = values[0];
            }
            else if (key.equalsIgnoreCase("id")) {
                id = values[0];
            }
        }
        
        // validate parameter values
        validateParam( "docbase", docbase );
        validateParam( "user", user );
        validateParam( "ticket", ticket );
        validateParam( "id", id );
            
        // Connect to docbase with given credentials
        IDfClient client = DfClient.getLocalClient();
        DfLoginInfo loginInfo = new DfLoginInfo();
        loginInfo.setUser(user);
        loginInfo.setPassword(ticket);
        session = client.newSession(docbase, loginInfo);

        // Retrieve the document object
        String queryStr = "dm_sysobject where r_object_id = '" + id + "'";
        IDfSysObject sysObj = (IDfSysObject) session.getObjectByQualification(queryStr);
        if (sysObj == null)
        {
            throw new Exception( "DmSampleMethod failed to retrieve dm_sysobject with r_object_id '" +
                        id + "' from docbase '" + docbase + "'." );
        }
        
        if(output != null) {
            output.write(XML_PROLOG.getBytes());
            output.write(XML_ELEMENT_START.getBytes());
            String xmlAttributes = sysObj.getString("_xml_string");
            output.write(xmlAttributes.getBytes());
            output.write(XML_ELEMENT_END.getBytes());
        }
	    session.disconnect();
    }

    private void validateParam(String parameter, String value) throws Exception
    {
        if((value == null) || (value.length() == 0)) {
            throw new Exception("Non-null value for parameter -" + parameter + 
                    " which is required for DmSampleMethod, was not defined in ARGUMENTS.");
        }
    }
}
