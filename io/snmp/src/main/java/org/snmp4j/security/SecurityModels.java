package org.snmp4j.security;

import org.snmp4j.smi.Integer32;

import java.util.Hashtable;
import java.util.Map;

/**
 * The <code>SecurityModels</code> class is a collection of all
 * supported security models of a SNMP entity.
 */
public class SecurityModels {

    private static SecurityModels instance = null;
    private Map<Integer32, SecurityModel> securityModels = new Hashtable<Integer32, SecurityModel>(3);

    protected SecurityModels() {
    }

    /**
     * Gets the security singleton instance.
     *
     * @return the <code>SecurityModels</code> instance.
     */
    public synchronized static SecurityModels getInstance() {
        if (instance == null) {
            instance = new SecurityModels();
        }
        return instance;
    }

    /**
     * Gets the SecurityModels collection instance that contains the supplied
     * {@link org.snmp4j.security.SecurityModel}s.
     *
     * @param models an array of {@link org.snmp4j.security.SecurityModel} instances.
     * @return a new instance of SecurityModels that contains the supplied models.
     */
    public static SecurityModels getCollection(SecurityModel[] models) {
        SecurityModels smc = new SecurityModels();
        for (SecurityModel model : models) {
            smc.addSecurityModel(model);
        }
        return smc;
    }

    /**
     * Adds a security model to the central repository of security models.
     *
     * @param model a <code>SecurityModel</code>. If a security model with the same ID
     *              already
     */
    public void addSecurityModel(SecurityModel model) {
        securityModels.put(new Integer32(model.getID()), model);
    }

    /**
     * Removes a security model from the central repository of security models.
     *
     * @param id the <code>Integer32</code> ID of the security model to remove.
     * @return the removed <code>SecurityModel</code> or <code>null</code> if
     * <code>id</code> is not registered.
     */
    public SecurityModel removeSecurityModel(Integer32 id) {
        return securityModels.remove(id);
    }

    /**
     * Returns a security model from the central repository of security models.
     *
     * @param id the <code>Integer32</code> ID of the security model to return.
     * @return the with <code>id</code> associated <code>SecurityModel</code> or
     * <code>null</code> if no such model is registered.
     */
    public SecurityModel getSecurityModel(Integer32 id) {
        return securityModels.get(id);
    }
}

