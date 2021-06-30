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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.ws.rs.core.MultivaluedMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.netscape.certsrv.base.ResourceMessage;
import com.netscape.certsrv.dbs.certdb.CertId;
import com.netscape.certsrv.profile.ProfileAttribute;
import com.netscape.certsrv.profile.ProfileInput;
import com.netscape.certsrv.profile.ProfileOutput;

/**
 * @author jmagne
 *
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class CertEnrollmentRequest extends ResourceMessage {

    private static final String PROFILE_ID = "profileId";
    private static final String RENEWAL = "renewal";
    private static final String SERIAL_NUM = "serial_num";
    private static final String SERVERSIDE_KEYGEN_P12_PASSWD = "serverSideKeygenP12Passwd";

    protected String profileId;

    protected String serverSideKeygenP12Passwd;

    protected boolean renewal;

    protected CertId serialNum;   // used for one type of renewal

    protected String remoteHost;

    protected String remoteAddr;

    protected Collection<ProfileInput> inputs = new ArrayList<>();

    protected Collection<ProfileOutput> outputs = new ArrayList<>();

    public CertEnrollmentRequest() {
        // required for jaxb
    }

    public CertEnrollmentRequest(MultivaluedMap<String, String> form) {
        profileId = form.getFirst(PROFILE_ID);
        String renewalStr = form.getFirst(RENEWAL);
        serialNum = new CertId(form.getFirst(SERIAL_NUM));
        renewal = Boolean.valueOf(renewalStr);

        serverSideKeygenP12Passwd = form.getFirst(SERVERSIDE_KEYGEN_P12_PASSWD);
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

    public String getServerSideKeygenP12Passwd() {
        return serverSideKeygenP12Passwd;
    }

    public void setServerSideKeygenP12Passwd(String serverSideKeygenP12Passwd) {
        this.serverSideKeygenP12Passwd = serverSideKeygenP12Passwd;
    }

    /**
     * @return renewal
     */
    public boolean isRenewal() {
        return renewal;
    }

    /**
     * @param renewal the renewal to set
     */
    public void setRenewal(boolean renewal) {
        this.renewal = renewal;
    }

    public void addInput(ProfileInput input) {
        ProfileInput curInput = getInput(input.getName());
        if (curInput != null) {
            inputs.remove(curInput);
        }
        inputs.add(input);
    }

    public void deleteInput(ProfileInput input) {
        ProfileInput curInput = getInput(input.getName());
        if (curInput != null) {
            inputs.remove(curInput);
        }
    }

    public ProfileInput createInput(String name) {

        ProfileInput oldInput = getInput(name);

        if (oldInput != null)
            return oldInput;

        ProfileInput newInput = new ProfileInput();
        newInput.setName(name);

        inputs.add(newInput);

        return newInput;
    }

    // TODO: deprecate this method in 10.3
    public ProfileInput getInput(String name) {
        return getInputByName(name);
    }

    public ProfileInput getInputByName(String name) {
        for (ProfileInput input : inputs) {
            if (input.getName().equals(name))
                return input;
        }
        return null;
    }

    public ProfileInput getInputByID(String id) {
        for (ProfileInput input : inputs) {
            if (input.getId().equals(id))
                return input;
        }
        return null;
    }

    public void addOutput(ProfileOutput output) {
        ProfileOutput curOutput = getOutput(output.getName());
        if (curOutput != null) {
            outputs.remove(curOutput);
        }
        outputs.add(output);
    }

    public void deleteOutput(ProfileOutput output) {
        ProfileOutput curOutput = getOutput(output.getName());
        if (curOutput != null) {
            outputs.remove(curOutput);
        }
    }

    // TODO: deprecate this method in 10.3
    public ProfileOutput getOutput(String name) {
        return getOutputByName(name);
    }

    public ProfileOutput getOutputByName(String name) {
        for (ProfileOutput output : outputs) {
            if (output.getName().equals(name))
                return output;
        }
        return null;
    }

    public ProfileOutput getOutputByID(String id) {
        for (ProfileOutput output : outputs) {
            if (output.getId().equals(id))
                return output;
        }
        return null;
    }

    public HashMap<String, String> toParams() {
        HashMap<String, String> ret = new HashMap<>();
        ret.put("isRenewal", Boolean.valueOf(renewal).toString());
        if (profileId != null) ret.put(PROFILE_ID, profileId);
        if (serialNum != null) ret.put(SERIAL_NUM, serialNum.toHexString());
        if (remoteHost != null) ret.put("remoteHost", remoteHost);
        if (remoteAddr != null) ret.put("remoteAddr", remoteAddr);
        if (serverSideKeygenP12Passwd != null) ret.put(SERVERSIDE_KEYGEN_P12_PASSWD, serverSideKeygenP12Passwd);

        for (ProfileInput input: inputs) {
            for (ProfileAttribute attr : input.getAttributes()) {
                ret.put(attr.getName(), attr.getValue());
            }
        }

        return ret;
    }

    public CertId getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(CertId serialNum) {
        this.serialNum = serialNum;
    }

    public Collection<ProfileInput> getInputs() {
        return inputs;
    }

    public void setInputs(Collection<ProfileInput> inputs) {
        this.inputs.clear();
        this.inputs.addAll(inputs);
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

    public Collection<ProfileOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(Collection<ProfileOutput> outputs) {
        this.outputs.clear();
        this.outputs.addAll(outputs);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
        result = prime * result + ((outputs == null) ? 0 : outputs.hashCode());
        result = prime * result + ((profileId == null) ? 0 : profileId.hashCode());
        result = prime * result + ((remoteAddr == null) ? 0 : remoteAddr.hashCode());
        result = prime * result + ((remoteHost == null) ? 0 : remoteHost.hashCode());
        result = prime * result + (renewal ? 1231 : 1237);
        result = prime * result + ((serialNum == null) ? 0 : serialNum.hashCode());

        result = prime * result + ((serverSideKeygenP12Passwd == null) ? 0 : serverSideKeygenP12Passwd.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CertEnrollmentRequest other = (CertEnrollmentRequest) obj;
        if (inputs == null) {
            if (other.inputs != null)
                return false;
        } else if (!inputs.equals(other.inputs))
            return false;
        if (outputs == null) {
            if (other.outputs != null)
                return false;
        } else if (!outputs.equals(other.outputs))
            return false;
        if (profileId == null) {
            if (other.profileId != null)
                return false;
        } else if (!profileId.equals(other.profileId))
            return false;
        if (remoteAddr == null) {
            if (other.remoteAddr != null)
                return false;
        } else if (!remoteAddr.equals(other.remoteAddr))
            return false;
        if (remoteHost == null) {
            if (other.remoteHost != null)
                return false;
        } else if (!remoteHost.equals(other.remoteHost))
            return false;
        if (renewal != other.renewal)
            return false;
        if (serialNum == null) {
            if (other.serialNum != null)
                return false;
        } else if (!serialNum.equals(other.serialNum))
            return false;
        if (serverSideKeygenP12Passwd == null) {
            if (other.serverSideKeygenP12Passwd != null)
                return false;
        } else if (!serverSideKeygenP12Passwd.equals(other.serverSideKeygenP12Passwd))
            return false;
        return true;
    }

}
