package org.snmp4j.uri;

import org.snmp4j.PDU;
import org.snmp4j.smi.VariableBinding;

import java.util.List;

/**
 * The <code>SnmpUriResponse</code> contains the data returned by a SNMP URI
 * operation. See {@link org.snmp4j.uri.SnmpURI} for details.
 *
 */
public class SnmpUriResponse {

    private List<VariableBinding[]> data;

    private int errorStatus = PDU.noError;
    private Type responseType = Type.FINAL;
    private String errorMessage;
    public SnmpUriResponse(List<VariableBinding[]> data) {
        this.data = data;
    }

    public SnmpUriResponse(Type responseType) {
        this.responseType = responseType;
    }

    public SnmpUriResponse(Type responseType, String errorMessage) {
        this.responseType = responseType;
        this.errorMessage = errorMessage;
    }

    public SnmpUriResponse(int errorStatus) {
        this.errorStatus = errorStatus;
        this.responseType = Type.SNMP_ERROR;
    }

    public SnmpUriResponse(List<VariableBinding[]> vbs, int errorStatus) {
        this(vbs);
        this.errorStatus = errorStatus;
        this.responseType = Type.SNMP_ERROR;
    }

    public SnmpUriResponse(List<VariableBinding[]> vbs, Type responseType) {
        this(vbs);
        this.responseType = responseType;
    }

    public List<VariableBinding[]> getData() {
        return data;
    }

    public int getErrorStatus() {
        return errorStatus;
    }

    public Type getResponseType() {
        return responseType;
    }

    public void setResponseType(Type responseType) {
        this.responseType = responseType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "SnmpUriResponse{" +
                "data=" + data +
                ", errorStatus=" + errorStatus +
                ", responseType=" + responseType +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

    public enum Type {FINAL, NEXT, TIMEOUT, SNMP_ERROR, IO_ERROR, SECURITY_ERROR, LEXICOGRAPHIC_ORDER_ERROR}
}


