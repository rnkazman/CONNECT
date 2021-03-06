package gov.hhs.fha.nhinc.admingui.managed.direct.helpers;

import java.io.ByteArrayInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.x500.X500Principal;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author cmay
 */
public class CertContainer {
    private X509Certificate cert;
    private Key key;
    private static final Logger LOG = Logger.getLogger(CertContainer.class);

    /**
     *
     * @param cert
     * @param key
     */
    public CertContainer(X509Certificate cert, Key key) {
        this.cert = cert;
        this.key = key;
    }

    /**
     *
     * @param data
     */
    public CertContainer(byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);

        // Trying a as a PKCS12 data stream
        try {
            KeyStore localKeyStore = KeyStore.getInstance("PKCS12", "BC");

            localKeyStore.load(bais, "".toCharArray());
            Enumeration<String> aliases = localKeyStore.aliases();

            // we are really expecting only one alias
            if (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate cert = (X509Certificate) localKeyStore.getCertificate(alias);

                // check if there is private key
                Key key = localKeyStore.getKey(alias, "".toCharArray());
                if (key != null && key instanceof PrivateKey) {
                    this.cert = cert;
                    this.key = key;
                }
            }
        } catch (Exception e) {
            LOG.warn("Cert data is not a PKCS12 stream, trying next option...");
        }

        // Trying as an X.509 certificate
        if (cert == null) {
            bais.reset();
            bais = new ByteArrayInputStream(data);

            try {
                X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(
                        bais);

                this.cert = cert;
                this.key = null;
            } catch (Exception e) {
                LOG.warn("Cert data cannot be converted to X.509, trying next option...");
            }
        }

        if (cert == null) {
            LOG.error("Could not parse cert data");
        }

        try {
            bais.close();
        } catch (Exception e) {
            LOG.error("Could not close input stream.");
        }
    }

    /**
     *
     * @return
     */
    public X509Certificate getCert() {
        return cert;
    }

    /**
     *
     * @return
     */
    public Key getKey() {
        return key;
    }

    /**
     *
     * @return
     */
    public String getTrustedEntityName() {
        X500Principal prin = cert.getSubjectX500Principal();

        // check the CN attribute first
        // get the domain name
        Map<String, String> oidMap = new HashMap<String, String>();
        oidMap.put("1.2.840.113549.1.9.1", "EMAILADDRESS"); // OID for email address
        String prinName = prin.getName(X500Principal.RFC1779, oidMap);

        String searchString = "CN=";
        int index = prinName.indexOf(searchString);
        if (index == -1) {
            searchString = "EMAILADDRESS=";
            // fall back to email
            index = prinName.indexOf(searchString);
            if (index == -1) {
                return ""; // no CN... nothing else that can be done from here
            }
        }

        // look for a "," to find the end of this attribute
        int endIndex = prinName.indexOf(",", index);
        String address;
        if (endIndex > -1) {
            address = prinName.substring(index + searchString.length(), endIndex);
        } else {
            address = prinName.substring(index + searchString.length());
        }

        return address;
    }
    
    /**
     *
     * @return
     */
    public String getThumbprint() {
        String thumbprint;
        
        try {
            thumbprint = DigestUtils.sha1Hex(cert.getEncoded());
        } catch (CertificateEncodingException e) {
            thumbprint = null;
        }
        
        return thumbprint;
    }
}