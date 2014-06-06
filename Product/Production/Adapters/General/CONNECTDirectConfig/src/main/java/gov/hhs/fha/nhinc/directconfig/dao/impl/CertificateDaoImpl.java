/*
 * Copyright (c) 2009-2014, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
Copyright (c) 2010, NHIN Direct Project
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the distribution.
3. Neither the name of the The NHIN Direct Project (nhindirect.org) nor the names of its contributors may be used to endorse or promote
products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
THE POSSIBILITY OF SUCH DAMAGE.
 */

package gov.hhs.fha.nhinc.directconfig.dao.impl;

import gov.hhs.fha.nhinc.directconfig.dao.CertificateDao;
import gov.hhs.fha.nhinc.directconfig.entity.Certificate;
import gov.hhs.fha.nhinc.directconfig.entity.Certificate.CertContainer;
import gov.hhs.fha.nhinc.directconfig.entity.helpers.EntityStatus;
import gov.hhs.fha.nhinc.directconfig.exception.CertificateException;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementing class for Certificate DAO methods.
 * 
 * @author ppyette
 */
@Repository
public class CertificateDaoImpl implements CertificateDao {

    @Autowired
    protected SessionFactory sessionFactory;

    private static final Log log = LogFactory.getLog(CertificateDaoImpl.class);

    /*
     * (non-Javadoc)
     * 
     * @see gov.hhs.fha.nhinc.directconfig.dao.CertificateDao#load(java.lang.String, java.lang.String)
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Transactional(readOnly = true)
    public Certificate load(String owner, String thumbprint) {
        log.debug("Enter");

        List<Certificate> result = null;
        Query select = null;

        if (owner == null && thumbprint == null) {
            select = sessionFactory.getCurrentSession().createQuery("SELECT c from Certificate c");
        } else if (owner != null && thumbprint == null) {
            select = sessionFactory.getCurrentSession().createQuery(
                    "SELECT c from Certificate c WHERE UPPER(c.owner) = ?");
            select.setParameter(0, owner.toUpperCase(Locale.getDefault()));
        } else if (owner == null && thumbprint != null) {
            select = sessionFactory.getCurrentSession().createQuery(
                    "SELECT c from Certificate c WHERE c.thumbprint = ?");
            select.setParameter(0, thumbprint);
        } else {
            select = sessionFactory.getCurrentSession().createQuery(
                    "SELECT c from Certificate c WHERE c.thumbprint = ? and UPPER(c.owner) = ?");
            select.setParameter(0, thumbprint);
            select.setParameter(1, owner.toUpperCase(Locale.getDefault()));
        }

        List rs = select.list();
        if ((rs.size() != 0) && (rs.get(0) instanceof Certificate)) {
            result = rs;
        } else {
            return null;
        }

        log.debug("Exit");

        return result.iterator().next();
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.hhs.fha.nhinc.directconfig.dao.CertificateDao#list(java.util.List)
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Transactional(readOnly = true)
    public List<Certificate> list(List<Long> idList) {
        log.debug("Enter");

        if (idList == null || idList.size() == 0) {
            return Collections.emptyList();
        }

        List<Certificate> result = Collections.emptyList();

        Query select = null;
        StringBuffer ids = new StringBuffer("(");
        for (Long id : idList) {
            if (ids.length() > 1) {
                ids.append(", ");
            }

            ids.append(id);
        }

        ids.append(")");
        String query = "SELECT c from Certificate c WHERE c.id IN " + ids.toString();

        select = sessionFactory.getCurrentSession().createQuery(query);

        List rs = select.list();
        if (rs != null && (rs.size() != 0) && (rs.get(0) instanceof Certificate)) {
            result = rs;
        }

        log.debug("Exit");

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.hhs.fha.nhinc.directconfig.dao.CertificateDao#list(java.lang.String)
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Transactional(readOnly = true)
    public List<Certificate> list(String owner) {
        log.debug("Enter");

        List<Certificate> result = Collections.emptyList();
        Query select = null;
        if (owner == null) {
            select = sessionFactory.getCurrentSession().createQuery("SELECT c from Certificate c");
        } else if (owner != null) {
            select = sessionFactory.getCurrentSession().createQuery(
                    "SELECT c from Certificate c WHERE UPPER(c.owner) = ?");
            select.setParameter(0, owner.toUpperCase(Locale.getDefault()));
        }

        List rs = select.list();
        if ((rs.size() != 0) && (rs.get(0) instanceof Certificate)) {
            result = rs;
        }

        log.debug("Exit");

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.hhs.fha.nhinc.directconfig.dao.CertificateDao#save(gov.hhs.fha.nhinc.directconfig.entity.Certificate)
     */
    @Override
    @Transactional(readOnly = false)
    public void save(Certificate cert) {
        save(Arrays.asList(cert));
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.hhs.fha.nhinc.directconfig.dao.CertificateDao#save(java.util.List)
     */
    @Override
    @Transactional(readOnly = false)
    public void save(List<Certificate> certList) {
        log.debug("Enter");

        if (certList != null && certList.size() > 0) {
            for (Certificate cert : certList) {
                cert.setId(null);
                cert.setCreateTime(Calendar.getInstance());

                try {
                    CertContainer container = null;
                    X509Certificate xcert = null;

                    try {
                        container = cert.toCredential();
                        xcert = container.getCert();
                    } catch (CertificateException e) {
                        // probably not a certificate but an IPKIX URL
                    }

                    if (cert.getValidStartDate() == null && xcert != null) {
                        Calendar startDate = Calendar.getInstance();
                        startDate.setTime(xcert.getNotBefore());
                        cert.setValidStartDate(startDate);
                    }

                    if (cert.getValidEndDate() == null && xcert != null) {
                        Calendar endDate = Calendar.getInstance();
                        endDate.setTime(xcert.getNotAfter());
                        cert.setValidEndDate(endDate);
                    }

                    if (cert.getStatus() == null) {
                        cert.setStatus(EntityStatus.NEW);
                    }

                    cert.setPrivateKey(container != null && container.getKey() != null);
                } catch (CertificateException e) {

                }

                log.debug("Calling JPA to persist the Certificate");

                sessionFactory.getCurrentSession().persist(cert);

                log.debug("Returned from JPA: Certificate ID=" + cert.getId());
            }
        }

        log.debug("Exit");
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.hhs.fha.nhinc.directconfig.dao.CertificateDao#setStatus(java.util.List,
     * gov.hhs.fha.nhinc.directconfig.entity.EntityStatus)
     */
    @Override
    @Transactional(readOnly = false)
    public void setStatus(List<Long> certificateIDs, EntityStatus status) {
        log.debug("Enter");

        List<Certificate> certs = this.list(certificateIDs);

        if (certs == null || certs.size() == 0) {
            return;
        }

        for (Certificate cert : certs) {
            cert.setStatus(status);
            sessionFactory.getCurrentSession().merge(cert);
        }

        log.debug("Exit");
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.hhs.fha.nhinc.directconfig.dao.CertificateDao#setStatus(java.lang.String,
     * gov.hhs.fha.nhinc.directconfig.entity.EntityStatus)
     */
    @Override
    @Transactional(readOnly = false)
    public void setStatus(String owner, EntityStatus status) {
        log.debug("Enter");

        List<Certificate> certs = list(owner);

        if (certs == null || certs.size() == 0) {
            return;
        }

        for (Certificate cert : certs) {
            cert.setStatus(status);
            sessionFactory.getCurrentSession().merge(cert);
        }

        log.debug("Exit");
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.hhs.fha.nhinc.directconfig.dao.CertificateDao#delete(java.util.List)
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(List<Long> idList) {
        log.debug("Enter");

        if (idList != null && idList.size() > 0) {
            StringBuffer ids = new StringBuffer("(");

            for (Long id : idList) {
                if (ids.length() > 1) {
                    ids.append(", ");
                }

                ids.append(id);
            }

            ids.append(")");
            String query = "DELETE FROM Certificate c WHERE c.id IN " + ids.toString();

            int count = 0;
            Query delete = sessionFactory.getCurrentSession().createQuery(query);
            count = delete.executeUpdate();

            log.debug("Exit: " + count + " certificate records deleted");
        }

        log.debug("Exit");
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.hhs.fha.nhinc.directconfig.dao.CertificateDao#delete(java.lang.String)
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(String owner) {
        log.debug("Enter");

        if (owner == null) {
            return;
        }

        int count = 0;
        if (owner != null) {
            Query delete = sessionFactory.getCurrentSession().createQuery(
                    "DELETE FROM Certificate c WHERE UPPER(c.owner) = ?");
            delete.setParameter(0, owner.toUpperCase(Locale.getDefault()));
            count = delete.executeUpdate();
        }

        log.debug("Exit: " + count + " certificate records deleted");
    }
}