package org.asynchttpclient.spnego;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.util.Base64;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import java.io.IOException;

/**
 * SPNEGO (Simple and Protected GSSAPI Negotiation Mechanism) authentication scheme.
 */
public class SpnegoEngine {

    private final static Logger logger = LogManager.getLogger(SpnegoEngine.class.getName());

    private static final String SPNEGO_OID = "1.3.6.1.5.5.2";

    private static final String KERBEROS_OID = "1.2.840.113554.1.2.2";

    private final SpnegoTokenGenerator spnegoGenerator;

    public SpnegoEngine(final SpnegoTokenGenerator spnegoGenerator) {
        this.spnegoGenerator = spnegoGenerator;
    }

    public SpnegoEngine() {
        this(null);
    }

    private static SpnegoEngine instance;

    public static SpnegoEngine instance() {
        if (instance == null)
            instance = new SpnegoEngine();
        return instance;
    }

    public String generateToken(String server) throws Throwable {
        GSSContext gssContext = null;
        byte[] token = null; // base64 decoded challenge
        Oid negotiationOid = null;

        try {
            logger.debug("init {}", server);
            /*
             * Using the SPNEGO OID is the correct method. Kerberos v5 works for IIS but not JBoss. Unwrapping the initial token when using SPNEGO OID looks like what is described
             * here...
             * 
             * http://msdn.microsoft.com/en-us/library/ms995330.aspx
             * 
             * Another helpful URL...
             * 
             * http://publib.boulder.ibm.com/infocenter/wasinfo/v7r0/index.jsp?topic=/com.ibm.websphere.express.doc/info/exp/ae/tsec_SPNEGO_token.html
             * 
             * Unfortunately SPNEGO is JRE >=1.6.
             */

            /** Try SPNEGO by default, fall back to Kerberos later if error */
            negotiationOid = new Oid(SPNEGO_OID);

            boolean tryKerberos = false;
            try {
                GSSManager manager = GSSManager.getInstance();
                GSSName serverName = manager.createName("HTTP@" + server, GSSName.NT_HOSTBASED_SERVICE);
                gssContext = manager.createContext(serverName.canonicalize(negotiationOid), negotiationOid, null, GSSContext.DEFAULT_LIFETIME);
                gssContext.requestMutualAuth(true);
                gssContext.requestCredDeleg(true);
            } catch (GSSException ex) {
                logger.error("generateToken", ex);
                // BAD MECH means we are likely to be using 1.5, fall back to Kerberos MECH.
                // Rethrow any other exception.
                if (ex.getMajor() == GSSException.BAD_MECH) {
                    logger.debug("GSSException BAD_MECH, retry with Kerberos MECH");
                    tryKerberos = true;
                } else {
                    throw ex;
                }

            }
            if (tryKerberos) {
                /* Kerberos v5 GSS-API mechanism defined in RFC 1964. */
                logger.debug("Using Kerberos MECH {}", KERBEROS_OID);
                negotiationOid = new Oid(KERBEROS_OID);
                GSSManager manager = GSSManager.getInstance();
                GSSName serverName = manager.createName("HTTP@" + server, GSSName.NT_HOSTBASED_SERVICE);
                gssContext = manager.createContext(serverName.canonicalize(negotiationOid), negotiationOid, null, GSSContext.DEFAULT_LIFETIME);
                gssContext.requestMutualAuth(true);
                gssContext.requestCredDeleg(true);
            }

            // TODO suspicious: this will always be null because no value has been assigned before. Assign directly?
            if (token == null) {
                token = new byte[0];
            }

            token = gssContext.initSecContext(token, 0, token.length);
            if (token == null) {
                throw new Exception("GSS security context initialization failed");
            }

            /*
             * IIS accepts Kerberos and SPNEGO tokens. Some other servers Jboss, Glassfish? seem to only accept SPNEGO. Below wraps Kerberos into SPNEGO token.
             */
            if (spnegoGenerator != null && negotiationOid.toString().equals(KERBEROS_OID)) {
                token = spnegoGenerator.generateSpnegoDERObject(token);
            }

            gssContext.dispose();

            String tokenstr = new String(Base64.encode(token));
            logger.debug("Sending response '{}' back to the server", tokenstr);

            return tokenstr;
        } catch (GSSException gsse) {
            logger.error("generateToken", gsse);
            if (gsse.getMajor() == GSSException.DEFECTIVE_CREDENTIAL || gsse.getMajor() == GSSException.CREDENTIALS_EXPIRED)
                throw new Exception(gsse.getMessage(), gsse);
            if (gsse.getMajor() == GSSException.NO_CRED)
                throw new Exception(gsse.getMessage(), gsse);
            if (gsse.getMajor() == GSSException.DEFECTIVE_TOKEN || gsse.getMajor() == GSSException.DUPLICATE_TOKEN || gsse.getMajor() == GSSException.OLD_TOKEN)
                throw new Exception(gsse.getMessage(), gsse);
            // other error
            throw new Exception(gsse.getMessage());
        } catch (IOException ex) {
            throw new Exception(ex.getMessage());
        }
    }
}
