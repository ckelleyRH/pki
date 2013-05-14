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
// (C) 2011 Red Hat, Inc.
// All rights reserved.
// --- END COPYRIGHT BLOCK ---

/**
 *
 */
package com.netscape.certsrv.cert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.netscape.certsrv.profile.ProfileAttribute;
import com.netscape.certsrv.profile.ProfileInput;
import com.netscape.certsrv.profile.ProfileOutput;

/**
 * @author jmagne
 *
 */

@XmlRootElement(name = "CertEnrollmentRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertEnrollmentRequest {

    private static final String PROFILE_ID = "profileId";
    private static final String RENEWAL = "renewal";
    private static final String SERIAL_NUM = "serial_num";

    @XmlElement
    protected String profileId;

    @XmlElement
    protected boolean isRenewal;

    @XmlElement
    protected String serialNum;   // used for one type of renewal

    @XmlElement
    protected String remoteHost;

    @XmlElement
    protected String remoteAddr;

    @XmlElement(name = "Input")
    protected List<ProfileInput> inputs = new ArrayList<ProfileInput>();

    @XmlElement(name = "Output")
    protected List<ProfileOutput> outputs = new ArrayList<ProfileOutput>();

    public CertEnrollmentRequest() {
        // required for jaxb
    }

    public CertEnrollmentRequest(MultivaluedMap<String, String> form) {
        profileId = form.getFirst(PROFILE_ID);
        String renewalStr = form.getFirst(RENEWAL);
        serialNum = form.getFirst(SERIAL_NUM);
        isRenewal = new Boolean(renewalStr);
    }

    /**
     * @return the profileId
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * @param profileId the profileId to set
     */

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    /**
     * @return renewal
     */

    public boolean getIsRenewal() {
        return isRenewal;
    }

    public void addInput(ProfileInput input) {
        ProfileInput curInput = getInput(input.getName());
        if (curInput != null) {
            getInputs().remove(curInput);
        }
        getInputs().add(input);
    }

    public void deleteInput(ProfileInput input) {
        ProfileInput curInput = getInput(input.getName());
        if (curInput != null) {
            getInputs().remove(curInput);
        }
    }

    public ProfileInput createInput(String name) {

        ProfileInput oldInput = getInput(name);

        if (oldInput != null)
            return oldInput;

        ProfileInput newInput = new ProfileInput();
        newInput.setName(name);

        getInputs().add(newInput);

        return newInput;
    }

    public ProfileInput getInput(String name) {
        ProfileInput input = null;
        for (ProfileInput curInput: getInputs()) {
            if (curInput != null && curInput.getName().equals(name))
                break;
        }
        return input;
    }

    public void addOutput(ProfileOutput output) {
        ProfileOutput curOutput = getOutput(output.getName());
        if (curOutput != null) {
            getOutputs().remove(curOutput);
        }
        getOutputs().add(output);
    }

    public void deleteOutput(ProfileOutput output) {
        ProfileOutput curOutput = getOutput(output.getName());
        if (curOutput != null) {
            getInputs().remove(curOutput);
        }
    }

    public ProfileOutput getOutput(String name) {
        ProfileOutput output = null;
        for (ProfileOutput curOutput: getOutputs()) {
            if (curOutput != null && curOutput.getName().equals(name))
                break;
        }
        return output;
    }

    /**
     * @param renewal the renewal to set
     */
    public void setIsRenewal(boolean isRenewal) {
        this.isRenewal = isRenewal;
    }

    public HashMap<String, String> toParams() {
        HashMap<String, String> ret = new HashMap<String, String>();
        ret.put("isRenewal", Boolean.valueOf(isRenewal).toString());
        if (profileId != null) ret.put(PROFILE_ID, profileId);
        if (serialNum != null) ret.put(SERIAL_NUM, serialNum);
        if (remoteHost != null) ret.put("remoteHost", remoteHost);
        if (remoteAddr != null) ret.put("remoteAddr", remoteAddr);

        for (ProfileInput input: inputs) {
            for (ProfileAttribute attr:input.getAttrs()) {
                ret.put(attr.getName(), attr.getValue());
            }
        }

        return ret;
    }

    public static void main(String args[]) throws Exception {
        CertEnrollmentRequest data = new CertEnrollmentRequest();
        data.setProfileId("caUserCert");
        data.setIsRenewal(false);

        //Simulate a "caUserCert" Profile enrollment

        ProfileInput certReq = data.createInput("KeyGenInput");
        certReq.addAttribute(new ProfileAttribute("cert_request_type", "crmf", null));
        certReq.addAttribute(new ProfileAttribute(
                "cert_request",
                "MIIBozCCAZ8wggEFAgQBMQp8MIHHgAECpQ4wDDEKMAgGA1UEAxMBeKaBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA2NgaPHp0jiohcP4M+ufrJOZEqH8GV+liu5JLbT8nWpkfhC+8EUBqT6g+n3qroSxIcNVGNdcsBEqs1utvpItzyslAbpdyat3WwQep1dWMzo6RHrPDuIoxNA0Yka1n3qEX4U//08cLQtUv2bYglYgN/hOCNQemLV6vZWAv0n7zelkCAwEAAakQMA4GA1UdDwEB/wQEAwIF4DAzMBUGCSsGAQUFBwUBAQwIcmVnVG9rZW4wGgYJKwYBBQUHBQECDA1hdXRoZW50aWNhdG9yoYGTMA0GCSqGSIb3DQEBBQUAA4GBAJ1VOQcaSEhdHa94s8kifVbSZ2WZeYE5//qxL6wVlEst20vq4ybj13CetnbN3+WT49Zkwp7Fg+6lALKgSk47suTg3EbbQDm+8yOrC0nc/q4PTRoHl0alMmUxIhirYc1t3xoCMqJewmjX1bNP8lpVIZAYFZo4eZCpZaiSkM5BeHhz",
                null));

        ProfileInput subjectName = data.createInput("SubjectNameInput");
        subjectName.addAttribute(new ProfileAttribute("sn_uid", "jmagne", null));
        subjectName.addAttribute(new ProfileAttribute("sn_e", "jmagne@redhat.com", null));
        subjectName.addAttribute(new ProfileAttribute("sn_c", "US", null));
        subjectName.addAttribute(new ProfileAttribute("sn_ou", "Development", null));
        subjectName.addAttribute(new ProfileAttribute("sn_ou1", "IPA", null));
        subjectName.addAttribute(new ProfileAttribute("sn_ou2", "Dogtag", null));
        subjectName.addAttribute(new ProfileAttribute("sn_ou3", "CA", null));
        subjectName.addAttribute(new ProfileAttribute("sn_cn", "Common", null));
        subjectName.addAttribute(new ProfileAttribute("sn_o", "RedHat", null));

        ProfileInput submitter = data.createInput("SubmitterInfoInput");
        submitter.addAttribute(new ProfileAttribute("requestor_name", "admin", null));
        submitter.addAttribute(new ProfileAttribute("requestor_email", "admin@redhat.com", null));
        submitter.addAttribute(new ProfileAttribute("requestor_phone", "650-555-5555", null));

        try {
            JAXBContext context = JAXBContext.newInstance(CertEnrollmentRequest.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            marshaller.marshal(data, stream);

            System.out.println("Originally marshalled enrollment object. \n");

            System.out.println(stream.toString());

            //Try to unmarshall

            Unmarshaller unmarshaller = context.createUnmarshaller();

            ByteArrayInputStream bais = new ByteArrayInputStream(stream.toByteArray());
            Object unmarshalled = unmarshaller.unmarshal(bais);

            //Try re-marshalling, unmarshalled object to compare

            stream.reset();

            marshaller.marshal(unmarshalled, stream);

            System.out.println("Remarshalled unmarshalled enrollment object. \n");

            System.out.println(stream.toString());

        } catch (JAXBException e) {
            System.out.println(e.toString());
        }
    }

    public String getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
    }

    public List<ProfileInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<ProfileInput> inputs) {
        this.inputs = inputs;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public List<ProfileOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<ProfileOutput> outputs) {
        this.outputs = outputs;
    }

    public void setRenewal(boolean isRenewal) {
        this.isRenewal = isRenewal;
    }

}
