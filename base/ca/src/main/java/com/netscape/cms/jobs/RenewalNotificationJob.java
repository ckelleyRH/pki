// --- BEGIN COPYRIGHT BLOCK ---
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; version 2 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
// (C) 2007 Red Hat, Inc.
// All rights reserved.
// --- END COPYRIGHT BLOCK ---
package com.netscape.cms.jobs;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;

import org.dogtagpki.server.ca.CAEngine;
import org.dogtagpki.server.ca.CAEngineConfig;

import com.netscape.certsrv.base.EBaseException;
import com.netscape.certsrv.base.IExtendedPluginInfo;
import com.netscape.certsrv.base.MetaInfo;
import com.netscape.certsrv.notification.ENotificationException;
import com.netscape.certsrv.notification.EmailResolver;
import com.netscape.certsrv.request.RequestId;
import com.netscape.cms.notification.MailNotification;
import com.netscape.cmscore.apps.CMS;
import com.netscape.cmscore.apps.CMSEngine;
import com.netscape.cmscore.base.ConfigStore;
import com.netscape.cmscore.dbs.CertRecord;
import com.netscape.cmscore.dbs.CertificateRepository;
import com.netscape.cmscore.dbs.ElementProcessor;
import com.netscape.cmscore.jobs.JobConfig;
import com.netscape.cmscore.jobs.JobsScheduler;
import com.netscape.cmscore.notification.EmailFormProcessor;
import com.netscape.cmscore.notification.EmailResolverKeys;
import com.netscape.cmscore.notification.ReqCertSANameEmailResolver;
import com.netscape.cmscore.request.Request;

/**
 * A job for the Jobs Scheduler. This job checks in the internal ldap
 * db for certs about to expire within the next configurable days and
 * sends email notifications to the appropriate recipients.
 *
 * the $TOKENS that are available for the this jobs's summary outer form are:<br
 >
 * <UL>
 * <LI>$Status
 * <LI>$InstanceID
 * <LI>$SummaryItemList
 * <LI>$SummaryTotalNum
 * <LI>$SummaryTotalSuccess
 * <LI>$SummaryTotalfailure
 * <LI>$ExecutionTime
 * </UL>
 * and for the inner list items:
 * <UL>
 * <LI>$SerialNumber
 * <LI>$IssuerDN
 * <LI>$SubjectDN
 * <LI>$NotAfter
 * <LI>$NotBefore
 * <LI>$RequestorEmail
 * <LI>$CertType
 * <LI>$RequestType
 * <LI>$HttpHost
 * <LI>$HttpPort
 * </UL>
 *
 * @version $Revision$, $Date$
 * @see com.netscape.cms.jobs.Job
 */
public class RenewalNotificationJob
        extends Job
        implements IExtendedPluginInfo {

    /**
     * Profile ID specifies which profile approves the certificate.
     */
    private static final String PROFILE_ID = "profileId";

    /**
     * This job will send notification at this much time before the
     * expiration date
     */
    private static final String NOTIFY_TRIGGER_OFFSET = "notifyTriggerOffset";

    /**
     * This job will stop sending notification this much time after
     * the expiration date
     */
    private static final String NOTIFY_END_OFFSET = "notifyEndOffset";

    /**
     * sender email address as appeared on the notification email
     */
    private static final String SENDER_EMAIL = "senderEmail";

    /**
     * email subject line as appeared on the notification email
     */
    private static final String EMAIL_SUBJECT = "emailSubject";

    /**
     * location of the template file used for email notification
     */
    private static final String EMAIL_TEMPLATE = "emailTemplate";

    /*
     * Holds configuration parameters accepted by this implementation.
     * This list is passed to the configuration console so configuration
     * for instances of this implementation can be configured through the
     * console.
     */
    protected static String[] mConfigParams =
            new String[] {
                    ENABLED,
                    CRON,
                    PROFILE_ID,
                    NOTIFY_TRIGGER_OFFSET,
                    NOTIFY_END_OFFSET,
                    SENDER_EMAIL,
                    EMAIL_SUBJECT,
                    EMAIL_TEMPLATE,
                    SUMMARY_ENABLED,
                    SUMMARY_RECIPIENT_EMAIL,
                    SUMMARY_SENDER_EMAIL,
                    SUMMARY_EMAIL_SUBJECT,
                    SUMMARY_ITEM_TEMPLATE,
                    SUMMARY_EMAIL_TEMPLATE,
        };

    protected CertificateRepository mCertDB;
    protected boolean mSummary = false;
    protected String mEmailSender = null;
    protected String mEmailSubject = null;
    protected String mEmailTemplateName = null;
    protected String mSummaryItemTemplateName = null;
    protected String mSummaryTemplateName = null;
    protected boolean mSummaryHTML = false;
    protected boolean mHTML = false;

    protected String mHttpHost = null;
    protected String mHttpPort = null;

    private int mPreDays = 0;
    private long mPreMS = 0;
    private int mPostDays = 0;
    private long mPostMS = 0;
    private String[] mProfileId = null;

    /**
     * class constructor
     */
    public RenewalNotificationJob() {
    }

    /**
     * holds help text for this plugin
     */
    @Override
    public String[] getExtendedPluginInfo() {
        String s[] = {
                IExtendedPluginInfo.HELP_TEXT +
                        "; A job that checks for expiring or expired certs" +
                        "notifyTriggerOffset before and notifyEndOffset after " +
                        "the expiration date",

                PROFILE_ID + ";string;Specify the ID of the profile which " +
                        "approved the certificates that are about to expire. For multiple " +
                        "profiles, each entry is separated by white space. For example, " +
                        "if the administrator just wants to give automated notification " +
                        "when the SSL server certificates are about to expire, then " +
                        "he should enter \"caServerCert caAgentServerCert\" in the profileId textfield. " +
                        "Blank field means all profiles.",
                NOTIFY_TRIGGER_OFFSET + ";number,required;How long (in days) before " +
                        "certificate expiration will the first notification " +
                        "be sent",
                NOTIFY_END_OFFSET + ";number,required;How long (in days) after " +
                        "certificate expiration will notifications " +
                        "continue to be resent if certificate is not renewed",
                CRON + ";string,required;Format: minute hour dayOfMonth Mmonth " +
                        "dayOfWeek. Use '*' for 'every'. For dayOfWeek, 0 is Sunday",
                SENDER_EMAIL + ";string,required;Specify the address to be used " +
                        "as the email's 'sender'. Bounces go to this address.",
                EMAIL_SUBJECT + ";string,required;Email subject",
                EMAIL_TEMPLATE + ";string,required;Fully qualified pathname of " +
                        "template file of email to be sent",
                "enabled;boolean;Enable this plugin",
                "summary.enabled;boolean;Enabled sending of summaries",
                SUMMARY_SENDER_EMAIL + ";string,required;Sender email address of summary",
                SUMMARY_RECIPIENT_EMAIL + ";string,required;Who should receive summaries",
                SUMMARY_EMAIL_SUBJECT + ";string,required;Subject of summary email",
                SUMMARY_EMAIL_TEMPLATE + ";string,required;Fully qualified pathname of " +
                        "template file of email to be sent",
                SUMMARY_ITEM_TEMPLATE + ";string,required;Fully qualified pathname of " +
                        "file with template to be used for each summary item",
                IExtendedPluginInfo.HELP_TOKEN +
                        ";configuration-jobrules-renewalnotification",
            };

        return s;
    }

    /**
     * Initialize from the configuration file.
     *
     * @param id String name of this instance
     * @param implName string name of this implementation
     * @param config configuration store for this instance
     * @exception EBaseException
     */
    @Override
    public void init(JobsScheduler scheduler, String id, String implName, JobConfig config) throws EBaseException {

        super.init(scheduler, id, implName, config);

        CAEngine caEngine = (CAEngine) engine;
        mCertDB = caEngine.getCertificateRepository();
    }

    /**
     * finds out which cert needs notification and notifies the
     * responsible parties
     */
    @Override
    public void run() {
        CAEngine caEngine = (CAEngine) engine;
        CAEngineConfig cs = caEngine.getConfig();
        try {
            // for forming renewal URL at template
            mHttpHost = cs.getHostname();
            mHttpPort = caEngine.getEESSLPort();

            // read from the configuration file
            mPreDays = mConfig.getInteger(NOTIFY_TRIGGER_OFFSET, 30); // in days
            mPostDays = mConfig.getInteger(NOTIFY_END_OFFSET, 15); // in days

            mEmailSender = mConfig.getString(SENDER_EMAIL);
            mEmailSubject = mConfig.getString(EMAIL_SUBJECT);
            mEmailTemplateName = mConfig.getString(EMAIL_TEMPLATE);

            // initialize the summary related config info
            ConfigStore sc = mConfig.getSubStore(SUMMARY, ConfigStore.class);

            if (sc.getBoolean(ENABLED, false)) {
                mSummary = true;
                mSummaryItemTemplateName =
                        mConfig.getString(SUMMARY_ITEM_TEMPLATE);
                mSummarySenderEmail =
                        mConfig.getString(SUMMARY_SENDER_EMAIL);
                mSummaryReceiverEmail =
                        mConfig.getString(SUMMARY_RECIPIENT_EMAIL);
                mSummaryMailSubject =
                        mConfig.getString(SUMMARY_EMAIL_SUBJECT);
                mSummaryTemplateName =
                        mConfig.getString(SUMMARY_EMAIL_TEMPLATE);
            } else {
                mSummary = false;
            }

            long msperday = 86400 * 1000;
            long mspredays = mPreDays;
            long mspostdays = mPostDays;

            mPreMS = mspredays * msperday;
            mPostMS = mspostdays * msperday;

            Date now = new Date();
            DateFormat dateFormat = DateFormat.getDateTimeInstance();
            String nowString = dateFormat.format(now);

            /*
             * look in the internal db for certificateRecords that are
             * 1. within the expiration notification period
             * 2. has not yet been renewed
             * 3. notify - use EmailTemplateProcessor to formulate
             *		 content, then send
             * if notified successfully, mark "STATUS_SUCCESS",
             *    else, if notified unsuccessfully, mark "STATUS_FAILURE".
             */

            /* 1) make target notAfter string */

            Date expiryDate = null;
            Date stopDate = null;

            /* 2) Assemble ldap Search filter string */
            // date format: 19991215125306Z
            long expiryMS = now.getTime() + mPreMS;
            long stopMS = now.getTime() - mPostMS;

            expiryDate = new Date(expiryMS);
            stopDate = new Date(stopMS);

            // All cert records which:
            //   1) expire before the deadline
            //   2) have not already been renewed
            // filter format:
            // (& (notafter<='time')(!(certAutoRenew=DONE))(!certAutoRenew=DISABLED))

            StringBuffer f = new StringBuffer();
            String profileId = "";
            try {
                profileId = mConfig.getString(PROFILE_ID, "");
            } catch (EBaseException ee) {
            }

            if (profileId != null && profileId.length() > 0) {
                StringTokenizer tokenizer = new StringTokenizer(profileId);
                int num = tokenizer.countTokens();
                mProfileId = new String[num];
                for (int i = 0; i < num; i++)
                    mProfileId[i] = tokenizer.nextToken();
            }

            f.append("(&");
            if (mProfileId != null) {
                if (mProfileId.length == 1)
                    f.append("(" + CertRecord.ATTR_META_INFO + "=" +
                            CertRecord.META_PROFILE_ID + ":" + mProfileId[0] + ")");
                else {
                    f.append("(|");
                    for (int i = 0; i < mProfileId.length; i++) {
                        f.append("(" + CertRecord.ATTR_META_INFO + "=" +
                                CertRecord.META_PROFILE_ID + ":" + mProfileId[i] + ")");
                    }
                    f.append(")");
                }
            }

            f.append("(" + CertRecord.ATTR_X509CERT + ".notAfter" + "<=" + expiryDate.getTime() + ")");
            f.append("(" + CertRecord.ATTR_X509CERT + ".notAfter" + ">=" + stopDate.getTime() + ")");
            f.append("(!(" + CertRecord.ATTR_AUTO_RENEW + "=" + CertRecord.AUTO_RENEWAL_DONE + "))");
            f.append("(!(" + CertRecord.ATTR_AUTO_RENEW + "=" + CertRecord.AUTO_RENEWAL_DISABLED + "))");
            f.append("(!(" + CertRecord.ATTR_CERT_STATUS + "=" + CertRecord.STATUS_REVOKED + "))");
            f.append("(!(" + CertRecord.ATTR_CERT_STATUS + "=" + CertRecord.STATUS_REVOKED_EXPIRED + "))");
            f.append(")");
            String filter = f.toString();

            String emailTemplate =
                    getTemplateContent(mEmailTemplateName);

            mHTML = mMailHTML;

            try {
                String summaryItemTemplate = null;

                if (mSummary == true) {
                    summaryItemTemplate =
                            getTemplateContent(mSummaryItemTemplateName);
                }

                ItemCounter ic = new ItemCounter();
                CertRecProcessor cp = new CertRecProcessor(engine, this, emailTemplate, summaryItemTemplate, ic);
                //CertRecordList list = mCertDB.findCertRecordsInList(filter, null, "serialno", 5);
                //list.processCertRecords(0, list.getSize() - 1, cp);

                Enumeration<Object> en = mCertDB.findCertRecs(filter);

                while (en.hasMoreElements()) {
                    Object element = en.nextElement();

                    try {
                        cp.process(element);
                    } catch (Exception e) {
                        //Don't abort the entire operation. The error should already be logged
                        logger.warn("RenewalNotificationJob: " + CMS.getLogMessage("JOBS_FAILED_PROCESS", e.toString()), e);
                    }
                }

                // Now send the summary

                if (mSummary == true) {
                    try {
                        String summaryTemplate =
                                getTemplateContent(mSummaryTemplateName);

                        mSummaryHTML = mMailHTML;

                        buildContentParams(EmailFormProcessor.TOKEN_ID, mId);

                        buildContentParams(EmailFormProcessor.TOKEN_SUMMARY_ITEM_LIST,
                                ic.mItemListContent);
                        buildContentParams(EmailFormProcessor.TOKEN_SUMMARY_TOTAL_NUM,
                                String.valueOf(ic.mNumFail + ic.mNumSuccessful));
                        buildContentParams(EmailFormProcessor.TOKEN_SUMMARY_SUCCESS_NUM,
                                String.valueOf(ic.mNumSuccessful));
                        buildContentParams(EmailFormProcessor.TOKEN_SUMMARY_FAILURE_NUM,
                                String.valueOf(ic.mNumFail));

                        buildContentParams(EmailFormProcessor.TOKEN_EXECUTION_TIME,
                                nowString);

                        EmailFormProcessor summaryEmfp = new EmailFormProcessor();

                        String summaryContent =
                                summaryEmfp.getEmailContent(summaryTemplate,
                                        mContentParams);

                        if (summaryContent == null) {
                            logger.warn("RenewalNotificationJob: " + CMS.getLogMessage("JOBS_SUMMARY_CONTENT_NULL"));
                            mailSummary(" no summaryContent");
                        } else {
                            mMailHTML = mSummaryHTML;
                            mailSummary(summaryContent);
                        }
                    } catch (Exception e) {
                        // log error
                        logger.warn("RenewalNotificationJob: " + CMS.getLogMessage("JOBS_EXCEPTION_IN_RUN", e.toString()), e);
                    }
                }
            } catch (EBaseException e) {
                // log error
                logger.warn("RenewalNotificationJob: " + CMS.getLogMessage("OPERATION_ERROR", e.toString()), e);
            }
        } catch (EBaseException ex) {
            logger.warn("RenewalNotificationJob: " + CMS.getLogMessage("Configuration error:", ex.toString()), ex);
        }
    }

    protected void mailUser(String subject,
            String msg,
            String sender,
            Request req,
            CertRecord cr)
            throws IOException, ENotificationException, EBaseException {

        CAEngine caEngine = (CAEngine) engine;
        MailNotification mn = caEngine.getMailNotification();

        String rcp = null;
        //		boolean sendFailed = false;
        Exception sendFailedException = null;

        EmailResolverKeys keys = new EmailResolverKeys();

        try {
            if (req != null) {
                keys.set(EmailResolverKeys.KEY_REQUEST, req);
            }
            if (cr != null) {
                Object c = cr.getCertificate();

                if (c != null) {
                    keys.set(EmailResolverKeys.KEY_CERT, cr.getCertificate());
                }
            }

            EmailResolver er = new ReqCertSANameEmailResolver();

            rcp = er.getEmail(keys);

        } catch (Exception e) {
            // already logged by the resolver
            //			sendFailed = true;
            sendFailedException = e;
            throw (ENotificationException) sendFailedException;
        }

        mn.setTo(rcp);

        if (sender != null)
            mn.setFrom(sender);
        else
            mn.setFrom("nobody");

        if (subject != null)
            mn.setSubject(subject);
        else
            mn.setFrom("Important message from Certificate Authority");

        if (mHTML == true)
            mn.setContentType("text/html");

        mn.setContent(msg);

        mn.sendNotification();
    }

    /**
     * Returns a list of configuration parameter names.
     * The list is passed to the configuration console so instances of
     * this implementation can be configured through the console.
     *
     * @return String array of configuration parameter names.
     */
    @Override
    public String[] getConfigParams() {
        return (mConfigParams);
    }
}

class CertRecProcessor extends ElementProcessor {

    public static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CertRecProcessor.class);

    protected CMSEngine engine;
    protected RenewalNotificationJob mJob;
    protected String mEmailTemplate;
    protected String mSummaryItemTemplate;
    protected ItemCounter mIC;

    public CertRecProcessor(
            CMSEngine engine,
            RenewalNotificationJob job,
            String emailTemplate,
            String summaryItemTemplate,
            ItemCounter ic) {
        this.engine = engine;
        mJob = job;
        mEmailTemplate = emailTemplate;
        mSummaryItemTemplate = summaryItemTemplate;
        mIC = ic;
    }

    @Override
    public void process(Object o) throws EBaseException {

        // Get each certRecord
        CertRecord cr = (CertRecord) o;

        String ridString = null;
        boolean numFailCounted = false;

        if (cr != null) {
            mJob.buildItemParams(cr.getCertificate());
            mJob.buildItemParams(EmailFormProcessor.TOKEN_HTTP_HOST, mJob.mHttpHost);
            mJob.buildItemParams(EmailFormProcessor.TOKEN_HTTP_PORT, mJob.mHttpPort);

            MetaInfo metaInfo = null;

            metaInfo = (MetaInfo) cr.get(CertRecord.ATTR_META_INFO);
            if (metaInfo == null) {
                mIC.mNumFail++;
                numFailCounted = true;
                if (mJob.mSummary == true)
                    mJob.buildItemParams(EmailFormProcessor.TOKEN_STATUS, Job.FAILED);
                logger.warn("CertRecProcessor: " + CMS.getLogMessage("JOBS_GET_CERT_ERROR",
                                cr.getCertificate().getSerialNumber().toString(16)));
            } else {
                ridString = (String) metaInfo.get(CertRecord.META_REQUEST_ID);
            }
        }

        CAEngine caEngine = (CAEngine) engine;
        Request req = null;

        if (ridString != null) {
            RequestId rid = new RequestId(ridString);

            try {
                req = caEngine.getRequestRepository().readRequest(rid);
            } catch (Exception e) {
                // it is ok not to be able to get the request. The main reason
                // to get the request is to retrieve the requestor's email.
                // We can retrieve the email from the CertRecord.
                logger.warn("huh RenewalNotificationJob Exception: " + e.getMessage(), e);
            }

            if (req != null)
                mJob.buildItemParams(req);
        } // ridString != null

        try {
            // send mail to user

            EmailFormProcessor emfp = new EmailFormProcessor();
            String message = emfp.getEmailContent(mEmailTemplate,
                    mJob.mItemParams);

            mJob.mailUser(mJob.mEmailSubject,
                    message,
                    mJob.mEmailSender,
                    req,
                    cr);

            mJob.buildItemParams(EmailFormProcessor.TOKEN_STATUS, Job.SUCCEEDED);

            mIC.mNumSuccessful++;

        } catch (Exception e) {
            logger.warn("RenewalNotificationJob: " + e.getMessage(), e);
            mJob.buildItemParams(EmailFormProcessor.TOKEN_STATUS, Job.FAILED);
            if (numFailCounted == false) {
                mIC.mNumFail++;
            }
        }

        if (mJob.mSummary == true) {
            EmailFormProcessor summaryItemEmfp = new EmailFormProcessor();
            String c =
                    summaryItemEmfp.getEmailContent(mSummaryItemTemplate,
                            mJob.mItemParams);

            if (mIC.mItemListContent == null) {
                mIC.mItemListContent = c;
            } else {
                mIC.mItemListContent += c;
            }
        }
    }
}

class ItemCounter {
    public int mNumSuccessful = 0;
    public int mNumFail = 0;
    public String mItemListContent = null;
}
