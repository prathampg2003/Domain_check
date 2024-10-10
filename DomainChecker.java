import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class DomainChecker {

    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            String domain;
            while ((domain = reader.readLine()) != null) {
                checkDomain(domain);
            }
        } catch (IOException e) {
            System.err.println("Error: Could not read input.");
        }
    }

    public static void checkDomain(String domain) {
        boolean hasMX = false;
        boolean hasSPF = false;
        boolean hasDMARC = false;
        String spfRecord = "";
        String dmarcRecord = "";

        try {
            // Check MX records
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
            DirContext dirContext = new InitialDirContext(env);

            Attributes mxAttrs = dirContext.getAttributes(domain, new String[]{"MX"});
            if (mxAttrs.get("MX") != null) {
                hasMX = true;
            }

            // Check SPF records
            Attributes txtAttrs = dirContext.getAttributes(domain, new String[]{"TXT"});
            if (txtAttrs.get("TXT") != null) {
                for (int i = 0; i < txtAttrs.get("TXT").size(); i++) {
                    String record = txtAttrs.get("TXT").get(i).toString();
                    if (record.startsWith("v=spf1")) {
                        hasSPF = true;
                        spfRecord = record;
                        break;
                    }
                }
            }

            // Check DMARC records
            String dmarcDomain = "_dmarc." + domain;
            Attributes dmarcAttrs = dirContext.getAttributes(dmarcDomain, new String[]{"TXT"});
            if (dmarcAttrs.get("TXT") != null) {
                for (int i = 0; i < dmarcAttrs.get("TXT").size(); i++) {
                    String record = dmarcAttrs.get("TXT").get(i).toString();
                    if (record.startsWith("v=DMARC1")) {
                        hasDMARC = true;
                        dmarcRecord = record;
                        break;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        // Output the results
        System.out.printf("%s,%s,%s,%s,%s,%s\n", domain, hasMX, hasSPF, spfRecord, hasDMARC, dmarcRecord);
    }
}
